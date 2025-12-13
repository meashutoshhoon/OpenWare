package jb.openware.app.ui.cells

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import jb.openware.app.databinding.ProjectBinding
import jb.openware.app.ui.items.Project

class BaseProjectCell(context: Context) : FrameLayout(context) {

    private val binding = ProjectBinding.inflate(LayoutInflater.from(context), this, true)

    private val icon = binding.icon
    private val title = binding.title
    private val commentsText = binding.comments
    private val likesText = binding.likes

    init {
        title.apply {
            isSingleLine = true
            isSelected = true   // For marquee
        }
    }

    @SuppressLint("DefaultLocale")
    private fun formatNumber(input: String): String {
        val num = input.toLongOrNull() ?: return input

        return when {
            num >= 1_000_000_000L -> String.format("%.1fB", num / 1_000_000_000f)
            num >= 1_000_000L -> String.format("%.1fM", num / 1_000_000f)
            num >= 1_000L -> String.format("%.1fK", num / 1_000f)
            else -> num.toString()
        }
    }

    fun setData(project: Project) {
        Glide.with(context).load(project.icon).placeholder(0xFFE0E0E0.toInt().toDrawable())
            .into(icon)

        title.text = project.title
        val comments = project.comments?.takeIf { it.isNotBlank() } ?: "0"
        val likes = project.likes?.takeIf { it.isNotBlank() } ?: "0"
        commentsText.text = formatNumber(comments)
        likesText.text = formatNumber(likes)
    }
}
