package jb.openware.app.util

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LightingColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.RandomAccessFile
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object FileUtil {

    // ---------- Internal helpers ----------

    private fun createNewFile(path: String) {
        val lastSep = path.lastIndexOf(File.separator)
        if (lastSep > 0) {
            val dirPath = path.take(lastSep)
            makeDir(dirPath)
        }

        val file = File(path)
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // ---------- Basic file IO ----------

    @JvmStatic
    fun readFile(path: String): String {
        createNewFile(path)

        val sb = StringBuilder()
        var fr: FileReader? = null
        try {
            fr = FileReader(File(path))
            val buff = CharArray(1024)
            var length: Int
            while (true) {
                length = fr.read(buff)
                if (length <= 0) break
                sb.append(buff, 0, length)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fr?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return sb.toString()
    }

    @JvmStatic
    fun getFileName(uri: Uri): String {
        return try {
            val path = uri.lastPathSegment
            path?.substring(path.lastIndexOf("/") + 1) ?: "unknown"
        } catch (e: Exception) {
            e.printStackTrace()
            "unknown"
        }
    }

    @JvmStatic
    fun writeFile(path: String, str: String) {
        createNewFile(path)
        var writer: FileWriter? = null
        try {
            writer = FileWriter(path, false)
            writer.write(str)
            writer.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                writer?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    fun copyFile(sourcePath: String, destPath: String) {
        if (!isExistFile(sourcePath)) return
        createNewFile(destPath)

        var fis: FileInputStream? = null
        var fos: FileOutputStream? = null
        try {
            fis = FileInputStream(sourcePath)
            fos = FileOutputStream(destPath, false)
            val buff = ByteArray(1024)
            var length: Int
            while (true) {
                length = fis.read(buff)
                if (length <= 0) break
                fos.write(buff, 0, length)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fis?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // ---------- Encryption / Decryption ----------

    @JvmStatic
    fun decrypt(path: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val bytes = "codekoshafisecur".toByteArray()
            cipher.init(
                Cipher.DECRYPT_MODE, SecretKeySpec(bytes, "AES"), IvParameterSpec(bytes)
            )
            RandomAccessFile(path, "r").use { raf ->
                val data = ByteArray(raf.length().toInt())
                raf.readFully(data)
                String(cipher.doFinal(data))
            }
        } catch (e: Exception) {
            ""
        }
    }

    @JvmStatic
    fun encrypt(path: String, text: String) {
        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val bytes = "codekoshafisecur".toByteArray()
            cipher.init(
                Cipher.ENCRYPT_MODE, SecretKeySpec(bytes, "AES"), IvParameterSpec(bytes)
            )
            RandomAccessFile(path, "rw").use { raf ->
                raf.write(cipher.doFinal(text.toByteArray()))
            }
        } catch (_: Exception) {
        }
    }

    // ---------- Directory operations ----------

    @JvmStatic
    fun copyDir(oldPath: String, newPath: String) {
        val oldFile = File(oldPath)
        val files = oldFile.listFiles() ?: return
        val newFile = File(newPath)
        if (!newFile.exists()) newFile.mkdirs()

        for (file in files) {
            if (file.isFile) {
                copyFile(file.path, "$newPath/${file.name}")
            } else if (file.isDirectory) {
                copyDir(file.path, "$newPath/${file.name}")
            }
        }
    }

    @JvmStatic
    fun moveFile(sourcePath: String, destPath: String) {
        copyFile(sourcePath, destPath)
        deleteFile(sourcePath)
    }

    @JvmStatic
    fun deleteFile(path: String) {
        val file = File(path)
        if (!file.exists()) return

        if (file.isFile) {
            file.delete()
            return
        }

        val fileArr = file.listFiles()
        fileArr?.forEach { subFile ->
            if (subFile.isDirectory) {
                deleteFile(subFile.absolutePath)
            }
            if (subFile.isFile) {
                subFile.delete()
            }
        }

        file.delete()
    }

    @JvmStatic
    fun isExistFile(path: String): Boolean = File(path).exists()

    @JvmStatic
    fun makeDir(path: String) {
        if (!isExistFile(path)) {
            File(path).mkdirs()
        }
    }

    @JvmStatic
    fun listDir(path: String, list: ArrayList<String>?) {
        val dir = File(path)
        if (!dir.exists() || dir.isFile) return
        val listFiles = dir.listFiles()
        if (listFiles.isNullOrEmpty()) return
        list ?: return

        list.clear()
        listFiles.forEach { file ->
            list.add(file.absolutePath)
        }
    }

    @JvmStatic
    fun isDirectory(path: String): Boolean = isExistFile(path) && File(path).isDirectory

    @JvmStatic
    fun isFile(path: String): Boolean = isExistFile(path) && File(path).isFile

    @JvmStatic
    fun getFileLength(path: String): Long = if (!isExistFile(path)) 0 else File(path).length()

    @JvmStatic
    fun getExternalStorageDir(): String = Environment.getExternalStorageDirectory().absolutePath

    @JvmStatic
    fun getPackageDataDir(context: Context): String =
        context.getExternalFilesDir(null)?.absolutePath ?: ""

    @JvmStatic
    fun getPublicDir(type: String): String =
        Environment.getExternalStoragePublicDirectory(type).absolutePath

    // ---------- Size formatting ----------

    @SuppressLint("DefaultLocale")
    @JvmStatic
    fun getFileSize(filePath: String): String {
        val file = File(filePath)
        val fileSizeInBytes = file.length()
        val fileSizeInKB = fileSizeInBytes / 1024.0
        val fileSizeInMB = fileSizeInKB / 1024.0
        val fileSizeInGB = fileSizeInMB / 1024.0

        return when {
            fileSizeInGB > 1 -> String.format("%.2f GB", fileSizeInGB)
            fileSizeInMB > 1 -> String.format("%.2f MB", fileSizeInMB)
            else -> String.format("%.2f KB", fileSizeInKB)
        }
    }

    // ---------- URI → file path ----------

    @JvmStatic
    fun convertUriToFilePath(context: Context, uri: Uri): String? {
        var path: String? = null

        if (DocumentsContract.isDocumentUri(context, uri)) {
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]
                    if (type.equals("primary", ignoreCase = true) && split.size > 1) {
                        path = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }
                }

                isDownloadsDocument(uri) -> {
                    val id = DocumentsContract.getDocumentId(uri)
                    if (!id.isNullOrEmpty()) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:", "")
                        }
                    }

                    val contentUri = ContentUris.withAppendedId(
                        "content://downloads/public_downloads".toUri(), id.toLong()
                    )

                    path = getDataColumn(context, contentUri, null, null)
                }

                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]
                    val contentUri: Uri? = when (type) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> null
                    }

                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])

                    path = getDataColumn(context, contentUri, selection, selectionArgs)
                }
            }
        } else if (ContentResolver.SCHEME_CONTENT.equals(uri.scheme, ignoreCase = true)) {
            path = getDataColumn(context, uri, null, null)
        } else if (ContentResolver.SCHEME_FILE.equals(uri.scheme, ignoreCase = true)) {
            path = uri.path
        }

        if (path != null) {
            return try {
                URLDecoder.decode(path, "UTF-8")
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?
    ): String? {
        if (uri == null) return null
        val column = MediaStore.Images.Media.DATA
        val projection = arrayOf(column)

        return try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndexOrThrow(column)
                        cursor.getString(index)
                    } else null
                }
        } catch (e: Exception) {
            null
        }
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean =
        "com.android.externalstorage.documents" == uri.authority

    private fun isDownloadsDocument(uri: Uri): Boolean =
        "com.android.providers.downloads.documents" == uri.authority

    private fun isMediaDocument(uri: Uri): Boolean =
        "com.android.providers.media.documents" == uri.authority

    // ---------- Bitmap helpers ----------

    private fun saveBitmap(bitmap: Bitmap, destPath: String) {
        createNewFile(destPath)
        try {
            FileOutputStream(File(destPath)).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun getScaledBitmap(path: String, max: Int): Bitmap {
        val src = BitmapFactory.decodeFile(path)
        var width = src.width
        var height = src.height
        val rate: Float

        if (width > height) {
            rate = max / width.toFloat()
            height = (height * rate).toInt()
            width = max
        } else {
            rate = max / height.toFloat()
            width = (width * rate).toInt()
            height = max
        }

        return src.scale(width, height)
    }

    @JvmStatic
    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    @JvmStatic
    fun decodeSampleBitmapFromPath(path: String, reqWidth: Int, reqHeight: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(path, options)
    }

    @JvmStatic
    fun resizeBitmapFileRetainRatio(fromPath: String, destPath: String, max: Int) {
        if (!isExistFile(fromPath)) return
        val bitmap = getScaledBitmap(fromPath, max)
        saveBitmap(bitmap, destPath)
    }

    @JvmStatic
    fun resizeBitmapFileToSquare(fromPath: String, destPath: String, max: Int) {
        if (!isExistFile(fromPath)) return
        val src = BitmapFactory.decodeFile(fromPath)
        val bitmap = src.scale(max, max)
        saveBitmap(bitmap, destPath)
    }

    @JvmStatic
    fun resizeBitmapFileToCircle(fromPath: String, destPath: String) {
        if (!isExistFile(fromPath)) return
        val src = BitmapFactory.decodeFile(fromPath)
        val bitmap = createBitmap(src.width, src.height)
        val canvas = Canvas(bitmap)

        val color = 0xff424242.toInt()
        val paint = Paint()
        val rect = Rect(0, 0, src.width, src.height)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle(src.width / 2f, src.height / 2f, src.width / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(src, rect, rect, paint)

        saveBitmap(bitmap, destPath)
    }

    @JvmStatic
    fun resizeBitmapFileWithRoundedBorder(fromPath: String, destPath: String, pixels: Int) {
        if (!isExistFile(fromPath)) return
        val src = BitmapFactory.decodeFile(fromPath)
        val bitmap = createBitmap(src.width, src.height)
        val canvas = Canvas(bitmap)

        val color = 0xff424242.toInt()
        val paint = Paint()
        val rect = Rect(0, 0, src.width, src.height)
        val rectF = RectF(rect)
        val roundPx = pixels.toFloat()

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(src, rect, rect, paint)

        saveBitmap(bitmap, destPath)
    }

    @JvmStatic
    fun cropBitmapFileFromCenter(fromPath: String, destPath: String, w: Int, h: Int) {
        if (!isExistFile(fromPath)) return
        val src = BitmapFactory.decodeFile(fromPath)

        val width = src.width
        val height = src.height

        if (width < w && height < h) return

        var x = 0
        var y = 0
        if (width > w) x = (width - w) / 2
        if (height > h) y = (height - h) / 2

        var cw = w
        var ch = h
        if (w > width) cw = width
        if (h > height) ch = height

        val bitmap = Bitmap.createBitmap(src, x, y, cw, ch)
        saveBitmap(bitmap, destPath)
    }

    @JvmStatic
    fun rotateBitmapFile(fromPath: String, destPath: String, angle: Float) {
        if (!isExistFile(fromPath)) return
        val src = BitmapFactory.decodeFile(fromPath)
        val matrix = Matrix().apply {
            postRotate(angle)
        }
        val bitmap = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
        saveBitmap(bitmap, destPath)
    }

    @JvmStatic
    fun scaleBitmapFile(fromPath: String, destPath: String, x: Float, y: Float) {
        if (!isExistFile(fromPath)) return
        val src = BitmapFactory.decodeFile(fromPath)
        val matrix = Matrix().apply {
            postScale(x, y)
        }
        val w = src.width
        val h = src.height
        val bitmap = Bitmap.createBitmap(src, 0, 0, w, h, matrix, true)
        saveBitmap(bitmap, destPath)
    }

    @JvmStatic
    fun skewBitmapFile(fromPath: String, destPath: String, x: Float, y: Float) {
        if (!isExistFile(fromPath)) return
        val src = BitmapFactory.decodeFile(fromPath)
        val matrix = Matrix().apply {
            postSkew(x, y)
        }
        val w = src.width
        val h = src.height
        val bitmap = Bitmap.createBitmap(src, 0, 0, w, h, matrix, true)
        saveBitmap(bitmap, destPath)
    }

    @JvmStatic
    fun setBitmapFileColorFilter(fromPath: String, destPath: String, color: Int) {
        if (!isExistFile(fromPath)) return
        val src = BitmapFactory.decodeFile(fromPath)
        val bitmap = Bitmap.createBitmap(src, 0, 0, src.width - 1, src.height - 1)
        val p = Paint()
        val filter: ColorFilter = LightingColorFilter(color, 1)
        p.colorFilter = filter
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, p)
        saveBitmap(bitmap, destPath)
    }

    @JvmStatic
    fun setBitmapFileBrightness(fromPath: String, destPath: String, brightness: Float) {
        if (!isExistFile(fromPath)) return
        val src = BitmapFactory.decodeFile(fromPath)
        val cm = ColorMatrix(
            floatArrayOf(
                1f,
                0f,
                0f,
                0f,
                brightness,
                0f,
                1f,
                0f,
                0f,
                brightness,
                0f,
                0f,
                1f,
                0f,
                brightness,
                0f,
                0f,
                0f,
                1f,
                0f
            )
        )

        val bitmap = createBitmap(src.width, src.height, src.config!!)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)

        saveBitmap(bitmap, destPath)
    }

    @JvmStatic
    fun setBitmapFileContrast(fromPath: String, destPath: String, contrast: Float) {
        if (!isExistFile(fromPath)) return
        val src = BitmapFactory.decodeFile(fromPath)
        val cm = ColorMatrix(
            floatArrayOf(
                contrast,
                0f,
                0f,
                0f,
                0f,
                0f,
                contrast,
                0f,
                0f,
                0f,
                0f,
                0f,
                contrast,
                0f,
                0f,
                0f,
                0f,
                0f,
                1f,
                0f
            )
        )

        val bitmap = createBitmap(src.width, src.height, src.config!!)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)

        saveBitmap(bitmap, destPath)
    }

    // ---------- EXIF / picture creation ----------

    @JvmStatic
    fun getJpegRotate(filePath: String): Int {
        var rotate: Int
        try {
            val exif = ExifInterface(filePath)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
            rotate = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            return 0
        }
        return rotate
    }

    @JvmStatic
    fun createNewPictureFile(context: Context): File {
        val date = SimpleDateFormat("yyyyMMdd_HHmmss")
        val fileName = date.format(Date()) + ".jpg"
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DCIM)
        val base = dir?.absolutePath ?: context.filesDir.absolutePath
        return File(base + File.separator + fileName)
    }
}
