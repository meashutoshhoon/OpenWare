package jb.openware.imageviewer.viewer.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.github.chrisbanes.photoview.PhotoView
import jb.openware.imageviewer.common.extensions.resetScale
import jb.openware.imageviewer.common.pager.RecyclingPagerAdapter
import jb.openware.imageviewer.loader.ImageLoader

internal class ImagesPagerAdapter<T>(
    private val context: Context,
    private var images: List<T>,
    private val imageLoader: ImageLoader<T>,
    private val isZoomingAllowed: Boolean
) : RecyclingPagerAdapter<ImagesPagerAdapter<T>.ViewHolder>() {

    private val holders = mutableListOf<ViewHolder>()

    fun isScaled(position: Int): Boolean =
        holders.firstOrNull { it.position == position }?.isScaled ?: false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val photoView = PhotoView(context).apply {
            // Correct property for PhotoView zoom capability
            isZoomable = isZoomingAllowed

            // Allow parent to intercept only when not zoomed in
            setOnViewDragListener { _, _ ->
                setAllowParentInterceptOnEdge(scale <= 1f)
            }
        }

        return ViewHolder(photoView).also { holders.add(it) }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = images.size

    internal fun updateImages(images: List<T>) {
        this.images = images
        notifyDataSetChanged()
    }

    internal fun resetScale(position: Int) {
        holders.firstOrNull { it.position == position }?.resetScale()
    }

    internal inner class ViewHolder(itemView: View) :
        RecyclingPagerAdapter.ViewHolder(itemView) {

        private val photoView: PhotoView = itemView as PhotoView

        internal val isScaled: Boolean
            get() = photoView.scale > 1f

        internal fun bind(position: Int) {
            this.position = position
            imageLoader.loadImage(photoView, images[position])
        }

        internal fun resetScale() {
            photoView.resetScale(animate = true)
        }
    }
}
