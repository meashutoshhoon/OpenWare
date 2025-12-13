package jb.openware.app.ui.activity.profile

import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.text.TextUtils
import android.widget.LinearLayout
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import jb.openware.app.R
import jb.openware.app.databinding.ActivityProfileEditBinding
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.ui.components.BottomSheetController
import jb.openware.app.util.ImageUtil
import jb.openware.app.util.Utils
import java.io.File

class ProfileEditActivity :
    BaseActivity<ActivityProfileEditBinding>(ActivityProfileEditBinding::inflate) {

    private val firebase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = firebase.getReference("Users")

    private val usernames = ArrayList<String>()
    private var url: String = "none"
    private var namePre: String = ""
    private var avatar: File? = null
    private var aBoolean: Boolean = false

    private var name: String? = ""
    private lateinit var color: String

    private val userMapType = object : GenericTypeIndicator<HashMap<String, Any?>>() {}

    override fun init() {
        name = intent.getStringExtra("name")
        color = intent.getStringExtra("color") ?: "#000000"
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
                        profilePath: String, imageFileName: String, imageUri: Uri
                    ) {
                        aBoolean = true
                        avatar = ImageUtil.compressImage(this@ProfileEditActivity, imageUri, 40)
                        hideViews(binding.linearWord)
                        showViews(binding.circleimageview)
                        Glide.with(this@ProfileEditActivity).load(Uri.fromFile(avatar))
                            .into(binding.circleimageview)
                    }

                })
            } else {
                val bottomSheet = BottomSheetController(this)
                bottomSheet.create(R.layout.sheet_profile)
                val bt1 = bottomSheet.find<LinearLayout>(R.id.bt1)
                val bt2 = bottomSheet.find<LinearLayout>(R.id.bt2)

                bt1.setOnClickListener {
                    pickSinglePhoto(object : ImagePickedListener {
                        override fun onImagePicked(
                            profilePath: String, imageFileName: String, imageUri: Uri
                        ) {
                            aBoolean = true
                            avatar = ImageUtil.compressImage(this@ProfileEditActivity, imageUri, 40)
                            hideViews(binding.linearWord)
                            showViews(binding.circleimageview)
                            Glide.with(this@ProfileEditActivity).load(Uri.fromFile(avatar))
                                .into(binding.circleimageview)
                            bottomSheet.dismiss()
                        }
                    })
                }

                bt2.setOnClickListener {
                    showProgressDialog()
                    val currentUserId = getUid()
                    val usersDatabaseRef = FirebaseDatabase.getInstance().getReference("Users")

                    val valueEventListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.key == currentUserId) {
                                val userData = dataSnapshot.getValue(userMapType)
                                if (userData != null) {
                                    val avatarName = userData["avatar_name"]?.toString()
                                    if (!avatarName.isNullOrBlank()) {
                                        val storageRef: StorageReference =
                                            FirebaseStorage.getInstance().getReference("avatar")
                                                .child(avatarName)

                                        storageRef.delete().addOnSuccessListener {
                                            val hashMap = HashMap<String, Any>()
                                            hashMap["avatar"] = "none"
                                            hashMap["avatar_name"] = "none"
                                            pushToDatabase(hashMap, "Users", getUid(), {
                                                url = "none"
                                                aBoolean = false
                                                hideViews(binding.circleimageview)
                                                showViews(binding.linearWord)
                                                binding.linearWord.background =
                                                    createRoundedDrawable(
                                                        color.toColorInt()
                                                    )
                                                name?.let {
                                                    if (it.isNotEmpty()) binding.txWord.text =
                                                        it.substring(0, 1)
                                                }
                                                toast("Profile picture removed")
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
                                        pushToDatabase(hashMap, "Users", getUid(), {
                                            url = "none"
                                            aBoolean = false
                                            hideViews(binding.circleimageview)
                                            showViews(binding.linearWord)
                                            binding.linearWord.background = createRoundedDrawable(
                                                color.toColorInt()
                                            )
                                            name?.let {
                                                if (it.isNotEmpty()) binding.txWord.text =
                                                    it.substring(0, 1)
                                            }
                                            toast("Profile picture removed")
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
                    usersDatabaseRef.child(currentUserId)
                        .addListenerForSingleValueEvent(valueEventListener)
                    bottomSheet.dismiss()
                }
                bottomSheet.show()
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

        val name = binding.name.text.toString()
        val bio = binding.bio.text.toString()
        val currentUserId = getUid()
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
                MaterialAlertDialogBuilder(this).setTitle("Alert")
                    .setMessage("Username already taken.").setPositiveButton("Ok", null).show()
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
                                        val storageRef =
                                            FirebaseStorage.getInstance().getReference("avatar")
                                                .child(value)
                                        storageRef.delete().addOnSuccessListener {
                                            push()
                                        }.addOnFailureListener {
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
                        usersDatabaseRef.child(currentUserId)
                            .addListenerForSingleValueEvent(valueEventListener)
                    } else {
                        push()
                    }
                } else {
                    val dataMap = HashMap<String, Any>()
                    dataMap["name"] = name
                    dataMap["bio"] = bio
                    val databasePath = "Users"
                    val child = userConfig.uid
                    pushToDatabase(dataMap, databasePath, child, {
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

        uploadFileToFirebaseStorage("avatar", s, file.toUri(), { uri ->
            val fileDownloadUrl = uri.toString()
            val dataMap = HashMap<String, Any>()
            dataMap["avatar"] = fileDownloadUrl
            dataMap["name"] = binding.name.text.toString()
            dataMap["avatar_name"] = s
            val databasePath = "Users"
            val child = userConfig.uid
            pushToDatabase(dataMap, databasePath, child, {
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