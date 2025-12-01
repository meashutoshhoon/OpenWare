package jb.openware.app.util

import android.content.Context
import android.os.Build
import `in`.afi.codekosh.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DeviceUtils {

    var savedVersionCode: Int = 0

    // Method for getting required device details for crash report
    fun getDeviceDetails(): String = """
        Brand : ${Build.BRAND}
        Device : ${Build.DEVICE}
        Model : ${Build.MODEL}
        Product : ${Build.PRODUCT}
        SDK : ${Build.VERSION.SDK_INT}
        Release : ${Build.VERSION.RELEASE}
        App version name : ${BuildConfig.VERSION_NAME}
        App version code : ${BuildConfig.VERSION_CODE}
    """.trimIndent()

    // returns android sdk version
    fun androidVersion(): Int = Build.VERSION.SDK_INT

    // returns device model name
    fun getDeviceName(): String = Build.MODEL

    // returns current (installed) app version code
    fun currentVersion(): Int = BuildConfig.VERSION_CODE

    /** Compare current app version code with the one retrieved from github to see if update available */
    fun isUpdateAvailable(latestVersionCode: Int): Boolean =
        BuildConfig.VERSION_CODE < latestVersionCode

    interface FetchLatestVersionCodeCallback {
        fun onResult(result: Int)
    }

    // Extracts the version code from the build.gradle file retrieved and converts it to integer
    fun extractVersionCode(text: String): Int {
        val regex = Regex("""versionCode\s+(\d+)""")
        val match = regex.find(text) ?: return -1
        return match.groupValues[1].toIntOrNull() ?: -1
    }

    // Extracts the version name from the build.gradle file retrieved and converts it to string
    fun extractVersionName(text: String): String {
        val regex = Regex("""versionName\s*"([^"]*)"""")
        val match = regex.find(text) ?: return ""
        return match.groupValues[1]
    }

    /**
     * Using this function to create unique file names for the saved txt files
     * (methods try to open files based on their name)
     */
    fun getCurrentDateTime(): String {
        val formatter = SimpleDateFormat("_yyyyMMddHHmmss", Locale.US)
        return formatter.format(Date())
    }
}

