package jb.openware.app.ui.cells

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import jb.openware.app.databinding.ProjectHorizontalBinding
import jb.openware.app.ui.items.ProjectItem

@SuppressLint("ViewConstructor")
class ProjectListCell(context: Context) : FrameLayout(context) {

    private val binding = ProjectHorizontalBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    private val icon = binding.icon
    private val title = binding.title
    private val comments = binding.comments
    private val likes = binding.likes
    private val downloads = binding.download
    private val name = binding.name

    init {
        title.apply {
            ellipsize = TextUtils.TruncateAt.MARQUEE
            marqueeRepeatLimit = -1
            isSingleLine = true
            isSelected = true
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatNumber(input: String?): String {
        val num = input?.toLongOrNull() ?: return input ?: ""
        return when {
            num >= 1_000_000_000L -> String.format("%.1fB", num / 1_000_000_000f)
            num >= 1_000_000L -> String.format("%.1fM", num / 1_000_000f)
            num >= 1_000L -> String.format("%.1fK", num / 1_000f)
            else -> num.toString()
        }
    }

    fun setData(item: ProjectItem) {
        Glide.with(context).load(item.icon).placeholder(0xFFE0E0E0.toInt().toDrawable()).into(icon)

        title.text = item.title
        comments.text = formatNumber(item.comments)
        likes.text = formatNumber(item.likes)
        downloads.text = formatNumber(item.downloads)
        name.text = item.userName
    }
}
