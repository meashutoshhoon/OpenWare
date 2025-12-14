package jb.openware.app.ui.activity.home

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import jb.openware.app.R
import jb.openware.app.databinding.ActivityHomeExtendBinding
import jb.openware.app.ui.activity.project.ProjectViewActivity
import jb.openware.app.ui.adapter.BaseProjectAdapter
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.ui.components.DrawableGenerator
import jb.openware.app.ui.components.SearchBarView
import jb.openware.app.ui.components.SearchEditText
import jb.openware.app.ui.items.Project
import jb.openware.app.util.Utils
import kotlinx.coroutines.launch

class MoreProjectsActivity :
    BaseActivity<ActivityHomeExtendBinding>(ActivityHomeExtendBinding::inflate) {

    private val normalRef = FirebaseDatabase.getInstance().getReference("projects/normal")

    private val projects = mutableListOf<Project>()

    private lateinit var searchBar: SearchBarView

    private var query: Query? = null
    private var limit = 30
    private var key: String = "all"

    private lateinit var adapter: BaseProjectAdapter

    override fun init() {
        setupSearchBar(binding.toolbar, binding.searchView)
        setupProfileIcon()
    }

    override fun initLogic() {
        binding.recyclerview.layoutManager = GridLayoutManager(this, 3)

        adapter = BaseProjectAdapter(projects) { project ->
            getSharedPreferences("developer", MODE_PRIVATE).edit {
                putString("type", "Free")
            }

            startActivity(
                Intent(this, ProjectViewActivity::class.java).apply {
                    putExtra("key", project.key)
                    putExtra("uid", project.uid)
                })
        }

        binding.recyclerview.adapter = adapter

        key = getPrefString(key = "id", default = "all")
        loadProjects()
    }

    private fun loadProjects() {
        showLoading(true)

        query = when (key) {
            "editors_choice" -> normalRef.limitToLast(limit).orderByChild("editors_choice")
                .equalTo("true")

            "like" -> normalRef.limitToLast(limit)

            else -> normalRef.limitToLast(limit)
        }

        query?.addValueEventListener(projectListener)
    }

    private val projectListener = object : ValueEventListener {
        @SuppressLint("NotifyDataSetChanged")
        override fun onDataChange(snapshot: DataSnapshot) {
            projects.clear()

            snapshot.children.mapNotNull { it.value as? HashMap<String, Any> }
                .mapNotNull { runCatching { Project.fromMap(it) }.getOrNull() }
                .filter { it.visible }.let { list ->
                    if (key == "like") {
                        projects.addAll(
                            list.sortedByDescending {
                                it.likes.toIntOrNull() ?: 0
                            })
                    } else {
                        projects.addAll(list.reversed())
                    }
                }

            adapter.notifyDataSetChanged()
            showLoading(false)
        }

        override fun onCancelled(error: DatabaseError) {
            showLoading(false)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (!rv.canScrollVertically(1)) {
                    limit += 15
                    loadProjects()
                    binding.progressbar.isVisible = true
                }
            }
        })
    }

    private fun setupSearchBar(
        toolbar: MaterialToolbar, editText: SearchEditText
    ) {
        searchBar = SearchBarView(this@MoreProjectsActivity, toolbar, editText).apply {
            setEditTextEnabled(false)
            setEditTextClickListener {
                openActivity<SearchActivity>()
            }
            setToolbarClickListener { Utils.getBackPressedClickListener(this@MoreProjectsActivity) }
            setMenuClickListener {
                if (it.itemId == R.id.profile) {
                    getSharedPreferences("developer", MODE_PRIVATE).edit {
                        putString("uid", getUid())
                    }
                    startActivity(ProfileActivity::class.java)
                }
                false
            }
        }
    }

    private fun setupProfileIcon() {
        val url = userConfig.profileUrl

        if (url == "none") {
            renderProfileImage(
                DrawableGenerator.generateDrawable(
                    this, "#006493".toColorInt(), userConfig.name.first().toString()
                )
            )
        } else {
            loadImage(url)
        }
    }

    private fun renderProfileImage(drawable: Drawable) {
        lifecycleScope.launch {
            searchBar.menuIcon?.icon = drawable
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


    private fun showLoading(show: Boolean) {
        TransitionManager.beginDelayedTransition(binding.background)
        binding.scroll.isVisible = !show
        binding.loading.isVisible = show
    }


}