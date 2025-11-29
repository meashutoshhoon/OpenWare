package jb.openware.app.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import jb.openware.app.ui.cells.RatioListCell

class RatioListAdapter(
    private val items: List<String>,
    private var selected: Int,
    private val onSelect: (Int) -> Unit
) : RecyclerView.Adapter<RatioListAdapter.VH>() {

    inner class VH(val cell: RatioListCell) : RecyclerView.ViewHolder(cell)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(RatioListCell(parent.context))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.cell.setData(items[position], position, selected)

        holder.cell.setOnClickListener {
            if (selected != position) {
                val old = selected
                selected = position

                notifyItemChanged(old)
                notifyItemChanged(selected)

                onSelect(position)
            }
        }
    }

    override fun getItemCount() = items.size
}
