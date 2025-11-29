package in.afi.codekosh.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

public abstract class ThemeBuilder {

    private final Context context;

    public ThemeBuilder(Context context) {
        this.context = context;
    }

    public void setTextColor(TextView textView, int dayColor, int nightColor) {
        int currentTextColor = isNightMode() ? nightColor : dayColor;
        textView.setTextColor(currentTextColor);
    }

    public void changeBackgroundColor(LinearLayout layout, int dayColor, int nightColor) {
        // Check whether it's daytime or nighttime
        int backgroundColor = isNightMode() ? dayColor : nightColor;

        // Set the background color of the LinearLayout
        layout.setBackgroundColor(backgroundColor);
    }

    public void setLinearLayoutBackgroundColor(LinearLayout linearLayout, int dayColor, int nightColor) {
        int currentBackgroundColor = isNightMode() ? nightColor : dayColor;
        linearLayout.setBackgroundColor(currentBackgroundColor);
    }


    public void setImageColorFilter(ImageView imageView, int dayColor, int nightColor) {
        int currentColorFilter = isNightMode() ? nightColor : dayColor;
        imageView.setColorFilter(currentColorFilter, PorterDuff.Mode.SRC_IN);
    }

    public void setImageIcon(ImageView imageView, int dayIcon, int nightIcon) {
        @SuppressLint("UseCompatLoadingForDrawables") Drawable currentIcon = isNightMode() ? context.getDrawable(nightIcon) : context.getDrawable(dayIcon);
        imageView.setImageDrawable(currentIcon);
    }

    public void setLottieAnimation(LottieAnimationView animationView, String dayAnimationFile, String nightAnimationFile) {
        String currentAnimationFile = isNightMode() ? nightAnimationFile : dayAnimationFile;
        animationView.setAnimation(currentAnimationFile);
        animationView.playAnimation();
    }

    public abstract void setValues(ThemeBuilder themeBuilder);

    public boolean isNightMode() {
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }
}
