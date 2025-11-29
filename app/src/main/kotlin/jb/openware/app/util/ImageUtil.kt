package jb.openware.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File

object ImageUtil {

    fun compressImage(
        context: Context,
        uri: Uri,
        quality: Int
    ): File? {
        return try {
            // Create temp file
            val file = File.createTempFile("compressed_", ".jpg", context.cacheDir)

            context.contentResolver.openInputStream(uri)?.use { input ->
                val bitmap = BitmapFactory.decodeStream(input)
                    ?: return null

                file.outputStream().use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
                }

                bitmap.recycle()
            }

            file
        } catch (e: Exception) {
            Log.e("ImageUtils", "Compression failed: ${e.message}")
            null
        }
    }
}
