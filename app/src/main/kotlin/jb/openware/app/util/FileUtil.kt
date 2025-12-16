package jb.openware.app.util

import java.io.File

object FileUtil {

    @JvmStatic
    fun isExistFile(path: String): Boolean = File(path).exists()

    @JvmStatic
    fun makeDir(path: String) {
        if (!isExistFile(path)) {
            File(path).mkdirs()
        }
    }
}
