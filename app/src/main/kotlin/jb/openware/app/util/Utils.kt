package jb.openware.app.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import jb.openware.app.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt

object Utils {

    fun getBackPressedClickListener(activity: ComponentActivity): View.OnClickListener {
        return View.OnClickListener {
            activity.onBackPressedDispatcher.onBackPressed()
        }
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

    fun dp(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).roundToInt()
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