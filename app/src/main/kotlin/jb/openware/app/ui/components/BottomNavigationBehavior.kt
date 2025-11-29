package jb.openware.app.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat

class BottomNavigationBehavior(
    context: Context,
    attrs: AttributeSet
) : CoordinatorLayout.Behavior<View>(context, attrs) {

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: View,
        layoutDirection: Int
    ): Boolean {
        parent.onLayoutChild(child, layoutDirection)

        // Clamp translation between 0 and -child.height
        child.translationY = child.translationY
            .coerceIn(-child.height.toFloat(), 0f)

        return true
    }

    override fun onStartNestedScroll(
        parent: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(
        parent: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        when {
            dy > 0 -> hide(child)  // scrolling up → hide bar
            dy < 0 -> show(child)  // scrolling down → show bar
        }
    }

    private fun hide(v: View) {
        v.animate()
            .translationY(v.height.toFloat())
            .setInterpolator(AccelerateInterpolator(2f))
            .start()
    }

    private fun show(v: View) {
        v.animate()
            .translationY(0f)
            .setInterpolator(DecelerateInterpolator(2f))
            .start()
    }
}
