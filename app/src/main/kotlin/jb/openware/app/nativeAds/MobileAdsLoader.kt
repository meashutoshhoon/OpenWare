package jb.openware.app.nativeAds;

import android.app.Activity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;

import java.util.Collections;

import in.afi.codekosh.tools.UserConfig;

public class MobileAdsLoader {
    public final String testDeviceId = "C8D32B707CD883FC6D3281468723FC8E";
    public final String nativeAdID = "ca-app-pub-8844795823361502/2387419837";
    private final Activity activity;
    private final String testNativeAdID = "ca-app-pub-3940256099942544/2247696110";
    private final String interstitialAdID = "ca-app-pub-8844795823361502/1912228374";
    private final String testInterstitialAdID = "ca-app-pub-3940256099942544/1033173712";

    public MobileAdsLoader(Activity activity) {
        this.activity = activity;
    }

    public MobileAdsLoader builtConfig() {
        MobileAds.initialize(activity, initializationStatus -> {
        });
        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Collections.singletonList(testDeviceId)).build();
        MobileAds.setRequestConfiguration(configuration);
        return this;
    }

    public MobileAdsLoader loadNativeAd(TemplateView templateView, View view) {
        loadNativeAd(templateView, view, false);
        return this;
    }

    public MobileAdsLoader loadNativeAd(TemplateView templateView, View view, boolean test) {
        String s = test ? testNativeAdID : nativeAdID;
        if (new UserConfig(activity).getBadge() == 0) {
            AdLoader adLoader = new AdLoader.Builder(activity, s)
                    .forNativeAd(nativeAd -> {
                        NativeTemplateStyle styles = new
                                NativeTemplateStyle.Builder().build();
                        templateView.setStyles(styles);
                        templateView.setNativeAd(nativeAd);
                        transitionManager(view);
                        templateView.setVisibility(View.VISIBLE);
                    })
                    .build();

            adLoader.loadAd(new AdRequest.Builder().build());
        } else {
            templateView.setVisibility(View.GONE);
        }
        return this;
    }


    private void transitionManager(final View view) {
        LinearLayout viewGroup = (LinearLayout) view;

        AutoTransition autoTransition = new AutoTransition();
        autoTransition.setDuration((long) (double) 400);
        autoTransition.setInterpolator(new DecelerateInterpolator());
        TransitionManager.beginDelayedTransition(viewGroup, autoTransition);
    }
}
