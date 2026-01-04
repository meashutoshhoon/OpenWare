package jb.openware.app.ui.activity.profile

import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.widget.LinearLayout
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import jb.openware.app.R
import jb.openware.app.databinding.ActivityProfileEditBinding
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.ui.components.BottomSheetController
import jb.openware.app.ui.items.UserProfile
import jb.openware.app.util.ImageUtil
import jb.openware.app.util.Utils
import java.io.File

class ProfileEditActivity :
    BaseActivity<ActivityProfileEditBinding>(ActivityProfileEditBinding::inflate) {

    private val usersRef = FirebaseDatabase.getInstance().getReference("Users")

    private val usernames = mutableSetOf<String>()
    private var url: String = "none"
    private var namePre: String = ""
    private var avatar: File? = null
    private var pickedNewAvatar = false

    private var name: String? = null
    private lateinit var color: String

    override fun init() {
        name = intent.getStringExtra("name")
        color = intent.getStringExtra("color") ?: "#000000"
        val bioText = intent.getStringExtra("bio") ?: ""
        url = intent.getStringExtra("url") ?: "none"

        pickedNewAvatar = false
        avatar = null

        // avatar display
        if (url == "none") {
            hideViews(binding.circleimageview)
            showViews(binding.linearWord)
            binding.linearWord.background = createRoundedDrawable(color.toColorInt())
            name?.let { if (it.isNotEmpty()) binding.txWord.text = it.first().toString() }
        } else {
            showViews(binding.circleimageview)
            hideViews(binding.linearWord)
            Glide.with(this).load(url.toUri()).into(binding.circleimageview)
        }

        binding.toolbar.setNavigationOnClickListener(Utils.getBackPressedClickListener(this))

        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_upload) {
                upload()
                true
            } else false
        }

        binding.name.setText(name)
        binding.bio.setText(bioText)

    }

    override fun initLogic() {
        observeUsers()
        setupAvatarPicker()
    }

    private fun observeUsers() {
        usersRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(s: DataSnapshot, p: String?) = handleUser(s)
            override fun onChildChanged(s: DataSnapshot, p: String?) = handleUser(s)
            override fun onChildMoved(s: DataSnapshot, p: String?) {}
            override fun onChildRemoved(s: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun handleUser(snapshot: DataSnapshot) {
        val user = snapshot.getValue(UserProfile::class.java) ?: return

        user.name.let { usernames.add(it) }

        if (snapshot.key == getUid()) namePre = user.name
    }

    private fun setupAvatarPicker() {
        binding.picker.setOnClickListener {

            // PICK NEW
            if (url == "none") {
                pickSinglePhoto { _, _, uri ->
                    pickedNewAvatar = true
                    avatar = ImageUtil.compressImage(this, uri, 40)
                    showAvatarPreview()
                }
                return@setOnClickListener
            }

            // REMOVE / CHANGE OPTIONS
            val sheet = BottomSheetController(this).apply { create(R.layout.sheet_profile) }
            sheet.find<LinearLayout>(R.id.bt1).setOnClickListener { // Change
                pickSinglePhoto { _, _, uri ->
                    pickedNewAvatar = true
                    avatar = ImageUtil.compressImage(this, uri, 40)
                    showAvatarPreview()
                    sheet.dismiss()
                }
            }

            sheet.find<LinearLayout>(R.id.bt2).setOnClickListener { // Remove
                removeAvatar(sheet)
            }

            sheet.show()
        }
    }

    private fun showAvatarPreview() {
        hideViews(binding.linearWord)
        showViews(binding.circleimageview)
        Glide.with(this).load(Uri.fromFile(avatar)).into(binding.circleimageview)
    }

    private fun removeAvatar(sheet: BottomSheetController) {
        showProgressDialog()
        val id = getUid()

        usersRef.child(id).get().addOnSuccessListener { snap ->
            val user = snap.getValue(UserProfile::class.java)
            val avatarUrl = user?.avatar ?: ""

            if (avatarUrl.isNotBlank() && avatarUrl != "none") {
                FirebaseStorage.getInstance().getReferenceFromUrl(avatarUrl).delete()
                    .addOnCompleteListener { updateAvatarNone(sheet) }
            } else {
                updateAvatarNone(sheet)
            }
        }.addOnFailureListener {
            dismissProgressDialog()
            alertCreator("Failed to remove avatar")
        }
    }

    private fun updateAvatarNone(sheet: BottomSheetController) {
        val map = mapOf("avatar" to "none")
        pushToDatabase(map, "Users", getUid(), {
            url = "none"
            pickedNewAvatar = false
            hideViews(binding.circleimageview)
            showViews(binding.linearWord)
            binding.linearWord.background = createRoundedDrawable(color.toColorInt())
            binding.txWord.text = namePre.firstOrNull()?.toString() ?: ""
            toast("Profile picture removed")
            dismissProgressDialog()
            sheet.dismiss()
        }) {
            dismissProgressDialog()
            alertCreator(it.message)
        }
    }

    private fun upload() {
        hideKeyboard()
        showProgressDialog()

        val inputName = binding.name.text.toString()
        val inputBio = binding.bio.text.toString()
        val id = getUid()

        when {
            inputName.isBlank() -> return fail("Name empty")
            inputBio.isBlank() -> return fail("Bio is empty")
            namePre != inputName && usernames.contains(inputName) -> {
                MaterialAlertDialogBuilder(this).setTitle("Alert")
                    .setMessage("Username already taken.").setPositiveButton("Ok", null).show()
                return fail()
            }
        }

        // if new avatar selected
        if (pickedNewAvatar) {
            handleAvatarUpload(inputName, inputBio)
            return
        }

        // normal update
        val data = mapOf("name" to inputName, "bio" to inputBio)
        pushToDatabase(data, "Users", id, {
            dismissProgressDialog()
            finish()
        }) { fail(it.message) }
    }

    private fun handleAvatarUpload(name: String, bio: String) {
        if (url != "none") removeOldAvatarThenPush(name, bio) else pushNewAvatar(name, bio)
    }

    private fun removeOldAvatarThenPush(name: String, bio: String) {
        val id = getUid()
        usersRef.child(id).get().addOnSuccessListener { snap ->
            val avatarUrl = snap.getValue(UserProfile::class.java)?.avatar ?: ""

            if (avatarUrl.isNotBlank() && avatarUrl != "none") {
                FirebaseStorage.getInstance().getReferenceFromUrl(avatarUrl).delete()
                    .addOnCompleteListener { pushNewAvatar(name, bio) }
            } else pushNewAvatar(name, bio)
        }.addOnFailureListener { pushNewAvatar(name, bio) }
    }

    private fun pushNewAvatar(name: String, bio: String) {
        val file = avatar ?: return fail("No avatar file found")
        val fileName = generateFileNameWithTimestamp()
        uploadFileToFirebaseStorage("avatar", fileName, file.toUri(), { uri ->
            val map = mapOf(
                "avatar" to uri.toString(), "name" to name, "bio" to bio
            )
            pushToDatabase(map, "Users", getUid(), {
                dismissProgressDialog()
                finish()
            }) { fail(it.message) }
        }) { fail(it.message) }
    }

    private fun fail(msg: String? = null) {
        msg?.let { toast(it) }
        dismissProgressDialog()
    }

    private fun createRoundedDrawable(color: Int) = GradientDrawable().apply {
        cornerRadius = 360f
        setColor(color)
    }

}