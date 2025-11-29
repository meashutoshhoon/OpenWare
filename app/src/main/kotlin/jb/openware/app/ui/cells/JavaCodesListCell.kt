package jb.openware.app.ui.cells

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.text.TextUtils
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import jb.openware.app.R
import jb.openware.app.ui.items.TitleListItem
import kotlin.math.roundToInt
import androidx.core.graphics.toColorInt

class JavaCodesListCell(context: Context) : LinearLayout(context) {

    private val titleView: TextView
    private val subtitleView: TextView

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, dp(3), 0, dp(3))
        }

        setPaddingRelative(dp(3), dp(5), dp(3), dp(5))
        isClickable = true
        background = rippleBg(dp(16))

        // Title
        titleView = MaterialTextView(context).apply {
            isSingleLine = true
            typeface = ResourcesCompat.getFont(context, R.font.en_light)
            setLineSpacing(dp(2).toFloat(), 1f)
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleLarge)
            setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface))
        }

        addView(titleView, LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(dp(5), 0, dp(5), 0)
        })

        // Subtitle
        subtitleView = MaterialTextView(context).apply {
            isSingleLine = true
            ellipsize = TextUtils.TruncateAt.END
            typeface = ResourcesCompat.getFont(context, R.font.opensans_regular)
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodySmall)
            setLineSpacing(dp(2).toFloat(), 1f)
        }

        addView(subtitleView, LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(dp(5), 0, dp(5), 0)
        })
    }

    fun setData(item: TitleListItem) {
        titleView.text = item.title
        subtitleView.text = item.description
    }

    private fun dp(value: Int): Int =
        (context.resources.displayMetrics.density * value).roundToInt()

    private fun rippleBg(radius: Int): Drawable {
        val mask = GradientDrawable().apply {
            cornerRadius = radius.toFloat()
        }

        val rippleColor = ColorStateList.valueOf(
            "#19000000".toColorInt()
        )

        return RippleDrawable(rippleColor, null, mask)
    }
}
