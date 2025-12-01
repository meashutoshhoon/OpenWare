package jb.openware.app.ui.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import jb.openware.app.R
import jb.openware.app.ui.items.CategoryAbout
import jb.openware.app.util.Const
import jb.openware.app.util.HapticUtils
import jb.openware.app.util.Utils
import androidx.core.view.isGone
import jb.openware.app.ui.activity.drawer.logs.ChangeLogActivity
import jb.openware.app.util.DeviceUtils

class AboutAdapter(
    private val items: List<Any>,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface AdapterListener {
        fun onCheckUpdate(button: Button, loadingDots: LottieAnimationView)
    }

    private var listener: AdapterListener? = null

    fun setAdapterListener(adapterListener: AdapterListener?) {
        listener = adapterListener
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is CategoryAbout -> CATEGORY
        is CategoryAbout.LeadDeveloperItem -> CATEGORY_LEAD_DEV_ITEM
        is CategoryAbout.ContributorsItem -> CATEGORY_CONTRIBUTORS_ITEM
        is CategoryAbout.AppItem -> CATEGORY_APP_ITEM
        else -> error("Invalid view type at position $position for ${items[position]::class.java}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            CATEGORY ->
                CategoryViewHolder(inflater.inflate(R.layout.category_about, parent, false))

            CATEGORY_LEAD_DEV_ITEM ->
                LeadDeveloperItemViewHolder(inflater.inflate(R.layout.category_lead_dev, parent, false))

            CATEGORY_CONTRIBUTORS_ITEM ->
                ContributorsItemViewHolder(inflater.inflate(R.layout.category_contributors, parent, false))

            CATEGORY_APP_ITEM ->
                AppItemViewHolder(inflater.inflate(R.layout.category_app, parent, false), listener)

            else -> error("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is CategoryViewHolder ->
                holder.bind(item as CategoryAbout)

            is LeadDeveloperItemViewHolder ->
                holder.bind(item as CategoryAbout.LeadDeveloperItem)

            is ContributorsItemViewHolder ->
                holder.bind(item as CategoryAbout.ContributorsItem)

            is AppItemViewHolder ->
                holder.bind(item as CategoryAbout.AppItem, position == items.lastIndex, activity)
        }
    }

    override fun getItemCount(): Int = items.size

    // ------------------------------------------------------------------------
    // ViewHolders
    // ------------------------------------------------------------------------

    private class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryTextView: TextView = itemView.findViewById(R.id.category_text_view)

        fun bind(category: CategoryAbout) {
            categoryTextView.text = category.name
        }
    }

    private class LeadDeveloperItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.image_view)
        private val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.description_text_view)
        private val mailButton: MaterialButton = itemView.findViewById(R.id.mail)
        private val githubButton: MaterialButton = itemView.findViewById(R.id.github)
        private val telegramButton: MaterialButton = itemView.findViewById(R.id.telegram)
        private val supportLayout: LinearLayout = itemView.findViewById(R.id.supportLayout)

        fun bind(item: CategoryAbout.LeadDeveloperItem) {
            imageView.setImageResource(item.imageRes)
            titleTextView.text = item.title
            descriptionTextView.text = item.description

            val viewUrlMap: Map<View, String> = mapOf(
                telegramButton to "https://t.me/meashutoshhoon",
                githubButton to Const.URL_DEV_GITHUB,
                mailButton to "mailto:${Const.DEV_MAIL}",
                supportLayout to Const.URL_DEV_BM_COFFEE
            )

            viewUrlMap.forEach { (view, url) ->
                view.setOnClickListener { v ->
                    HapticUtils.weakVibrate(v)
                    Utils.openUrl(itemView.context, url)
                }
            }
        }
    }

    private class ContributorsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.image_view)
        private val expandButton: ImageView = itemView.findViewById(R.id.expand_button)
        private val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.description_text_view)
        private val githubButton: Button = itemView.findViewById(R.id.github_handle)
        private val expandableLayout: LinearLayout =
            itemView.findViewById(R.id.contrib_expanded_layout)
        private val categoryContributorsLayout: MaterialCardView =
            itemView.findViewById(R.id.category_contributors_layout)

        fun bind(item: CategoryAbout.ContributorsItem) {
            imageView.setImageResource(item.imageRes)
            titleTextView.text = item.title
            descriptionTextView.text = item.description

            // reset state for recycled views
            expandableLayout.visibility = View.GONE
            expandButton.rotation = 0f

            githubButton.setOnClickListener { v ->
                HapticUtils.weakVibrate(v)
                Utils.openUrl(itemView.context, item.id.githubUrl)
            }

            categoryContributorsLayout.setOnClickListener { v ->
                HapticUtils.weakVibrate(v)
                toggleExpandableLayout()
            }

            categoryContributorsLayout.strokeWidth =
                if (DeviceUtils.androidVersion() >= Build.VERSION_CODES.S) 0 else 3
        }

        private fun toggleExpandableLayout() {
            val duration = 250L

            if (expandableLayout.isGone) {
                expandableLayout.visibility = View.VISIBLE
                expandableLayout.measure(
                    View.MeasureSpec.makeMeasureSpec(
                        categoryContributorsLayout.width,
                        View.MeasureSpec.EXACTLY
                    ),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                val targetHeight = expandableLayout.measuredHeight
                animateLayoutHeight(expandableLayout, 0, targetHeight, duration)
                expandButton.animate().rotation(180f).setDuration(duration).start()
            } else {
                val initialHeight = expandableLayout.height
                animateLayoutHeight(expandableLayout, initialHeight, 0, duration)
                expandButton.animate().rotation(0f).setDuration(duration).start()
            }
        }

        private fun animateLayoutHeight(
            view: View,
            startHeight: Int,
            endHeight: Int,
            duration: Long
        ) {
            val animator = ValueAnimator.ofInt(startHeight, endHeight)
            animator.addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                view.layoutParams.height = value
                view.requestLayout()
            }
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (endHeight == 0) {
                        view.visibility = View.GONE
                    } else {
                        view.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        view.requestLayout()
                    }
                }
            })
            animator.duration = duration
            animator.start()
        }
    }

    private class AppItemViewHolder(
        itemView: View,
        private val listener: AdapterListener?
    ) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.image_view)
        private val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.description_text_view)
        private val categoryAppLayout: LinearLayout = itemView.findViewById(R.id.category_app_layout)
        private val button: Button = itemView.findViewById(R.id.check_update_button)
        private val loadingDots: LottieAnimationView =
            itemView.findViewById(R.id.loading_animation)

        fun bind(item: CategoryAbout.AppItem, isLastItem: Boolean, activity: Activity) {
            imageView.setImageResource(item.imageRes)
            titleTextView.text = item.title
            descriptionTextView.text = item.description

            val clickListener = View.OnClickListener { v ->
                HapticUtils.weakVibrate(v)

                // Open URL if mapped
                getAppIdUrlMap()[item.id]?.let { url ->
                    Utils.openUrl(itemView.context, url)
                }

                when (item.id) {
                    Const.ID_CHANGELOGS -> {
                        activity.startActivity(Intent(activity, ChangeLogActivity::class.java))
                    }

                    Const.ID_REPORT -> {
                        HapticUtils.weakVibrate(v)
                        Utils.openUrl(activity, Const.URL_EMAIL_BUG)
                    }

                    Const.ID_FEATURE -> {
                        HapticUtils.weakVibrate(v)
                        Utils.openUrl(activity, Const.URL_EMAIL_FEATURE)
                    }
                }
            }

            if (item.id == Const.ID_VERSION) {
                button.visibility = View.VISIBLE
                loadingDots.visibility = View.GONE

                button.setOnClickListener { v ->
                    HapticUtils.weakVibrate(v)
                    listener?.onCheckUpdate(button, loadingDots)
                }
            } else {
                button.visibility = View.GONE
                loadingDots.visibility = View.GONE
            }

            categoryAppLayout.setOnClickListener(clickListener)

            val paddingInPixels =
                Utils.convertDpToPixel(30f, itemView.context).toInt()
            val lp = itemView.layoutParams as? ViewGroup.MarginLayoutParams
            lp?.let {
                it.bottomMargin = if (isLastItem) paddingInPixels else 0
                itemView.layoutParams = it
            }
        }
    }

    companion object {
        private const val CATEGORY = 0
        private const val CATEGORY_LEAD_DEV_ITEM = 1
        private const val CATEGORY_CONTRIBUTORS_ITEM = 2
        private const val CATEGORY_APP_ITEM = 3

        private fun getAppIdUrlMap(): Map<String, String> = mapOf(
            Const.ID_GITHUB to Const.URL_GITHUB_REPOSITORY,
            Const.ID_TELEGRAM to Const.URL_TELEGRAM,
            Const.ID_DISCORD to "https://discord.gg",
            Const.ID_LICENSE to Const.URL_APP_LICENSE
        )
    }
}
