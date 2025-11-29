package jb.openware.app.ui.activity.project

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import com.google.firebase.database.*
import jb.openware.app.databinding.ActivityAboutBinding
import jb.openware.app.ui.items.Project
import androidx.core.graphics.toColorInt
import jb.openware.app.ui.components.TextFormatter

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding
    private val usersRef = FirebaseDatabase.getInstance().getReference("Users")
    private val userDatabase = FirebaseDatabase.getInstance().getReference("Users")
    private var project: Project? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDivider()
        binding.back.setOnClickListener { finish() }
        restoreOrInitData(savedInstanceState)
    }

    private fun restoreOrInitData(savedInstanceState: Bundle?) {
        project = savedInstanceState?.getParcelable("project")
            ?: intent.getParcelableExtra("data")

        project?.let {
            updateUI(it)
            it.uid?.let { uid -> attachUserListener(uid) }
        }
    }

    // ---------------- Firebase ----------------

    private fun attachUserListener(uid: String) {
        usersRef.child(uid).addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java)
                if (!name.isNullOrEmpty()) binding.name.text = name
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ---------------- Divider scroll behaviour ----------------

    private fun setupDivider() = with(binding) {
        divider.isVisible = false
        nestedScrollView.isVerticalScrollBarEnabled = false

        nestedScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                divider.isVisible = scrollY > 0
            }
        )

        val isNight = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        val dividerColorRes = if (isNight) R.color.divider_color_night else R.color.divider_color
        divider.setBackgroundColor(ContextCompat.getColor(this@AboutActivity, dividerColorRes))
    }

    // ---------------- UI update ----------------

    @SuppressLint("SetTextI18n")
    private fun updateUI(data: Project) = with(binding) {

        title.text = data.title.orEmpty()

        // Sketchware Pro note
        if (data.projectType == "Sketchware Pro(Mod)") {
            showViews(card)
            TextFormatter.format(
                version,
                "Use *b${data.isSketchwarePro}* version to avoid errors."
            )
        } else {
            hideViews(card)
        }

        // What's new
        if (!data.whatsNew.isNullOrEmpty() && data.whatsNew != "none") {
            showViews(linear6)
            TextFormatter.format(whatsNew, data.whatsNew)
        } else {
            hideViews(linear6, dividerL)
        }

        // Description
        TextFormatter.format(about, data.description.orEmpty())

        downloads.text = data.downloads.orEmpty()
        dateR.text = data.time.orEmpty()

        // Updated date
        if (!data.updateTime.isNullOrEmpty() && data.updateTime != "none") {
            showViews(linear13)
            date.text = data.updateTime
        } else {
            hideViews(linear13)
        }

        detectLinks(whatsNew)
        detectLinks(about)
    }

    // ---------------- Link helper ----------------

    private fun detectLinks(textView: TextView) {
        Linkify.addLinks(textView, Linkify.ALL)
        textView.setLinkTextColor("#2196F3".toColorInt())
    }

    // ---------------- Visibility helpers ----------------

    private fun showViews(vararg views: View) {
        views.forEach { it.visibility = View.VISIBLE }
    }

    private fun hideViews(vararg views: View) {
        views.forEach { it.visibility = View.GONE }
    }

    // ---------------- State ----------------

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        project?.let { outState.putParcelable("project", it) }
    }
}
