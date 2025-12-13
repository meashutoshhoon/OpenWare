package jb.openware.app.ui.activity.home

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import jb.openware.app.databinding.ActivitySearchBinding
import jb.openware.app.ui.cells.ProjectCell
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.ui.components.SearchBarView
import jb.openware.app.ui.items.Project
import jb.openware.app.util.Utils

class SearchActivity : BaseActivity<ActivitySearchBinding>(ActivitySearchBinding::inflate) {

    private val normalRef = FirebaseDatabase.getInstance().getReference("projects/normal")

    private val projects = mutableListOf<Project>()
    private lateinit var adapter: SearchAdapter

    private lateinit var searchBar: SearchBarView

    override fun init() {

        openKeyboard(binding.searchView)

        searchBar = SearchBarView(this, binding.toolbar, binding.searchView).apply {
            setEditTextEnabled(true)
            setToolbarClickListener { Utils.getBackPressedClickListener(this@SearchActivity) }
            setMenuVisibility(false)
            setSearchSubmitListener {
                search(searchText.trim())
            }
        }

        binding.searchView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(searchBar.searchText.trim())
                true
            } else false
        }

        hideViews(binding.recyclerview, binding.loading, binding.warning)

    }

    override fun initLogic() {
        adapter = SearchAdapter(projects) { project ->
            getSharedPreferences("developer", MODE_PRIVATE).edit {
                putString("type", "Free")
            }

            startActivity(
                Intent(this, ProjectViewActivity::class.java).apply {
                    putExtra("key", project.key)
                    putExtra("uid", project.uid)
                })
        }

        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = adapter
    }

    private fun search(query: String) {
        if (query.isBlank()) {
            toast("Enter something to search")
            return
        }

        hideViews(binding.recyclerview, binding.warning)
        showViews(binding.loading)
        projects.clear()

        normalRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                snapshot.children.forEach { child ->
                    val raw = child.getValue(object : GenericTypeIndicator<Map<String, Any>>() {})
                        ?: return@forEach

                    val project = runCatching {
                        Project.fromMap(raw)
                    }.getOrNull() ?: return@forEach

                    if (project.visible && project.title?.contains(
                            query, ignoreCase = true
                        ) == true
                    ) {
                        projects.add(project)
                    }
                }

                updateUi()
            }

            override fun onCancelled(error: DatabaseError) {
                hideViews(binding.loading)
                toast("Search failed")
            }
        })
    }

    private fun updateUi() {
        delayTask {
            hideViews(binding.loading)

            if (projects.isEmpty()) {
                showViews(binding.warning)
                hideViews(binding.recyclerview)
            } else {
                showViews(binding.recyclerview)
                hideViews(binding.warning)
                hideKeyboard()
            }

            adapter.notifyDataSetChanged()
        }
    }

    inner class SearchAdapter(
        private val items: List<Project>, private val onClick: (Project) -> Unit
    ) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ProjectCell(parent.context))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val project = items[position]
            (holder.itemView as ProjectCell).setData(project)
            holder.itemView.setOnClickListener { onClick(project) }
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }

}