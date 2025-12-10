package jb.openware.imageviewer.loader

import android.widget.ImageView

fun interface ImageLoader<T> {
    fun loadImage(imageView: ImageView, image: T)
}