package jb.openware.app.ui.activity.project

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.util.Linkify
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.hdodenhof.circleimageview.CircleImageView
import jb.openware.app.R
import jb.openware.app.databinding.ActivityCommentsBinding
import jb.openware.app.ui.activity.profile.ProfileActivity
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.ui.common.HapticFeedback.slightHapticFeedback
import jb.openware.app.ui.components.BadgeDrawable
import jb.openware.app.ui.dialogs.ReportDialog
import jb.openware.app.ui.items.Comment
import jb.openware.app.ui.items.ReportItem
import jb.openware.app.util.RequestNetworkController.Companion.GET
import jb.openware.app.util.Utils
import jb.openware.app.util.moderatorUrl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.regex.Pattern

private enum class CommentOption(val label: String) {
    VIEW_PROFILE("View Profile"), COPY("Copy"), DELETE("Delete"), REPORT("Report comment")
}

class CommentsActivity : BaseActivity<ActivityCommentsBinding>(ActivityCommentsBinding::inflate) {

    private val firebase by lazy { FirebaseDatabase.getInstance() }
    private val commentRef by lazy { firebase.getReference("comments") }
    private val usersRef by lazy { firebase.getReference("Users") }

    private val userNames = mutableMapOf<String, String>()
    private val badge = mutableMapOf<String, String>()
    private val verified = mutableMapOf<String, String>()
    private val colors = mutableMapOf<String, String>()
    private val uidList = mutableMapOf<String, String>()
    private val avatarList = mutableMapOf<String, Any>()
    private val comments = mutableListOf<Comment>()
    private val moderatorIds = mutableListOf<String>()
    private var postKey: String = ""

    private lateinit var commentsAdapter: CommentsAdapter

    private var commentListener: ChildEventListener? = null
    private var usersChildListener: ChildEventListener? = null

    private val MENTION_REGEX = "(?<!\\S)(([@#])([A-Za-z0-9_-]\\.?)+)(?![^\\s,])"
    private val MENTION_PATTERN = Pattern.compile(MENTION_REGEX)
    private val MENTION_COLOR = getThemeColor(com.google.android.material.R.attr.colorTertiary)


    override fun init() {
        binding.edittext1.enableMentionHighlighting()

        binding.imageview2.setOnClickListener {
            it.slightHapticFeedback()

            val message = binding.edittext1.text.toString().trim()
            if (message.isEmpty()) return@setOnClickListener

            val key = commentRef.push().key ?: return@setOnClickListener


            val data = mapOf(
                "uid" to getUid(),
                "postKey" to postKey,
                "key" to key,
                "time" to System.currentTimeMillis(),
                "message" to message
            )

            commentRef.child(key).updateChildren(data)

            binding.edittext1.text = null
        }
    }

    override fun initLogic() {
        toggleLoading(true)
        binding.back.setOnClickListener { Utils.getBackPressedClickListener(this) }
        binding.title.text = intent.getStringExtra("title")
        postKey = intent.getStringExtra("key").toString()

        binding.listview1.apply {
            transcriptMode = ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
            isStackFromBottom = true

            setOnItemLongClickListener { _, _, position, _ ->
                showCommentOptions(position)
                true
            }
        }
        commentsAdapter = CommentsAdapter(comments)
        binding.listview1.adapter = commentsAdapter


        commentListener = object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val comment = snapshot.getValue(Comment::class.java) ?: return
                if (comment.postKey != postKey) return

                comments.add(comment)
                commentsAdapter.notifyDataSetChanged()
                toggleLoading(false)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val updated = snapshot.getValue(Comment::class.java) ?: return
                if (updated.postKey != postKey) return

                val index = comments.indexOfFirst { it.key == updated.key }
                if (index != -1) {
                    comments[index] = updated
                    commentsAdapter.notifyDataSetChanged()
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val key = snapshot.key ?: return
                val removed = comments.removeAll { it.key == key }
                if (removed) {
                    commentsAdapter.notifyDataSetChanged()
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
            override fun onCancelled(error: DatabaseError) = Unit
        }
        commentRef.addChildEventListener(commentListener!!)

        usersChildListener = object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                handleUserSnapshot(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                handleUserSnapshot(snapshot)
                commentsAdapter.notifyDataSetChanged()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
            override fun onChildRemoved(snapshot: DataSnapshot) = Unit
            override fun onCancelled(error: DatabaseError) = Unit
        }
        usersRef.addChildEventListener(usersChildListener!!)

        getConnectionsManager(
            url = moderatorUrl, method = GET, listener = object : RequestListener {

                override fun onResponse(
                    tag: String, response: String, responseHeaders: HashMap<String, Any>
                ) {
                    val data: List<Map<String, String>> = Gson().fromJson(
                        response, object : TypeToken<List<Map<String, String>>>() {}.type
                    )

                    moderatorIds.clear()
                    data.mapNotNullTo(moderatorIds) { it["uid"] }
                }

                override fun onErrorResponse(tag: String, message: String) = Unit
            })

    }

    private fun handleUserSnapshot(snapshot: DataSnapshot) {
        val uid = snapshot.child("uid").getValue(String::class.java) ?: return

        snapshot.child("name").getValue(String::class.java)?.let {
            userNames[uid] = it
            uidList[it] = uid
        }

        snapshot.child("avatar").getValue(String::class.java)?.let {
            avatarList[uid] = it
        }

        snapshot.child("badge").getValue(String::class.java)?.let {
            badge[uid] = it
        }

        snapshot.child("verified").getValue(String::class.java)?.let {
            verified[uid] = it
        }

        snapshot.child("color").getValue(String::class.java)?.let {
            colors[uid] = it
        }
    }

    private fun showCommentOptions(position: Int) {
        val item = comments[position]

        val commentUid = item.uid
        val isOwner = commentUid == getUid()
        val isModerator = moderatorIds.contains(getUid())

        val options = buildList {
            add(CommentOption.VIEW_PROFILE)
            add(CommentOption.COPY)
            if (isOwner || isModerator) add(CommentOption.DELETE)
            add(CommentOption.REPORT)
        }

        MaterialAlertDialogBuilder(this).setTitle("Options")
            .setItems(options.map { it.label }.toTypedArray()) { _, which ->
                handleCommentOption(options[which], item)
            }.show()
    }

    private fun handleCommentOption(
        option: CommentOption, item: Comment
    ) {
        when (option) {
            CommentOption.VIEW_PROFILE -> {
                val uid = item.uid
                putPrefString("developer", "uid", uid)
                openActivity<ProfileActivity>()
            }

            CommentOption.COPY -> {
                val message = item.message
                Utils.copyToClipboard(message, this)
            }

            CommentOption.DELETE -> {
                val key = item.key
                commentRef.child(key).removeValue()
            }

            CommentOption.REPORT -> {
                val report = ReportItem(
                    commentId = item.key, commentText = item.message, authorId = item.uid
                )
                ReportDialog().show(this, report)
            }
        }
    }

    fun toggleLoading(isLoading: Boolean) {
        TransitionManager.beginDelayedTransition(binding.refer)

        if (isLoading) {
            binding.linearShimmer.show()
            binding.listview1.hide()
        } else {
            binding.listview1.show()
            binding.linearShimmer.hide()
        }
    }

    fun TextView.enableAutoLinks() {
        isClickable = true
        linksClickable = true
        Linkify.addLinks(
            this, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES
        )
        setLinkTextColor(MENTION_COLOR)
    }

    @SuppressLint("SimpleDateFormat")
    fun TextView.setTimeAgo(timestampMillis: Long) {
        val now = System.currentTimeMillis()
        val diff = now - timestampMillis

        val seconds = diff / 1_000
        val minutes = diff / 60_000
        val hours = diff / 3_600_000
        val days = diff / 86_400_000

        text = when {
            seconds < 2 -> "1 second ago"
            seconds < 60 -> "$seconds seconds ago"

            minutes < 2 -> "1 minute ago"
            minutes < 60 -> "$minutes minutes ago"

            hours < 2 -> "1 hour ago"
            hours < 24 -> "$hours hours ago"

            days < 2 -> "1 day ago"
            days < 7 -> "$days days ago"

            else -> SimpleDateFormat("dd MMM yyyy").format(Date(timestampMillis))
        }
    }

    fun EditText.enableMentionHighlighting() {
        addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                s.removeMentionSpans()

                val matcher = MENTION_PATTERN.matcher(s)
                while (matcher.find()) {
                    s.setSpan(
                        ForegroundColorSpan(MENTION_COLOR),
                        matcher.start(),
                        matcher.end(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    s.setSpan(
                        StyleSpan(Typeface.BOLD),
                        matcher.start(),
                        matcher.end(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })
    }

    private fun Editable.removeMentionSpans() {
        getSpans(0, length, ForegroundColorSpan::class.java).forEach { removeSpan(it) }

        getSpans(0, length, StyleSpan::class.java).forEach { removeSpan(it) }
    }

    fun TextView.enableMentionClicks() {
        movementMethod = LinkMovementMethod.getInstance()
        updateMentionSpans()
    }

    private fun TextView.updateMentionSpans() {
        val text = text.toString()
        val spannable = SpannableString(text)

        val matcher = MENTION_PATTERN.matcher(text)
        while (matcher.find()) {
            spannable.setSpan(
                ProfileSpan(), matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        setText(spannable, TextView.BufferType.SPANNABLE)
    }

    private inner class ProfileSpan : ClickableSpan() {

        override fun onClick(view: View) {
            val textView = view as? TextView ?: return
            val spannable = textView.text as? Spannable ?: return

            val start = spannable.getSpanStart(this)
            val end = spannable.getSpanEnd(this)
            if (start < 0 || end < 0) return

            val clickedText = spannable.subSequence(start, end).toString()
            if (!clickedText.startsWith("@")) return

            val uid = uidList[clickedText.drop(1)] ?: return

            putPrefString("developer", "uid", uid)

            openActivity<ProfileActivity>()
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.isUnderlineText = false
            ds.color = MENTION_COLOR
        }
    }

    inner class CommentsAdapter(
        private val data: List<Comment>
    ) : BaseAdapter() {

        override fun getCount(): Int = data.size

        override fun getItem(position: Int): Comment = data[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View
            val holder: ViewHolder

            if (convertView == null) {
                view = layoutInflater.inflate(R.layout.comments_cell, parent, false)
                holder = ViewHolder(view)
                view.tag = holder
            } else {
                view = convertView
                holder = convertView.tag as ViewHolder
            }

            bind(holder, getItem(position))
            return view
        }

        private fun bind(h: ViewHolder, comment: Comment) {
            val uid = comment.uid

            h.message.text = comment.message
            h.message.enableAutoLinks()
            h.message.enableMentionClicks()

            h.time.setTimeAgo(comment.time.toLong())

            val userName = userNames[uid] ?: return
            val avatarUrl = avatarList[uid]
            val colorHex = colors[uid]

            h.name.text = userName

            if (avatarUrl == "none") {
                h.avatar.hide()
                h.wordLayout.show()
                h.wordText.text = userName.first().toString()
            } else {
                h.wordLayout.hide()
                h.avatar.show()
                Glide.with(h.avatar).load(avatarUrl.toString().toUri()).into(h.avatar)
            }

            colorHex?.let {
                h.wordLayout.background = GradientDrawable().apply {
                    cornerRadius = 360f
                    setColor(it.toColorInt())
                }
            }

            applyBadge(h, uid)

            h.name.setOnClickListener { h.avatar.performClick() }

            h.avatar.setOnClickListener {
                putPrefString("developer", "uid", uid)
                openActivity<ProfileActivity>()
            }
        }

        private fun applyBadge(h: ViewHolder, uid: String) {
            val badgeValue = badge[uid]?.toIntOrNull()
            val isVerified = verified[uid]?.toBoolean() ?: false

            when {
                badgeValue == null -> h.badge.hide()

                badgeValue == 0 && isVerified -> {
                    h.badge.setImageResource(R.drawable.verify)
                    h.badge.setColorFilter(0xFF00C853.toInt())
                    h.name.setTextColor(0xFF00C853.toInt())
                }

                badgeValue > 0 -> {
                    BadgeDrawable(this@CommentsActivity).setBadge(badgeValue.toString(), h.badge)

                    h.name.setTextColor(getThemeColor(com.google.android.material.R.attr.colorPrimaryVariant))
                }

                else -> h.badge.hide()
            }
        }

        private inner class ViewHolder(view: View) {
            val avatar: CircleImageView = view.findViewById(R.id.circleimageview1)
            val wordLayout: LinearLayout = view.findViewById(R.id.linear_word)
            val wordText: TextView = view.findViewById(R.id.tx_word)
            val message: TextView = view.findViewById(R.id.message)
            val name: TextView = view.findViewById(R.id.name)
            val time: TextView = view.findViewById(R.id.time)
            val badge: ImageView = view.findViewById(R.id.badge)
        }
    }


}