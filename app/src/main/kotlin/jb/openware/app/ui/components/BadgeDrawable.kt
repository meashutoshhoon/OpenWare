package jb.openware.app.ui.components

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.widget.ImageView
import com.google.android.material.color.MaterialColors
import jb.openware.app.R

class BadgeDrawable(private val context: Context) {

    fun setBadge(badge: String, imageView: ImageView) {
        val badgeInt = badge.toIntOrNull() ?: return

        val iconRes = when (badgeInt) {
            1 -> R.drawable.premium
            2 -> R.drawable.diamond
            3 -> R.drawable.labs
            4 -> R.drawable.award_star
            5 -> R.drawable.admin
            6 -> R.drawable.code
            else -> return
        }

        imageView.setImageResource(iconRes)
        imageView.setColorFilter(getBadgeColor(), PorterDuff.Mode.SRC_IN)
    }

    fun Context.getThemeColor(attr: Int, fallback: Int = Color.TRANSPARENT): Int {
        return MaterialColors.getColor(this, attr, fallback)
    }

    private fun getBadgeColor(): Int {
        return context.getThemeColor(com.google.android.material.R.attr.colorSecondary)
    }
}
