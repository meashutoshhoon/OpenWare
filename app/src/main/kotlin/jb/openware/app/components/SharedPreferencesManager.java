package jb.openware.app.components;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private final String PREFERENCE_NAME = "SharedPreferences";
    private final Context context;

    public SharedPreferencesManager(Context context) {
        this.context = context;
    }


    // Save a string value to SharedPreferences
    public void saveString(String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    // Get a string value from SharedPreferences, default value is returned if the key is not found
    public String getString(String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    // Save an integer value to SharedPreferences
    public void saveInt(String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    // Get an integer value from SharedPreferences, default value is returned if the key is not found
    public int getInt(String key, int defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, defaultValue);
    }

    // Save a boolean value to SharedPreferences
    public void saveBoolean(String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    // Get a boolean value from SharedPreferences, default value is returned if the key is not found
    public boolean getBoolean(String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, defaultValue);
    }
}
