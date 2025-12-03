package jb.openware.app.ui.activity.drawer

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Pair
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import jb.openware.app.R
import jb.openware.app.databinding.ActivityAboutUsBinding
import jb.openware.app.ui.adapter.AboutAdapter
import jb.openware.app.ui.items.CategoryAbout
import jb.openware.app.util.Const.Contributors
import jb.openware.app.util.Const
import jb.openware.app.util.ThemeUtil
import jb.openware.app.util.Utils

class AboutUsActivity :
    AppCompatActivity(),
    AboutAdapter.AdapterListener {
    private lateinit var binding: ActivityAboutUsBinding

    private var rvPositionAndOffset: Pair<Int, Int>? = null
    private var loadingDots: LottieAnimationView? = null
    private var updateButtonIcon: Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ThemeUtil.updateTheme(this)
        binding = ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() = with(binding.rvAbout) {
        layoutManager = LinearLayoutManager(this@AboutUsActivity)

        val adapter = AboutAdapter(initializeItems(), this@AboutUsActivity).apply {
            setAdapterListener(this@AboutUsActivity)
        }

        this.adapter = adapter

        viewTreeObserver.addOnDrawListener {
            startPostponedEnterTransition()
        }
    }

    private fun setupListeners() {
        binding.toolbar.setNavigationOnClickListener(Utils.getBackPressedClickListener(this))
    }

    private fun initializeItems(): List<Any> = buildList {
        // Lead Developer
        add(CategoryAbout(getString(R.string.lead_developer)))
        add(
            CategoryAbout.LeadDeveloperItem(
                title = Contributors.ASHUTOSH.displayName,
                description = getString(R.string.ashutosh_about),
                imageRes = R.mipmap.dp_ashutosh
            )
        )

        // Contributors
        add(CategoryAbout(getString(R.string.contributors)))
        add(
            CategoryAbout.ContributorsItem(
                id = Contributors.ANKIT,
                title = Contributors.ANKIT.displayName,
                description = getString(R.string.ankit_about),
                imageRes = R.mipmap.dp_ankit
            )
        )
        add(
            CategoryAbout.ContributorsItem(
                id = Contributors.ANUSHKA,
                title = Contributors.ANUSHKA.displayName,
                description = getString(R.string.anushka_about),
                imageRes = R.mipmap.dp_anushka
            )
        )
        add(
            CategoryAbout.ContributorsItem(
                id = Contributors.ATHARV,
                title = Contributors.ATHARV.displayName,
                description = getString(R.string.atharv_about),
                imageRes = R.mipmap.dp_atharv
            )
        )

        // App section
        add(CategoryAbout(getString(R.string.app)))
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            add(
                CategoryAbout.AppItem(
                    id = Const.ID_VERSION,
                    title = getString(R.string.version),
                    description = pInfo.versionName ?: "",
                    imageRes = R.drawable.ic_version_tag
                )
            )
        } catch (_: PackageManager.NameNotFoundException) {
            // ignore
        }

        add(
            CategoryAbout.AppItem(
                id = Const.ID_CHANGELOGS,
                title = getString(R.string.changelogs),
                description = getString(R.string.des_changelogs),
                imageRes = R.drawable.ic_changelog
            )
        )
        add(
            CategoryAbout.AppItem(
                id = Const.ID_REPORT,
                title = getString(R.string.report_issue),
                description = getString(R.string.des_report_issue),
                imageRes = R.drawable.ic_report
            )
        )
        add(
            CategoryAbout.AppItem(
                id = Const.ID_FEATURE,
                title = getString(R.string.feature_request),
                description = getString(R.string.des_feature_request),
                imageRes = R.drawable.ic_feature
            )
        )
        add(
            CategoryAbout.AppItem(
                id = Const.ID_GITHUB,
                title = getString(R.string.github),
                description = getString(R.string.des_github),
                imageRes = R.drawable.ic_github
            )
        )
        add(
            CategoryAbout.AppItem(
                id = Const.ID_LICENSE,
                title = getString(R.string.license),
                description = getString(R.string.des_license),
                imageRes = R.drawable.ic_license
            )
        )
    }

    fun generateLongMessage(): String = buildString {
        append("Long Exception Message: ")
        for (i in 0 until 1000) {
            append("This is an intentional crash ")
            append(i)
            append(". ")
        }
    }

    fun throwLongException() {
        throw RuntimeException(generateLongMessage())
    }

    override fun onCheckUpdate(
        button: Button,
        loadingDots: LottieAnimationView
    ) {
    }
}
