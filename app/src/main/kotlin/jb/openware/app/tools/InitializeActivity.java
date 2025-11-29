package in.afi.codekosh.tools;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.elevation.SurfaceColors;

public class InitializeActivity {

    private static volatile InitializeActivity Instance;

    public static InitializeActivity getInstance() {
        if (Instance != null) return Instance;
        Class<InitializeActivity> class_ = InitializeActivity.class;
        synchronized (InitializeActivity.class) {
            Instance = new InitializeActivity();
            return Instance;
        }
    }

    public void initializeActivity(Activity activity) {
        // Set the default night mode to MODE_NIGHT_NO
        if (!isNightMode(activity)) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public void initializeActivity2(Activity activity) {
        // Set the default night mode to MODE_NIGHT_NO
        if (!isNightMode(activity)) {
            // Configure the window flags and colors
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(0xFFFDFCFF);
            window.setNavigationBarColor(activity.getResources().getColor(android.R.color.white));
        }
    }

    public void bottomNavigation(Activity activity) {
        // Set the default night mode to MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Configure the window flags and colors
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(0xFFFDFCFF);
        window.setNavigationBarColor(SurfaceColors.SURFACE_2.getColor(activity));
    }

    private boolean isNightMode(Activity activity) {
        int nightModeFlags = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }
}
