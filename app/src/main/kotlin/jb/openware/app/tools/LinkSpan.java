package in.afi.codekosh.tools;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.afi.codekosh.R;

public class LinkSpan extends LinearLayout {
    // Variables
    public static float density = 1;
    private TextView textView1;
    private TextView textView2;

    public LinkSpan(Context context) {
        super(context);
        initializeViews(context);
    }

    public LinkSpan(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public LinkSpan(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context);
    }

    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    public static Drawable getRoundRectSelectorDrawable(int color) {
        Drawable maskDrawable = createRoundRectDrawable(dp((15)), 0xffffffff);
        ColorStateList colorStateList = new ColorStateList(new int[][]{StateSet.WILD_CARD}, new int[]{(color & 0x00ffffff) | 0x19000000});
        return new RippleDrawable(colorStateList, null, maskDrawable);
    }

    public static Drawable createRoundRectDrawable(int rad, int defaultColor) {
        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
        defaultDrawable.getPaint().setColor(defaultColor);
        return defaultDrawable;
    }

    private void initializeViews(Context context) {
        View.inflate(context, R.layout.link_span_layout, this);
        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        int getColor = 0xff4991cc;
        textView2.setBackground(getRoundRectSelectorDrawable(getColor));
        int padding = (int) getResources().getDimension(R.dimen.link_span_padding);
        setPaddingRelative(padding, padding, padding, padding);
    }

    public void setFirstText(String text) {
        textView1.setText(text);
    }

    public void setSecondText(String text) {
        textView2.setText(text);
    }

    public void setFirstTextColor(int color) {
        textView1.setTextColor(color);
    }

    public void setSecondTextColor(int color) {
        textView2.setTextColor(color);
    }

    public void setTextSize(float size) {
        textView1.setTextSize(size);
        textView2.setTextSize(size);
    }

    public void setOnClickListener(OnClickListener listener) {
        textView2.setOnClickListener(listener);
    }
}
