package jb.openware.imageviewer.viewer.dialog

import android.content.Context
import android.view.KeyEvent
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import jb.openware.imageviewer.R
import jb.openware.imageviewer.viewer.builder.BuilderData
import jb.openware.imageviewer.viewer.view.ImageViewerView

internal class ImageViewerDialog<T>(
    context: Context,
    private val builderData: BuilderData<T>
) {

    private val viewerView: ImageViewerView<T> = ImageViewerView(context)
    private var animateOpen: Boolean = true

    private val dialogStyle: Int
        get() = if (builderData.shouldStatusBarHide) {
            R.style.ImageViewerDialog_NoStatusBar
        } else {
            R.style.ImageViewerDialog_Default
        }

    private val dialog: AlertDialog = AlertDialog
        .Builder(context, dialogStyle)
        .setView(viewerView)
        .setOnKeyListener { _, keyCode, event -> onDialogKeyEvent(keyCode, event) }
        .create()
        .apply {
            setOnShowListener { viewerView.open(builderData.transitionView, animateOpen) }
            setOnDismissListener { builderData.onDismissListener?.onDismiss() }
        }


    var currentPosition: Int
        get() = viewerView.currentPosition
        set(value) {
            viewerView.currentPosition = value
        }

    fun show(animate: Boolean = true) {
        animateOpen = animate
        dialog.show()
    }

    fun close() = viewerView.close()

    fun dismiss() = dialog.dismiss()

    fun updateImages(images: List<T>) = viewerView.updateImages(images)

    fun updateTransitionImage(imageView: ImageView?) =
        viewerView.updateTransitionImage(imageView)


    private fun onDialogKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
            event.action == KeyEvent.ACTION_UP &&
            !event.isCanceled
        ) {
            if (viewerView.isScaled) {
                viewerView.resetScale()
            } else {
                viewerView.close()
            }
            return true
        }
        return false
    }

    private fun setupViewerView() {
        viewerView.apply {
            isZoomingAllowed = builderData.isZoomingAllowed
            isSwipeToDismissAllowed = builderData.isSwipeToDismissAllowed

            containerPadding = builderData.containerPaddingPixels
            imagesMargin = builderData.imageMarginPixels
            overlayView = builderData.overlayView

            setBackgroundColor(builderData.backgroundColor)
            setImages(builderData.images, builderData.startPosition, builderData.imageLoader)

            onPageChange = { position -> builderData.imageChangeListener?.onImageChange(position) }
            onDismiss = { dialog.dismiss() }
        }
    }

    init {
        setupViewerView()
    }
}