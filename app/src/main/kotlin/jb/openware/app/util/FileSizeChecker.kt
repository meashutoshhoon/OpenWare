package jb.openware.app.util

object FileSizeChecker {

    fun isFileSizeLessThan25MB(size: String): Boolean {
        return parseSizeToBytes(size) <= 25L * 1024 * 1024
    }

    private fun parseSizeToBytes(size: String): Long {
        val trimmed = size.trim().uppercase()
        val number = trimmed.takeWhile { it.isDigit() || it == '.' }.toDoubleOrNull()
            ?: throw IllegalArgumentException("Invalid size format: $size")

        val multiplier = when {
            trimmed.endsWith("KB") -> 1024L
            trimmed.endsWith("MB") -> 1024L * 1024
            trimmed.endsWith("GB") -> 1024L * 1024 * 1024
            else -> 1L
        }

        return (number * multiplier).toLong()
    }
}
