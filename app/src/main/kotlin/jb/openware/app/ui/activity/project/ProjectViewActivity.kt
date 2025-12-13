package jb.openware.app.ui.activity.project

import android.animation.TimeInterpolator
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.text.util.Linkify
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColorInt
import androidx.core.widget.NestedScrollView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import jb.openware.app.databinding.ActivityProjectViewBinding
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.util.Utils
import jb.openware.app.util.Utils.shareText
import jb.openware.app.util.websiteUrl

class ProjectViewActivity :
    BaseActivity<ActivityProjectViewBinding>(ActivityProjectViewBinding::inflate) {

    override fun init() {

    }

    override fun initLogic() {

    }

    private fun share() {
        val isFree = developer.getString("type", "Free") == "Free"

        val baseUrl = buildString {
            append(websiteUrl)
            append(if (isFree) "p/n/" else "p/p/")
        }

        getProjectId(
            key = key,
            premium = !isFree
        ) { projectId ->
            shareText(
                text = baseUrl + projectId
            )
        }
    }

    fun View.clickScaleAnimation(
        scaleFrom: Float = 0.9f,
        scaleTo: Float = 1f,
        duration: Long = 150L
    ) {
        animate()
            .scaleX(scaleFrom)
            .scaleY(scaleFrom)
            .setDuration(0)
            .withEndAction {
                animate()
                    .scaleX(scaleTo)
                    .scaleY(scaleTo)
                    .setDuration(duration)
                    .start()
            }
            .start()
    }

    fun setupScrollDivider(
        scrollView: NestedScrollView,
        divider: View
    ) {
        divider.visibility = View.GONE
        scrollView.isVerticalScrollBarEnabled = false

        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            divider.visibility = if (scrollY > 0) View.VISIBLE else View.GONE
        }
    }

    fun View.removeScrollbars() {
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun TextView.detectLinksMaterial() {
        isClickable = true
        linksClickable = true

        Linkify.addLinks(
            this,
            Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES
        )

        // Material 3 link color
        val typedArray = context.obtainStyledAttributes(
            intArrayOf(com.google.android.material.R.attr.colorTertiary)
        )

        val linkColor = typedArray.use { it.getColor(0, currentTextColor) }
        setLinkTextColor(linkColor)
    }

    fun View.animateLayoutChange(
        duration: Long = 200L,
        interpolator: TimeInterpolator = DecelerateInterpolator()
    ) {
        val parent = this as? ViewGroup ?: return

        AutoTransition().apply {
            this.duration = duration
            this.interpolator = interpolator
            TransitionManager.beginDelayedTransition(parent, this)
        }
    }

    fun View.roundedBackground(
        backgroundColor: Int,
        cornerRadius: Float,
        elevationDp: Float = 0f,
        ripple: Boolean = false,
        rippleColor: Int = "#9E9E9E".toColorInt()
    ) {
        val shape = GradientDrawable().apply {
            setColor(backgroundColor)
            this.cornerRadius = cornerRadius
        }

        elevation = elevationDp

        background = if (ripple) {
            isClickable = true
            RippleDrawable(
                ColorStateList.valueOf(rippleColor), shape, null
            )
        } else {
            shape
        }
    }

    fun View.roundedStrokeRipple(
        fillColor: Int, pressedColor: Int, cornerRadius: Float, strokeWidth: Int, strokeColor: Int
    ) {
        val shape = GradientDrawable().apply {
            setColor(fillColor)
            this.cornerRadius = cornerRadius
            setStroke(strokeWidth, strokeColor)
        }

        background = RippleDrawable(
            ColorStateList.valueOf(pressedColor), shape, null
        )
    }

    fun View.rippleOnly(color: Int) {
        background = RippleDrawable(
            ColorStateList.valueOf(color), null, null
        )
    }

}