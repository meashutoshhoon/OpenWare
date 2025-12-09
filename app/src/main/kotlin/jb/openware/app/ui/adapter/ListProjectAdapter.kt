package jb.openware.app.ui.adapter

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.recyclerview.widget.RecyclerView
import jb.openware.app.ui.cells.ProjectListCell
import jb.openware.app.ui.items.Project
import jb.openware.app.ui.items.ProjectItem

class ListProjectAdapter(
    private val data: List<Project>,
    private val context: Activity,
    private val nameMap: Map<String, String>,
    private val code: Int
) : RecyclerView.Adapter<ListProjectAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cell = ProjectListCell(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return ViewHolder(cell)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val project = data[position]

        // Prefer mapped name (from your hashMap), else fallback to project.name
        val displayName = nameMap[project.uid] ?: project.name.orEmpty()

        // Convert Project → ProjectItem for display
        val item = ProjectItem(
            icon = project.icon.orEmpty(),
            title = project.title.orEmpty(),
            comments = project.comments.orEmpty(),
            likes = project.likes.orEmpty(),
            downloads = project.downloads.orEmpty(),
            userName = displayName
        )

        (holder.itemView as ProjectListCell).setData(item)

        holder.itemView.setOnClickListener {
            val prefs = context.getSharedPreferences("developer", Activity.MODE_PRIVATE)
            prefs.edit {
                putString("type", if (code == 1) "Free" else "Paid")
            }

            val intent = Intent(context, ProjectViewActivity::class.java).apply {
                putExtra("project", project)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
