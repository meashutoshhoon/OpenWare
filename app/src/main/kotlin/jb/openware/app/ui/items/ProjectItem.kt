package jb.openware.app.ui.items

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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


data class ProjectCategory(
    val icon: String,
    val title: String,
    val categoryName: String,
    val size: String
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
    val name: String?
) :  Parcelable {
    companion object {

        fun fromMap(map: Map<String, Any>): Project {
            val gson = Gson()

            return Project(
                icon = map["icon"]?.toString(),
                title = map["title"]?.toString(),
                description = map["description"]?.toString(),
                whatsNew = map["whats_new"]?.toString(),
                downloadUrl = map["download_url"]?.toString(),
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
                updateTime = map["update_time"]?.toString(),
                unlockCode = map["unlock_code"]?.toString(),
                verified = map["verify"]?.toString() == "true",
                editorsChoice = map["editors_choice"]?.toString() == "true",
                commentsVisible = map["comments_visibility"]?.toString() == "true",
                visible = map["visibility"]?.toString() == "true",
                name = map["name"]?.toString()
            )
        }
    }
}
