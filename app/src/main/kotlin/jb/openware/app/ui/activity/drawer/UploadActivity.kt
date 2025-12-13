package jb.openware.app.ui.activity.drawer

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jb.openware.app.R
import jb.openware.app.databinding.ActivityUploadBinding
import jb.openware.app.databinding.ScreenshotUploadCellBinding
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.ui.items.Project
import jb.openware.app.ui.items.ScreenshotItem
import jb.openware.app.util.FirebaseUtils
import jb.openware.app.util.ImageUtil
import jb.openware.app.util.Utils
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class UploadActivity : BaseActivity<ActivityUploadBinding>(ActivityUploadBinding::inflate) {

    // Lists
    private val screen = arrayListOf<String>()
    private val screenshots2: MutableList<ScreenshotItem> = mutableListOf()
    private var screenshots: MutableList<ScreenshotItem> = mutableListOf()

    // Firebase references
    private val avatar = FirebaseStorage.getInstance().getReference("screenshots")
    private val project = FirebaseDatabase.getInstance().getReference("projects/normal")
    private val premiumServer = FirebaseDatabase.getInstance().getReference("projects/premium")

    // Strings
    private var iconUrl: String = ""
    private var iconPath: String = ""
    private var getId: String = ""

    // Booleans / numbers
    private var newProject: Boolean = false
    private var retro: Boolean = false

    private val categories = arrayOf(
        "Books & Reference",
        "Business & Trading",
        "Communication",
        "Education",
        "Entertainment",
        "Example & Tutorial",
        "Games",
        "Multi-Device",
        "Music & Audio",
        "Other",
        "Photographic",
        "Productivity",
        "Social",
        "Tools",
        "UI & UX",
        "Videography"
    )


    // Other
    private var receivedHashMap: HashMap<String, Any>? = null

    // RecyclerView
    private lateinit var recyclerView: RecyclerView


    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun init() {
        binding.toolbar.setNavigationOnClickListener(Utils.getBackPressedClickListener(this))

        recyclerView = RecyclerView(this)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        binding.screenshot.addView(recyclerView)


        binding.category.setOnClickListener {
            val selectedCategory = binding.categoryText.text.toString()
            var checkedIndex = categories.indexOf(selectedCategory).takeIf { it >= 0 } ?: 0

            MaterialAlertDialogBuilder(
                this,
                com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
            ).setTitle("Select a category").setIcon(R.drawable.category)
                .setSingleChoiceItems(categories, checkedIndex) { _, which ->
                    checkedIndex = which
                }.setPositiveButton("Apply") { _, _ ->
                    binding.categoryText.text = categories[checkedIndex]
                }.setNegativeButton("Cancel", null).show()
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_upload -> {
                    checkUpload()
                    true
                }

                else -> false
            }
        }

        @Suppress("UNCHECKED_CAST")
        receivedHashMap =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("hashmap", HashMap::class.java) as? HashMap<String, Any>
            } else {
                intent.getSerializableExtra("hashmap") as? HashMap<String, Any>
            } ?: error("Missing hashmap in intent")


        if (receivedHashMap == null) {
            // -------- NEW PROJECT --------
            hideViews(binding.warning, binding.whatsNewMsg)
            newProject = true

            hideViews(binding.t4)
            binding.comments.isChecked = true
            binding.visibility.isChecked = true

        } else {
            // -------- EDIT PROJECT --------
            newProject = false

            val lastUpdateTime = receivedHashMap?.get("time")?.toString()
            val dateString = add(lastUpdateTime ?: "")

            binding.data.text = """
        You have recently updated your project.
        Your project will not update to the top of the projects list until $dateString.
        Updating your project will reset the timer back to 3 days.
    """.trimIndent()

            showViews(binding.whatsNewMsg)
            hideViews(binding.warning)

            // Icon
            iconPath = receivedHashMap?.get("icon")?.toString().orEmpty()
            Glide.with(this).load(iconPath).centerCrop().into(binding.circleImg)

            showViews(binding.circleImg)
            hideViews(binding.circle)

            // Text fields
            binding.titleMsg.setText(receivedHashMap?.get("title").toString())
            binding.descriptionMsg.setText(receivedHashMap?.get("description").toString())

            receivedHashMap?.get("whats_new")?.toString()?.takeIf { it != "none" }
                ?.let { binding.whatsNewMsg.setText(it) }

            binding.categoryText.text = receivedHashMap?.get("category").toString()

            // Switches
            binding.visibility.isChecked = receivedHashMap?.get("visibility").toString() == "true"
            binding.comments.isChecked =
                receivedHashMap?.get("comments_visibility").toString() == "true"

            // Premium
            val unlockCode = receivedHashMap?.get("unlock_code")?.toString()
            if (!unlockCode.isNullOrEmpty() && unlockCode != "none") {
                binding.premium.isChecked = true
                binding.premiumString.setText(unlockCode)
                showViews(binding.t4)
            } else {
                binding.premium.isChecked = false
                hideViews(binding.t4)
            }

            // -------- Screenshots --------
            screenshots.clear()

            // Placeholder
            screenshots.add(
                ScreenshotItem(
                    path = "empty", name = "Add photos"
                )
            )

            val screenshotsJson = receivedHashMap?.get("screenshots").toString()
            val screenshotsTemp: List<String> = Gson().fromJson(
                screenshotsJson, object : TypeToken<List<String>>() {}.type
            )

            screenshotsTemp.forEachIndexed { index, url ->
                screenshots.add(
                    ScreenshotItem(
                        name = "Screenshot ${index + 1}", path = url
                    )
                )
            }
        }

    }

    override fun initLogic() {
        retrieveProjectsListFromFirebase { value -> getId = value }

        binding.premium.setOnCheckedChangeListener { _, isChecked ->
            binding.t4.apply {
                if (isChecked) {
                    alpha = 0f
                    visibility = View.VISIBLE
                    animate().alpha(1f).setDuration(150).start()
                } else {
                    animate().alpha(0f).setDuration(150).withEndAction {
                        visibility = View.GONE
                    }.start()
                }
            }
        }

        if (newProject) {

            hideViews(binding.circleImg)
            showViews(binding.circle)

            screenshots.add(
                ScreenshotItem(
                    path = "empty", name = "Add photos"
                )
            )
        }


        recyclerView.adapter = ScreenShotListAdapter(screenshots)


        binding.pickImage.setOnClickListener {
            pickSinglePhoto(object : ImagePickedListener {
                override fun onImagePicked(
                    profilePath: String, imageFileName: String, imageUri: Uri
                ) {
                    val avatar = ImageUtil.compressImage(this@UploadActivity, imageUri, 40)

                    Glide.with(this@UploadActivity).load(Uri.fromFile(avatar)).centerCrop()
                        .into(binding.circleImg)

                    iconPath = Uri.fromFile(avatar).toString()

                    showViews(binding.circleImg)
                    hideViews(binding.circle)
                }

            })
        }


    }

    private fun checkUpload() {
        hideKeyboard()
        if (checkData()) {
            val dialog = MaterialAlertDialogBuilder(
                this,
                com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
            ).setNegativeButton("Cancel", null)

            if (!newProject) {

                // ----- UPDATE PROJECT -----
                if (binding.downloadUrl.text.toString() == "" && binding.downloadUrl.text.toString() == receivedHashMap?.get(
                        "downloadUrl"
                    )
                ) {
                    // Retro update (file unchanged)
                    retro = true

                    dialog.setIcon(R.drawable.info).setTitle("Warning").setMessage(
                            "Your project file hasn't been modified. " + "It will still be uploaded, but it won't be bumped to the top of the list.\n\n" + "Do you want to proceed?"
                        ).setPositiveButton("Proceed") { _, _ ->
                            uploadUpdate()
                        }

                } else {
                    // Normal update
                    retro = false

                    dialog.setIcon(R.drawable.upload).setTitle("Update Project").setMessage(
                            "By uploading your project, you agree to our terms and policies. " + "Please make sure you have read and understood them before proceeding."
                        ).setPositiveButton("Upload") { _, _ ->
                            uploadUpdate()
                        }
                }

            } else {

                // ----- NEW PROJECT -----
                dialog.setIcon(R.drawable.upload).setTitle("Upload Project").setMessage(
                        "By uploading your project, you agree to our terms and policies. " + "Please make sure you have read and understood them before proceeding."
                    ).setPositiveButton("Upload") { _, _ ->
                        showProgressDialog()
                        uploadUpdate()
                    }
            }

            dialog.show()
        }
    }

    private fun checkData(): Boolean {
        val title = binding.titleMsg.text.toString().trim()
        val description = binding.descriptionMsg.text.toString().trim()
        val whatsNew = binding.whatsNewMsg.text.toString().trim()
        val category = binding.categoryText.text.toString().trim()
        val premiumCode = binding.premiumString.text.toString().trim()

        val githubUrl = binding.githubUrl.text.toString().trim()
        val downloadUrl = binding.downloadUrl.text.toString().trim()

        // ---------- COMMON VALIDATIONS (NEW + EDIT) ----------

        if (iconPath.isEmpty()) {
            alertToast("Please select icon")
            return false
        }

        if (title.isBlank()) {
            alertToast("Title must not be empty")
            return false
        }

        if (title.length > 50) {
            binding.t1.error = "Max 50 characters allowed"
            return false
        } else binding.t1.error = null

        if (description.isBlank()) {
            alertToast("Description must not be empty")
            return false
        }

        if (description.length > 1500) {
            binding.t2.error = "Max 1500 characters allowed"
            return false
        } else binding.t2.error = null

        if (screenshots.size < 3) {
            alertToast("Select at least 3 screenshots")
            return false
        }

        // ---------- URL VALIDATIONS (COMMON) ----------

        if (githubUrl.isBlank()) {
            binding.t5.error = "Source code URL is required"
            return false
        } else if (!githubUrl.isValidUrl()) {
            binding.t5.error = "Invalid URL"
            return false
        } else binding.t5.error = null

        if (downloadUrl.isBlank()) {
            binding.t6.error = "Download URL is required"
            return false
        } else if (!downloadUrl.isValidUrl()) {
            binding.t6.error = "Invalid URL"
            return false
        } else binding.t6.error = null

        // ---------- NEW PROJECT VALIDATIONS ----------

        if (newProject) {

            if (category == "Select a category") {
                alertToast("Select category of the project")
                return false
            }

            if (binding.premium.isChecked && premiumCode.isBlank()) {
                alertToast("Enter the unlock code")
                return false
            }

            return true
        }

        // ---------- UPDATE PROJECT VALIDATIONS ----------

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

    private fun upload() {
        syncTask(object : SyncTaskListener {

            override fun beforeTaskStart() = Unit

            override fun onBackground() {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("MMMM d, h:mm a", Locale.getDefault())

                val key = project.push().key ?: return
                val isPremium = binding.premium.isChecked

                // ---- Preserve counters correctly ----
                val likesValue = if (newProject) {
                    "0"
                } else {
                    receivedHashMap?.get("likes")?.toString() ?: "0"
                }

                val commentsValue = if (newProject) {
                    "0"
                } else {
                    receivedHashMap?.get("comments")?.toString() ?: "0"
                }

                val downloadsValue = if (newProject) {
                    "0"
                } else {
                    receivedHashMap?.get("downloads")?.toString() ?: "0"
                }

                val baseProject = Project(
                    icon = iconUrl,
                    title = binding.titleMsg.text.toString().trim(),
                    description = binding.descriptionMsg.text.toString().trim(),
                    whatsNew = if (newProject) "none"
                    else binding.whatsNewMsg.text.toString().trim(),

                    downloadUrl = binding.downloadUrl.toString(),
                    size = Utils.formatFileSize(Utils.getGithubApkSize(binding.downloadUrl.toString())),
                    uid = getUid(),

                    likes = likesValue,
                    comments = commentsValue,
                    downloads = downloadsValue,

                    category = binding.categoryText.text.toString(),
                    trending = false,
                    latest = true,
                    screenshots = screen,

                    time = if (newProject) dateFormat.format(calendar.time)
                    else receivedHashMap?.get("time")?.toString(),

                    updateTime = if (newProject) "none"
                    else dateFormat.format(calendar.time),

                    unlockCode = if (isPremium) binding.premiumString.text.toString()
                    else "none",

                    verified = false,
                    editorsChoice = false,
                    commentsVisible = binding.comments.isChecked,
                    visible = binding.visibility.isChecked,
                    key = key,
                    name = if (newProject) userConfig.name
                    else receivedHashMap?.get("name")?.toString()
                )

                val id = if (newProject) {
                    (getId.toInt() + 1).toString()
                } else {
                    receivedHashMap?.get("id").toString()
                }

                val dataMap = baseProject.toFirebaseMap(id)

                val targetRef = if (isPremium) premiumServer else project

                // ---- Update logic ----
                if (!newProject && !retro) {
                    targetRef.child(key).removeValue().addOnSuccessListener {
                            targetRef.child(key).updateChildren(dataMap)
                        }
                } else {
                    targetRef.child(key).updateChildren(dataMap)
                }

                updateProjectsListToFirebase { }
            }

            override fun onTaskComplete() {
                dismissProgressDialog()
                FirebaseUtils().increaseUserKeyData("projects", getUid())

                MaterialAlertDialogBuilder(this@UploadActivity).setTitle("Success")
                    .setMessage("Project uploaded successfully.")
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        finish()
                    }.show()
            }
        })
    }

    private fun uploadUpdate() {

        // 1️⃣ Collect already-uploaded screenshot URLs
        screenshots.drop(1) // skip placeholder
            .filter { it.path.startsWith("https") }.forEach { screen.add(it.path) }

        showProgressDialog()

        // 2️⃣ ICON handling
        if (iconPath.startsWith("https")) {
            iconUrl = iconPath
            handleScreenshotsAndFile()
        } else {
            val iconName = generateFileNameWithTimestamp()
            uploadFileToFirebaseStorage(
                storagePath = "icon",
                child = iconName,
                fileUri = iconPath.toUri(),
                onSuccess = { uri ->
                    iconUrl = uri.toString()
                    handleScreenshotsAndFile()
                },
                onFailure = { e -> alertCreator(e.message) })
        }
    }

    private fun handleScreenshotsAndFile() {

        if (screenshots2.isNotEmpty()) {
            uploadScreenshotAtIndex(0)
            return
        }

        upload()
    }

    private fun uploadScreenshotAtIndex(index: Int) {

        if (index >= screenshots2.size) {
            handleScreenshotsAndFile()
            return
        }

        val item = screenshots2[index]

        // Already uploaded
        if (item.path.startsWith("https")) {
            screen.add(item.path)
            uploadScreenshotAtIndex(index + 1)
            return
        }

        val fileName = item.path.toUri().lastPathSegment ?: return
        val fileRef = avatar.child(fileName)

        val compressed = ImageUtil.compressImage(
            this, Uri.fromFile(File(item.path)), 40
        )

        fileRef.putFile(Uri.fromFile(compressed)).continueWithTask { fileRef.downloadUrl }
            .addOnSuccessListener { uri ->
                screen.add(uri.toString())
                uploadScreenshotAtIndex(index + 1)
            }.addOnFailureListener { e ->
                MaterialAlertDialogBuilder(this).setTitle("Error").setMessage(e.message)
                    .setPositiveButton("Retry", null).show()
            }
    }


    fun Project.toFirebaseMap(
        id: String
    ): Map<String, Any> = mapOf(
        "icon" to (icon ?: "none"),
        "title" to (title ?: ""),
        "description" to (description ?: ""),
        "whatsNew" to (whatsNew ?: "none"),
        "downloadUrl" to (downloadUrl ?: "none"),
        "size" to (size ?: "0"),
        "uid" to (uid ?: ""),
        "key" to (key ?: ""),

        // counters (preserved correctly)
        "likes" to (likes ?: "0"),
        "comments" to (comments ?: "0"),
        "downloads" to (downloads ?: "0"),

        "category" to (category ?: ""),
        "trending" to trending.toString(),
        "latest" to latest.toString(),

        "screenshots" to Gson().toJson(screenshots),

        "time" to (time ?: "none"),
        "updateTime" to (updateTime ?: "none"),

        "unlockCode" to (unlockCode ?: "none"),
        "verify" to verified.toString(),
        "editorsChoice" to editorsChoice.toString(),
        "commentsVisibility" to commentsVisible.toString(),
        "visibility" to visible.toString(),

        "name" to (name ?: ""),
        "id" to id
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun add(input: String): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("MMMM d, h:mm a", Locale.ENGLISH)
            val date = LocalDateTime.parse(input, formatter)

            date.plusDays(3).format(formatter)
        } catch (_: Exception) {
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
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(
            private val binding: ScreenshotUploadCellBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(item: ScreenshotItem) = with(binding) {
                val context = root.context

                // Load image
                val path = item.path
                if (!path.equals("empty", ignoreCase = true) && path.isNotEmpty()) {
                    if (path.startsWith("https", ignoreCase = true)) {
                        Glide.with(context).load(path.toUri()).into(img)
                    } else {
                        Glide.with(context).load(path).into(img)
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
                            Toast.makeText(
                                this@UploadActivity,
                                "You can add only 5 photos",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            pickMultiplePhotos(object : MultipleImagePickedListener {
                                override fun onImagePicked(list: MutableList<ScreenshotItem>) {
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
                                    this@ScreenShotListAdapter.notifyItemRangeInserted(
                                        oldSize,
                                        count
                                    )
                                }

                            })
                        }
                    } else {
                        removeScreenshot(pos)
                    }
                }
            }
        }
    }

    private fun String.isValidUrl(): Boolean = Patterns.WEB_URL.matcher(this).matches()


}