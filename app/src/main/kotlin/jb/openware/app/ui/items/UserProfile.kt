package jb.openware.app.ui.items

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val avatar: String = "none",
    val badge: String = "0",
    val bio: String = "Programmer ^_~",
    val block: String = "false",
    val color: String = "",
    val deviceId: String = "",
    val downloads: String = "0",
    val email: String = "",
    val likes: String = "0",
    val name: String = "",
    val notify: String = "true",
    val password: String = "",
    val projects: String = "0",
    val reason: String = "",
    val registrationDate: String = "",
    val token: String? = "null",
    val uid: String = "",
    val verified: String = "false"
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "avatar" to avatar,
        "badge" to badge,
        "bio" to bio,
        "block" to block,
        "color" to color,
        "deviceId" to deviceId,
        "downloads" to downloads,
        "email" to email,
        "likes" to likes,
        "name" to name,
        "notify" to notify,
        "password" to password,
        "projects" to projects,
        "reason" to reason,
        "registrationDate" to registrationDate,
        "token" to token,
        "uid" to uid,
        "verified" to verified
    )
}
