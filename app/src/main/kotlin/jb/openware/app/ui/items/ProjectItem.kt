package jb.openware.app.ui.items

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.Parcelize

data class ProjectItem(
    val icon: String,
    val title: String,
    val comments: String,
    val likes: String,
    val downloads: String,
    val userName: String
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
    val time: String = "",
    val title: String = "",
    val trending: Boolean = false,
    val uid: String = "",
    val unlockCode: String = "",
    val updateTime: String = "",
    val verified: Boolean = false,
    val visibility: Boolean = false,
    val whatsNew: String = ""
) : Parcelable {
    companion object {

        fun fromMap(map: Map<String, Any>): Project {
            val gson = Gson()

            return Project(
                category = map["category"].toString(),
                comments = map["comments"].toString(),
                commentsVisibility = map["commentsVisibility"]?.toString().toBoolean(),
                description = map["description"].toString(),
                downloadUrl = map["downloadUrl"].toString(),
                downloads = map["downloads"].toString(),
                editorsChoice = map["editorsChoice"]?.toString().toBoolean(),
                icon = map["icon"].toString(),
                id = map["id"].toString(),
                key = map["key"]?.toString() ?: "",
                latest = map["latest"]?.toString().toBoolean(),
                likes = map["likes"].toString(),
                name = map["name"].toString(),
                screenshots = gson.fromJson(
                    map["screenshots"]?.toString() ?: "[]",
                    object : TypeToken<List<String>>() {}.type
                ),
                size = map["size"].toString(),
                time = map["time"].toString(),
                title = map["title"].toString(),
                trending = map["trending"]?.toString().toBoolean(),
                uid = map["uid"].toString(),
                unlockCode = map["unlockCode"].toString(),
                updateTime = map["updateTime"].toString(),
                verified = map["verify"]?.toString().toBoolean(),
                visibility = map["visibility"]?.toString().toBoolean(),
                whatsNew = map["whatsNew"].toString()
            )
        }
    }
}
