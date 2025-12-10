package jb.openware.app.ui.components

import android.os.Environment
import jb.openware.app.util.FileUtil

class FolderManagement {

    companion object {
        private val FOLDER_NAMES = listOf(
            "Download/OpenWare",
            "Download/OpenWare/Apk",
        )
    }

    fun makeFolders() {
        val basePath = Environment.getExternalStorageDirectory().absolutePath

        FOLDER_NAMES.forEach { name ->
            val path = "$basePath/$name"
            if (!FileUtil.isExistFile(path)) {
                FileUtil.makeDir(path)
            }
        }
    }
}
