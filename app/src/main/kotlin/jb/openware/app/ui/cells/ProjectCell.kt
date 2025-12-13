package jb.openware.app.ui.cells

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import jb.openware.app.databinding.ProjectCellBinding
import androidx.core.graphics.drawable.toDrawable
import jb.openware.app.ui.items.Project

@SuppressLint("ViewConstructor")
class ProjectCell(context: Context) : FrameLayout(context) {

    private val binding = ProjectCellBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    private val iconUser = binding.iconUser
    private val projectName = binding.projectName
    private val category = binding.category
    private val size = binding.size

    init {
        projectName.apply {
            ellipsize = TextUtils.TruncateAt.MARQUEE
            marqueeRepeatLimit = -1
            isSingleLine = true
            isSelected = true
        }
    }
    @SuppressLint("DefaultLocale")
    private fun formatNumber(value: String?): String {
        val num = value?.toLongOrNull() ?: return value ?: ""
        return when {
            num >= 1_000_000_000L -> String.format("%.1fB", num / 1_000_000_000f)
            num >= 1_000_000L -> String.format("%.1fM", num / 1_000_000f)
            num >= 1_000L -> String.format("%.1fK", num / 1_000f)
            else -> num.toString()
        }
    }

    fun setData(item: Project) {
        Glide.with(context)
            .load(item.icon)
            .placeholder(0xFFE0E0E0.toInt().toDrawable())
            .into(iconUser)

        projectName.text = item.title
        category.text = item.category
        size.text = formatNumber(item.size)
    }
}
