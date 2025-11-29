package in.afi.codekosh.components;

import android.app.Activity;
import android.content.Context;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;

import in.afi.codekosh.R;

public class SearchBarView {
    private final Activity activity;
    private MaterialToolbar toolbar;
    private SearchEditText editText;

    public SearchBarView(Activity activity) {
        this.activity = activity;
    }

    public void init(MaterialToolbar toolbar, SearchEditText editText) {
        this.editText = editText;
        this.toolbar = toolbar;
    }

    public void setMenuVisibility(boolean isVisible) {
        if (!isVisible) {
            toolbar.getMenu().clear();
        } else {
            toolbar.inflateMenu(R.menu.profile_menu);
        }
    }


    public void setToolbarClickListener(View.OnClickListener listener) {
        toolbar.setNavigationOnClickListener(listener);
    }

    public void editTextOpener() {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void setHint(String hint) {
        editText.setHint(hint);
    }

    public void setOnTextChangedListener(TextWatcher textWatcher) {
        if (editText != null) {
            editText.addTextChangedListener(textWatcher);
        }
    }

    public void setEditTextEnable(boolean enable) {
        if (!enable) {
            editText.setFocusable(false);
            editText.setClickable(true);
            editText.setCursorVisible(false);
        } else {
            editText.setFocusable(true);
            editText.setClickable(true);
        }
    }

    public void setEditTextClickListener(View.OnClickListener listener) {
        if (editText != null) {
            editText.setOnClickListener(listener);
        }
    }

    public void setSearchSubmitListener(View.OnClickListener listener) {
        if (editText != null) {
            editText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    listener.onClick(v);
                    return true;
                }
                return false;
            });
        }
    }

    public String getSearchText() {
        return String.valueOf(editText.getText());
    }

    public void setMenuClickListener(Toolbar.OnMenuItemClickListener listener) {
        toolbar.setOnMenuItemClickListener(listener);
    }

    public MenuItem getMenuIcon() {
        return toolbar.getMenu().findItem(R.id.profile);
    }


}
