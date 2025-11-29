package in.afi.codekosh.pages.components;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import in.afi.codekosh.R;

public class DefaultLayout extends LinearLayout {
    private final Context context;
    public LinearLayout toolsLayout;
    public ImageView backButton;
    public View divider;
    public NestedScrollView nestedScrollView;
    public LinearLayout baseLayout;
    public CoordinatorLayout coordinator;

    public DefaultLayout(@NonNull Context context) {
        super(context);
        this.context = context;
        initialize();
    }


    private void initialize() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.default_layout, this, true);
        toolsLayout = findViewById(R.id.tools);
        backButton = findViewById(R.id.back);
        divider = findViewById(R.id.divider);
        baseLayout = findViewById(R.id.base);
        coordinator = findViewById(R.id.coordinator);
        nestedScrollView = findViewById(R.id.nested_scroll_view);
        int currentColorFilter = isNightMode() ? 0xFFFFFFFF : 0xFF000000;
        backButton.setColorFilter(currentColorFilter, PorterDuff.Mode.SRC_IN);
        if (isNightMode()) {
            divider.setBackgroundColor(ContextCompat.getColor(context, R.color.divider_color_night));
        } else {
            divider.setBackgroundColor(ContextCompat.getColor(context, R.color.divider_color));
        }
        setUpDivider();
    }

    public void backButtonClickListener(OnClickListener listener) {
        backButton.setOnClickListener(listener);
    }

    public void attachToRoot(View view, LayoutParams params) {
        baseLayout.addView(view, params);
    }


    private void setUpDivider() {
        divider = findViewById(R.id.divider);
        divider.setVisibility(View.GONE);
        nestedScrollView.setVerticalScrollBarEnabled(false);
        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> divider.setVisibility(scrollY > 0 ? View.VISIBLE : View.GONE));
    }

    private boolean isNightMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }


}
