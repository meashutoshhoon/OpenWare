package jb.openware.app.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import jb.openware.app.databinding.ProfileMenuBinding

object DrawableGenerator {

    fun generateDrawable(
        context: Context, color: Int, text: String
    ): Drawable {

        val binding = ProfileMenuBinding.inflate(LayoutInflater.from(context))

        binding.root.background = GradientDrawable().apply {
            cornerRadius = 360f
            setColor(color)
        }

        binding.txWord.text = text

        val size = dp(context)

        binding.root.measure(
            View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
        )
        binding.root.layout(0, 0, size, size)

        val bitmap = createBitmap(size, size)
        binding.root.draw(Canvas(bitmap))

        return bitmap.toDrawable(context.resources)
    }

    private fun dp(context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (28 * density).toInt()
    }
}
