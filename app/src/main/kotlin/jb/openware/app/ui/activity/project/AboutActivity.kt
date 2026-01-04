package jb.openware.app.ui.activity.project

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.TextView
import com.google.android.material.color.MaterialColors
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import jb.openware.app.databinding.ActivityAboutBinding
import jb.openware.app.ui.common.BaseActivity
import jb.openware.app.ui.components.TextFormatter
import jb.openware.app.ui.items.Project
import jb.openware.app.util.Utils

class AboutActivity : BaseActivity<ActivityAboutBinding>(ActivityAboutBinding::inflate) {

    private val usersReference = FirebaseDatabase.getInstance().getReference("Users")
    private var project: Project? = null

    override fun init() {
        binding.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    override fun initLogic() {
        val project = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("data", Project::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getParcelableExtra("data")
        }

        project?.let {
            updateUI(it)
            attachUserListener(it.uid)
        }

    }

    private fun attachUserListener(uid: String) {
        usersReference.child(uid).addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java)
                if (!name.isNullOrEmpty()) binding.name.text = name
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(data: Project) = with(binding) {

        title.text = data.title

        hideViews(card)

        // What's new
        if (data.whatsNew.isNotEmpty() && data.whatsNew != "none") {
            showViews(linear6)
            TextFormatter.format(whatsNew, data.whatsNew)
        } else {
            hideViews(linear6, dividerL)
        }

        // Description
        TextFormatter.format(about, data.description)

        downloads.text = data.downloads
        dateR.text = data.time

        // Updated date
        if (data.updateTime.isNotEmpty() && data.updateTime != "none") {
            showViews(linear13)
            date.text = data.updateTime
        } else {
            hideViews(linear13)
        }

        detectLinks(whatsNew)
        detectLinks(about)
    }

    private fun detectLinks(textView: TextView) {
        Linkify.addLinks(
            textView, Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES
        )

        val linkColor = MaterialColors.getColor(
            textView, com.google.android.material.R.attr.colorTertiary
        )

        textView.linksClickable = true
        textView.movementMethod = LinkMovementMethod.getInstance()


        textView.setLinkTextColor(linkColor)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        project?.let { outState.putParcelable("project", it) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val project = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState.getParcelable("project", Project::class.java)
        } else {
            @Suppress("DEPRECATION") savedInstanceState.getParcelable("project")
        } ?: return


        project.let {
            updateUI(it)
            attachUserListener(it.uid)
        }
    }

}
