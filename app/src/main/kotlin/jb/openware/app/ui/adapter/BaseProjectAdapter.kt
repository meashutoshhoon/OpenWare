package jb.openware.app.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import jb.openware.app.ui.cells.BaseProjectCell
import jb.openware.app.ui.items.Project

class BaseProjectAdapter(
    private val items: List<Project>, private val onClick: (Project) -> Unit
) : RecyclerView.Adapter<BaseProjectAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(BaseProjectCell(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val project = items[position]

        (holder.itemView as BaseProjectCell).setData(project)

        holder.itemView.setOnClickListener {
            onClick(project)
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
