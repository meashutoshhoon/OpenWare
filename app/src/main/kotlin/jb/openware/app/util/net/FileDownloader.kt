package jb.openware.app.util.net

import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

fun downloadFiles(fileUrl: String?, destinationPath: String?, callback: DownloadCallback) {
    try {
        val url = URL(fileUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()

        val fileLength = connection.contentLength

        val input: InputStream = BufferedInputStream(url.openStream())
        val output = FileOutputStream(destinationPath)

        val data = ByteArray(1024)
        var total: Long = 0
        var count: Int

        callback.onDownloadStart()

        while ((input.read(data).also { count = it }) != -1) {
            total += count.toLong()
            output.write(data, 0, count)
            callback.onProgressUpdate(((total * 100) / fileLength).toInt())
        }

        output.flush()
        output.close()
        input.close()
        callback.onDownloadComplete()
    } catch (e: Exception) {
        callback.onDownloadFailed(e)
    }
}