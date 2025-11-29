package jb.openware.app.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import jb.openware.app.R
import jb.openware.app.ui.common.booleanState
import jb.openware.app.ui.common.intState

object ThemeUtil {
    private var isAmoledTheme: Boolean = false
    private var isDynamicTheme: Boolean = false
    private var themeMode: Int = AppCompatDelegate.MODE_NIGHT_NO

    fun updateTheme(activity: AppCompatActivity) {
        isAmoledTheme = AMOLED_THEME.booleanState
        isDynamicTheme = DYNAMIC_THEME.booleanState
        themeMode = THEME_MODE.intState

        AppCompatDelegate.setDefaultNightMode(themeMode)

        if (isAmoledTheme && isNightMode(activity)) {
            setHighContrastDarkTheme(activity)
        } else {
            setNormalTheme(activity)
        }
    }

    private fun setHighContrastDarkTheme(activity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activity.setTheme(
                if (isDynamicTheme) R.style.OW_AmoledTheme_DynamicColors
                else R.style.OW_AmoledTheme
            )
        } else {
            activity.setTheme(R.style.ThemeOverlay_OW_AmoledThemeBelowV31)
        }
    }

    private fun setNormalTheme(activity: AppCompatActivity) {
        if (isDynamicTheme) {
            activity.setTheme(R.style.OW_DynamicColors)
        } else {
            activity.setTheme(R.style.OW_AppTheme)
        }
    }

    // Returns if the device is in dark mode
    fun isNightMode(context: Context): Boolean {
        return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
    }

    // Returns a color
    fun getColor(color: Int, context: Context): Int {
        return ContextCompat.getColor(context, color)
    }

    fun colorError(context: Context): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorError, typedValue, true)
        return typedValue.data
    }
}