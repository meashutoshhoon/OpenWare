package jb.openware.app.ui.cells

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import jb.openware.app.databinding.ProjectBannerBinding
import androidx.core.graphics.drawable.toDrawable

@SuppressLint("ViewConstructor")
class BannerCell(context: Context) : FrameLayout(context) {

    private val binding = ProjectBannerBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    private val iconProject = binding.iconProject
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

    fun setData(
        iconUserUrl: String,
        title: String,
        categoryText: String,
        sizeText: String,
        screenshotUrl: String
    ) {
        Glide.with(context)
            .load(iconUserUrl)
            .placeholder(0xFFE0E0E0.toInt().toDrawable())
            .into(iconUser)

        projectName.text = title
        category.text = categoryText
        size.text = sizeText

        Glide.with(context)
            .load(screenshotUrl)
            .placeholder(0xFFE0E0E0.toInt().toDrawable())
            .into(iconProject)
    }
}
