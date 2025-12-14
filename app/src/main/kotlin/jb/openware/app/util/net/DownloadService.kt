package jb.openware.app.util.net

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import jb.openware.app.R
import kotlinx.coroutines.*
import java.io.File
import androidx.core.net.toUri

class DownloadService : Service() {

    companion object {
        const val ACTION_PROGRESS = "download_progress"
        const val EXTRA_PROGRESS = "progress"
        const val EXTRA_DONE = "done"
        const val EXTRA_ERROR = "error"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val channelId = "download_channel"
    private val notificationId = 1

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("url") ?: return START_NOT_STICKY
        val dest = intent.getStringExtra("dest") ?: return START_NOT_STICKY

        // Start foreground safely
        startForegroundSafely()

        scope.launch {
            Downloader.download(url, dest).collect { state ->

                sendProgress(state)

                if (state.error != null || state.done) {
                    stopSelf()
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun sendProgress(state: DownloadProgress) {
        sendBroadcast(
            Intent(ACTION_PROGRESS).apply {
                putExtra(EXTRA_PROGRESS, state.progress)
                putExtra(EXTRA_DONE, state.done)
                putExtra(EXTRA_ERROR, state.error != null)
            }
        )

        updateNotificationSafely(state.progress)
    }

    private fun startForegroundSafely() {
        val notification = buildNotification(0)
        try {
            startForeground(notificationId, notification)
        } catch (_: SecurityException) {
            // Notification permission denied → service still runs
        }
    }

    private fun updateNotificationSafely(progress: Int) {
        try {
            NotificationManagerCompat.from(this)
                .notify(notificationId, buildNotification(progress))
        } catch (_: SecurityException) {
            // Permission denied → ignore
        }
    }

    private fun buildNotification(progress: Int): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Downloading")
            .setContentText("$progress%")
            .setSmallIcon(R.drawable.demo_icon)
            .setOnlyAlertOnce(true)
            .setProgress(100, progress, false)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Downloads",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}