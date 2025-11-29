package jb.openware.app.data

data class LogEntry(
    val name: String,
    val releasedOn: String,
    val log: String
)

object LogsData {

    val updateLog = """
        OpenWare v1.1.0
        - F-droid integration.
        - Youtube video link supported in project uploading.
        - Linking with website.
    """.trimIndent()

    val logsList: List<LogEntry> = listOf(



        LogEntry(
            name = "OpenWare v1.0.0",
            releasedOn = "5-12-2025",
            log = """
                - Open Source Android Apps available
                - Material expressive design
                - Premium projects
                - Fast loading
                - User-friendly app
            """.trimIndent()
        )
    )
}
