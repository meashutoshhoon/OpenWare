package jb.openware.app.ui.items

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val name: String,
    val email: String,
    val uid: String,
    val bio: String = "Programmer ^_~",
    val likes: String = "0",
    val downloads: String = "0",
    val projects: String = "0",
    val color: String,
    val block: String = "false",
    val registrationDate: String,
    val verified: String = "false",
    val password: String,
    val id: String,
    val token: String? = "null",
    val notify: String = "true",
    val avatar: String = "none",
    val badge: String = "0",
    val reason: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "email" to email,
        "uid" to uid,
        "bio" to bio,
        "likes" to likes,
        "downloads" to downloads,
        "projects" to projects,
        "color" to color,
        "deviceId" to id,
        "block" to block,
        "registrationDate" to registrationDate,
        "verified" to verified,
        "password" to password,
        "token" to token,
        "notify" to notify,
        "avatar" to avatar,
        "badge" to badge,
        "reason" to reason
    )
}
