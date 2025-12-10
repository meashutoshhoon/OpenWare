package jb.openware.imageviewer

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import jb.openware.imageviewer.listeners.OnDismissListener
import jb.openware.imageviewer.listeners.OnImageChangeListener
import jb.openware.imageviewer.loader.ImageLoader
import jb.openware.imageviewer.viewer.builder.BuilderData
import jb.openware.imageviewer.viewer.dialog.ImageViewerDialog

class ImageViewer<T> private constructor(
    private val context: Context,
    private val builderData: BuilderData<T>
) {
    private val dialog: ImageViewerDialog<T> = ImageViewerDialog(context, builderData)

    @JvmOverloads
    fun show(animate: Boolean = true) {
        if (builderData.images.isNotEmpty()) {
            dialog.show(animate)
        } else {
            Log.w(
                context.getString(R.string.library_name),
                "Images list cannot be empty! Viewer ignored."
            )
        }
    }

    fun close() {
        dialog.close()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    fun updateImages(images: Array<T>) {
        updateImages(images.toList())
    }

    fun updateImages(images: List<T>) {
        if (images.isNotEmpty()) {
            dialog.updateImages(images)
        } else {
            dialog.close()
        }
    }

    var currentPosition: Int
        get() = dialog.currentPosition
        set(value) { dialog.currentPosition = value }

    fun updateTransitionImage(imageView: ImageView?) {
        dialog.updateTransitionImage(imageView)
    }

    class Builder<T> private constructor(
        private val context: Context,
        private val data: BuilderData<T>
    ) {

        constructor(
            context: Context,
            images: Array<T>,
            imageLoader: ImageLoader<T>
        ) : this(context, BuilderData(images.toList(), imageLoader))

        constructor(
            context: Context,
            images: List<T>,
            imageLoader: ImageLoader<T>
        ) : this(context, BuilderData(images, imageLoader))

        fun withStartPosition(position: Int): Builder<T> = apply {
            data.startPosition = position
        }

        fun withBackgroundColor(@ColorInt color: Int): Builder<T> = apply {
            data.backgroundColor = color
        }

        fun withBackgroundColorResource(@ColorRes colorRes: Int): Builder<T> =
            withBackgroundColor(ContextCompat.getColor(context, colorRes))

        fun withOverlayView(view: View): Builder<T> = apply {
            data.overlayView = view
        }

        fun withImagesMargin(@DimenRes dimen: Int): Builder<T> = apply {
            val margin = context.resources.getDimension(dimen).toInt()
            data.imageMarginPixels = margin
        }

        fun withImageMarginPixels(@Px marginPixels: Int): Builder<T> = apply {
            data.imageMarginPixels = marginPixels
        }

        fun withContainerPadding(@DimenRes padding: Int): Builder<T> {
            val paddingPx = context.resources.getDimension(padding).toInt()
            return withContainerPaddingPixels(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        fun withContainerPadding(
            @DimenRes start: Int,
            @DimenRes top: Int,
            @DimenRes end: Int,
            @DimenRes bottom: Int
        ): Builder<T> {
            val startPx = context.resources.getDimension(start).toInt()
            val topPx = context.resources.getDimension(top).toInt()
            val endPx = context.resources.getDimension(end).toInt()
            val bottomPx = context.resources.getDimension(bottom).toInt()
            return withContainerPaddingPixels(startPx, topPx, endPx, bottomPx)
        }

        fun withContainerPaddingPixels(@Px padding: Int): Builder<T> = apply {
            data.containerPaddingPixels = intArrayOf(padding, padding, padding, padding)
        }

        fun withContainerPaddingPixels(
            @Px start: Int,
            @Px top: Int,
            @Px end: Int,
            @Px bottom: Int
        ): Builder<T> = apply {
            data.containerPaddingPixels = intArrayOf(start, top, end, bottom)
        }

        fun withHiddenStatusBar(value: Boolean): Builder<T> = apply {
            data.shouldStatusBarHide = value
        }

        fun allowZooming(value: Boolean): Builder<T> = apply {
            data.isZoomingAllowed = value
        }

        fun allowSwipeToDismiss(value: Boolean): Builder<T> = apply {
            data.isSwipeToDismissAllowed = value
        }

        fun withTransitionFrom(imageView: ImageView?): Builder<T> = apply {
            data.transitionView = imageView
        }

        fun withImageChangeListener(imageChangeListener: OnImageChangeListener): Builder<T> =
            apply {
                data.imageChangeListener = imageChangeListener
            }

        fun withDismissListener(onDismissListener: OnDismissListener): Builder<T> = apply {
            data.onDismissListener = onDismissListener
        }

        fun build(): ImageViewer<T> = ImageViewer(context, data)

        @JvmOverloads
        fun show(animate: Boolean = true): ImageViewer<T> {
            val viewer = build()
            viewer.show(animate)
            return viewer
        }
    }

}