package jb.openware.app.ui.cells

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import jb.openware.app.databinding.PhotoCellBinding
import androidx.core.graphics.drawable.toDrawable

class ProjectScreenshotCell(context: Context) : FrameLayout(context) {

    private val binding =
        PhotoCellBinding.inflate(LayoutInflater.from(context), this, true)

    private val image = binding.image

    fun setImage(url: String) {
        Glide.with(image.context)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .placeholder(0xFFD3D3D3.toInt().toDrawable())
            .centerCrop()
            .into(image)
    }
}
