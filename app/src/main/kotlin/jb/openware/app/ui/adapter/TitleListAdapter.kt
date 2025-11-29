package jb.openware.app.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import jb.openware.app.ui.cells.TitleListCell
import jb.openware.app.ui.items.TitleListItem

class TitleListAdapter(
    private val items: List<TitleListItem>,
    private val onClick: (TitleListItem) -> Unit
) : RecyclerView.Adapter<TitleListAdapter.VH>() {

    class VH(val cell: TitleListCell) : RecyclerView.ViewHolder(cell)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val cell = TitleListCell(parent.context)
        return VH(cell)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.cell.setData(item)

        holder.cell.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
