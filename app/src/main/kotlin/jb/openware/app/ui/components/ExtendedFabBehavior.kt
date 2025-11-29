package jb.openware.app.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class ExtendedFabBehavior(
    context: Context,
    attrs: AttributeSet
) : CoordinatorLayout.Behavior<ExtendedFloatingActionButton>(context, attrs) {

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: ExtendedFloatingActionButton,
        dependency: View
    ): Boolean {
        return dependency is NestedScrollView
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: ExtendedFloatingActionButton,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return true // Always react to nested scroll
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: ExtendedFloatingActionButton,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        when {
            dyConsumed > 0 && child.isExtended -> child.shrink()  // user scrolls down
            dyConsumed < 0 && !child.isExtended -> child.extend() // user scrolls up
        }
    }
}
