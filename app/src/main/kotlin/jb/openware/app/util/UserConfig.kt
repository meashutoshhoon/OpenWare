package jb.openware.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class UserConfig(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var badge: Int
        get() = prefs.getInt(KEY_BADGE, 0)
        set(value) {
            prefs.edit { putInt(KEY_BADGE, value) }
        }

    var email: String
        get() = prefs.getString(KEY_EMAIL, "") ?: ""
        private set(value) {
            prefs.edit { putString(KEY_EMAIL, value) }
        }

    var name: String
        get() = prefs.getString(KEY_NAME, "") ?: ""
        set(value) {
            prefs.edit { putString(KEY_NAME, value) }
        }

    var password: String
        get() = prefs.getString(KEY_PASSWORD, "") ?: ""
        private set(value) {
            prefs.edit { putString(KEY_PASSWORD, value) }
        }

    var profileUrl: String
        get() = prefs.getString(KEY_PROFILE_URL, "") ?: ""
        set(value) {
            prefs.edit { putString(KEY_PROFILE_URL, value) }
        }

    var uid: String
        get() = prefs.getString(KEY_UID, "") ?: ""
        private set(value) {
            prefs.edit { putString(KEY_UID, value) }
        }

    val isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun saveLoginDetails(email: String, password: String, uid: String) {
        prefs.edit {
            putBoolean(KEY_LOGGED_IN, true)
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            putString(KEY_UID, uid)
        }
    }

    fun logout() {
        prefs.edit {
            putBoolean(KEY_LOGGED_IN, false)
            remove(KEY_EMAIL)
            remove(KEY_PASSWORD)
            remove(KEY_PROFILE_URL)
            remove(KEY_UID)
            putInt(KEY_BADGE, 0)
        }
    }

    companion object {
        private const val PREF_NAME = "user_config"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_BADGE = "badge"
        private const val KEY_PASSWORD = "password"
        private const val KEY_PROFILE_URL = "profileUrl"
        private const val KEY_LOGGED_IN = "loggedIn"
        private const val KEY_UID = "uid"
    }
}