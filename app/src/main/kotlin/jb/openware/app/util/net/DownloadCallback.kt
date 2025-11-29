package jb.openware.app.util.net

interface DownloadCallback {
    fun onDownloadStart()
    fun onProgress(progress: Int)
    fun onComplete()
    fun onError(e: Exception)
}
