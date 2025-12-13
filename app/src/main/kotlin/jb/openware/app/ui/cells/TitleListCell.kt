package jb.openware.app.ui.cells

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.text.TextUtils
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.core.widget.ImageViewCompat
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import jb.openware.app.R
import jb.openware.app.ui.items.TitleListItem
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
class TitleListCell(context: Context) : LinearLayout(context) {

    private val icon: ImageView
    private val titleView: MaterialTextView
    private val subtitleView: MaterialTextView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        val margin = dp(3)
        layoutParams = MarginLayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, margin, 0, margin)
        }

        setPaddingRelative(dp(5), dp(10), dp(5), dp(10))
        isClickable = true
        background = rippleDrawable(dp(16))

        // ICON
        icon = ImageView(context).apply {
            setPaddingRelative(dp(2), dp(2), dp(2), dp(2))
        }

        addView(icon, LayoutParams(dp(35), dp(35)).apply {
            marginStart = dp(5)
            marginEnd = dp(5)
        })

        // VERTICAL TEXT LAYOUT
        val textLayout = LinearLayout(context).apply {
            orientation = VERTICAL
        }

        // TITLE
        titleView = MaterialTextView(context).apply {
            isSingleLine = true
            typeface = ResourcesCompat.getFont(context, R.font.en_light)
            setLineSpacing(dp(2).toFloat(), 1f)
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleLarge)
            setTextColor(
                MaterialColors.getColor(
                    this,
                    com.google.android.material.R.attr.colorOnSurface
                )
            )
        }

        textLayout.addView(
            titleView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        )

        // SUBTITLE
        subtitleView = MaterialTextView(context).apply {
            isSingleLine = true
            ellipsize = TextUtils.TruncateAt.END
            typeface = ResourcesCompat.getFont(context, R.font.opensans_regular)
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodySmall)
            setTextColor(
                MaterialColors.getColor(
                    this,
                    com.google.android.material.R.attr.colorOnSurfaceVariant
                )
            )
            setLineSpacing(dp(2).toFloat(), 1f)
        }

        textLayout.addView(
            subtitleView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        )

        addView(
            textLayout, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(5)
                marginEnd = dp(5)
            })

        // Tint icon according to theme
        ImageViewCompat.setImageTintList(
            icon, ColorStateList.valueOf(
                MaterialColors.getColor(
                    icon,
                    com.google.android.material.R.attr.colorOnSurfaceVariant
                )
            )
        )
    }

    fun setData(item: TitleListItem) {
        icon.setImageResource(item.image)
        titleView.text = item.title
        subtitleView.text = item.description
    }

    private fun dp(value: Int): Int = (resources.displayMetrics.density * value).roundToInt()

    private fun rippleDrawable(radius: Int): Drawable {
        val mask = GradientDrawable().apply {
            cornerRadius = radius.toFloat()
        }

        val ripple = ColorStateList.valueOf("#19000000".toColorInt())

        return RippleDrawable(ripple, null, mask)
    }
}
