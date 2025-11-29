package in.afi.codekosh.components;

import android.content.Context;
import android.content.SharedPreferences;

public class AppConfig {
    private static final String PREF_NAME = "app_config";
    private static final String KEY_EDITOR_THEME = "editor_theme";
    private static final String KEY_APP_THEME = "app_theme";
    private static final String KEY_DYNAMIC_THEME = "dynamic_theme";
    private static final String KEY_SWB_INSTALLER = "swb_installer_";
    private static final String KEY_DOWNLOAD_MANAGER = "download_manager";

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public AppConfig(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public String getEditorTheme() {
        return preferences.getString(KEY_EDITOR_THEME, "android_studio");
    }
    public void setEditorTheme(String n) {
        preferences.edit().putString(KEY_EDITOR_THEME, n).apply();
    }
    public String getAppTheme() {
        return preferences.getString(KEY_APP_THEME, "system");
    }
    public void setAppTheme(String name) {
        editor.putString(KEY_APP_THEME, name).apply();
    }
    public String getSwbInstaller() {
        return preferences.getString(KEY_SWB_INSTALLER, "sketchware");
    }
    public void setSwbInstaller(String n) {
        preferences.edit().putString(KEY_SWB_INSTALLER, n).apply();
    }
    public String getDownloadManager() {
        return preferences.getString(KEY_DOWNLOAD_MANAGER, "0");
    }
    public void setDownloadManager(String n) {
        preferences.edit().putString(KEY_DOWNLOAD_MANAGER, n).apply();
    }
    public int getDynamicTheme() {
        return preferences.getInt(KEY_DYNAMIC_THEME, 0);
    }
    public void setDynamicTheme(int theme) {
        editor.putInt(KEY_DYNAMIC_THEME, theme).apply();
    }


}

