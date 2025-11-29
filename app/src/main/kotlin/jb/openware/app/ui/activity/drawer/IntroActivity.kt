package jb.openware.app.ui.activity.drawer

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import jb.openware.app.R
import jb.openware.app.databinding.ActivityIntroBinding
import jb.openware.app.ui.adapter.OnBoardingAdapter
import jb.openware.app.ui.items.OnBoardingItem
import jb.openware.app.util.ThemeUtil

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private lateinit var onBoardingAdapter: OnBoardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        ThemeUtil.updateTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupOnBoardingItems()
        setupViewPager()
        setupIndicators()
        setCurrentOnBoardingIndicator(0)
        setupButton()
    }

    private fun setupOnBoardingItems() {
        val items = listOf(
            OnBoardingItem(
                image = R.drawable.demo_icon,
                title = "OpenWare",
                description = "An application has been developed to facilitate the free sharing of exceptional creations with a global audience, while also enabling connectivity to developers worldwide."
            ),
            OnBoardingItem(
                image = R.drawable.projects,
                title = "Projects",
                description = "I offer an open-source Android app (APK) for users and developers."
            ),
            OnBoardingItem(
                image = R.drawable.simple,
                title = "Simple & Fast",
                description = "OpenWare is user-friendly and highly comprehensible, featuring Material Design 3. Additionally, it is network-efficient, enabling swift and seamless app utilization."
            ),
            OnBoardingItem(
                image = R.drawable.friendly,
                title = "User Friendly",
                description = "OpenWare is a user-friendly application, which can be comprehended and utilized with ease."
            )
        )

        onBoardingAdapter = OnBoardingAdapter(items)
    }

    private fun setupViewPager() = with(binding) {
        viewpager.adapter = onBoardingAdapter

        viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentOnBoardingIndicator(position)
            }
        })
    }

    private fun setupButton() = with(binding) {
        button.setOnClickListener {
            val vp = viewpager
            val nextIndex = vp.currentItem + 1

            if (nextIndex < onBoardingAdapter.itemCount) {
                vp.currentItem = nextIndex
            } else {
                finish()
            }
        }
    }

    private fun setupIndicators() = with(binding) {
        val count = onBoardingAdapter.itemCount
        val indicators = Array(count) { ImageView(this@IntroActivity) }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            val margin = (8 * resources.displayMetrics.density).toInt()
            setMargins(margin, 0, margin, 0)
        }

        indicator.removeAllViews()

        indicators.forEach { imageView ->
            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    this@IntroActivity,
                    R.drawable.onboarding_indicator_inactive
                )
            )
            imageView.layoutParams = params
            indicator.addView(imageView)
        }
    }

    private fun setCurrentOnBoardingIndicator(index: Int) = with(binding) {
        val childCount = indicator.childCount

        for (i in 0 until childCount) {
            val imageView = indicator.getChildAt(i) as ImageView
            val drawableRes = if (i == index) {
                R.drawable.onboarding_indicator
            } else {
                R.drawable.onboarding_indicator_inactive
            }
            imageView.setImageDrawable(
                ContextCompat.getDrawable(this@IntroActivity, drawableRes)
            )
        }

        button.text = if (index == onBoardingAdapter.itemCount - 1) {
            "Start"
        } else {
            "Next"
        }
    }
}
