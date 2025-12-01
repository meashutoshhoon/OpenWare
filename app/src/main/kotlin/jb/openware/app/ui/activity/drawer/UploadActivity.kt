package jb.openware.app.ui.activity.drawer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jb.openware.app.databinding.ActivityUploadBinding
import jb.openware.app.util.ThemeUtil
import jb.openware.app.util.Utils
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import jb.openware.app.databinding.ScreenshotUploadCellBinding
import jb.openware.app.ui.items.ScreenshotItem
import androidx.core.net.toUri
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import jb.openware.app.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class UploadActivity: AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding

    // Lists
    private val screen = arrayListOf<String>()
    private val screenshots2 = arrayListOf<HashMap<String, Any>>()
    private var screenshots = arrayListOf<HashMap<String, Any>>()
    private var tempListmap1 = arrayListOf<HashMap<String, Any>>()

    // Firebase references
    private val avatar = FirebaseStorage.getInstance().getReference("screenshots")
    private val project = FirebaseDatabase.getInstance().getReference("projects/normal")
    private val premiumServer = FirebaseDatabase.getInstance().getReference("projects/premium")

    // Strings
    private var iconUrl: String = ""
    private var iconPath: String = ""
    private var fileUrl: String = ""
    private var getId: String = ""

    // Booleans / numbers
    private var newProject: Boolean = false
    private var retro: Boolean = false
    private var n: Double = 1.0
    private var j: Double = 0.0

    // Other
    private var receivedHashMap: HashMap<String, Any>? = null
    private lateinit var imageSharedUploadSuccessListener: OnCompleteListener<Uri>

    // RecyclerView
    private lateinit var recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        ThemeUtil.updateTheme(this)
        super.onCreate(savedInstanceState)

        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener(Utils.getBackPressedClickListener(this))

        recyclerView = RecyclerView(this)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        binding.screenshot.addView(recyclerView)


    }

    private fun showViewWithAnimation(view: View) {
        if (view.isVisible) return

        view.alpha = 0f

        view.animate()
            .alpha(1f)
            .setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.VISIBLE
                }
            })
    }

    private fun hideViewWithAnimation(view: View) {
        if (view.isGone) return

        view.animate()
            .alpha(0f)
            .setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                }
            })
    }

    private fun checkData(): Boolean {
        val title = title_msg.text?.toString().orEmpty()
        val description = description_msg.text?.toString().orEmpty()
        val whatsNew = whats_new_msg.text?.toString().orEmpty()
        val category = category_text.text?.toString().orEmpty()
        val fileMsg = file_msg.text?.toString().orEmpty()
        val premiumCode = premium_string.text?.toString().orEmpty()

        // Common validations for both new + existing project
        if (icon_path.isEmpty()) {
            alertToast("Please select icon")
            return false
        }

        if (title.isBlank()) {
            alertToast("Title must not be empty")
            return false
        }

        if (title.length > 50) {
            t1.error = "Field must not exceed max length"
            return false
        }

        if (description.isBlank()) {
            alertToast("Description must not be empty")
            return false
        }

        if (description.length > 1500) {
            t2.error = "Field must not exceed max length"
            return false
        }

        if (screenshots.size < 3) {
            alertToast("Select at-least 2 screenshots")
            return false
        }

        // NEW PROJECT LOGIC (simplified)
        if (newProject) {

            if (category == "Select a category") {
                alertToast("Select category of the project")
                return false
            }

            if (fileMsg == "Select a file") {
                alertToast("Select project file")
                return false
            }

            if (binding.premium.isChecked) {
                if (premiumCode.isBlank()) {
                    alertToast("Enter the unlock code")
                    return false
                }
            }

            return true
        } else {

            if (whatsNew.isBlank()) {
                alertToast("What's new message must not be empty")
                return false
            }

            if (category == "Select a category") {
                alertToast("Select category of the project")
                return false
            }

            return true
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun add(input: String): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("MMMM d, h:mm a", Locale.ENGLISH)
            val date = LocalDateTime.parse(input, formatter)

            date.plusDays(3).format(formatter)
        } catch (e: Exception) {
            " "
        }
    }

    private fun removeScreenshot(position: Int) {
        if (position !in screenshots.indices) return
        screenshots.removeAt(position)
        recyclerView.adapter?.notifyItemRemoved(position)
        recyclerView.adapter?.notifyItemRangeChanged(position, screenshots.size - position)
    }

    private inner class ScreenShotListAdapter(
        private val items: MutableList<ScreenshotItem>,
    ) : RecyclerView.Adapter<ScreenShotListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ScreenshotUploadCellBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position], position == 0)
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(
            private val binding: ScreenshotUploadCellBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(item: ScreenshotItem, isFirstItem: Boolean) = with(binding) {
                val context = root.context

                // Load image
                val path = item.path
                if (!path.equals("empty", ignoreCase = true) && path.isNotEmpty()) {
                    if (path.startsWith("https", ignoreCase = true)) {
                        Glide.with(context)
                            .load(path.toUri())
                            .into(img)
                    } else {
                        Glide.with(context)
                            .load(path)
                            .into(img)
                    }
                } else {
                    img.setImageResource(R.drawable.file_image)
                }

                // Text + icons
                text.text = item.name

                if (item.name != "Add photos") {
                    img2.setImageResource(R.drawable.close)
                } else {
                    img2.setImageResource(R.drawable.add_circle)
                    if (path.equals("empty", ignoreCase = true) || path.isEmpty()) {
                        img.setImageResource(R.drawable.file_image)
                    }
                }

                // Click listener
                rex.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                    if (pos == 0) {
                        if (screenshots.size >= 6) {
                            Toast.makeText(this@UploadActivity,"You can add only 5 photos", Toast.LENGTH_SHORT).show()
                        } else {
                            pickMultiplePhoto { list ->
                                val available = 6 - screenshots.size
                                val count = minOf(available, list.size)
                                val oldSize = screenshots.size
                                for (i in 0 until count) {
                                    val itemToAdd = list[i]
                                    screenshots.add(itemToAdd)
                                    if (!newProject) {
                                        screenshots2.add(itemToAdd)
                                    }
                                }
                                this@ScreenShotListAdapter.notifyItemRangeInserted(oldSize, count)
                            }
                        }
                    } else {
                        removeScreenshot(pos)
                    }
                }
            }
        }
    }

}