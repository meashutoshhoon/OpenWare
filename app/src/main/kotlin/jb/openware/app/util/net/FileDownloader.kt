package jb.openware.app.util.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class DownloadProgress(
    val progress: Int,
    val done: Boolean = false,
    val error: Exception? = null
)

object Downloader {

    fun download(
        fileUrl: String,
        destinationPath: String
    ): Flow<DownloadProgress> = flow {

        emit(DownloadProgress(0))

        withContext(Dispatchers.IO) {
            try {
                val url = URL(fileUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.connect()

                val length = connection.contentLength
                val input = BufferedInputStream(connection.inputStream)
                val output = FileOutputStream(destinationPath)

                val buffer = ByteArray(8 * 1024)
                var downloaded = 0L
                var count: Int

                while (input.read(buffer).also { count = it } != -1) {
                    output.write(buffer, 0, count)
                    downloaded += count

                    if (length > 0) {
                        val progress = ((downloaded * 100) / length).toInt()
                        emit(DownloadProgress(progress))
                    }
                }

                output.flush()
                output.close()
                input.close()

                emit(DownloadProgress(100, done = true))

            } catch (e: Exception) {
                emit(DownloadProgress(0, done = true, error = e))
            }
        }
    }
}
