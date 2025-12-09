package jb.openware.app.nativeAds

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import jb.openware.app.util.UserConfig

class MobileAdsLoader(
    private val activity: Activity
) {
    val testDeviceId: String = "C8D32B707CD883FC6D3281468723FC8E"
    private val nativeAdId: String = "ca-app-pub-8844795823361502/2387419837"
    private val testNativeAdId: String = "ca-app-pub-3940256099942544/2247696110"
    private val interstitialAdId: String = "ca-app-pub-8844795823361502/1912228374"
    private val testInterstitialAdId: String = "ca-app-pub-3940256099942544/1033173712"

    fun buildConfig(): MobileAdsLoader = apply {
        MobileAds.initialize(activity) { }

        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf(testDeviceId))
            .build()

        MobileAds.setRequestConfiguration(configuration)
    }

    fun loadNativeAd(templateView: TemplateView, container: View): MobileAdsLoader =
        loadNativeAd(templateView, container, test = false)

    fun loadNativeAd(
        templateView: TemplateView,
        container: View,
        test: Boolean
    ): MobileAdsLoader = apply {
        val adUnitId = if (test) testNativeAdId else nativeAdId

        // Badge 0 => show ads, otherwise hide
        if (UserConfig(activity).badge == 0) {
            val adLoader = AdLoader.Builder(activity, adUnitId)
                .forNativeAd { nativeAd ->
                    val styles = NativeTemplateStyle.Builder().build()
                    templateView.setStyles(styles)
                    templateView.setNativeAd(nativeAd)

                    runTransition(container)
                    templateView.visibility = View.VISIBLE
                }
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        } else {
            templateView.visibility = View.GONE
        }
    }

    private fun runTransition(view: View) {
        val viewGroup = view as? ViewGroup ?: return

        val autoTransition = AutoTransition().apply {
            duration = 400L
            interpolator = DecelerateInterpolator()
        }

        TransitionManager.beginDelayedTransition(viewGroup, autoTransition)
    }
}
