package jb.openware.app.util.net

import android.content.Context
import androidx.core.content.edit

object DownloadStateStore {

    private const val PREF = "download_state"

    fun save(context: Context, progress: Int, done: Boolean, path: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit {
            putInt("progress", progress).putBoolean("done", done).putString("path", path)
        }
    }

    fun get(context: Context): Triple<Int, Boolean, String?> {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return Triple(
            sp.getInt("progress", 0), sp.getBoolean("done", false), sp.getString("path", null)
        )
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit { clear() }
    }
}
