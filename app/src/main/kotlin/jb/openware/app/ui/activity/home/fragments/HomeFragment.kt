package jb.openware.app.ui.activity.home.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import jb.openware.app.R
import jb.openware.app.databinding.FragmentHomeBinding
import jb.openware.app.ui.activity.home.MoreProjectsActivity
import jb.openware.app.ui.activity.project.ProjectViewActivity
import jb.openware.app.ui.adapter.BannerProjectAdapter
import jb.openware.app.ui.adapter.BaseProjectAdapter
import jb.openware.app.ui.items.Project

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding

    private val db by lazy {
        FirebaseDatabase.getInstance().getReference("projects/normal")
    }

    private val limit = 20

    private val editorsChoice = mutableListOf<Project>()
    private val latestProjects = mutableListOf<Project>()
    private val mostLiked = mutableListOf<Project>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentHomeBinding.bind(view)

        setupRecyclerViews()
        setupClicks()
        binding?.let { hideViews(it.recyclerviewEditors) }

        loadEditorsChoice()
        binding?.let { beginTransition(it.base, 400L) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // clear binding
    }

    // ---------------- SETUP ----------------

    private fun setupRecyclerViews() {
        val b = binding ?: return

        b.recyclerviewEditors.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        b.recyclerviewLatest.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        b.recyclerviewLiked.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        b.recyclerviewEditors.setHasFixedSize(true)
        b.recyclerviewLatest.setHasFixedSize(true)
        b.recyclerviewLiked.setHasFixedSize(true)
    }

    private fun setupClicks() {
        val b = binding ?: return

        b.editorSeemore.setOnClickListener { openMore("editorsChoice") }
        b.textview7.setOnClickListener { openMore("all") }
        b.textview11.setOnClickListener { openMore("like") }
    }

    private fun openMore(id: String) {
        requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE).edit {
                putString("id", id)
            }
        startActivity(Intent(requireActivity(), MoreProjectsActivity::class.java))
    }

    private fun loadEditorsChoice() {
        db.limitToLast(limit).orderByChild("editorsChoice").equalTo(true)
            .addListenerForSingleValueEvent(projectListener { list ->
                if (!isAdded || binding == null) return@projectListener

                editorsChoice.clear()
                editorsChoice.addAll(list)

                binding?.apply {
                    recyclerviewEditors.adapter = BannerProjectAdapter(editorsChoice, requireActivity(), 1)
                    hideViews(linearShimmer1)
                    showViews(recyclerviewEditors)
                }

                loadLatest()
            })
    }

    private fun loadLatest() {
        db.limitToLast(limit).orderByChild("latest").equalTo(true)
            .addListenerForSingleValueEvent(projectListener { list ->
                if (!isAdded || binding == null) return@projectListener
                latestProjects.clear()
                latestProjects.addAll(list)

                binding?.apply {
                    recyclerviewLatest.adapter = BaseProjectAdapter(latestProjects) { project ->
                        openProject(project)
                    }
                    hideViews(linearShimmer2)
                    showViews(recyclerviewLatest)
                }

                loadMostLiked()
            })
    }

    private fun loadMostLiked() {
        db.limitToLast(limit).orderByChild("visibility").equalTo(true)
            .addListenerForSingleValueEvent(projectListener { list ->
                if (!isAdded || binding == null) return@projectListener

                mostLiked.clear()
                mostLiked.addAll(list.sortedByDescending { it.likes.toIntOrNull() ?: 0 })

                binding?.apply {
                    recyclerviewLiked.adapter = BaseProjectAdapter(mostLiked) { project ->
                        openProject(project)
                    }
                    hideViews(linearShimmer4)
                    showViews(recyclerviewLiked)
                }
            })
    }

    private fun projectListener(
        onResult: (List<Project>) -> Unit
    ) = object : ValueEventListener {

        override fun onDataChange(snapshot: DataSnapshot) {
            if (!isAdded || _binding == null) return  // STOP if view is destroyed

            val list = snapshot.children.mapNotNull { it.getValue(Project::class.java) }
                .filter { it.visibility }

            onResult(list.reversed())
        }

        override fun onCancelled(error: DatabaseError) = Unit
    }

    private fun openProject(project: Project) {
        if (!isAdded) return

        requireContext().getSharedPreferences("developer", MODE_PRIVATE)
            .edit {
                putString("type", "Free")
            }

        startActivity(
            Intent(requireContext(), ProjectViewActivity::class.java).apply {
                putExtra("key", project.key)
                putExtra("uid", project.uid)
            }
        )
    }

    fun beginTransition(
        viewGroup: ViewGroup, duration: Long = 300L
    ) {
        val transition = AutoTransition().apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
        }

        TransitionManager.beginDelayedTransition(viewGroup, transition)
    }

    fun hideViews(vararg views: View) {
        views.forEach { it.visibility = View.GONE }
    }

    fun showViews(vararg views: View) {
        views.forEach { it.visibility = View.VISIBLE }
    }

}