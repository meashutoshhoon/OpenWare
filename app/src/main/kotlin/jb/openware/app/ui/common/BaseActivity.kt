package jb.openware.app.ui.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import jb.openware.app.ui.components.LinkSpan
import jb.openware.app.ui.components.MaterialProgressDialog
import jb.openware.app.ui.items.ScreenshotItem
import jb.openware.app.util.ConnectionManager
import jb.openware.app.util.HapticUtils
import jb.openware.app.util.ThemeUtil
import jb.openware.app.util.UserConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.math.roundToInt
import androidx.core.content.edit
import com.google.android.material.color.MaterialColors

abstract class BaseActivity<VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater) -> VB
) : AppCompatActivity() {

    // --- image picker listeners ---
    private var imagePickedListener: ImagePickedListener? = null
    private var multipleImagePickedListener: MultipleImagePickedListener? = null

    private var filePickedListener: FilePickedListener? = null
    private var videoPickedListener: VideoPickedListener? = null
    private var permissionListener: PermissionListener? = null
    private var materialProgressDialog: MaterialProgressDialog? = null
    private val photos: ArrayList<MutableMap<String, Any>> = arrayListOf()
    private var vibrationDuration: Long = 0L

    private lateinit var vibrator: Vibrator

    private val NAME = "app_prefs"

    protected lateinit var binding: VB
        private set

    companion object {
        private const val REQUEST_CODE_PICK_IMAGES = 1
        private const val STORAGE_PERMISSION_CODE_NEW = 100
    }

    private object UriTypes {
        const val EXTERNAL = "com.android.externalstorage.documents"
        const val DOWNLOADS = "com.android.providers.downloads.documents"
        const val MEDIA = "com.android.providers.media.documents"
    }

    private fun isExternalStorageDocument(uri: Uri) = uri.authority == UriTypes.EXTERNAL
    private fun isDownloadsDocument(uri: Uri) = uri.authority == UriTypes.DOWNLOADS
    private fun isMediaDocument(uri: Uri) = uri.authority == UriTypes.MEDIA

    protected val storageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || !Environment.isExternalStorageManager()) {
                permissionListener?.onNotGranted()
            } else {
                permissionListener?.onGranted()
            }
        }

    private val pickSingleImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val (path, name) = resolveImageMeta(uri)
                imagePickedListener?.onImagePicked(path, name, uri)
            }
        }

    private val pickMultipleImagesLauncher =
        registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(5)
        ) { uris ->

            if (uris.isEmpty()) return@registerForActivityResult

            val screenshots = uris.map { uri ->
                val (path, name) = resolveImageMeta(uri)
                ScreenshotItem(
                    path = path,
                    name = name
                )
            }.toMutableList()

            multipleImagePickedListener?.onImagePicked(screenshots)
        }


    fun convertUriToFilePath(context: Context, uri: Uri): String? {
        var path: String? = null

        if (DocumentsContract.isDocumentUri(context, uri)) {
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type = split[0]

                    if (type.equals("primary", ignoreCase = true) && split.size > 1) {
                        path = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }
                }

                isDownloadsDocument(uri) -> {
                    val id = DocumentsContract.getDocumentId(uri)

                    if (!id.isNullOrEmpty()) {
                        if (id.startsWith("raw:")) {
                            return id.removePrefix("raw:")
                        }

                        val contentUri = ContentUris.withAppendedId(
                            "content://downloads/public_downloads".toUri(),
                            id.toLongOrNull() ?: return null
                        )

                        path = getDataColumn(context, contentUri, null, null)
                    }
                }

                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    if (split.size < 2) return null

                    val type = split[0]
                    val idPart = split[1]

                    val contentUri: Uri? = when (type) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> null
                    }

                    if (contentUri != null) {
                        val selection = "_id=?"
                        val selectionArgs = arrayOf(idPart)
                        path = getDataColumn(context, contentUri, selection, selectionArgs)
                    }
                }
            }
        } else if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT, ignoreCase = true)) {
            path = getDataColumn(context, uri, null, null)
        } else if (uri.scheme.equals(ContentResolver.SCHEME_FILE, ignoreCase = true)) {
            path = uri.path
        }

        return path?.let {
            try {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun resolveImageMeta(uri: Uri): Pair<String, String> {
        var name: String? = null

        contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }

        val fileName = name ?: (uri.lastPathSegment ?: "unknown")
        val path = convertUriToFilePath(this, uri).toString()

        return path to fileName
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        ThemeUtil.updateTheme(this)
        super.onCreate(savedInstanceState)

        binding = bindingInflater(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION") getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        vibrationDuration = 250L

        init()
        initLogic()
    }

    protected abstract fun init()
    protected abstract fun initLogic()

    fun checkStorage(listener: PermissionListener) {
        permissionListener = listener
        if (checkPermission()) listener.onGranted() else requestPermission()
    }

    fun createLinkSpan(
        linkSpan: LinkSpan,
        firstText: String,
        secondText: String,
        clickListener: View.OnClickListener
    ) {
        linkSpan.apply {
            setFirstText(firstText)
            setSecondText(secondText)
            setFirstTextColor(0xFF757575.toInt())
            setSecondTextColor(0xFF2678B6.toInt())
            setTextSize(13f)
            setOnClickListener(clickListener)
        }
    }


    fun pickSinglePhoto(listener: ImagePickedListener) {
        this.imagePickedListener = listener

        pickSingleImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    fun pickMultiplePhotos(listener: MultipleImagePickedListener) {
        this.multipleImagePickedListener = listener

        pickMultipleImagesLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != STORAGE_PERMISSION_CODE_NEW || grantResults.size < 2) return

        val writeGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        val readGranted = grantResults[1] == PackageManager.PERMISSION_GRANTED
        val allGranted = writeGranted && readGranted

        permissionListener?.let { listener ->
            if (allGranted) {
                listener.onGranted()
            } else {
                listener.onNotGranted()
            }
        }
    }

    fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val write = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val read = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE
            )

            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val packageUri = Uri.fromParts("package", packageName, null)
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = packageUri
            }

            try {
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                // fallback to generic settings screen
                val fallbackIntent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                storageActivityResultLauncher.launch(fallbackIntent)
            }
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ), STORAGE_PERMISSION_CODE_NEW
            )
        }
    }

    private fun getDataColumn(
        context: Context, uri: Uri, selection: String? = null, selectionArgs: Array<String>? = null
    ): String? {
        val column = MediaStore.Images.Media.DATA
        val projection = arrayOf(column)

        return try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndexOrThrow(column)
                        cursor.getString(index)
                    } else {
                        null
                    }
                }
        } catch (_: Exception) {
            null
        }
    }

    fun showProgressDialog() {
        if (materialProgressDialog == null) {
            materialProgressDialog = MaterialProgressDialog(this)
        }
        materialProgressDialog?.show()
    }

    fun dismissProgressDialog() {
        materialProgressDialog?.hide()
    }

    fun dp(value: Int): Int = (resources.displayMetrics.density * value).roundToInt()

    fun Context.isNightMode(): Boolean {
        val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    suspend fun getMessagingToken(): String =
        FirebaseMessaging.getInstance().token.await()

    fun hideViews(vararg views: View) {
        views.forEach { it.visibility = View.GONE }
    }

    fun showViews(vararg views: View) {
        views.forEach { it.visibility = View.VISIBLE }
    }

    fun View.show() { isVisible = true }
    fun View.hide() { isGone = true }

    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        val windowToken = currentFocus?.windowToken
        if (windowToken != null) {
            imm?.hideSoftInputFromWindow(windowToken, 0)
        }
    }

    fun restartApp() {
        finishAffinity()
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(it)
        }
    }

    val userConfig: UserConfig
        get() = UserConfig(this)

    fun delayTask(delayMillis: Long = 200, task: () -> Unit) {
        lifecycleScope.launch {
            delay(delayMillis)
            task()
        }
    }

    fun getBaseUrl(id: String): String = "https://ashutoshgupta01.github.io/vid/$id.json"

    fun alertCreator(
        message: String?, onPositive: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(this).setTitle("Alert").setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                onPositive?.invoke()
            }.show()
    }

    fun getConnectionsManager(
        url: String,
        method: String,
        headers: Map<String, Any> = emptyMap(),
        listener: RequestListener
    ) {
        val manager = ConnectionManager().apply {
            if (headers.isNotEmpty()) {
                this.headers = HashMap(headers)
            }
        }

        manager.startRequest(
            method, url, "Request_Tag", object : ConnectionManager.RequestListener {
                override fun onResponse(
                    tag: String, response: String, responseHeaders: HashMap<String, Any>
                ) {
                    listener.onResponse(tag, response, responseHeaders)
                }

                override fun onErrorResponse(tag: String, message: String) {
                    listener.onErrorResponse(tag, message)
                }
            })
    }

    fun backgroundRunner(block: suspend CoroutineScope.() -> Unit) {
        lifecycleScope.launch(Dispatchers.IO, block = block)
    }

    fun Context.toast(message: CharSequence, long: Boolean = false) {
        Toast.makeText(
            this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).show()
    }

    fun View.hapticClick() {
        HapticUtils.weakVibrate(this)
    }

    fun getUid(): String =
        FirebaseAuth.getInstance().currentUser?.uid
            ?: error("User not authenticated")


    inline fun <reified T : Activity> openActivity(
        clearTaskAndFinish: Boolean = false
    ) {
        val intent = Intent(this, T::class.java)

        if (clearTaskAndFinish) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        } else {
            startActivity(intent)
        }
    }

    fun Context.putPrefString(name: String = NAME, key: String, value: String) {
        getSharedPreferences(name, MODE_PRIVATE)
            .edit {
                putString(key, value)
            }
    }

    fun Context.getPrefString(
        name: String = NAME,
        key: String,
        default: String
    ): String {
        return getSharedPreferences(name, MODE_PRIVATE)
            .getString(key, default) ?: default
    }

    fun Context.getThemeColor(attr: Int, fallback: Int = Color.TRANSPARENT): Int {
        return MaterialColors.getColor(this, attr, fallback)
    }

    fun isAndroid13OrAbove(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun generateFileNameWithTimestamp(): String {
        val uid = getUid()
        val timestamp = System.currentTimeMillis()

        return "AVATAR_${uid}_$timestamp.png"
    }

    fun generateProjectNameWithTimestamp(): String {
        val uid = getUid()
        val timestamp = System.currentTimeMillis()

        return "p${uid}_$timestamp"
    }

    fun extractUidAndTimestamp(
        fileName: String, listener: UidTimestampListener?
    ) {
        val cleaned = fileName.removeSuffix(".png")
        val parts = cleaned.split("_")

        if (parts.size >= 3) {
            val uid = parts[1]
            val timestamp = parts[2]
            listener?.onUidTimestampGenerated(uid, timestamp)
        } else {
            // Bad format → ignore or log if you have logging
        }
    }

    @SuppressLint("DefaultLocale")
    fun formatNumber(input: String?): String {
        val number = input?.toLongOrNull() ?: return ""

        return when {
            number >= 1_000_000_000L -> String.format("%.1fB", number / 1_000_000_000.0)
            number >= 1_000_000L -> String.format("%.1fM", number / 1_000_000.0)
            number >= 1_000L -> String.format("%.1fK", number / 1_000.0)
            else -> number.toString()
        }
    }

    fun alertToast(text: String) {

        if (vibrationDuration > 0) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    vibrationDuration, VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
        toast(text)
    }

    fun sendEmail(subject: String, body: String, email: String) {
        val uri = "mailto:$email".toUri().buildUpon().appendQueryParameter("subject", subject)
            .appendQueryParameter("body", body).build()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = uri
        }

        startActivity(intent)
    }

    fun openKeyboard(editText: EditText) {
        editText.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    fun uiThread(action: () -> Unit) {
        if (!isFinishing) {
            lifecycleScope.launch(Dispatchers.Main) { action() }
        }
    }

    fun createDialog(
        title: String,
        message: String,
        positiveText: String,
        negativeText: String? = null,
        cancelable: Boolean = true,
        onPositive: (() -> Unit)? = null,
        onNegative: (() -> Unit)? = null
    ): MaterialAlertDialogBuilder {

        return MaterialAlertDialogBuilder(this).apply {
            setTitle(title)
            setMessage(message)
            setCancelable(cancelable)

            setPositiveButton(positiveText) { _, _ ->
                onPositive?.invoke()
            }

            if (negativeText != null) {
                setNegativeButton(negativeText) { _, _ ->
                    onNegative?.invoke()
                }
            }
        }
    }

    fun getFileExtension(filePath: String): String =
        filePath.substringAfterLast('.', missingDelimiterValue = "")

    fun syncTask(listener: SyncTaskListener) {
        listener.beforeTaskStart()

        CoroutineScope(Dispatchers.IO).launch {
            listener.onBackground()

            withContext(Dispatchers.Main) {
                listener.onTaskComplete()
            }
        }
    }

    fun uploadFileToFirebaseStorage(
        storagePath: String,
        child: String,
        fileUri: Uri,
        onSuccess: (Uri) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileRef = FirebaseStorage
            .getInstance()
            .getReference(storagePath)
            .child(child)

        fileRef.putFile(fileUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                fileRef.downloadUrl
            }
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
    }


    fun pushToDatabase(
        dataMap: Map<String, Any>,
        reference: String,
        child: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseDatabase.getInstance()
            .getReference(reference)
            .child(child)
            .updateChildren(dataMap)
            .addOnSuccessListener {
                Log.d("Firebase", "Data pushed successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to push data", e)
                onFailure(e)
            }
    }

    fun getProjectId(
        key: String,
        premium: Boolean,
        callback: (String) -> Unit
    ) {
        val databasePath = if (premium) "projects/premium" else "projects/normal"

        FirebaseDatabase.getInstance()
            .getReference(databasePath)
            .child(key)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        callback("error")
                        return
                    }

                    val data = snapshot.getValue(
                        object : GenericTypeIndicator<Map<String, Any>>() {}
                    ) ?: run {
                        callback("error")
                        return
                    }

                    val id = data["id"]?.toString()
                    if (id != null) {
                        callback(id)
                    } else {
                        callback("error")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback("error")
                }
            })
    }


    fun getDataFromDatabase(
        reference: String,
        child: String,
        callback: (Map<String, Any>?) -> Unit
    ) {
        FirebaseDatabase.getInstance()
            .getReference(reference)
            .child(child)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        callback(null)
                        return
                    }

                    val data = snapshot.getValue(
                        object : GenericTypeIndicator<Map<String, Any>>() {}
                    )

                    callback(data)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    fun retrieveProjectsListFromFirebase(
        callback: (String) -> Unit
    ) {
        val nodeKey = "pp_3"
        val databaseRef = FirebaseDatabase.getInstance()
            .getReference("pp")
            .child(nodeKey)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback("0")
                    return
                }

                val data = snapshot.getValue(
                    object : GenericTypeIndicator<Map<String, Any>>() {}
                )

                val points = data?.get("points")?.toString() ?: "0"
                callback(points)
            }

            override fun onCancelled(error: DatabaseError) {
                callback("error")
            }
        })
    }

    fun updateProjectsListToFirebase(
        onSuccess: () -> Unit
    ) {
        val nodeKey = "pp_3"
        val databasePath = "pp"

        retrieveProjectsListFromFirebase { value ->
            if (value == "error") {
                alertCreator("Failed to retrieve points")
                return@retrieveProjectsListFromFirebase
            }

            val currentPoints = value.toIntOrNull() ?: 0
            val updatedPoints = currentPoints + 1

            val dataMap = mapOf(
                "points" to updatedPoints.toString()
            )

            pushToDatabase(
                dataMap = dataMap,
                reference = databasePath,
                child = nodeKey,
                onSuccess = onSuccess,
                onFailure = { e -> alertCreator(e.message) }
            )
        }
    }


    interface UidTimestampListener {
        fun onUidTimestampGenerated(uid: String, timestamp: String)
    }

    interface RequestListener {
        fun onResponse(tag: String, response: String, responseHeaders: HashMap<String, Any>)
        fun onErrorResponse(tag: String, message: String)
    }

    interface ChildListener {
        fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?)
        fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?)
        fun onChildRemoved(snapshot: DataSnapshot)
        fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?)
        fun onCancelled(error: DatabaseError)
    }

    interface SyncTaskListener {
        fun beforeTaskStart()
        fun onBackground()
        fun onTaskComplete()
    }

    interface ImagePickedListener {
        fun onImagePicked(profilePath: String, imageFileName: String, imageUri: Uri)
    }

    interface MultipleImagePickedListener {
        fun onImagePicked(list: MutableList<ScreenshotItem>)
    }

    interface FilePickedListener {
        fun onFilePicked(filePath: String, fileName: String)
    }

    interface VideoPickedListener {
        fun onVideoPicked(profilePath: String, videoFileName: String, videoUri: Uri)
    }

    interface TimeCallback {
        fun onDateListener(data: MutableMap<String, Any>)
        fun onDateChangedListener(data: MutableMap<String, Any>)
        fun onNoUser()
    }

    interface FirebaseDataCallback {
        fun onDataRetrieved(value: String)
    }

    interface PermissionListener {
        fun onGranted()
        fun onNotGranted()
    }
}