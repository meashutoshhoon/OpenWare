package jb.openware.app.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.android.material.appbar.AppBarLayout
import jb.openware.app.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


object Utils {

    fun getBackPressedClickListener(activity: ComponentActivity): View.OnClickListener {
        return View.OnClickListener {
            activity.onBackPressedDispatcher.onBackPressed()
        }
    }

    fun isConnected(context: Context): Boolean {
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun convertDpToPixel(dp: Float, context: Context): Float {
        val scale = context.resources.displayMetrics.density
        return dp * scale + 0.5f
    }

    fun isToolbarExpanded(appBarLayout: AppBarLayout): Boolean {
        return appBarLayout.top == 0
    }


    fun openUrl(context: Context, url: String) {
        val uri = url.toUri()

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            // ignore — same behavior as your Java code
        }
    }

    fun getGithubApkSize(apkUrl: String): Long {
        val connection = (URL(apkUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "HEAD"
            instanceFollowRedirects = true
            connectTimeout = 10_000
            readTimeout = 10_000
        }

        connection.connect()
        val size = connection.contentLengthLong
        connection.disconnect()

        return size // bytes
    }

    @SuppressLint("DefaultLocale")
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.lastIndex) {
            size /= 1024
            unitIndex++
        }

        return if (unitIndex == 0)
            "${size.toInt()} ${units[unitIndex]}"
        else
            String.format("%.2f %s", size, units[unitIndex])
    }

    fun hideKeyboard(view: View?) {
        view?.let {
            try {
                val imm =
                    it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (!imm.isActive) {
                    return@let
                }
                imm.hideSoftInputFromWindow(it.windowToken, 0)
            } catch (_: Exception) {
            }
        }
    }

    fun copyToClipboard(text: String, context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(context.getString(R.string.copied_to_clipboard), text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }

    fun shareOutput(activity: Activity, context: Context, fileName: String, content: String) {
        try {
            val file = File(activity.cacheDir, fileName)

            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            activity.startActivity(Intent.createChooser(shareIntent, "Share File"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


}