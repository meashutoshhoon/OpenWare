package jb.openware.app.ui.activity.other

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import jb.openware.app.databinding.ActivityCategoryBinding
import jb.openware.app.ui.adapter.ListProjectAdapter
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.ui.items.Project
import jb.openware.app.util.Utils

class CategoryActivity : BaseActivity<ActivityCategoryBinding>(ActivityCategoryBinding::inflate) {

    private val firebase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = firebase.getReference("Users")
    private val normalRef: DatabaseReference = firebase.getReference("projects/normal")
    private val premiumRef: DatabaseReference = firebase.getReference("projects/premium")

    // Now typed list of Project
    private val projects: MutableList<Project> = ArrayList()
    private val userNames: MutableMap<String, String> = HashMap()

    private var key: String? = null
    private var limit = 0.0

    private var query1: Query? = null

    private val valueEventListener1 = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            try {
                projects.clear()
                val ind = object : GenericTypeIndicator<HashMap<String, Any>>() {}
                for (child in snapshot.children) {
                    val raw = child.getValue(ind) ?: continue
                    val project = try {
                        Project.fromMap(raw)
                    } catch (t: Throwable) {
                        // skip malformed entry but continue processing others
                        t.printStackTrace()
                        continue
                    }
                    if (project.visible) projects.add(project)
                }
                // Keep same behavior as original reverse
                projects.reverse()
                binding.loading.visibility = View.GONE
                binding.recyclerview.adapter?.notifyDataSetChanged()
            } catch (e: Exception) {
                toast(e.toString())
            }
        }

        override fun onCancelled(error: DatabaseError) {
            // optional: show log/toast
        }
    }


    override fun init() {
        binding.toolbar.setNavigationOnClickListener(Utils.getBackPressedClickListener(this))


        binding.recyclerview.adapter = ListProjectAdapter(projects, this, userNames, 1)
        binding.recyclerview.layoutManager = LinearLayoutManager(this)

        limit = 30.0
        key = intent?.getStringExtra("code")

        key?.let { k ->
            when (k) {
                "category" -> binding.toolbar.setTitle(intent?.getStringExtra("title"))
                "editors_choice" -> binding.toolbar.setTitle("Editor's Choice Projects")
                "verify" -> binding.toolbar.setTitle("Verified Projects")
                "project_type" -> {
                    val t = intent?.getStringExtra("title") ?: ""
                    binding.toolbar.setTitle("$t Projects")
                }
            }

            if (k == "premium") {
                binding.toolbar.setTitle("Premium Projects")
                binding.recyclerview.adapter = ListProjectAdapter(projects, this, userNames, 0)
                setQuery(premiumRef.limitToLast(limit.toInt()))
            } else {
                binding.recyclerview.adapter = ListProjectAdapter(projects, this, userNames, 1)
                val title = intent?.getStringExtra("title") ?: ""
                // original used startAt(...).endAt(...); consider equalTo(...) if you want strict equality
                setQuery(
                    normalRef.limitToLast(limit.toInt()).orderByChild(k).startAt(title).endAt(title)
                )
            }
        }
    }

    override fun initLogic() {
        val ind = object : GenericTypeIndicator<HashMap<String, Any?>>() {}
        val usersChildListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val childValue = snapshot.getValue(ind) ?: return
                val uid = childValue["uid"] as? String ?: return
                val name = childValue["name"] as? String
                if (name != null) userNames[uid] = name
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val childValue = snapshot.getValue(ind) ?: return
                val uid = childValue["uid"] as? String ?: return
                val name = childValue["name"] as? String
                if (name != null) userNames[uid] = name
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        usersRef.addChildEventListener(usersChildListener)
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    limit += 15.0
                    key?.let { k ->
                        if (k == "premium") {
                            setQuery(premiumRef.limitToLast(limit.toInt()))
                        } else {
                            val title = intent?.getStringExtra("title") ?: ""
                            setQuery(
                                normalRef.limitToLast(limit.toInt()).orderByChild(k).startAt(title)
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