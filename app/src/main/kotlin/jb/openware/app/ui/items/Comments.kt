package jb.openware.app.ui.items

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val key: String,
    val uid: String,
    val message: String,
    val time: String,
    val postKey: String
)