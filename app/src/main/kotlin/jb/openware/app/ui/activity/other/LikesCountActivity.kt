package jb.openware.app.ui.activity.other

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import jb.openware.app.databinding.ActivityLikesCountBinding
import jb.openware.app.ui.cells.UsersCell
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.ui.items.Like
import jb.openware.app.ui.items.UserItem
import jb.openware.app.util.Utils

class LikesCountActivity :
    BaseActivity<ActivityLikesCountBinding>(ActivityLikesCountBinding::inflate) {

    private val firebase by lazy { FirebaseDatabase.getInstance() }
    private val usersRef by lazy { firebase.getReference("Users") }
    private val likesRef by lazy { firebase.getReference("likes") }

    private val likesList = mutableListOf<Like>()
    private val usersMap = mutableMapOf<String, UserItem>()

    private lateinit var adapter: LikesAdapter

    private var projectKey: String = ""


    override fun init() {
        projectKey = intent.getStringExtra("key") ?: ""

        loadUsers()
        loadLikes()
        binding.toolbar.setNavigationOnClickListener(Utils.getBackPressedClickListener(this))
    }

    override fun initLogic() {
        adapter = LikesAdapter(likesList, usersMap) { uid ->
            putPrefString("developer", "uid", uid)

            startActivity(ProfileActivity::class.java)
        }

        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = adapter

        setView(1)
    }

    private fun loadLikes() {
        likesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                handleLike(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                handleLike(snapshot)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun handleLike(snapshot: DataSnapshot) {
        val like = snapshot.getValue(Like::class.java) ?: return

        if (like.key != projectKey) return

        if (like.value) {
            if (likesList.none { it.uid == like.uid }) {
                likesList.add(0, like)
            }
            setView(2)
        } else {
            likesList.removeAll { it.uid == like.uid }
            if (likesList.isEmpty()) setView(3)
        }

        adapter.notifyDataSetChanged()
    }

    private fun loadUsers() {
        usersRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(UserItem::class.java)?.let {
                    usersMap[it.uid] = it
                }
                adapter.notifyDataSetChanged()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(UserItem::class.java)?.let {
                    usersMap[it.uid] = it
                }
                adapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setView(code: Int) {
        when (code) {
            1 -> {
                showViews(binding.loading)
                hideViews(binding.noLikeLayout, binding.recyclerview)
            }

            2 -> {
                showViews(binding.recyclerview)
                hideViews(binding.noLikeLayout, binding.loading)
            }

            3 -> {
                showViews(binding.noLikeLayout)
                hideViews(binding.loading, binding.recyclerview)
            }
        }
    }

    class LikesAdapter(
        private val likes: List<Like>,
        private val users: Map<String, UserItem>,
        private val onUserClick: (String) -> Unit
    ) : RecyclerView.Adapter<LikesAdapter.VH>() {

        class VH(val cell: UsersCell) : RecyclerView.ViewHolder(cell)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val cell = UsersCell(parent.context)
            cell.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            return VH(cell)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val like = likes[position]
            val user = users[like.uid] ?: return

            holder.cell.setData(user)

            holder.itemView.setOnClickListener {
                onUserClick(like.uid)
            }
        }

        override fun getItemCount() = likes.size
    }


}