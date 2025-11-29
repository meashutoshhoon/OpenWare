package jb.openware.app.ui.common

import android.annotation.SuppressLint
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import org.jetbrains.annotations.Contract
import kotlin.math.ceil

object LayoutHelper {
    const val MATCH_PARENT = -1
    const val WRAP_CONTENT = -2
    private var density = 1f
    private fun getSize(size: Float): Int {
        return (if (size < 0) size else dp(size)).toInt()
    }

    private fun dp(value: Float): Int {
        return if (value == 0f) {
            0
        } else ceil((density * value).toDouble()).toInt()
    }

    //region Gravity
    private fun getAbsoluteGravity(gravity: Int): Int {
        return Gravity.getAbsoluteGravity(gravity, ViewCompat.LAYOUT_DIRECTION_LTR)
    }

    @get:SuppressLint("RtlHardcoded")
    val absoluteGravityStart: Int
        get() = Gravity.LEFT

    @get:SuppressLint("RtlHardcoded")
    val absoluteGravityEnd: Int
        get() = Gravity.RIGHT

    //endregion
    //region ScrollView
    @Contract("_, _, _ -> new")
    fun createScroll(width: Int, height: Int, gravity: Int): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(
            getSize(width.toFloat()), getSize(height.toFloat()), gravity
        )
    }

    fun createScroll(
        width: Int,
        height: Int,
        gravity: Int,
        leftMargin: Float,
        topMargin: Float,
        rightMargin: Float,
        bottomMargin: Float
    ): FrameLayout.LayoutParams {
        val layoutParams =
            FrameLayout.LayoutParams(getSize(width.toFloat()), getSize(height.toFloat()), gravity)
        layoutParams.leftMargin = dp(leftMargin)
        layoutParams.topMargin = dp(topMargin)
        layoutParams.rightMargin = dp(rightMargin)
        layoutParams.bottomMargin = dp(bottomMargin)
        return layoutParams
    }

    //endregion
    //region FrameLayout
    fun createFrame(
        width: Int,
        height: Float,
        gravity: Int,
        leftMargin: Float,
        topMargin: Float,
        rightMargin: Float,
        bottomMargin: Float
    ): FrameLayout.LayoutParams {
        val layoutParams =
            FrameLayout.LayoutParams(getSize(width.toFloat()), getSize(height), gravity)
        layoutParams.setMargins(dp(leftMargin), dp(topMargin), dp(rightMargin), dp(bottomMargin))
        return layoutParams
    }

    @Contract("_, _, _ -> new")
    fun createFrame(width: Int, height: Int, gravity: Int): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(
            getSize(width.toFloat()), getSize(height.toFloat()), gravity
        )
    }

    @Contract("_, _ -> new")
    fun createFrame(width: Int, height: Float): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(getSize(width.toFloat()), getSize(height))
    }

    @Contract("_, _, _ -> new")
    fun createFrame(width: Float, height: Float, gravity: Int): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(getSize(width), getSize(height), gravity)
    }

    fun createFrameRelatively(
        width: Float,
        height: Float,
        gravity: Int,
        startMargin: Float,
        topMargin: Float,
        endMargin: Float,
        bottomMargin: Float
    ): FrameLayout.LayoutParams {
        val layoutParams =
            FrameLayout.LayoutParams(getSize(width), getSize(height), getAbsoluteGravity(gravity))
        layoutParams.leftMargin = dp(startMargin)
        layoutParams.topMargin = dp(topMargin)
        layoutParams.rightMargin = dp(endMargin)
        layoutParams.bottomMargin = dp(bottomMargin)
        return layoutParams
    }

    @Contract("_, _, _ -> new")
    fun createFrameRelatively(width: Float, height: Float, gravity: Int): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(
            getSize(width), getSize(height), getAbsoluteGravity(gravity)
        )
    }

    //endregion
    //region RelativeLayout
    fun createRelative(
        width: Float,
        height: Float,
        leftMargin: Int,
        topMargin: Int,
        rightMargin: Int,
        bottomMargin: Int,
        alignParent: Int,
        alignRelative: Int,
        anchorRelative: Int
    ): RelativeLayout.LayoutParams {
        val layoutParams = RelativeLayout.LayoutParams(getSize(width), getSize(height))
        if (alignParent >= 0) {
            layoutParams.addRule(alignParent)
        }
        if (alignRelative >= 0 && anchorRelative >= 0) {
            layoutParams.addRule(alignRelative, anchorRelative)
        }
        layoutParams.leftMargin = dp(leftMargin.toFloat())
        layoutParams.topMargin = dp(topMargin.toFloat())
        layoutParams.rightMargin = dp(rightMargin.toFloat())
        layoutParams.bottomMargin = dp(bottomMargin.toFloat())
        return layoutParams
    }

    fun createRelative(
        width: Int,
        height: Int,
        leftMargin: Int,
        topMargin: Int,
        rightMargin: Int,
        bottomMargin: Int
    ): RelativeLayout.LayoutParams {
        return createRelative(
            width.toFloat(),
            height.toFloat(),
            leftMargin,
            topMargin,
            rightMargin,
            bottomMargin,
            -1,
            -1,
            -1
        )
    }

    fun createRelative(
        width: Int,
        height: Int,
        leftMargin: Int,
        topMargin: Int,
        rightMargin: Int,
        bottomMargin: Int,
        alignParent: Int
    ): RelativeLayout.LayoutParams {
        return createRelative(
            width.toFloat(),
            height.toFloat(),
            leftMargin,
            topMargin,
            rightMargin,
            bottomMargin,
            alignParent,
            -1,
            -1
        )
    }

    fun createRelative(
        width: Float,
        height: Float,
        leftMargin: Int,
        topMargin: Int,
        rightMargin: Int,
        bottomMargin: Int,
        alignRelative: Int,
        anchorRelative: Int
    ): RelativeLayout.LayoutParams {
        return createRelative(
            width,
            height,
            leftMargin,
            topMargin,
            rightMargin,
            bottomMargin,
            -1,
            alignRelative,
            anchorRelative
        )
    }

    fun createRelative(
        width: Int, height: Int, alignParent: Int, alignRelative: Int, anchorRelative: Int
    ): RelativeLayout.LayoutParams {
        return createRelative(
            width.toFloat(),
            height.toFloat(),
            0,
            0,
            0,
            0,
            alignParent,
            alignRelative,
            anchorRelative
        )
    }

    fun createRelative(width: Int, height: Int): RelativeLayout.LayoutParams {
        return createRelative(width.toFloat(), height.toFloat(), 0, 0, 0, 0, -1, -1, -1)
    }

    fun createRelative(width: Int, height: Int, alignParent: Int): RelativeLayout.LayoutParams {
        return createRelative(width.toFloat(), height.toFloat(), 0, 0, 0, 0, alignParent, -1, -1)
    }

    fun createRelative(
        width: Int, height: Int, alignRelative: Int, anchorRelative: Int
    ): RelativeLayout.LayoutParams {
        return createRelative(
            width.toFloat(), height.toFloat(), 0, 0, 0, 0, -1, alignRelative, anchorRelative
        )
    }

    //endregion
    //region LinearLayout
    fun createLinear(
        width: Int,
        height: Int,
        weight: Float,
        gravity: Int,
        leftMargin: Int,
        topMargin: Int,
        rightMargin: Int,
        bottomMargin: Int
    ): LinearLayout.LayoutParams {
        val layoutParams =
            LinearLayout.LayoutParams(getSize(width.toFloat()), getSize(height.toFloat()), weight)
        layoutParams.setMargins(
            dp(leftMargin.toFloat()),
            dp(topMargin.toFloat()),
            dp(rightMargin.toFloat()),
            dp(bottomMargin.toFloat())
        )
        layoutParams.gravity = gravity
        return layoutParams
    }

    @JvmStatic
    fun createLinear(
        width: Int,
        height: Int,
        weight: Float,
        leftMargin: Int,
        topMargin: Int,
        rightMargin: Int,
        bottomMargin: Int
    ): LinearLayout.LayoutParams {
        val layoutParams =
            LinearLayout.LayoutParams(getSize(width.toFloat()), getSize(height.toFloat()), weight)
        layoutParams.setMargins(
            dp(leftMargin.toFloat()),
            dp(topMargin.toFloat()),
            dp(rightMargin.toFloat()),
            dp(bottomMargin.toFloat())
        )
        return layoutParams
    }

    fun createLinear(
        width: Int,
        height: Int,
        leftMargin: Float,
        topMargin: Float,
        rightMargin: Float,
        bottomMargin: Float
    ): LinearLayout.LayoutParams {
        val layoutParams =
            LinearLayout.LayoutParams(getSize(width.toFloat()), getSize(height.toFloat()))
        layoutParams.setMargins(dp(leftMargin), dp(topMargin), dp(rightMargin), dp(bottomMargin))
        return layoutParams
    }

    fun createLinear(
        width: Int, height: Int, weight: Float, gravity: Int
    ): LinearLayout.LayoutParams {
        val layoutParams =
            LinearLayout.LayoutParams(getSize(width.toFloat()), getSize(height.toFloat()), weight)
        layoutParams.gravity = gravity
        return layoutParams
    }

    @Contract("_, _, _ -> new")
    fun createLinear(width: Int, height: Int, weight: Float): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            getSize(width.toFloat()), getSize(height.toFloat()), weight
        )
    }

    @Contract("_, _ -> new")
    fun createLinear(width: Int, height: Int): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(getSize(width.toFloat()), getSize(height.toFloat()))
    }

    fun createLinearRelatively(
        width: Float,
        height: Float,
        gravity: Int,
        startMargin: Float,
        topMargin: Float,
        endMargin: Float,
        bottomMargin: Float
    ): LinearLayout.LayoutParams {
        val layoutParams = LinearLayout.LayoutParams(
            getSize(width), getSize(height), getAbsoluteGravity(gravity).toFloat()
        )
        layoutParams.leftMargin = dp(startMargin)
        layoutParams.topMargin = dp(topMargin)
        layoutParams.rightMargin = dp(endMargin)
        layoutParams.bottomMargin = dp(bottomMargin)
        return layoutParams
    }

    @Contract("_, _, _ -> new")
    fun createLinearRelatively(
        width: Float, height: Float, gravity: Int
    ): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            getSize(width), getSize(height), getAbsoluteGravity(gravity).toFloat()
        )
    } //endregion
}