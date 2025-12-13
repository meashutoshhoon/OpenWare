package jb.openware.app.ui.cells

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import jb.openware.app.R
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
class RatioListCell(context: Context) : LinearLayout(context) {

    val textView: MaterialTextView
    private val button: RadioButton
    val container: LinearLayout

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        )
        isClickable = true

        // Main inner container
        container = LinearLayout(context).apply {
            orientation = HORIZONTAL
            setPaddingRelative(dp(10), dp(10), dp(10), dp(10))
            background = rippleDrawable(dp(16))
        }

        // MaterialTextView
        textView = MaterialTextView(context).apply {
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

        container.addView(
            textView, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(10)
                marginEnd = dp(5)
            })

        // RadioButton
        button = RadioButton(context).apply {
            isClickable = false   // selection is handled by cell click
        }

        container.addView(
            button, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginStart = dp(5)
                marginEnd = dp(10)
            })

        addView(
            container, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                setMargins(dp(5), dp(15), dp(5), dp(5))
            })

        // Click on container → toggle radio
        container.setOnClickListener {
            button.isChecked = true
        }
    }

    fun setData(text: String, position: Int, checkedPosition: Int) {
        textView.text = text
        button.isChecked = (position == checkedPosition)
    }

    private fun dp(value: Int): Int = (resources.displayMetrics.density * value).roundToInt()

    private fun rippleDrawable(radius: Int): Drawable {
        val mask = GradientDrawable().apply {
            cornerRadius = radius.toFloat()
        }

        val rippleColor = ColorStateList.valueOf(
            "#19000000".toColorInt()
        )

        return RippleDrawable(rippleColor, null, mask)
    }
}
