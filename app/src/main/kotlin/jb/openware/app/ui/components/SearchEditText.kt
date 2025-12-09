package jb.openware.app.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import jb.openware.app.R

class SearchEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    companion object {
        private const val DRAWABLE_END = 2
    }

    private val clearIcon: Drawable? =
        AppCompatResources.getDrawable(context, R.drawable.ic_clear)

    init {
        hint = wrapHint(hint)
        updateActionIcon()
    }

    fun setQuery(query: String) {
        setText(query)
        setSelection(query.length)
    }

    private val isEmpty: Boolean
        get() = text.isNullOrEmpty()

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP && hasFocus()) {
            clearFocus()
        }
        return super.onKeyPreIme(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return if (
            event.source == InputDevice.SOURCE_KEYBOARD &&
            (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) &&
            event.modifiers == 0 &&
            !isEmpty
        ) {
            cancelLongPress()
            clearFocus()
            true
        } else {
            super.onKeyUp(keyCode, event)
        }
    }

    override fun onEditorAction(actionCode: Int) {
        super.onEditorAction(actionCode)
        if (actionCode == IME_ACTION_SEARCH) {
            // hook if you want, or leave it for external listener
        }
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        updateActionIcon()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        updateActionIcon()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val drawable = compoundDrawablesRelative[DRAWABLE_END]
            if (drawable != null && drawable.isVisible) {
                val drawableWidth = drawable.bounds.width()
                val start = width - paddingEnd - drawableWidth
                val end = width - paddingEnd
                if (event.x in start.toFloat()..end.toFloat()) {
                    onActionIconClick()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun clearFocus() {
        super.clearFocus()
        text?.clear()
    }

    fun setHintCompat(@StringRes resId: Int) {
        hint = wrapHint(context.getString(resId))
    }

    private fun onActionIconClick() {
        if (!isEmpty) {
            text?.clear()
        }
    }

    private fun updateActionIcon() {
        val icon = if (isEmpty) null else clearIcon
        setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, icon, null)
    }

    private fun wrapHint(raw: CharSequence?): CharSequence? {
        if (raw.isNullOrEmpty()) return raw
        return SpannableString(raw).apply {
            setSpan(
                TextAppearanceSpan(context, R.style.TextAppearance_OW_SearchView),
                0,
                length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
}