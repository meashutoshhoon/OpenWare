package jb.openware.app.ui.activity.other

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import jb.openware.app.databinding.ActivityCategoryBinding
import jb.openware.app.ui.adapter.ListProjectAdapter
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.ui.items.Project
import jb.openware.app.ui.items.UserProfile
import jb.openware.app.util.Utils

class CategoryActivity : BaseActivity<ActivityCategoryBinding>(ActivityCategoryBinding::inflate) {

    private val firebase = FirebaseDatabase.getInstance()
    private val usersRef = firebase.getReference("Users")
    private val normalRef = firebase.getReference("projects/normal")
    private val premiumRef = firebase.getReference("projects/premium")

    // Now typed list of Project
    private val projects: MutableList<Project> = ArrayList()
    private val userNames: MutableMap<String, String> = HashMap()

    private var key: String? = null
    private var limit = 0

    private var query1: Query? = null

    private val valueEventListener1 = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            projects.clear()

            for (child in snapshot.children) {
                val project = child.getValue(Project::class.java) ?: continue
                if (project.visibility) projects.add(project)
            }

            projects.reverse()

            binding.loading.visibility = View.GONE
            binding.recyclerview.adapter?.notifyDataSetChanged()
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("CategoryActivity", "Firebase cancelled: ${error.message}")
        }
    }



    override fun init() {
        binding.toolbar.setNavigationOnClickListener(Utils.getBackPressedClickListener(this))


        binding.recyclerview.adapter = ListProjectAdapter(projects, this, userNames, 1)
        binding.recyclerview.layoutManager = LinearLayoutManager(this)

        limit = 30
        key = intent?.getStringExtra("code")

    }

    override fun initLogic() {
        val usersChildListener = object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val user = snapshot.getValue(UserProfile::class.java) ?: return
                val uid = snapshot.key ?: user.uid
                val name = user.name

                userNames[uid] = name
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val user = snapshot.getValue(UserProfile::class.java) ?: return
                val uid = snapshot.key ?: user.uid
                val name = user.name

                userNames[uid] = name
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
            override fun onChildRemoved(snapshot: DataSnapshot) = Unit

            override fun onCancelled(error: DatabaseError) {
                Log.e("USER_LISTENER", "Firebase cancelled → ${error.message}")
            }
        }

        usersRef.addChildEventListener(usersChildListener)


        key?.let { k ->
            when (k) {
                "category" -> binding.toolbar.setTitle(intent?.getStringExtra("title"))
                "editorsChoice" -> binding.toolbar.setTitle("Editor's Choice Projects")
                "verify" -> binding.toolbar.setTitle("Verified Projects")
            }

            if (k == "premium") {
                binding.toolbar.setTitle("Premium Projects")
                binding.recyclerview.adapter = ListProjectAdapter(projects, this, userNames, 0)
                setQuery(premiumRef.limitToLast(limit))
            } else {
                binding.recyclerview.adapter = ListProjectAdapter(projects, this, userNames, 1)
                val title = intent?.getStringExtra("title") ?: ""
                // original used startAt(...).endAt(...); consider equalTo(...) if you want strict equality
                setQuery(
                    normalRef.limitToLast(limit).orderByChild(k).startAt(title).endAt(title)
                )
            }
        }
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    limit += 15
                    key?.let { k ->
                        if (k == "premium") {
                            setQuery(premiumRef.limitToLast(limit))
                        } else {
                            val title = intent?.getStringExtra("title") ?: ""
                            setQuery(
                                normalRef.limitToLast(limit).orderByChild(k).startAt(title)
                                    .endAt(title)
                            )
                        }
                    }
                    binding.loading.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setQuery(newQuery: Query) {
        // remove previous listener if any
        try {
            query1?.removeEventListener(valueEventListener1)
        } catch (_: Exception) { /* ignore if not attached */
        }

        query1 = newQuery
        query1?.addValueEventListener(valueEventListener1)
    }

}