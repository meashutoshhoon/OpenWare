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
    val icon: String?,
    val title: String?,
    val description: String?,
    val whatsNew: String?,
    val downloadUrl: String?,
    val size: String?,
    val uid: String?,
    val likes: String?,
    val comments: String?,
    val downloads: String?,
    val category: String?,
    val trending: Boolean,
    val latest: Boolean,
    val screenshots: List<String>,
    val time: String?,
    val updateTime: String?,
    val unlockCode: String?,
    val verified: Boolean,
    val editorsChoice: Boolean,
    val commentsVisible: Boolean,
    val visible: Boolean,
    val key: String,
    val name: String?
) : Parcelable {
    companion object {

        fun fromMap(map: Map<String, Any>): Project {
            val gson = Gson()

            return Project(
                icon = map["icon"]?.toString(),
                title = map["title"]?.toString(),
                description = map["description"]?.toString(),
                whatsNew = map["whatsNew"]?.toString(),
                downloadUrl = map["downloadUrl"]?.toString(),
                size = map["size"]?.toString(),
                uid = map["uid"]?.toString(),
                likes = map["likes"]?.toString(),
                comments = map["comments"]?.toString(),
                downloads = map["downloads"]?.toString(),
                category = map["category"]?.toString(),
                trending = map["trending"]?.toString() == "true",
                latest = map["latest"]?.toString() == "true",
                screenshots = gson.fromJson(
                    map["screenshots"]?.toString() ?: "[]",
                    object : TypeToken<List<String>>() {}.type
                ),
                time = map["time"]?.toString(),
                updateTime = map["updateTime"]?.toString(),
                unlockCode = map["unlockCode"]?.toString(),
                verified = map["verify"]?.toString() == "true",
                editorsChoice = map["editorsChoice"]?.toString() == "true",
                commentsVisible = map["commentsVisibility"]?.toString() == "true",
                visible = map["visibility"]?.toString() == "true",
                name = map["name"]?.toString(),
                key = map["key"]?.toString() ?: ""
            )
        }
    }
}
