package jb.openware.app.ui.items

import kotlinx.serialization.Serializable

@Serializable
data class Like(
    val key: String = "",
    val uid: String = "",
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
