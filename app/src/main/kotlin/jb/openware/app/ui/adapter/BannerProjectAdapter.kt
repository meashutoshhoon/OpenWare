package jb.openware.app.ui.adapter

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jb.openware.app.ui.cells.BannerCell
import jb.openware.app.ui.items.Project

class BannerProjectAdapter(
    private val data: List<Project>, private val activity: Activity, private val typeId: Int
) : RecyclerView.Adapter<BannerProjectAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(BannerCell(activity))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        val icon = item.icon.toString()
        val title = item.title.toString()
        val category = item.category.toString()
        val size = item.size.toString()

        val screenshotsJson = item.screenshots.toString()
        val screenshots: List<String> = runCatching {
            Gson().fromJson<List<String>>(
                screenshotsJson, object : TypeToken<List<String>>() {}.type
            )
        }.getOrElse { emptyList() }

        val firstScreenshot = screenshots.firstOrNull().orEmpty()

        (holder.itemView as BannerCell).setData(
            icon, title, category, size, firstScreenshot
        )

        holder.itemView.setOnClickListener {
            val developerPrefs = activity.getSharedPreferences("developer", Activity.MODE_PRIVATE)

            developerPrefs.edit {
                putString("type", if (typeId == 1) "Free" else "Paid")
            }

            val intent = Intent(activity, ProjectViewActivity::class.java).apply {
                putExtra("key", item["key"]?.toString())
                putExtra("uid", item["uid"]?.toString())
            }

            activity.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
