package jb.openware.app.ui.activity.home.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.res.Configuration
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
    private val binding get() = _binding!!

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
        hideViews(binding.recyclerviewEditors)

        loadEditorsChoice()
        beginTransition(binding.base, 400L)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    // ---------------- SETUP ----------------

    private fun setupRecyclerViews() = with(binding) {
        recyclerviewEditors.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerviewLatest.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        recyclerviewLiked.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        recyclerviewEditors.setHasFixedSize(true)
        recyclerviewLatest.setHasFixedSize(true)
        recyclerviewLiked.setHasFixedSize(true)
    }

    private fun setupClicks() = with(binding) {
        editorSeemore.setOnClickListener { openMore("editors_choice") }
        textview7.setOnClickListener { openMore("all") }
        textview11.setOnClickListener { openMore("like") }
    }

    private fun openMore(id: String) {
        requireActivity().getSharedPreferences("app_prefs", MODE_PRIVATE).edit {
                putString("id", id)
            }
        startActivity(Intent(requireActivity(), MoreProjectsActivity::class.java))
    }

    private fun loadEditorsChoice() {
        db.limitToLast(limit).orderByChild("editors_choice").equalTo(true)
            .addListenerForSingleValueEvent(projectListener { list ->
                editorsChoice.clear()
                editorsChoice.addAll(list)

                binding.recyclerviewEditors.adapter =
                    BannerProjectAdapter(editorsChoice, requireActivity(), 1)

                hideViews(binding.linearShimmer1)
                showViews(binding.recyclerviewEditors)

                loadLatest()
            })
    }

    private fun loadLatest() {
        db.limitToLast(limit).orderByChild("latest").equalTo(true)
            .addListenerForSingleValueEvent(projectListener { list ->
                latestProjects.clear()
                latestProjects.addAll(list)

                binding.recyclerviewLatest.adapter = BaseProjectAdapter(latestProjects) { project ->

                    requireActivity().getSharedPreferences("developer", MODE_PRIVATE).edit {
                            putString("type", "Free")
                        }

                    startActivity(
                        Intent(requireContext(), ProjectViewActivity::class.java).apply {
                            putExtra("key", project.key)
                            putExtra("uid", project.uid)
                        })
                }

                hideViews(binding.linearShimmer2)
                showViews(binding.recyclerviewLatest)

                loadMostLiked()
            })
    }

    private fun loadMostLiked() {
        db.limitToLast(limit).orderByChild("visible").equalTo(true)
            .addListenerForSingleValueEvent(projectListener { list ->
                mostLiked.clear()
                mostLiked.addAll(list)

                mostLiked.sortByDescending { it.likes }

                binding.recyclerviewLiked.adapter = BaseProjectAdapter(mostLiked) { project ->

                    requireActivity().getSharedPreferences("developer", MODE_PRIVATE).edit {
                            putString("type", "Free")
                        }

                    startActivity(
                        Intent(requireContext(), ProjectViewActivity::class.java).apply {
                            putExtra("key", project.key)
                            putExtra("uid", project.uid)
                        })
                }

                hideViews(binding.linearShimmer4)
                showViews(binding.recyclerviewLiked)
            })
    }

    private fun projectListener(
        onResult: (List<Project>) -> Unit
    ) = object : ValueEventListener {

        override fun onDataChange(snapshot: DataSnapshot) {
            val list = snapshot.children.mapNotNull { it.getValue(Project::class.java) }
                .filter { it.visible }

            onResult(list.reversed())
        }

        override fun onCancelled(error: DatabaseError) = Unit
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