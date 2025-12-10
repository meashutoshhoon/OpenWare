package jb.openware.app.ui.items

import kotlinx.serialization.Serializable

@Serializable
data class NotificationItem(
    val url: String = "none",
    val title: String = "",
    val message: String = "",
    val date: String = ""
)