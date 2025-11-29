package in.afi.codekosh.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import java.util.Objects;

import in.afi.codekosh.R;

public class SearchEditText extends AppCompatEditText {

    private static final int DRAWABLE_END = 2;
    private final Drawable clearIcon;
    private boolean isEmpty = true;

    public SearchEditText(@NonNull Context context) {
        this(context, null);
    }

    public SearchEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, com.google.android.material.R.attr.editTextStyle);
    }

    public SearchEditText(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        clearIcon = ContextCompat.getDrawable(context, com.google.android.material.R.drawable.abc_ic_clear_material);
        init();
    }

    private void init() {
        setHint(wrapHint(getHint()));
        updateActionIcon();
    }

    public void setQuery(String query) {
        setText(query);
        setSelection(query.length());
    }

    @Override
    public boolean onKeyPreIme(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (hasFocus()) {
                clearFocus();
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (event.getSource() == InputDevice.SOURCE_KEYBOARD && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.getModifiers() == 0 && !isEmpty()) {
            cancelLongPress();
            clearFocus();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onEditorAction(int actionCode) {
        super.onEditorAction(actionCode);
        if (actionCode == EditorInfo.IME_ACTION_SEARCH) {
            // Perform search action here
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        boolean empty = isEmpty();
        if (isEmpty != empty) {
            isEmpty = empty;
            updateActionIcon();
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        updateActionIcon();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Drawable drawable = getCompoundDrawablesRelative()[DRAWABLE_END];
            if (drawable != null && drawable.isVisible()) {
                int drawableWidth = drawable.getBounds().width();
                int start = getWidth() - getPaddingEnd() - drawableWidth;
                int end = getWidth() - getPaddingEnd();
                if (event.getX() >= start && event.getX() <= end) {
                    onActionIconClick();
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void clearFocus() {
        super.clearFocus();
        Objects.requireNonNull(getText()).clear();
    }

    public void setHintCompat(@StringRes int resId) {
        setHint(wrapHint(getResources().getString(resId)));
    }

    private void onActionIconClick() {
        if (!isEmpty()) {
            getText().clear();
        }
    }

    private void updateActionIcon() {
        Drawable icon = isEmpty() ? null : clearIcon;
        setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, icon != null ? com.google.android.material.R.drawable.abc_ic_clear_material : 0, 0);
    }


    private boolean isEmpty() {
        return getText() == null || getText().length() == 0;
    }

    private SpannableString wrapHint(CharSequence raw) {
        if (raw == null) {
            return null;
        }
        String rawHint = raw.toString();
        SpannableString formatted = new SpannableString(rawHint);
        formatted.setSpan(new TextAppearanceSpan(getContext(), R.style.TextAppearance_CodeKosh_SearchView), 0, formatted.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return formatted;
    }
}
