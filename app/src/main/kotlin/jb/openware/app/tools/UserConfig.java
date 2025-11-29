package in.afi.codekosh.tools;

import android.content.Context;
import android.content.SharedPreferences;

public class UserConfig {
    private static final String PREF_NAME = "user_config";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_BADGE = "badge";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_PROFILE_URL = "profileUrl";
    private static final String KEY_LOGGED_IN = "loggedIn";
    private static final String KEY_UID = "uid";
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public UserConfig(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public int getBadge() {
        return preferences.getInt(KEY_BADGE, 0);
    }

    public void setBadge(int n) {
        preferences.edit().putInt(KEY_BADGE, n).apply();
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, "");
    }

    public String getName() {
        return preferences.getString(KEY_NAME, "");
    }

    public void setName(String name) {
        editor.putString(KEY_NAME, name).apply();
    }

    public String getPassword() {
        return preferences.getString(KEY_PASSWORD, "");
    }

    public String getProfileUrl() {
        return preferences.getString(KEY_PROFILE_URL, "");
    }

    public void setProfileUrl(String string2) {
        preferences.edit().putString(KEY_PROFILE_URL, string2).apply();
    }

    public String getUid() {
        return preferences.getString(KEY_UID, "");
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_LOGGED_IN, false);
    }

    public void saveLoginDetails(String email, String password, String uid) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_UID, uid);
        editor.apply();
    }

    public void logout() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_LOGGED_IN, false);
        editor.putString(KEY_EMAIL, null);
        editor.putString(KEY_PASSWORD, null);
        editor.putString(KEY_PROFILE_URL, null);
        editor.putString(KEY_UID, null);
        editor.putInt(KEY_BADGE, 0);
        editor.apply();

    }

}
