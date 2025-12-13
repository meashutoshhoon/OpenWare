package jb.openware.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Handler
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import com.tencent.mmkv.MMKV
import jb.openware.app.ui.common.intState
import jb.openware.app.util.THEME_MODE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)

        context = applicationContext
        applicationHandler = Handler(context.mainLooper)
        packageInfo = packageManager.run {
            if (Build.VERSION.SDK_INT >= 33) getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
            else getPackageInfo(packageName, 0)
        }
        applicationScope = CoroutineScope(SupervisorJob())
        connectivityManager = getSystemService()!!

        AppCompatDelegate.setDefaultNightMode(THEME_MODE.intState)

        applicationScope.launch((Dispatchers.IO)) {
            try {
//                UpdateUtil.deleteOutdatedApk()
            } catch (th: Throwable) {
                withContext(Dispatchers.Main) { startCrashReportActivity(th) }
            }
        }

        Thread.setDefaultUncaughtExceptionHandler { _, e -> startCrashReportActivity(e) }
    }

    private fun startCrashReportActivity(th: Throwable) {
        th.printStackTrace()
        startActivity(
            Intent(this, CrashReportActivity::class.java).setAction("$packageName.error_report")
                .apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra("error_report", getVersionReport() + "\n" + th.stackTraceToString())
                })
    }

    companion object {
        lateinit var applicationScope: CoroutineScope
        lateinit var connectivityManager: ConnectivityManager
        lateinit var packageInfo: PackageInfo
        lateinit var applicationHandler: Handler

        fun getVersionReport(): String {
            val versionName = packageInfo.versionName
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
            val release = if (Build.VERSION.SDK_INT >= 30) {
                Build.VERSION.RELEASE_OR_CODENAME
            } else {
                Build.VERSION.RELEASE
            }

            return StringBuilder().append("App version: $versionName ($versionCode)\n")
                .append("Device information: Android $release (API ${Build.VERSION.SDK_INT})\n")
                .append("Supported ABIs: ${Build.SUPPORTED_ABIS.contentToString()}\n").toString()
        }

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

}