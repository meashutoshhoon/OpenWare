package jb.openware.app.ui.items

data class ServerConfig(
    val serverStatus: Boolean,
    val serverMessage: String,
    val version: Int,
    val typoVersion: String,
    val necessaryUpdateVersion: Int,
    val updateMessage: String,
    val updateLink: String
)