package jb.openware.app.ui.activity.profile

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.content.edit
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import jb.openware.app.R
import jb.openware.app.databinding.ActivityProfileBinding
import jb.openware.app.ui.activity.login.LoginActivity
import jb.openware.app.ui.activity.project.ProjectViewActivity
import jb.openware.app.ui.adapter.BannerProjectAdapter
import jb.openware.app.ui.adapter.BaseProjectAdapter
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.ui.common.HapticFeedback.slightHapticFeedback
import jb.openware.app.ui.components.BadgeDrawable
import jb.openware.app.ui.components.BottomSheetController
import jb.openware.app.ui.items.Project
import jb.openware.app.ui.items.UserProfile
import jb.openware.app.util.UserConfig
import jb.openware.app.util.Utils
import jb.openware.app.util.Utils.shareText
import jb.openware.app.util.websiteUrl
import jb.openware.imageviewer.ImageViewer

sealed class UiState {
    object Content : UiState()
    object LoadingList : UiState()
    object Empty : UiState()
    object FullLoading : UiState()
}

class ProfileActivity : BaseActivity<ActivityProfileBinding>(ActivityProfileBinding::inflate) {

    private val firebase by lazy { FirebaseDatabase.getInstance() }
    private val usersRef by lazy { firebase.getReference("Users") }
    private val normalProjectsRef by lazy { firebase.getReference("projects/normal") }
    private val premiumProjectsRef by lazy { firebase.getReference("projects/premium") }

    private val normalProjects = mutableListOf<Project>()
    private val verifiedProjects = mutableListOf<Project>()
    private val mostProjects = mutableListOf<Project>()
    private val editorProjects = mutableListOf<Project>()
    private val privateProjects = mutableListOf<Project>()
    private val premiumProjects = mutableListOf<Project>()

    private var premiumChildListener: ChildEventListener? = null
    private var userChildEventListener: ChildEventListener? = null
    private var normalChildListener: ChildEventListener? = null

    private lateinit var userProfile: UserProfile
    private var userProjectCount = 0

    override fun init() {
        binding.refer.performTransition()
        render(UiState.FullLoading)

        attachUserListener()
        attachPremiumListener()

        checkInitialProjectsState()
        attachNormalListener()
    }

    override fun initLogic() {
        setUpDivider()

        binding.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.logout.setOnClickListener {
            showLogoutDialog()
        }

        binding.edit.setOnClickListener {
            startActivity(
                Intent(this, ProfileEditActivity::class.java).apply {
                    putExtra("name", userProfile.name)
                    putExtra("url", userProfile.avatar)
                    putExtra("color", userProfile.color)
                    putExtra("bio", userProfile.bio)
                })
        }

        binding.share.setOnClickListener {
            shareText("${websiteUrl}u/${userProfile.name}")
        }

        binding.badge.setOnClickListener {
            it.slightHapticFeedback()
            showBadgeBottomSheet()
        }

        binding.circleimageview1.setOnClickListener {
            openProfileImage(binding.circleimageview1)
        }

        binding.allProjects.setup(isGrid = true)
        binding.premium.setup()
        binding.verifyR.setup()
        binding.editorR.setup()
        binding.mostR.setup()
        binding.privateProjectsR.setup()

        hideViews(
            binding.premiumLayout,
            binding.verified,
            binding.editor,
            binding.privateL,
            binding.most,
            binding.allP
        )

    }

    private fun attachUserListener() {
        userChildEventListener = object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, prev: String?) {
                handleUserSnapshot(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, prev: String?) {
                handleUserSnapshot(snapshot)
            }

            override fun onChildMoved(snapshot: DataSnapshot, prev: String?) = Unit
            override fun onChildRemoved(snapshot: DataSnapshot) = Unit
            override fun onCancelled(error: DatabaseError) = Unit
        }

        usersRef.addChildEventListener(userChildEventListener!!)
    }

    private fun handleUserSnapshot(snapshot: DataSnapshot) {
        val currentUid = getPrefString("developer", "uid", "")

        if (snapshot.key == currentUid) {
            val user = snapshot.getValue(UserProfile::class.java) ?: return
            renderUserProfile(user, getUid() == currentUid)
        }
    }

    private fun renderUserProfile(user: UserProfile, isUser: Boolean) {
        this.userProfile = user

        if (user.badge != "0") {
            BadgeDrawable(this).setBadge(user.badge, binding.badgeImg)
            showViews(binding.badge)
        } else {
            hideViews(binding.badge)
        }

        if (isUser) {
            binding.logout.show()
            binding.edit.show()
        } else {
            binding.logout.hide()
            binding.edit.hide()
        }

        binding.usernameText.text = user.name
        binding.totalProjects.text = formatNumber(user.projects)
        binding.totalDownloads.text = formatNumber(user.downloads)
        binding.likes.text = formatNumber(user.likes)

        binding.imVerified.isVisible = user.verified.toBoolean()

        if (user.bio.isNotBlank()) {
            delayTask {
                binding.profileLayout.performTransition()
                binding.roleText.isVisible = true
                binding.roleText.text = user.bio
            }
        }

        if (user.avatar == "none") {
            binding.linearWord.show()
            binding.circleimageview1.hide()
            binding.txWord.text = user.name.firstOrNull()?.toString().orEmpty()
        } else {
            Glide.with(this).load(user.avatar.toUri()).into(binding.circleimageview1)
            binding.circleimageview1.show()
            binding.linearWord.hide()
        }

        binding.linearWord.background = GradientDrawable().apply {
            cornerRadius = 360f
            setColor(user.color.toColorInt())
        }
    }

    private fun attachPremiumListener() {
        premiumChildListener = object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, prev: String?) {
                val currentUid = getPrefString("developer", "uid", "")

                val project = snapshot.getValue(Project::class.java) ?: return

                if (project.uid == currentUid && project.visibility) {

                    premiumProjects.add(0, project)

                    if (binding.premium.adapter == null) {
                        binding.premium.adapter =
                            BannerProjectAdapter(premiumProjects, this@ProfileActivity, 0)
                    } else {
                        binding.premium.adapter?.notifyDataSetChanged()
                    }

                    showViews(binding.premiumLayout)
                } else {
                    hideViews(binding.premiumLayout)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, prev: String?) {
                val updated = snapshot.getValue(Project::class.java) ?: return
                val index = premiumProjects.indexOfFirst { it.uid == updated.uid }

                if (index != -1) {
                    premiumProjects[index] = updated
                    binding.premium.adapter?.notifyDataSetChanged()
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val removed = snapshot.getValue(Project::class.java) ?: return
                val changed = premiumProjects.removeAll { it.uid == removed.uid }

                if (changed) {
                    binding.premium.adapter?.notifyDataSetChanged()
                }

                if (premiumProjects.isEmpty()) {
                    hideViews(binding.premiumLayout)
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, prev: String?) = Unit
            override fun onCancelled(error: DatabaseError) = Unit
        }

        premiumProjectsRef.addChildEventListener(premiumChildListener!!)
    }

    private fun checkInitialProjectsState() {
        val currentUid = getPrefString("developer", "uid", "")

        normalProjectsRef
            .orderByChild("uid")
            .equalTo(currentUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userProjectCount = snapshot.childrenCount.toInt()
                        render(UiState.Content)
                    } else {
                        userProjectCount = 0
                        render(UiState.Empty)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    render(UiState.Empty)
                }
            }
        )
    }

    private fun attachNormalListener() {
        normalChildListener = object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, prev: String?) {
                val currentUid = getPrefString("developer", "uid", "")
                val project = snapshot.getValue(Project::class.java) ?: return

                if (project.uid == currentUid) {
                    userProjectCount++
                    processProject(project)

                    // ≥ 1 project → Content
                    render(UiState.Content)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, prev: String?) {
                val updated = snapshot.getValue(Project::class.java) ?: return

                val index = normalProjects.indexOfFirst { it.key == updated.key }
                if (index != -1) {
                    normalProjects[index] = updated
                    processProject(updated, replace = true)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val currentUid = getPrefString("developer", "uid", "")
                val removed = snapshot.getValue(Project::class.java) ?: return

                if (removed.uid == currentUid) {
                    userProjectCount--

                    // 0 projects → Empty
                    if (userProjectCount <= 0) {
                        userProjectCount = 0
                        render(UiState.Empty)
                    }
                }

                normalProjects.removeAll { it.key == removed.key }
            }

            override fun onChildMoved(snapshot: DataSnapshot, prev: String?) = Unit
            override fun onCancelled(error: DatabaseError) = Unit
        }

        normalProjectsRef.addChildEventListener(normalChildListener!!)
    }

    private fun processProject(project: Project, replace: Boolean = false) {
        if (!replace && normalProjects.none { it.key == project.key }) {
            normalProjects.add(0, project)
        }

        updateVerified(project)
        updateEditor(project)
        updatePrivate(project)
        updateAll()
        updateMost(project)

        refreshAdapters()
    }

    private fun updateVerified(project: Project) {
        if (project.visibility && project.verified) {
            verifiedProjects.add(0, project)

            if (binding.verifyR.adapter == null) {
                binding.verifyR.adapter = BaseProjectAdapter(verifiedProjects) { project ->
                    getSharedPreferences("developer", MODE_PRIVATE).edit {
                        putString("type", "Free")
                    }

                    startActivity(
                        Intent(this, ProjectViewActivity::class.java).apply {
                            putExtra("key", project.key)
                            putExtra("uid", project.uid)
                        })
                }
            } else {
                binding.verifyR.adapter?.notifyDataSetChanged()
            }

            if (verifiedProjects.isNotEmpty()) {
                showViews(binding.verified)
            } else {
                hideViews(binding.verified)
            }
        }
    }

    private fun updateEditor(project: Project) {
        if (project.visibility && project.editorsChoice) {
            editorProjects.add(0, project)

            if (binding.editorR.adapter == null) {
                binding.editorR.adapter = BaseProjectAdapter(editorProjects) { project ->
                    getSharedPreferences("developer", MODE_PRIVATE).edit {
                        putString("type", "Free")
                    }

                    startActivity(
                        Intent(this, ProjectViewActivity::class.java).apply {
                            putExtra("key", project.key)
                            putExtra("uid", project.uid)
                        })
                }
            } else {
                binding.editorR.adapter?.notifyDataSetChanged()
            }

            if (editorProjects.isNotEmpty()) {
                showViews(binding.editor)
            } else {
                hideViews(binding.editor)
            }
        }
    }

    private fun updatePrivate(project: Project) {
        if (!project.visibility) {
            privateProjects.add(0, project)

            if (binding.privateProjectsR.adapter == null) {
                binding.privateProjectsR.adapter = BaseProjectAdapter(privateProjects) { project ->
                    getSharedPreferences("developer", MODE_PRIVATE).edit {
                        putString("type", "Free")
                    }

                    startActivity(
                        Intent(this, ProjectViewActivity::class.java).apply {
                            putExtra("key", project.key)
                            putExtra("uid", project.uid)
                        })
                }
            } else {
                binding.privateProjectsR.adapter?.notifyDataSetChanged()
            }

            if (privateProjects.isNotEmpty()) {
                showViews(binding.privateL)
            } else {
                hideViews(binding.privateL)
            }
        }
    }

    private fun updateAll() {
        if (binding.allProjects.adapter == null) {
            binding.allProjects.adapter = BaseProjectAdapter(normalProjects) { project ->
                getSharedPreferences("developer", MODE_PRIVATE).edit {
                    putString("type", "Free")
                }

                startActivity(
                    Intent(this, ProjectViewActivity::class.java).apply {
                        putExtra("key", project.key)
                        putExtra("uid", project.uid)
                    })
            }
        } else {
            binding.allProjects.adapter?.notifyDataSetChanged()
        }

        if (normalProjects.isNotEmpty()) {
            showViews(binding.allP)
        } else {
            hideViews(binding.allP)
        }
    }

    private fun updateMost(project: Project) {
        if (!project.visibility) return

        mostProjects.add(project)

        mostProjects.sortByDescending {
            it.likes.toIntOrNull() ?: 0
        }

        val top = mostProjects.firstOrNull()

        if (top == null || top.likes == "0") {
            hideViews(findViewById(R.id.most))
            return
        }

        if (binding.mostR.adapter == null) {
            binding.mostR.adapter = BannerProjectAdapter(listOf(top), this, 1)
        } else {
            binding.mostR.adapter = BannerProjectAdapter(listOf(top), this, 1)
        }

        binding.refer.performTransition()

        delayTask {
            showViews(binding.most)
        }
    }

    private fun refreshAdapters() {

        /* ---------- ALL PROJECTS ---------- */
        if (binding.allProjects.adapter == null) {
            binding.allProjects.adapter = BaseProjectAdapter(normalProjects) { project ->
                getSharedPreferences("developer", MODE_PRIVATE).edit {
                    putString("type", "Free")
                }

                startActivity(
                    Intent(this, ProjectViewActivity::class.java).apply {
                        putExtra("key", project.key)
                        putExtra("uid", project.uid)
                    })
            }
        }
        binding.allProjects.adapter?.notifyDataSetChanged()

        if (normalProjects.isNotEmpty()) {
            showViews(binding.allP)
        } else {
            hideViews(binding.allP)
        }

        /* ---------- VERIFIED ---------- */
        if (verifiedProjects.isNotEmpty()) {
            if (binding.verifyR.adapter == null) {
                binding.verifyR.adapter = BaseProjectAdapter(verifiedProjects) { project ->
                    getSharedPreferences("developer", MODE_PRIVATE).edit {
                        putString("type", "Free")
                    }

                    startActivity(
                        Intent(this, ProjectViewActivity::class.java).apply {
                            putExtra("key", project.key)
                            putExtra("uid", project.uid)
                        })
                }
            }
            binding.verifyR.adapter?.notifyDataSetChanged()
            showViews(binding.verified)
        } else {
            hideViews(binding.verified)
        }

        /* ---------- EDITOR ---------- */
        if (editorProjects.isNotEmpty()) {
            if (binding.editorR.adapter == null) {
                binding.editorR.adapter = BaseProjectAdapter(editorProjects) { project ->
                    getSharedPreferences("developer", MODE_PRIVATE).edit {
                        putString("type", "Free")
                    }

                    startActivity(
                        Intent(this, ProjectViewActivity::class.java).apply {
                            putExtra("key", project.key)
                            putExtra("uid", project.uid)
                        })
                }
            }
            binding.editorR.adapter?.notifyDataSetChanged()
            showViews(binding.editor)
        } else {
            hideViews(binding.editor)
        }

        /* ---------- PRIVATE ---------- */
        if (privateProjects.isNotEmpty()) {
            if (binding.privateProjectsR.adapter == null) {
                binding.privateProjectsR.adapter = BaseProjectAdapter(privateProjects) { project ->
                    getSharedPreferences("developer", MODE_PRIVATE).edit {
                        putString("type", "Free")
                    }

                    startActivity(
                        Intent(this, ProjectViewActivity::class.java).apply {
                            putExtra("key", project.key)
                            putExtra("uid", project.uid)
                        })
                }
            }
            binding.privateProjectsR.adapter?.notifyDataSetChanged()
            showViews(binding.privateL)
        } else {
            hideViews(binding.privateL)
        }

        /* ---------- MOST ---------- */
        if (mostProjects.isNotEmpty()) {
            val top = mostProjects.first()

            if (top.likes == "0") {
                hideViews(binding.most)
            } else {
                binding.mostR.adapter = BannerProjectAdapter(listOf(top), this, 1)

                binding.refer.performTransition()
                showViews(binding.most)
            }
        } else {
            hideViews(binding.most)
        }
    }

    private fun openProfileImage(anchorView: ImageView) {
        val imageUrl = userProfile.avatar
        if (imageUrl.isBlank()) return

        ImageViewer.Builder(
            this, listOf(imageUrl)
        ) { imageView, image ->
            Glide.with(this).load(image).transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(0xFFD3D3D3.toInt().toDrawable()).into(imageView)
        }.withStartPosition(0).withHiddenStatusBar(true).allowZooming(true)
            .allowSwipeToDismiss(true).withTransitionFrom(anchorView).withDismissListener {
                anchorView.isVisible = true
            }.show()
    }

    private fun showBadgeBottomSheet() {
        val bottomSheetController = BottomSheetController(this)

        bottomSheetController.create(R.layout.badge_layout)
        bottomSheetController.show()
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(
            this,
            com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered
        ).setTitle("Logout Confirmation").setMessage(
            "Are you sure you want to log out of your account? " + "This action will end your current session."
        ).setIcon(R.drawable.logout).setPositiveButton("Log Out") { _, _ ->
            performLogout()
        }.setNegativeButton("Cancel", null).show()
    }

    override fun onDestroy() {
        userChildEventListener?.let { usersRef.removeEventListener(it) }
        premiumChildListener?.let { premiumProjectsRef.removeEventListener(it) }
        normalChildListener?.let { normalProjectsRef.removeEventListener(it) }
        super.onDestroy()
    }

    private fun performLogout() {
        UserConfig(this).logout()
        FirebaseAuth.getInstance().signOut()

        getSharedPreferences("developer", MODE_PRIVATE).edit { remove("uid").remove("type") }

        openActivity<LoginActivity>()
    }

    private fun render(state: UiState) {
        TransitionManager.beginDelayedTransition(binding.refer)

        when (state) {

            UiState.Content -> {
                binding.tools.show()
                binding.loading.hide()
                binding.content.show()

                binding.projects.show()
                binding.textview2.hide()
                binding.loading2.hide()
                binding.share.show()
            }

            UiState.LoadingList -> {
                binding.tools.show()
                binding.loading.hide()
                binding.content.show()

                binding.projects.hide()
                binding.textview2.hide()
                binding.loading2.show()
                binding.share.show()
            }

            UiState.Empty -> {
                binding.tools.show()
                binding.loading.hide()
                binding.content.show()

                binding.projects.hide()
                binding.loading2.hide()
                binding.textview2.show()
                binding.share.show()
            }

            UiState.FullLoading -> {
                binding.loading.show()
                binding.content.hide()
                binding.tools.hide()
            }
        }
    }

    private fun setParameters(state: Int) {
        when (state) {
            1 -> {
                binding.tools.show()
                binding.projects.show()
                binding.textview2.hide()
                binding.loading2.hide()
            }

            2 -> {
                binding.projects.hide()
                binding.textview2.hide()
                binding.tools.show()
                binding.loading2.show()
            }

            3 -> {
                binding.projects.hide()
                binding.loading2.hide()
                binding.tools.show()
                binding.textview2.show()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        TransitionManager.beginDelayedTransition(binding.refer)

        if (isLoading) {
            binding.loading.show()
            binding.content.hide()
            binding.share.hide()
            binding.logout.hide()
            binding.edit.hide()
        } else {
            binding.content.show()
            binding.share.show()
            binding.logout.show()
            binding.edit.show()
            binding.loading.hide()
        }
    }

    private fun RecyclerView.setup(isGrid: Boolean = false, spanCount: Int = 3) {
        layoutManager = if (isGrid) GridLayoutManager(context, spanCount)
        else LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }

    private fun setUpDivider() {
        binding.divider.hide()

        binding.nestedScrollView.isVerticalScrollBarEnabled = false
        binding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            binding.divider.isVisible = scrollY > 0
        }
    }

    fun ViewGroup.performTransition(durationMillis: Long = 400L) {
        val transition = AutoTransition().apply {
            duration = durationMillis
            interpolator = DecelerateInterpolator()
        }
        TransitionManager.beginDelayedTransition(this, transition)
    }

}