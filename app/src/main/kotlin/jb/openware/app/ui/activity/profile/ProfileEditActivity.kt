package jb.openware.app.ui.activity.profile

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.widget.NestedScrollView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import jb.openware.app.R
import jb.openware.app.databinding.ActivityProfileEditBinding
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.util.ImageUtil
import jb.openware.app.util.Utils
import java.io.File

class ProfileEditActivity : BaseActivity<ActivityProfileEditBinding>(ActivityProfileEditBinding::inflate) {

    private val firebase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = firebase.getReference("Users")

    private val usernames = ArrayList<String>()
    private var url: String = "none"
    private var namePre: String = ""
    private var avatar: File? = null
    private var aBoolean: Boolean = false

    private val userMapType = object : GenericTypeIndicator<HashMap<String, Any?>>() {}

    override fun init() {
        val name = intent.getStringExtra("name")
        val color = intent.getStringExtra("color") ?: "#000000"
        val bioT = intent.getStringExtra("bio") ?: ""
        url = intent.getStringExtra("url") ?: "none"

        avatar = null
        aBoolean = false

        // Setup avatar view or placeholder initial
        if (url == "none") {
            hideViews(binding.circleimageview)
            showViews(binding.linearWord)
            binding.linearWord.background = createRoundedDrawable(color.toColorInt())
            name?.let { if (it.isNotEmpty()) binding.txWord.text = it.substring(0, 1) }
        } else {
            hideViews(binding.linearWord)
            showViews(binding.circleimageview)
            Glide.with(this).load(url.toUri()).into(binding.circleimageview)
        }

        binding.toolbar.setNavigationOnClickListener(Utils.getBackPressedClickListener(this))

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_upload -> {
                    upload()
                    true
                }
                else -> false
            }
        }

        binding.name.setText(name)
        binding.bio.setText(bioT)

    }

    override fun initLogic() {
        binding.picker.setOnClickListener {
            if (url == "none") {
                pickSinglePhoto(listener = object : ImagePickedListener {
                    override fun onImagePicked(
                        profilePath: String,
                        imageFileName: String,
                        imageUri: Uri
                    ) {
                        aBoolean = true
                        avatar = ImageUtil.compressImage(this@ProfileEditActivity, imageUri, 40)
                        hideViews(binding.linearWord)
                        showViews(binding.circleimageview)
                        Glide.with(this@ProfileEditActivity).load(Uri.fromFile(avatar)).into(binding.circleimageview)
                    }

                })
            } else {
                createBottomSheetDialog(R.layout.sheet_profile)
                val bt1 = bsId<LinearLayout>(R.id.bt1)
                val bt2 = bsId<LinearLayout>(R.id.bt2)

                bt1.setOnClickListener {
                    pickSinglePhoto { profilePath, imageFileName, imageUri ->
                        aBoolean = true
                        avatar = ImageUtils.compressImage(this, imageUri, 40)
                        hideViews(linearWord)
                        showViews(circleImageView1)
                        Glide.with(this).load(Uri.fromFile(avatar)).into(circleImageView1)
                        dismissBottomSheetDialog()
                    }
                }

                bt2.setOnClickListener {
                    showProgressDialog()
                    val currentUserId = getUID()
                    val usersDatabaseRef = FirebaseDatabase.getInstance().getReference("Users")

                    val valueEventListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.key == currentUserId) {
                                val userData = dataSnapshot.getValue(userMapType)
                                if (userData != null) {
                                    val avatarName = userData["avatar_name"]?.toString()
                                    if (!avatarName.isNullOrBlank()) {
                                        val storageRef: StorageReference =
                                            FirebaseStorage.getInstance().getReference("avatar").child(avatarName)

                                        storageRef.delete().addOnSuccessListener {
                                            val hashMap = HashMap<String, Any>()
                                            hashMap["avatar"] = "none"
                                            hashMap["avatar_name"] = "none"
                                            pushToDatabase(hashMap, "Users", getUID(), { _ ->
                                                url = "none"
                                                aBoolean = false
                                                hideViews(circleImageView1)
                                                showViews(linearWord)
                                                linearWord.background = createRoundedDrawable(360, Color.parseColor(color))
                                                name?.let { if (it.isNotEmpty()) txWord.text = it.substring(0, 1) }
                                                showToast("Profile picture removed")
                                                dismissProgressDialog()
                                            }, { e ->
                                                dismissProgressDialog()
                                                alertCreator(e.message)
                                            })
                                        }.addOnFailureListener { e ->
                                            dismissProgressDialog()
                                            alertCreator(e.message)
                                        }
                                    } else {
                                        // No avatar name - just update DB if required
                                        val hashMap = HashMap<String, Any>()
                                        hashMap["avatar"] = "none"
                                        hashMap["avatar_name"] = "none"
                                        pushToDatabase(hashMap, "Users", getUID(), { _ ->
                                            url = "none"
                                            aBoolean = false
                                            hideViews(circleImageView1)
                                            showViews(linearWord)
                                            linearWord.background = createRoundedDrawable(360, Color.parseColor(color))
                                            name?.let { if (it.isNotEmpty()) txWord.text = it.substring(0, 1) }
                                            showToast("Profile picture removed")
                                            dismissProgressDialog()
                                        }, { e ->
                                            dismissProgressDialog()
                                            alertCreator(e.message)
                                        })
                                    }
                                } else {
                                    dismissProgressDialog()
                                }
                            } else {
                                dismissProgressDialog()
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            dismissProgressDialog()
                            alertCreator("An error has occurred. Please try again later.")
                        }
                    }
                    usersDatabaseRef.child(currentUserId).addListenerForSingleValueEvent(valueEventListener)
                    dismissBottomSheetDialog()
                }
                showBottomSheetDialog()
            }
        }

        val usersChildListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val childKey = snapshot.key
                val childValue = snapshot.getValue(userMapType)
                childValue?.get("name")?.toString()?.let { usernames.add(it) }
                if (childKey == getUid() && childValue != null) {
                    namePre = childValue["name"]?.toString() ?: ""
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val childKey = snapshot.key
                val childValue = snapshot.getValue(userMapType)
                childValue?.get("name")?.toString()?.let { usernames.add(it) }
                if (childKey == getUid() && childValue != null) {
                    namePre = childValue["name"]?.toString() ?: ""
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {}
        }

        usersRef.addChildEventListener(usersChildListener)


    }

    private fun upload() {
        hideKeyboard()
        showProgressDialog()

        val name = nameEditText.text?.toString() ?: ""
        val bio = bioEditText.text?.toString() ?: ""
        val currentUserId = getUID()
        val usersDatabaseRef = FirebaseDatabase.getInstance().getReference("Users")

        when {
            TextUtils.isEmpty(name) -> {
                toast("Name empty")
                dismissProgressDialog()
            }
            TextUtils.isEmpty(bio) -> {
                toast("Bio is empty")
                dismissProgressDialog()
            }
            namePre != name && usernames.contains(name) -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Alert")
                    .setMessage("Username already taken.")
                    .setPositiveButton("Ok", null)
                    .show()
                dismissProgressDialog()
            }
            else -> {
                if (aBoolean) {
                    if (url != "none") {
                        val valueEventListener = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists() && dataSnapshot.key == currentUserId) {
                                    val userData = dataSnapshot.getValue(userMapType)
                                    val value = userData?.get("avatar_name")?.toString()
                                    if (!value.isNullOrBlank()) {
                                        val storageRef = FirebaseStorage.getInstance().getReference("avatar").child(value)
                                        storageRef.delete().addOnSuccessListener {
                                            push()
                                        }.addOnFailureListener {
                                            // even if delete fails, continue with push (original behavior)
                                            push()
                                        }
                                    } else {
                                        push()
                                    }
                                } else {
                                    push()
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                alertCreator("An error has occurred. Please try again later.")
                            }
                        }
                        usersDatabaseRef.child(currentUserId).addListenerForSingleValueEvent(valueEventListener)
                    } else {
                        push()
                    }
                } else {
                    val dataMap = HashMap<String, Any>()
                    dataMap["name"] = name
                    dataMap["bio"] = bio
                    val databasePath = "Users"
                    val child = getUserConfig().uid
                    pushToDatabase(dataMap, databasePath, child, { _ ->
                        dismissProgressDialog()
                        finish()
                    }, { e ->
                        alertCreator(e.message)
                    })
                }
            }
        }
    }

    private fun push() {
        val s = generateFileNameWithTimestamp()
        val file = avatar ?: run {
            alertCreator("No avatar file found")
            dismissProgressDialog()
            return
        }

        uploadFileToFirebaseStorage("avatar", s, Uri.fromFile(file), { uri ->
            val fileDownloadUrl = uri.toString()
            val dataMap = HashMap<String, Any>()
            dataMap["avatar"] = fileDownloadUrl
            dataMap["name"] = nameEditText.text?.toString().orEmpty()
            dataMap["avatar_name"] = s
            val databasePath = "Users"
            val child = getUserConfig().uid
            pushToDatabase(dataMap, databasePath, child, { _ ->
                dismissProgressDialog()
                finish()
            }, { e ->
                alertCreator(e.message)
            })
        }, { e ->
            alertCreator(e.message)
        })
    }

    // Small helper to create rounded colored drawable (replaces anonymous inner)
    private fun createRoundedDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            cornerRadius = 360.toFloat()
            setColor(color)
        }
    }

}