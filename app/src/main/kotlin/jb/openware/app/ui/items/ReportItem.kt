package jb.openware.app.ui.items

data class ReportItem(
    val commentId: String, val commentText: String, val authorId: String
) {
    fun toMap(uid: String, reason: String): Map<String, Any> = mapOf(
        "reported_by" to uid,
        "reason" to reason,
        "comment_id" to commentId,
        "comment_text" to commentText,
        "author_id" to authorId,
        "timestamp" to System.currentTimeMillis()
    )
}
