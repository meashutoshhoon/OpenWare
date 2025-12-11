package jb.openware.app.ui.items

import kotlinx.serialization.Serializable

@Serializable
data class LikeItem(
    val uid: String = "",
    val key: String = "",
    val value: Boolean = false
)

@Serializable
data class UserItem(
    val uid: String = "",
    val name: String = "",
    val avatar: String = "",
    val color: String = "",
    val badge: String = "",
    val verified: String = ""
)
