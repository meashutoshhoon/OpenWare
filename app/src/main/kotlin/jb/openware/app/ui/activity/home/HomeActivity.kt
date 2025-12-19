package jb.openware.app.ui.activity.home

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import jb.openware.app.R
import jb.openware.app.databinding.ActivityHomeBinding
import jb.openware.app.databinding.DrawerBinding
import jb.openware.app.ui.activity.drawer.AboutUsActivity
import jb.openware.app.ui.activity.drawer.IntroActivity
import jb.openware.app.ui.activity.drawer.UploadActivity
import jb.openware.app.ui.activity.drawer.logs.ChangeLogActivity
import jb.openware.app.ui.activity.drawer.settings.SettingsActivity
import jb.openware.app.ui.activity.home.fragments.CategoryFragment
import jb.openware.app.ui.activity.home.fragments.HomeFragment
import jb.openware.app.ui.activity.home.fragments.NotificationFragment
import jb.openware.app.ui.activity.profile.ProfileActivity
import jb.openware.app.ui.adapter.ListProjectAdapter
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.ui.common.booleanState
import jb.openware.app.ui.components.BadgeDrawable
import jb.openware.app.ui.components.BottomSheetController
import jb.openware.app.ui.components.DrawableGenerator
import jb.openware.app.ui.components.SearchBarView
import jb.openware.app.ui.items.Project
import jb.openware.app.ui.items.UserProfile
import jb.openware.app.util.Const
import jb.openware.app.util.NEW_USER
import jb.openware.app.util.PreferenceUtil.updateBoolean
import jb.openware.app.util.Utils.shareText
import jb.openware.app.util.defaultReason
import jb.openware.app.util.websiteUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeActivity : BaseActivity<ActivityHomeBinding>(ActivityHomeBinding::inflate) {

    // Firebase references
    private val premiumRef = FirebaseDatabase.getInstance().getReference("projects/premium")
    private val usersRef = FirebaseDatabase.getInstance().getReference("Users")

    // Data holders
    private val projects = mutableListOf<Project>()
    private val userNames = mutableMapOf<String, String>()
    private lateinit var headerBinding: DrawerBinding

    // Recycler / paging
    private var query: Query? = null
    private var limit = 0
    private var page = 0
    private var enable = false

    private lateinit var searchBar: SearchBarView


    private val projectListener = object : ValueEventListener {

        override fun onDataChange(snapshot: DataSnapshot) {
            projects.clear()

            snapshot.children.mapNotNull { it.value as? Map<String, Any> }
                .mapNotNull { runCatching { Project.fromMap(it) }.getOrNull() }
                .let { projects.addAll(it.reversed()) }

            binding.progressbar1.isVisible = false
        }

        override fun onCancelled(error: DatabaseError) {
            toast(error.message)
        }
    }

    private val navActions = mapOf(
        R.id.label_one to { navigateWithDelay<AboutUsActivity>() },
        R.id.label_five to { navigateWithDelay<ChangeLogActivity>() },
        R.id.label_four to { navigateWithDelay<IntroActivity>() },
        R.id.label_two to { sendFeedbackEmail() },
        R.id.label_three to { shareApp() },
        R.id.label_five_ to { openUrl(websiteUrl) },
        R.id.dashboard_item_two to { navigateWithDelay<SettingsActivity>() },
        R.id.dashboard_item to { navigateWithDelay<UploadActivity>() },
    )


    inline fun <reified T : Activity> Activity.navigateWithDelay(
        delayMillis: Long = 300L
    ) {
        lifecycleScope.launch {
            delay(delayMillis)
            startActivity(Intent(this@navigateWithDelay, T::class.java))
        }
    }


    override fun init() {
        headerBinding = DrawerBinding.bind(binding.navigationView.getHeaderView(0))

        searchBar = SearchBarView(this, binding.toolbar, binding.searchView).apply {
            setEditTextEnabled(false)

            setEditTextClickListener {
                openActivity<SearchActivity>()
            }

            setToolbarClickListener {
                if (enable) {
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                }
            }

            setMenuClickListener { item ->
                if (item.itemId == R.id.profile && enable) {
                    putPrefString(name = "developer", key = "uid", value = getUid())
                    openActivity<ProfileActivity>()
                }
                false
            }
        }

        binding.navigationView.setNavigationItemSelectedListener {
            navActions[it.itemId]?.invoke()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (!enable) return@setOnItemSelectedListener false

            when (item.itemId) {
                R.id.item_1 -> {
                    showViews(binding.toolbarCard, binding.home, binding.fab)
                    hideViews(binding.premium, binding.notifications, binding.category)
                    page = 0
                    true
                }

                R.id.item_2 -> {
                    showViews(binding.premium, binding.toolbarCard, binding.fab)
                    hideViews(binding.notifications, binding.home, binding.category)
                    page = 1
                    true
                }

                R.id.item_3 -> {
                    showViews(binding.notifications)
                    hideViews(
                        binding.premium,
                        binding.home,
                        binding.category,
                        binding.toolbarCard,
                        binding.fab
                    )
                    page = 2
                    true
                }

                R.id.item_4 -> {
                    showViews(binding.category)
                    hideViews(
                        binding.premium,
                        binding.home,
                        binding.notifications,
                        binding.toolbarCard,
                        binding.fab
                    )
                    page = 3
                    true
                }

                else -> false
            }
        }


        // More Methods
        setDrawerLocked(false)
        runSavedData()

        // Header text
        headerBinding.textView.text = userConfig.name
        headerBinding.version.text = userConfig.email

        // Profile click
        headerBinding.profileLayout.setOnClickListener {
            putPrefString(name = "developer", key = "uid", value = getUid())

            openActivity<ProfileActivity>()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Views visibility
        showViews(binding.home, binding.toolbar, headerBinding.circleimageview1)
        hideViews(
            binding.premium,
            binding.notifications,
            binding.category,
            binding.progressbar1,
            headerBinding.imVerified,
            headerBinding.linearWord
        )


    }

    override fun initLogic() {
        if (!NEW_USER.booleanState) {
            lifecycleScope.launch {
                delay(650)
                openActivity<IntroActivity>()
                NEW_USER.updateBoolean(false)
            }
        }

        setupFragments()

        binding.fab.setOnClickListener {
            lifecycleScope.launch {
                delay(150)
                openActivity<UploadActivity>()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finishAffinity()
                }
            }
        })

    }

    private fun setupFragments() {
        replaceFragment(binding.home, HomeFragment())
        replaceFragment(binding.notifications, NotificationFragment())
        replaceFragment(binding.category, CategoryFragment())
        setUpPremiumClass()
    }

    private fun setUpPremiumClass() {
        binding.recyclerview1.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = ListProjectAdapter(projects, this@HomeActivity, userNames, 0)
        }

        enable = true
        setDrawerLocked(true)
        showViews(headerBinding.imVerified)

        val userListener = object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                handleUserSnapshot(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                handleUserSnapshot(snapshot)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) = Unit
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
            override fun onCancelled(error: DatabaseError) = Unit
        }

        usersRef.addChildEventListener(userListener)

        limit = 30
        query = premiumRef.limitToLast(limit)
        query?.addValueEventListener(projectListener)
    }

//    private fun handleUserSnapshot(snapshot: DataSnapshot) {
//        val user = snapshot.getValue(UserProfile::class.java) ?: return
//
//        val uid = user.uid
//        val nameValue = user.name
//
//        userNames[uid] = nameValue
//
//        if (snapshot.key != getUid()) return
//
//        updateCurrentUserUI(user)
//    }

    private fun updateCurrentUserUI(user: UserProfile) {
        userConfig.apply {
            name = user.name
            profileUrl = user.avatar
            badge = user.badge.toInt()
        }

        headerBinding.textView.text = user.name

        updateAvatarUI(user.avatar, user.name, user.color.toColorInt())
        updateBadgeUI(user.badge.toInt(), user.verified.toBoolean())

        if (user.block.toBoolean()) {
            showBlockedDialog(user.reason)
        }
    }

    private fun updateAvatarUI(avatar: String, name: String, color: Int) {
        if (avatar == "none") {
            showViews(headerBinding.linearWord)
            hideViews(headerBinding.circleimageview1)
            headerBinding.txWord.text = name.first().toString()

            headerBinding.linearWord.background = GradientDrawable().apply {
                cornerRadius = 360f
                setColor(color)
            }

            renderProfileImage(
                DrawableGenerator.generateDrawable(
                    this, color, name.first().toString()
                )
            )
        } else {
            if (!isDestroyed) {
                Glide.with(this).load(avatar.toUri()).into(headerBinding.circleimageview1)

                loadImage(avatar)
            }
            showViews(headerBinding.circleimageview1)
            hideViews(headerBinding.linearWord)
        }
    }

    private fun updateBadgeUI(badge: Int, verified: Boolean) {
        if (badge == 0) {
            if (verified) {
                headerBinding.imVerified.setImageResource(R.drawable.verify)
                headerBinding.imVerified.setColorFilter(0xFF00C853.toInt(), PorterDuff.Mode.SRC_IN)
                headerBinding.textView.setTextColor(0xFF00C853.toInt())
                showViews(headerBinding.imVerified)
            } else {
                hideViews(headerBinding.imVerified)
            }
        } else {
            BadgeDrawable(this).setBadge(badge.toString(), headerBinding.imVerified)
            showViews(headerBinding.imVerified)
        }
    }

    private fun showBlockedDialog(reason: String?) {
        val bottomSheet = BottomSheetController(this)
        bottomSheet.create(
            layoutRes = R.layout.block_cell, cancelable = false
        )
        val message = bottomSheet.find<TextView>(R.id.t1)
        val button = bottomSheet.find<MaterialButton>(R.id.b1)

        message.text = reason ?: defaultReason
        button.setOnClickListener { finishAffinity() }

        bottomSheet.show()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        binding.recyclerview1.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView, dx: Int, dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(1)) {
                    limit += 15
                    query = premiumRef.limitToLast(limit)
                    query?.addValueEventListener(projectListener)
                    binding.progressbar1.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setDrawerLocked(locked: Boolean) {
        binding.drawerLayout.setDrawerLockMode(
            if (locked) DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            else DrawerLayout.LOCK_MODE_UNLOCKED
        )
    }

    private fun renderProfileImage(drawable: Drawable) {
        lifecycleScope.launch {
            searchBar.menuIcon?.icon = drawable
        }
    }

    private fun sendFeedbackEmail() {

        val body = buildString {
            appendLine("Hello OpenWare Support,")
            appendLine()
            appendLine("My Name :- ${userConfig.name}")
            appendLine("Email :- ${userConfig.email}")
            appendLine()
            append("I just want to give you feedback:-")
        }

        sendEmail(
            subject = "OpenWare Feedback", body = body
        )
    }

    private fun shareApp() {
        val message = """
        Hey, download this awesome app!
        $websiteUrl
    """.trimIndent()

        shareText(message)
    }

    private fun runSavedData() {
        when (val url = userConfig.profileUrl) {
            "none" -> renderProfileImage(
                DrawableGenerator.generateDrawable(
                    this, "#006493".toColorInt(), userConfig.name.firstOrNull()?.toString() ?: "?"
                )
            )

            else -> loadImage(url)
        }
    }

    private fun loadImage(profileUrl: String) {
        Glide.with(this).load(profileUrl).centerCrop().circleCrop().sizeMultiplier(0.5f)
            .listener(object : com.bumptech.glide.request.RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    renderProfileImage(resource!!)
                    return false
                }

            }).submit()
    }

    fun replaceFragment(
        container: View, fragment: Fragment
    ) {
        if (isFinishing) return

        supportFragmentManager.commit {
            replace(container.id, fragment)
        }
    }

}