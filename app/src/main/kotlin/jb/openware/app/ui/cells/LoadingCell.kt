package jb.openware.app.ui.cells

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.transition.TransitionManager
import jb.openware.app.ui.components.RadialProgressView

class LoadingCell(context: Context) : FrameLayout(context) {

    private val loader = RadialProgressView(context).apply {
        setProgressColor(0xFF006493.toInt())
    }

    init {
        addView(
            loader,
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        )
    }

    fun toggle(show: Boolean) {
        val parent = parent as? ViewGroup ?: return
        TransitionManager.beginDelayedTransition(parent)
        visibility = if (show) VISIBLE else GONE
    }
}
