package jb.openware.app.ui.components

import android.app.Activity
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.Toolbar
import androidx.core.content.getSystemService
import com.google.android.material.appbar.MaterialToolbar
import jb.openware.app.R

class SearchBarView(
    private val activity: Activity,
    private val toolbar: MaterialToolbar,
    private val editText: SearchEditText
) {

    fun setMenuVisibility(isVisible: Boolean) {
        toolbar.menu.clear()
        if (isVisible) {
            toolbar.inflateMenu(R.menu.profile_menu)
        }
    }

    fun setToolbarClickListener(listener: View.OnClickListener) {
        toolbar.setNavigationOnClickListener(listener)
    }

    fun openEditText() {
        editText.requestFocus()
        val imm: InputMethodManager? = activity.getSystemService()
        imm?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    fun setHint(hint: CharSequence) {
        editText.hint = hint
    }

    fun setOnTextChangedListener(textWatcher: TextWatcher) {
        editText.addTextChangedListener(textWatcher)
    }

    fun setEditTextEnabled(enabled: Boolean) {
        editText.apply {
            isFocusable = enabled
            isClickable = true
            isCursorVisible = enabled
            if (enabled) {
                isFocusableInTouchMode = true
            }
        }
    }

    fun setEditTextClickListener(listener: View.OnClickListener) {
        editText.setOnClickListener(listener)
    }

    fun setSearchSubmitListener(listener: View.OnClickListener) {
        editText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                listener.onClick(v)
                true
            } else {
                false
            }
        }
    }

    val searchText: String
        get() = editText.text?.toString().orEmpty()

    fun setMenuClickListener(listener: Toolbar.OnMenuItemClickListener) {
        toolbar.setOnMenuItemClickListener(listener)
    }

    val menuIcon: MenuItem?
        get() = toolbar.menu.findItem(R.id.profile)
}