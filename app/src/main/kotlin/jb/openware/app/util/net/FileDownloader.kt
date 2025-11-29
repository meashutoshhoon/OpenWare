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

        emit(DownloadProgress(progress = 0))

        withContext(Dispatchers.IO) {
            try {
                val url = URL(fileUrl)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 15000
                    requestMethod = "GET"
                    connect()
                }

                val fileLength = conn.contentLength.takeIf { it > 0 }

                val input = BufferedInputStream(conn.inputStream)
                val output = FileOutputStream(destinationPath)

                val buffer = ByteArray(8 * 1024)
                var total = 0L
                var count: Int

                while (input.read(buffer).also { count = it } != -1) {
                    output.write(buffer, 0, count)
                    total += count

                    fileLength?.let {
                        val progress = ((total * 100) / it).toInt()
                        emit(DownloadProgress(progress = progress))
                    }
                }

                output.flush()
                output.close()
                input.close()

                emit(DownloadProgress(progress = 100, done = true))

            } catch (e: Exception) {
                emit(DownloadProgress(progress = 0, done = true, error = e))
            }
        }
    }
}
