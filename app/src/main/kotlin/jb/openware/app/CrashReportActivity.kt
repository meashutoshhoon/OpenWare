package jb.openware.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import jb.openware.app.databinding.ActivityCrashReportBinding
import jb.openware.app.util.Const
import jb.openware.app.util.HapticUtils
import jb.openware.app.util.ThemeUtil
import jb.openware.app.util.Utils

class CrashReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        ThemeUtil.updateTheme(this)
        super.onCreate(savedInstanceState)

        val binding = ActivityCrashReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val errorMessage: String = intent.getStringExtra("error_report").toString()

        binding.crashInfo.text = errorMessage

        binding.reportButton.setOnClickListener {
            HapticUtils.weakVibrate(it)
            sendCrashReport(errorMessage)
        }

        binding.copy.setOnClickListener {
            HapticUtils.weakVibrate(it)
            Utils.copyToClipboard(errorMessage, this)
        }

        binding.copyButton.setOnClickListener {
            HapticUtils.weakVibrate(it)
            Utils.copyToClipboard(errorMessage, this)
        }

        binding.fabShare.setOnClickListener {
            HapticUtils.weakVibrate(it)
            Utils.shareOutput(this, this, "crash_report.txt", errorMessage)
        }
    }

    private fun sendCrashReport(message: String) {
        val subject = "Crash Report"
        val to = Const.DEV_MAIL

        try {
            val uriText = "mailto:$to?subject=$subject&body=$message"
            val uri = uriText.toUri()
            val emailIntent = Intent(Intent.ACTION_SENDTO, uri)
            startActivity(Intent.createChooser(emailIntent, "Send email using..."))
        } catch (_: Exception) {
            Toast.makeText(
                this@CrashReportActivity, "Failed to encode email content.", Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) finishAffinity()
    }
}