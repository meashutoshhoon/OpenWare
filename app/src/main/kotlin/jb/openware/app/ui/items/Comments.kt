package jb.openware.app.ui.items

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val key: String = "",
    val message: String = "",
    val postKey: String = "",
    val time: String = "",
    val uid: String = ""
)