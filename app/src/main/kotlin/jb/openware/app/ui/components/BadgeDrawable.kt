package jb.openware.app.ui.components

import android.content.Context
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.widget.ImageView
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

    private fun getBadgeColor(): Int {
        val mask = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return if (mask == Configuration.UI_MODE_NIGHT_YES) 0xFF8DCDFF.toInt()
        else 0xFF006493.toInt()
    }
}
