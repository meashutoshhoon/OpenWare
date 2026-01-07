package jb.openware.app.ui.items

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ProjectItem(
    val icon: String,
    val title: String,
    val comments: String,
    val likes: String,
    val downloads: String,
    val userName: String
)

data class ProjectPoints(
    val points: Int = 0
)

@Parcelize
data class Project(
    val category: String = "",
    val comments: String = "",
    val commentsVisibility: Boolean = false,
    val description: String = "",
    val downloadUrl: String = "",
    val downloads: String = "",
    val editorsChoice: Boolean = false,
    val icon: String = "",
    val id: String = "",
    val key: String = "",
    val latest: Boolean = false,
    val likes: String = "",
    val name: String = "",
    val screenshots: String = "",
    val size: String = "",
    val sourceUrl: String = "",
    val time: String = "",
    val title: String = "",
    val trending: Boolean = false,
    val uid: String = "",
    val unlockCode: String = "",
    val updateTime: String = "",
    val verified: Boolean = false,
    val visibility: Boolean = false,
    val whatsNew: String = ""
) : Parcelable
