package `in`.afi.codekosh.activities.about

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import `in`.afi.codekosh.R
import `in`.afi.codekosh.tools.BaseFragment
import `in`.afi.codekosh.tools.ThemeBuilder

/**
 * AboutDevActivity - Displays developer information.
 * Inherits from BaseFragment and supports dynamic theming.
 */
class AboutDevActivity : BaseFragment() {

    private lateinit var back: ImageView
    private lateinit var divider: View

    override fun isHomeFragment(): Boolean = false

    override fun getLayoutRes(): Int = R.layout.activity_about_dev

    /**
     * Applies dynamic theming to the UI elements using ThemeBuilder.
     */
    override fun getThemeDescriptions(themeBuilder: ThemeBuilder) {
        val textViewIds = listOf(R.id.t1, R.id.t2, R.id.name)
        textViewIds.forEach { id ->
            findViewById<TextView>(id)?.let {
                themeBuilder.setTextColor(it, BLACK, TEXT_WHITE)
            }
        }

        // Apply color to content and hint text
        themeBuilder.setTextColor(findViewById(R.id.content), TEXT_BLACK, TEXT_WHITE)


        themeBuilder.setTextColor(findViewById(R.id.hint), GREY, TEXT_GREY)


        // Apply back button icon color
        themeBuilder.setImageColorFilter(back, BLACK, WHITE)

        // Update divider color based on theme mode
        val colorRes = if (themeBuilder.isNightMode()) {
            R.color.divider_color_night
        } else {
            R.color.divider_color
        }
        divider.setBackgroundColor(ContextCompat.getColor(this, colorRes))
    }

    /**
     * Initializes basic view references.
     */
    override fun initialize() {
        back = findViewById(R.id.back)
    }

    /**
     * Called after initialization to set up UI logic.
     */
    override fun initializeLogic() {
        setUpDivider()
        back.setOnClickListener { goBack() }
    }

    /**
     * Sets up the divider visibility behavior when the user scrolls.
     * The divider appears only when the user scrolls down.
     */
    private fun setUpDivider() {
        val nestedScrollView = findViewById<NestedScrollView>(R.id.nested_scroll_view)
        divider = findViewById(R.id.divider)

        divider.visibility = View.GONE
        nestedScrollView.isVerticalScrollBarEnabled = false

        nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            divider.visibility = if (scrollY > 0) View.VISIBLE else View.GONE
        }
    }
}
