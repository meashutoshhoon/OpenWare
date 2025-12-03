package jb.openware.app.ui.items

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import jb.openware.app.util.PreferenceUtil.updateBoolean
import jb.openware.app.util.SMOOTH_SCROLLING

data class SettingsItem(
    val id: String,
    @DrawableRes val symbolResId: Int?,
    val title: String,
    val description: String,
    val hasSwitch: Boolean,
    var isChecked: Boolean
) {

    fun getSymbol(context: Context): Drawable? {
        return symbolResId?.let { ContextCompat.getDrawable(context, it) }
    }

    fun saveSwitchState() {
        SMOOTH_SCROLLING.updateBoolean(isChecked)
    }
}
