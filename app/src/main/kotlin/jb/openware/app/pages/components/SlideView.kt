package jb.openware.app.pages.components

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

open class SlideView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = VERTICAL
    }
}