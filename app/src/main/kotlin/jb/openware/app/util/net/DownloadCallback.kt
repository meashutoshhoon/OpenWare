package jb.openware.app.util.net

interface DownloadCallback {
    fun onDownloadStart()
    fun onProgressUpdate(progress: Int)
    fun onDownloadComplete()
    fun onDownloadFailed(e: Exception?)
}