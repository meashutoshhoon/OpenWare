package jb.openware.app.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.util.StateSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import jb.openware.app.R
import kotlin.math.ceil

class LinkSpan @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val textView1: TextView
    private val textView2: TextView

    init {
        inflate(context, R.layout.link_span_layout, this)
        textView1 = findViewById(R.id.textView1)
        textView2 = findViewById(R.id.textView2)

        val color = 0xFF4991CC.toInt()
        textView2.background = getRoundRectSelectorDrawable(color)

        val padding = resources.getDimensionPixelSize(R.dimen.link_span_padding)
        setPaddingRelative(padding, padding, padding, padding)
    }

    fun setFirstText(text: String) {
        textView1.text = text
    }

    fun setSecondText(text: String) {
        textView2.text = text
    }

    fun setFirstTextColor(color: Int) {
        textView1.setTextColor(color)
    }

    fun setSecondTextColor(color: Int) {
        textView2.setTextColor(color)
    }

    fun setTextSize(sizeSp: Float) {
        textView1.textSize = sizeSp
        textView2.textSize = sizeSp
    }

    override fun setOnClickListener(l: OnClickListener?) {
        // Delegate clicks only to the second text, same as your Java behavior
        textView2.setOnClickListener(l)
    }

    companion object {
        @JvmField
        var density: Float = 1f

        @JvmStatic
        fun dp(value: Float): Int {
            if (value == 0f) return 0
            return ceil(density * value).toInt()
        }

        @JvmStatic
        fun getRoundRectSelectorDrawable(color: Int): Drawable {
            val radius = dp(15f)
            val maskDrawable = createRoundRectDrawable(radius, 0xFFFFFFFF.toInt())
            val colorStateList = ColorStateList(
                arrayOf(StateSet.WILD_CARD),
                intArrayOf((color and 0x00FFFFFF) or 0x19000000)
            )
            return RippleDrawable(colorStateList, null, maskDrawable)
        }

        @JvmStatic
        fun createRoundRectDrawable(rad: Int, defaultColor: Int): Drawable {
            val r = rad.toFloat()
            val radii = floatArrayOf(r, r, r, r, r, r, r, r)
            return ShapeDrawable(RoundRectShape(radii, null, null)).apply {
                paint.color = defaultColor
            }
        }
    }
}
