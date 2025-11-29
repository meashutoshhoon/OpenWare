package jb.openware.app.ui.cells

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.color.MaterialColors
import jb.openware.app.R
import jb.openware.app.ui.components.RadialProgressView
import kotlin.math.roundToInt

class ProgressButtonCell(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private val text = TextView(context)
    private val progress = RadialProgressView(context)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        minimumHeight = dp(42)
        setPaddingRelative(dp(8), 0, dp(8), 0)

        text.apply {
            isAllCaps = false
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = ResourcesCompat.getFont(context, R.font.opensans_regular)
            setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnPrimary))
        }

        addView(text)

        progress.apply {
            visibility = GONE
            setSize(dp(24))
            setNoProgress(false)
        }

        addView(progress)
    }

    private fun dp(value: Int): Int =
        (context.resources.displayMetrics.density * value).roundToInt()

    fun setText(value: String) {
        text.text = value
    }

    fun setTextColor(color: Int) {
        text.setTextColor(color)
    }

    fun setProgressColor(color: Int) {
        progress.setProgressColor(color)
    }

    fun setProgress(percent: Int) {
        progress.setProgress(percent / 100f)
    }

    fun showProgress(show: Boolean) {
        TransitionManager.beginDelayedTransition(this, AutoTransition())
        text.visibility = if (show) GONE else VISIBLE
        progress.visibility = if (show) VISIBLE else GONE
    }

    fun getProgressView(): RadialProgressView = progress
}
