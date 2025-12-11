package jb.openware.app.ui.activity.other

import android.app.Activity
import android.content.SharedPreferences
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import jb.openware.app.databinding.ActivityLikesCountBinding
import jb.openware.app.ui.cells.UsersCell
import jb.openware.app.ui.common.BaseActivity
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import jb.openware.app.ui.items.LikeItem
import jb.openware.app.ui.items.UserItem
import jb.openware.app.util.Utils

class LikesCountActivity : BaseActivity<ActivityLikesCountBinding>(ActivityLikesCountBinding::inflate) {

    private val firebase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef: DatabaseReference = firebase.getReference("Users")
    private val likesRef: DatabaseReference = firebase.getReference("likes")

    // Caches
    private val users = HashMap<String, UserItem>()              // uid -> UserItem
    private val likes = HashMap<String, LikeItem>()              // firebaseKey -> LikeItem (or use snapshot key)
    private val activeLikedUids = LinkedHashSet<String>()        // maintain order, unique uids for adapter

    // Adapter
    private val adapter = UsersAdapter()

    // Listeners
    private var usersListener: ChildEventListener? = null
    private var likesListener: ChildEventListener? = null

    private var keyFilter: String = ""


    override fun init() {
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = adapter

        // initial state
        setView(1)

        // read filter key from intent
        keyFilter = intent.getStringExtra("key") ?: ""

        binding.toolbar.setNavigationOnClickListener(Utils.getBackPressedClickListener(this))
    }

    override fun initLogic() {

    }

    private fun attachListeners() {
        // Users listener: populate users map (and refresh adapter entries that match)
        usersListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(UserItem::class.java)?.let { user ->
                    users[user.uid] = user
                    refreshAdapterForUser(user.uid)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(UserItem::class.java)?.let { user ->
                    users[user.uid] = user
                    refreshAdapterForUser(user.uid)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        usersRef.addChildEventListener(usersListener!!)

        // Likes listener: track likes filtered by keyFilter and value == true
        likesListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(LikeItem::class.java)?.let { like ->
                    handleLikeSnapshot(snapshot.key ?: "", like)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(LikeItem::class.java)?.let { like ->
                    handleLikeSnapshot(snapshot.key ?: "", like)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // remove any like entry with this snapshot key
                val snapshotKey = snapshot.key ?: return
                likes.remove(snapshotKey)
                recalcActiveLikes()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        likesRef.addChildEventListener(likesListener!!)
    }

    inner class LikesAdapter(private val items: ArrayList<UserItem>) :
        RecyclerView.Adapter<LikesAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val usersCell = UsersCell(this@LikesCountActivity).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            return ViewHolder(usersCell)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val uid = item.uid

            if (uid != null &&
                userNames.containsKey(uid) &&
                userDp.containsKey(uid) &&
                userColor.containsKey(uid) &&
                userBadge.containsKey(uid) &&
                userVerified.containsKey(uid)
            ) {
                val name = userNames[uid].orEmpty()
                val avatar = userDp[uid].orEmpty()
                val color = userColor[uid].orEmpty()
                val badge = userBadge[uid].orEmpty()
                val verified = userVerified[uid].orEmpty()
                (holder.itemView as UsersCell).setData(avatar, name, color, badge, verified)
            }

            holder.itemView.setOnClickListener {
                val prefs: SharedPreferences = getSharedPreferences("developer", Activity.MODE_PRIVATE)
                prefs.edit { putString("uid", item["uid"]?.toString()) }
                startActivity(ProfileActivity::class.java)
            }
        }

        override fun getItemCount(): Int = data.size

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v)
    }

}