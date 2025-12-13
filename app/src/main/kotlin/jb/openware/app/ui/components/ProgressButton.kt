package jb.openware.app.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import jb.openware.app.R

class ProgressButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val textView: TextView
    private val progressView: RadialProgressView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        setPaddingRelative(dp(8), 0, dp(8), 0)
        minimumHeight = dp(42)

        textView = TextView(context).apply {
            isAllCaps = false
            textSize = 14f
            typeface = ResourcesCompat.getFont(context, R.font.opensans_regular)
        }
        addView(textView)

        progressView = RadialProgressView(context).apply {
            visibility = GONE
            setSize(dp(24))
            setNoProgress(false)
        }
        addView(progressView)
    }

    // --- Public API ---

    fun setText(text: CharSequence) {
        textView.text = text
    }

    fun setTextColor(color: Int) {
        textView.setTextColor(color)
    }

    fun setProgress(percent: Int) {
        progressView.setProgress(percent.coerceIn(0, 100) / 100f)
    }

    fun setProgressColor(color: Int) {
        progressView.setProgressColor(color)
    }

    fun showProgress(show: Boolean) {
        TransitionManager.beginDelayedTransition(this, AutoTransition())

        textView.visibility = if (show) GONE else VISIBLE
        progressView.visibility = if (show) VISIBLE else GONE
    }

    fun getProgressView(): RadialProgressView = progressView

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
