package jb.openware.app.ui.activity.project

import android.Manifest
import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.util.Linkify
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jb.openware.app.R
import jb.openware.app.databinding.ActivityProjectViewBinding
import jb.openware.app.ui.activity.drawer.UploadActivity
import jb.openware.app.ui.activity.other.CategoryActivity
import jb.openware.app.ui.activity.other.LikesCountActivity
import jb.openware.app.ui.activity.profile.ProfileActivity
import jb.openware.app.ui.cells.ProjectScreenshotCell
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.ui.components.BottomSheetController
import jb.openware.app.ui.components.ProgressButton
import jb.openware.app.ui.components.TextFormatter
import jb.openware.app.ui.items.Comment
import jb.openware.app.ui.items.Like
import jb.openware.app.ui.items.Project
import jb.openware.app.ui.items.UserProfile
import jb.openware.app.util.FirebaseUtils
import jb.openware.app.util.HapticUtils
import jb.openware.app.util.RequestNetworkController.Companion.GET
import jb.openware.app.util.Utils
import jb.openware.app.util.Utils.shareText
import jb.openware.app.util.moderatorUrl
import jb.openware.app.util.net.DownloadCallback
import jb.openware.app.util.net.downloadFiles
import jb.openware.app.util.websiteUrl
import jb.openware.imageviewer.ImageViewer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar

class ProjectViewActivity :
    BaseActivity<ActivityProjectViewBinding>(ActivityProjectViewBinding::inflate) {

    private val firebase by lazy { FirebaseDatabase.getInstance() }

    private val projectRef by lazy { firebase.getReference("projects/normal") }
    private val premiumRef by lazy { firebase.getReference("projects/premium") }
    private val likeRef by lazy { firebase.getReference("likes") }
    private val commentRef by lazy { firebase.getReference("comments") }
    private val usersRef by lazy { firebase.getReference("Users") }
    private val reportRef by lazy { firebase.getReference("Report/project") }
    private lateinit var projectReference: DatabaseReference

    // IDs & keys
    private var projectKey: String = ""
    private var userUid: String = ""
    private var likeKey: String = ""

    // Project meta
    private var downloadUrl: String = ""

    // UI state
    private var liked = false
    private var commentCount = 0
    private var likeCount = 0
    private var isFree = false
    private lateinit var filePath: String

    // Collections
    private var screenshots = listOf<String>()
    private var moderatorIds = mutableListOf<String>()

    private lateinit var progressButton: ProgressButton
    private lateinit var screenshotsRecycler: RecyclerView

    // Models & callbacks
    private lateinit var projectData: Project

    private lateinit var callback: DownloadCallback

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            runDownload2()
        }

    private val projectChildListener = object : ChildEventListener {

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            handleProjectSnapshot(snapshot)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            handleProjectSnapshot(snapshot)
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
        override fun onChildRemoved(snapshot: DataSnapshot) = Unit
        override fun onCancelled(error: DatabaseError) = Unit
    }

    private val usersChildListener = object : ChildEventListener {

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            handleUserSnapshot(snapshot)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            handleUserSnapshot(snapshot)
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
        override fun onChildRemoved(snapshot: DataSnapshot) = Unit
        override fun onCancelled(error: DatabaseError) = Unit
    }

    private val commentChildListener = object : ChildEventListener {

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            handleCommentSnapshot(snapshot, increment = true)
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            handleCommentSnapshot(snapshot, increment = false)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) = Unit
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
        override fun onCancelled(error: DatabaseError) = Unit
    }

    private val likeChildListener = object : ChildEventListener {

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            handleLikeSnapshot(snapshot, isChange = false)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            handleLikeSnapshot(snapshot, isChange = true)
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
        override fun onChildRemoved(snapshot: DataSnapshot) = Unit
        override fun onCancelled(error: DatabaseError) = Unit
    }

    override fun init() {

        isFree = getPrefString("developer", "type", "Free") == "Free"

        if (isNightMode()) {
            binding.linear28.background = outlined(30f, 0xFF41474D.toInt())
            binding.category.background = outlined(50f, 0xFF9E9E9E.toInt())
        } else {
            binding.category.background = outlined(50f, 0xFF9E9E9E.toInt())
            binding.linear28.background = outlined(30f, Color.BLACK)
        }


        binding.likesLin.background =
            createRoundedDrawable(50f, 0f, Color.TRANSPARENT, 0xFFFF5722.toInt())

        binding.editorChoice.background =
            createRoundedDrawable(50f, 0f, Color.TRANSPARENT, 0xFF2196F3.toInt())

        binding.privateProject.background =
            createRoundedDrawable(50f, 0f, Color.TRANSPARENT, 0xFFAA47BC.toInt())

        binding.premiumProject.background =
            createRoundedDrawable(50f, 0f, Color.TRANSPARENT, 0xFF009688.toInt())

        binding.verified.background =
            createRoundedDrawable(50f, 0f, Color.TRANSPARENT, 0xFF4CAF50.toInt())


        progressButton = ProgressButton(this)

        val colorOnPrimary = getThemeColor(
            com.google.android.material.R.attr.colorOnPrimary
        )

        progressButton.apply {
            setText(if (isFree) "Download" else "Get")
            setProgressColor(colorOnPrimary)
            setTextColor(colorOnPrimary)
        }

        binding.downloadBtn.addView(progressButton)

        hideViews(binding.fab)

        // Disable scrollbars safely
        binding.scroll2.removeScrollbars()

        // Screenshots RecyclerView
        screenshotsRecycler = RecyclerView(this).apply {
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(
                this@ProjectViewActivity, RecyclerView.HORIZONTAL, false
            )
            adapter = ScreenShotsAdapter(screenshots)
        }

        binding.list.addView(screenshotsRecycler)

        getConnectionsManager(
            url = moderatorUrl, method = GET, listener = object : RequestListener {

                override fun onResponse(
                    tag: String, response: String, responseHeaders: HashMap<String, Any>
                ) {
                    val type = object : TypeToken<List<Map<String, Any>>>() {}.type

                    val data: List<Map<String, Any>> = Gson().fromJson(response, type)

                    if (data.isNotEmpty()) {
                        moderatorIds = data.mapNotNull { it["uid"] as? String }.toMutableList()
                    }
                }

                override fun onErrorResponse(tag: String, message: String) {
                    // log or ignore intentionally
                }
            })

        projectReference = if (isFree) projectRef else premiumRef

        projectReference.addChildEventListener(projectChildListener)

        usersRef.addChildEventListener(usersChildListener)

        likeRef.addChildEventListener(likeChildListener)

        commentRef.addChildEventListener(commentChildListener)

    }

    override fun initLogic() {
        binding.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.options.setOnClickListener { showMenu(it) }

        setupScrollDivider(binding.nestedScrollView, binding.divider)
        projectKey = intent.getStringExtra("key").orEmpty()
        userUid = intent.getStringExtra("uid").orEmpty()

        likeCount = 0
        commentCount = 0
        liked = false
        likeKey = "$projectKey${getUid()}"

        binding.fab.isVisible = getUid() == userUid

        hideViews(binding.linear5, binding.photos)

        binding.linear1.postDelayed({
            TransitionManager.beginDelayedTransition(
                binding.linear1, AutoTransition().apply { duration = 400 })
            showViews(binding.linear5, binding.photos)
        }, 200)

        binding.commentsLin.setOnClickListener {
            startActivity(
                Intent(this, CommentsActivity::class.java).apply {
                    putExtra("title", projectData.title)
                    putExtra("key", projectKey)
                    putExtra("uid", userUid)
                })
        }

        binding.fab.setOnClickListener {
            startActivity(
                Intent(this, UploadActivity::class.java).apply {
                    putExtra("data", projectData) // Project must be Parcelable
                })
        }

        binding.readMore.setOnClickListener {
            startActivity(
                Intent(this, AboutActivity::class.java).apply {
                    putExtra("data", projectData) // Project must be Parcelable
                })
        }

        binding.content.detectLinksMaterial()

        binding.category.setOnClickListener { openTag("category") }
        binding.editorChoice.setOnClickListener { openTag("editorsChoice") }
        binding.verified.setOnClickListener { openTag("verify") }
        binding.premiumProject.setOnClickListener { openTag("premium") }

        binding.username.setOnClickListener {
            when {
                !Utils.isConnected(this) -> alertToast("No Internet")

                userUid.isBlank() -> alertToast("Something Went Wrong")

                else -> {
                    putPrefString(name = "developer", key = "uid", value = userUid)
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
            }
        }

        binding.likesLin.setOnClickListener {
            startActivity(
                Intent(this, LikesCountActivity::class.java).apply {
                    putExtra("key", projectKey)
                })
        }

        binding.likeBtn.setOnClickListener { view ->
            view.clickScaleAnimation()
            HapticUtils.weakVibrate(view)

            if (!Utils.isConnected(this)) {
                alertToast("No Internet Connection")
                return@setOnClickListener
            }

            val newValue = !liked

            val likeMap = hashMapOf<String, Any>(
                "value" to newValue, "key" to projectKey, "uid" to getUid()
            )

            likeRef.child(likeKey).updateChildren(likeMap).addOnSuccessListener {
                val utils = FirebaseUtils()
                if (newValue) {
                    utils.increaseUserKeyData("likes", userUid)
                } else {
                    utils.decreaseUserKeyData("likes", userUid)
                }
                liked = newValue
            }
        }

        binding.icon.setOnClickListener {
            val images = listOfNotNull(projectData.icon)

            ImageViewer.Builder(
                this, images
            ) { imageView, image ->
                Glide.with(this).load(image).placeholder(0xFFD3D3D3.toInt().toDrawable())
                    .transition(DrawableTransitionOptions.withCrossFade()).into(imageView)
            }.withStartPosition(0).withHiddenStatusBar(true).allowZooming(true)
                .allowSwipeToDismiss(true).withTransitionFrom(binding.icon)
                .withDismissListener { binding.icon.visibility = View.VISIBLE }.show()
        }

        binding.downloadBtn.setOnClickListener {
            if (!packageManager.canRequestPackageInstalls()) {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData("package:$packageName".toUri())
                )
                return@setOnClickListener
            }

            val isPaidUser = !isFree
            val unlockCode = projectData.unlockCode

            if (isPaidUser) {
                if (unlockCode.isBlank()) {
                    runDownload()
                    return@setOnClickListener
                }

                val bottomSheetController = BottomSheetController(this)

                // Show unlock code bottom sheet
                bottomSheetController.create(R.layout.premium_bottom_cell)
                val input = bottomSheetController.find<EditText>(R.id.edittext1)
                val button = bottomSheetController.find<MaterialButton>(R.id.b1)

                input.requestFocus()

                button.setOnClickListener {
                    if (!Utils.isConnected(this)) {
                        alertToast("No Internet")
                        return@setOnClickListener
                    }

                    val enteredCode = input.text.toString()

                    if (enteredCode == unlockCode) {
                        runDownload()
                        bottomSheetController.dismiss()
                    } else {
                        bottomSheetController.dismiss()
                        alertToast("Wrong code")
                    }
                }

                bottomSheetController.show()

            } else {
                runDownload()
            }
        }

        callback = object : DownloadCallback {

            override fun onDownloadStart() {
            }

            override fun onProgressUpdate(progress: Int) {
                runOnUiThread {
                    progressButton.setProgress(progress)
                }
            }

            override fun onDownloadComplete() {
                runOnUiThread {
                    FirebaseUtils().increaseUserKeyData("downloads", getUid())
                    progressButton.showProgress(false)
                    progressButton.setText("Download complete")
                    installApk()
                }
            }

            override fun onDownloadFailed(e: Exception?) {
                runOnUiThread {
                    progressButton.showProgress(false)
                    alertCreator(e?.message)
                }
            }

        }

    }

    fun outlined(
        radius: Float, strokeColor: Int, fillColor: Int = Color.TRANSPARENT
    ) = createRoundedDrawable(radius, 2f, strokeColor, fillColor)


    private fun handleUserSnapshot(snapshot: DataSnapshot) {
        val childKey = snapshot.key ?: return
        if (childKey != userUid) return

        val user = snapshot.getValue(UserProfile::class.java) ?: return

        binding.username.text = user.name
    }

    @SuppressLint("SetTextI18n")
    private fun handleProjectSnapshot(snapshot: DataSnapshot) {
        val childKey = snapshot.key ?: return
        if (childKey != projectKey) return

        val project = snapshot.getValue(Project::class.java) ?: return

        projectData = project

        // Premium badge visibility
        if (isFree) {
            hideViews(binding.premiumProject)
        } else {
            showViews(binding.premiumProject)
        }

        if (!isDestroyed) {
            Glide.with(this).load(projectData.icon.toUri()).into(binding.icon)
        }

        binding.title.text = projectData.title
        binding.username.text = project.name

        delayTask {
            binding.downloadBtn.animateLayoutChange()

            val action = if (isFree) "Download" else "Get"
            val label = "$action ( ${projectData.size} )"

            progressButton.setText(label)
        }

        binding.likes.text = formatNumber(projectData.likes)
        binding.comments.text = formatNumber(projectData.comments)

        downloadUrl = projectData.downloadUrl

        if (projectData.whatsNew == "none") {
            binding.type1.text = "About Project"
            TextFormatter.format(binding.content, projectData.description)
        } else {
            binding.type1.text = "What's new"
            TextFormatter.format(binding.content, projectData.whatsNew)
        }

        binding.content.detectLinksMaterial()

        binding.categoryText.text = projectData.category

        binding.verified.visibility = if (projectData.verified) View.VISIBLE else View.GONE

        binding.editorChoice.visibility = if (projectData.editorsChoice) View.VISIBLE else View.GONE

        binding.commentsLin.visibility =
            if (projectData.commentsVisibility) View.VISIBLE else View.GONE

        binding.privateProject.visibility = if (!projectData.visibility) View.VISIBLE else View.GONE

        screenshots =
            Gson().fromJson(
                projectData.screenshots, object : TypeToken<List<String>>() {}.type)
        screenshotsRecycler.adapter = ScreenShotsAdapter(screenshots)
    }

    private fun handleCommentSnapshot(
        snapshot: DataSnapshot, increment: Boolean
    ) {
        val comment = snapshot.getValue(Comment::class.java) ?: return

        if (comment.postKey != projectKey) return

        val targetRef = if (isFree) projectRef else premiumRef

        commentCount += if (increment) 1 else -1

        targetRef.child(projectKey).child("comments").setValue(commentCount.toString())
    }

    private fun handleLikeSnapshot(
        snapshot: DataSnapshot, isChange: Boolean
    ) {
        val like = snapshot.getValue(Like::class.java) ?: return

        if (like.key != projectKey) return

        val isLiked = like.value
        val isCurrentUser = like.uid == getUid()

        val targetRef = if (isFree) projectRef else premiumRef

        // ---- like count update ----
        likeCount += when {
            !isChange && isLiked -> 1
            isChange && isLiked -> 1
            isChange && !isLiked -> -1
            else -> 0
        }

        if (!isChange || isCurrentUser) {
            targetRef.child(projectKey).child("likes").setValue(likeCount.toString())
        }

        // ---- UI update for current user ----
        if (isCurrentUser) {
            liked = isLiked
            binding.likeBtn.setImageResource(
                if (isLiked) R.drawable.like_fill else R.drawable.like
            )
        }
    }

    @SuppressLint("RestrictedApi")
    fun showMenu(anchor: View) {
        val popupMenu = PopupMenu(this, anchor)

        // Force icons to show (restricted API, but works)
        (popupMenu.menu as? MenuBuilder)?.setOptionalIconsVisible(true)

        // Conditionally add Delete
        if (moderatorIds.contains(getUid()) || getUid() == userUid) {
            popupMenu.menu.add(
                Menu.NONE, Menu.FIRST, Menu.NONE, "Delete"
            ).setIcon(R.drawable.delete)
        }

        // Report
        popupMenu.menu.add(
            Menu.NONE, Menu.FIRST + 1, Menu.NONE, "Report"
        ).setIcon(R.drawable.report)

        // Share
        popupMenu.menu.add(
            Menu.NONE, Menu.FIRST + 2, Menu.NONE, "Share"
        ).setIcon(R.drawable.share)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                Menu.FIRST -> {
                    deleteProject()
                    true
                }

                Menu.FIRST + 1 -> {
                    report()
                    true
                }

                Menu.FIRST + 2 -> {
                    share()
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun runDownload() {
        requestNotificationPermissionWithDialog()
    }

    fun requestNotificationPermissionWithDialog() {

        // Android < 13 → no permission required
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            runDownload2()
            return
        }

        // Already granted
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            runDownload2()
            return
        }

        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        ).setIcon(R.drawable.ic_notifications).setTitle("Enable notifications").setMessage(
            "We use notifications to show download progress and completion. " + "Downloads will still work without this permission."
        ).setPositiveButton("Allow") { _, _ ->
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }.setNegativeButton("Not now") { _, _ ->
            runDownload2()
        }.setCancelable(false).show()
    }

    private fun runDownload2() {
        val project = projectData
        val title = project.title.replace(Regex("""[\\/:*?"<>|]"""), "_")

        val baseDir = File(
            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "OpenWare"
        )

        val filePath = File(baseDir, "Apk/$title.apk")

        filePath.parentFile?.mkdirs()

        val downloadUrl = project.downloadUrl

        this.filePath = filePath.absolutePath

        startDownload(downloadUrl, filePath.absolutePath)
    }

    private fun startDownload(url: String, apkPath: String) {
        progressButton.showProgress(true)

        lifecycleScope.launch(Dispatchers.IO) {
            downloadFiles(
                fileUrl = url, destinationPath = apkPath, callback = callback
            )
        }
    }

    private fun installApk() {
        if (!packageManager.canRequestPackageInstalls()) {
            startActivity(
                Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData("package:$packageName".toUri())
            )
            return
        }

        val apkUri = FileProvider.getUriForFile(
            this, "$packageName.fileprovider", File(filePath)
        )

        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            })
    }

    private fun openTag(tag: String) {
        val title = when (tag) {
            "category" -> projectData.category
            "editorChoice", "verify", "premium" -> "true"
            else -> return // invalid tag → do nothing
        }

        startActivity(
            Intent(this, CategoryActivity::class.java).apply {
                putExtra("code", tag)
                putExtra("title", title)
            })
    }

    private fun deleteProject() {
        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        ).setIcon(R.drawable.delete).setTitle("Delete")
            .setMessage("Do you really want to delete this project? This action cannot be undone.")
            .setNegativeButton("Cancel", null).setPositiveButton("Delete") { _, _ ->
                syncTask(object : SyncTaskListener {

                    override fun beforeTaskStart() {
                        showProgressDialog()
                    }

                    override fun onBackground() {
                        val firebaseUtils = FirebaseUtils()

                        // delete icon + main file
                        firebaseUtils.deleteFileFromStorageByUrl(
                            projectData.icon, projectData.downloadUrl
                        )

                        // delete screenshots
                        val s: List<String> = Gson().fromJson(
                            projectData.screenshots, object : TypeToken<List<String>>() {}.type
                        )

                        s.forEach { url ->
                            firebaseUtils.deleteFileFromStorageByUrl(url)
                        }


                        val targetRef = if (isFree) projectRef else premiumRef

                        targetRef.child(projectKey).removeValue()
                    }

                    override fun onTaskComplete() {
                        dismissProgressDialog()

                        FirebaseUtils().decreaseUserKeyData("projects", getUid())

                        alertCreator("Project deleted successfully.") {
                            finish()
                        }
                    }
                })
            }.show()
    }

    private fun share() {
        val baseUrl = buildString {
            append(websiteUrl)
            append(if (isFree) "p/n/" else "p/p/")
        }

        getProjectId(
            key = projectKey, premium = !isFree
        ) { projectId ->
            shareText(
                text = baseUrl + projectId
            )
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun report() {
        val bottomSheet = BottomSheetController(this)
        bottomSheet.create(R.layout.report)

        val inputField: EditText = bottomSheet.find(R.id.edittext1)
        val submitButton: MaterialButton = bottomSheet.find(R.id.b1)

        inputField.requestFocus()
        openKeyboard(inputField)

        submitButton.setOnClickListener {
            val message = inputField.text.toString().trim()

            if (!Utils.isConnected(this)) {
                alertToast("No Internet")
                return@setOnClickListener
            }

            if (message.length <= 5) {
                toast("Please explain your report")
                return@setOnClickListener
            }

            val reportKey = reportRef.push().key ?: run {
                toast("Something went wrong")
                return@setOnClickListener
            }

            val time = SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Calendar.getInstance().time)

            val reportData = mapOf(
                "projectName" to binding.title.text.toString(),
                "message" to message,
                "projectKey" to projectKey,
                "projectUid" to userUid,
                "key" to reportKey,
                "type" to getPrefString("developer", "type", ""),
                "mode" to "Report",
                "time" to time,
                "userName" to userConfig.name,
                "userEmail" to userConfig.email,
                "userUid" to getUid()
            )

            reportRef.child(reportKey).updateChildren(reportData)
            alertToast("We will review your report")
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    fun View.clickScaleAnimation(
        scaleFrom: Float = 0.9f, scaleTo: Float = 1f, duration: Long = 150L
    ) {
        animate().scaleX(scaleFrom).scaleY(scaleFrom).setDuration(0).withEndAction {
            animate().scaleX(scaleTo).scaleY(scaleTo).setDuration(duration).start()
        }.start()
    }

    fun setupScrollDivider(
        scrollView: NestedScrollView, divider: View
    ) {
        divider.visibility = View.GONE
        scrollView.isVerticalScrollBarEnabled = false

        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            divider.visibility = if (scrollY > 0) View.VISIBLE else View.GONE
        }
    }

    fun View.removeScrollbars() {
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
    }

    fun TextView.detectLinksMaterial() {
        isClickable = true
        linksClickable = true

        Linkify.addLinks(
            this, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES
        )

        val colorTertiary = this@ProjectViewActivity.getThemeColor(
            com.google.android.material.R.attr.colorTertiary
        )

        setLinkTextColor(colorTertiary)
    }

    fun View.animateLayoutChange(
        duration: Long = 200L, interpolator: TimeInterpolator = DecelerateInterpolator()
    ) {
        val parent = this as? ViewGroup ?: return

        AutoTransition().apply {
            this.duration = duration
            this.interpolator = interpolator
            TransitionManager.beginDelayedTransition(parent, this)
        }
    }

    fun createRoundedDrawable(
        cornerRadiusDp: Float,
        strokeWidthDp: Float,
        @ColorInt strokeColor: Int,
        @ColorInt fillColor: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            cornerRadius = dp(cornerRadiusDp.toInt()).toFloat()
            setColor(fillColor)

            if (strokeWidthDp > 0f) {
                setStroke(dp(strokeWidthDp.toInt()), strokeColor)
            }
        }
    }

    inner class ScreenShotsAdapter(
        private val images: List<String>
    ) : RecyclerView.Adapter<ScreenShotsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(ProjectScreenshotCell(parent.context))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val imageUrl = images[position]
            holder.cell.setImage(imageUrl)

            holder.itemView.setOnClickListener {
                ImageViewer.Builder(
                    holder.cell.context, images
                ) { imageView, image ->
                    Glide.with(imageView).load(image)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(0xFFD3D3D3.toInt().toDrawable()).into(imageView)
                }.withStartPosition(position).withHiddenStatusBar(true).allowZooming(true)
                    .allowSwipeToDismiss(true).withTransitionFrom(holder.imageView)
                    .withDismissListener {
                        holder.imageView.visibility = View.VISIBLE
                    }.show()
            }
        }

        override fun getItemCount(): Int = images.size

        inner class ViewHolder(
            val cell: ProjectScreenshotCell
        ) : RecyclerView.ViewHolder(cell) {
            val imageView = cell.image
        }
    }

}