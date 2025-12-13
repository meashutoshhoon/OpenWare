package jb.openware.app.nativeAds

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import jb.openware.app.R

class TemplateView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val SMALL_TEMPLATE = "small_template"
        private const val MEDIUM_TEMPLATE = "medium_template"
    }

    private var templateType: Int = 0
    private var styles: NativeTemplateStyle? = null
    private var nativeAd: NativeAd? = null

    private lateinit var nativeAdView: NativeAdView
    private lateinit var primaryView: TextView
    private lateinit var secondaryView: TextView
    private var tertiaryView: TextView? = null
    private lateinit var ratingBar: RatingBar
    private lateinit var iconView: ImageView
    private lateinit var mediaView: MediaView
    private lateinit var callToActionView: Button
    private lateinit var background: LinearLayout

    init {
        initView(context, attrs)
    }

    fun setStyles(styles: NativeTemplateStyle) {
        this.styles = styles
        applyStyles()
    }

    fun getNativeAdView(): NativeAdView = nativeAdView

    private fun applyStyles() {
        val style = styles ?: return

        style.mainBackgroundColor?.let { mainBackground ->
            background.background = mainBackground
            primaryView.background = mainBackground
            secondaryView.background = mainBackground
            tertiaryView?.background = mainBackground
        }

        style.primaryTextTypeface?.let { tf: Typeface ->
            primaryView.typeface = tf
        }

        style.secondaryTextTypeface?.let { tf: Typeface ->
            secondaryView.typeface = tf
        }

        style.tertiaryTextTypeface?.let { tf: Typeface ->
            tertiaryView?.typeface = tf
        }

        style.callToActionTextTypeface?.let { tf: Typeface ->
            callToActionView.typeface = tf
        }

        style.primaryTextTypefaceColor?.let { color ->
            primaryView.setTextColor(color)
        }

        style.secondaryTextTypefaceColor?.let { color ->
            secondaryView.setTextColor(color)
        }

        style.tertiaryTextTypefaceColor?.let { color ->
            tertiaryView?.setTextColor(color)
        }

        style.callToActionTypefaceColor?.let { color ->
            callToActionView.setTextColor(color)
        }

        if (style.callToActionTextSize > 0f) {
            callToActionView.textSize = style.callToActionTextSize
        }

        if (style.primaryTextSize > 0f) {
            primaryView.textSize = style.primaryTextSize
        }

        if (style.secondaryTextSize > 0f) {
            secondaryView.textSize = style.secondaryTextSize
        }

        if (style.tertiaryTextSize > 0f) {
            tertiaryView?.textSize = style.tertiaryTextSize
        }

        style.callToActionBackgroundColor?.let { bg ->
            callToActionView.background = bg
        }

        style.primaryTextBackgroundColor?.let { bg ->
            primaryView.background = bg
        }

        style.secondaryTextBackgroundColor?.let { bg ->
            secondaryView.background = bg
        }

        style.tertiaryTextBackgroundColor?.let { bg ->
            tertiaryView?.background = bg
        }

        invalidate()
        requestLayout()
    }

    private fun adHasOnlyStore(nativeAd: NativeAd): Boolean {
        val store = nativeAd.store
        val advertiser = nativeAd.advertiser
        return !store.isNullOrEmpty() && advertiser.isNullOrEmpty()
    }

    fun setNativeAd(nativeAd: NativeAd) {
        this.nativeAd = nativeAd

        val store = nativeAd.store
        val advertiser = nativeAd.advertiser
        val headline = nativeAd.headline
        val body = nativeAd.body
        val cta = nativeAd.callToAction
        val starRating = nativeAd.starRating
        val icon = nativeAd.icon

        var secondaryText: String? = null

        nativeAdView.callToActionView = callToActionView
        nativeAdView.headlineView = primaryView
        nativeAdView.mediaView = mediaView

        secondaryView.visibility = VISIBLE

        if (adHasOnlyStore(nativeAd)) {
            nativeAdView.storeView = secondaryView
            secondaryText = store
        } else if (!advertiser.isNullOrEmpty()) {
            nativeAdView.advertiserView = secondaryView
            secondaryText = advertiser
        } else {
            secondaryText = ""
        }

        primaryView.text = headline
        callToActionView.text = cta

        if (starRating != null && starRating > 0) {
            secondaryView.visibility = GONE
            ratingBar.visibility = VISIBLE
            ratingBar.rating = starRating.toFloat()
            nativeAdView.starRatingView = ratingBar
        } else {
            secondaryView.text = secondaryText
            secondaryView.visibility = VISIBLE
            ratingBar.visibility = GONE
        }

        if (icon != null) {
            iconView.visibility = VISIBLE
            iconView.setImageDrawable(icon.drawable)
        } else {
            iconView.visibility = GONE
        }

        tertiaryView?.let { bodyView ->
            bodyView.text = body
            nativeAdView.bodyView = bodyView
        }

        nativeAdView.setNativeAd(nativeAd)
    }

    fun destroyNativeAd() {
        nativeAd?.destroy()
        nativeAd = null
    }

    fun getTemplateTypeName(): String = when (templateType) {
        R.layout.admob -> MEDIUM_TEMPLATE
        R.layout.gnt_small_template_view -> SMALL_TEMPLATE
        else -> ""
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        val attributes: TypedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.TemplateView, 0, 0
        )

        try {
            templateType = attributes.getResourceId(
                R.styleable.TemplateView_gnt_template_type, R.layout.admob
            )
        } finally {
            attributes.recycle()
        }

        LayoutInflater.from(context).inflate(templateType, this, true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        nativeAdView = findViewById(R.id.native_ad_view)
        primaryView = findViewById(R.id.primary)
        secondaryView = findViewById(R.id.secondary)
        tertiaryView = findViewById(R.id.body)

        ratingBar = findViewById(R.id.rating_bar)
        ratingBar.isEnabled = false

        callToActionView = findViewById(R.id.cta)
        iconView = findViewById(R.id.icon)
        mediaView = findViewById(R.id.media_view)
        background = findViewById(R.id.background)
    }
}
