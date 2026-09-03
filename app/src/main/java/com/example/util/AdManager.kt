package com.example.util

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.BuildConfig

// Unity Ads Imports
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsLoadOptions
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize
import com.unity3d.services.banners.BannerErrorInfo
import java.util.UUID

object AdManager {
    // ----------------------------------------------------
    // Unity Ads Official Configuration
    // ----------------------------------------------------
    const val UNITY_GAME_ID = "800363260"
    
    // Test mode is enabled for debug builds/emulators so Unity Ads server sends test ads,
    // and disabled for release builds for live production ads.
    val UNITY_TEST_MODE: Boolean = BuildConfig.DEBUG
    
    // Unity Placements
    const val UNITY_BANNER_PLACEMENT_ID = "Banner_Android"
    const val UNITY_INTERSTITIAL_PLACEMENT_ID = "Interstitial_Android"
    const val UNITY_REWARDED_PLACEMENT_ID = "Rewarded_Android"

    var isUnityInterstitialLoaded = false
        private set
    var isUnityRewardedLoaded = false
        private set

    fun initialize(context: Context) {
        try {
            DebugLogger.d("UnityAds", "Initializing Unity Ads SDK with Game ID: $UNITY_GAME_ID (TestMode=$UNITY_TEST_MODE)")
            UnityAds.initialize(
                context.applicationContext,
                UNITY_GAME_ID,
                UNITY_TEST_MODE,
                object : IUnityAdsInitializationListener {
                    override fun onInitializationComplete() {
                        DebugLogger.d("UnityAds", "Unity Ads SDK Initialized Successfully! Preloading ads...")
                        loadUnityInterstitial(context)
                        loadUnityRewarded(context)
                    }

                    override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                        DebugLogger.e("UnityAds", "Unity Ads SDK Initialization Failed: $message (Error: $error)")
                    }
                }
            )
        } catch (e: Throwable) {
            DebugLogger.e("UnityAds", "Error initializing Unity Ads: ${e.message}")
        }
    }

    private var currentInterstitialObjectId: String? = null
    private var currentRewardedObjectId: String? = null

    // ----------------------------------------------------
    // Unity Ads Load Methods
    // ----------------------------------------------------
    fun loadUnityInterstitial(context: Context) {
        if (!UnityAds.isInitialized) return
        try {
            PostHogAnalyticsManager.trackAdRequested("INTERSTITIAL", UNITY_INTERSTITIAL_PLACEMENT_ID, "background_preload")
            UnityAds.load(UNITY_INTERSTITIAL_PLACEMENT_ID, object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(placementId: String?) {
                    isUnityInterstitialLoaded = true
                    PostHogAnalyticsManager.trackAdLoaded("INTERSTITIAL", placementId ?: UNITY_INTERSTITIAL_PLACEMENT_ID)
                    DebugLogger.d("UnityAds", "Unity Interstitial Ad Loaded Successfully ($placementId)")
                }

                override fun onUnityAdsFailedToLoad(placementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {
                    isUnityInterstitialLoaded = false
                    PostHogAnalyticsManager.trackAdFailed("INTERSTITIAL", placementId ?: UNITY_INTERSTITIAL_PLACEMENT_ID, message ?: "Unknown error")
                    DebugLogger.w("UnityAds", "Unity Interstitial failed to load: $message (Error: $error)")
                }
            })
        } catch (e: Throwable) {
            DebugLogger.e("UnityAds", "Exception loading Unity Interstitial: ${e.message}")
        }
    }

    fun loadUnityRewarded(context: Context) {
        if (!UnityAds.isInitialized) return
        try {
            PostHogAnalyticsManager.trackAdRequested("REWARDED", UNITY_REWARDED_PLACEMENT_ID, "background_preload")
            UnityAds.load(UNITY_REWARDED_PLACEMENT_ID, object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(placementId: String?) {
                    isUnityRewardedLoaded = true
                    PostHogAnalyticsManager.trackAdLoaded("REWARDED", placementId ?: UNITY_REWARDED_PLACEMENT_ID)
                    DebugLogger.d("UnityAds", "Unity Rewarded Ad Loaded Successfully ($placementId)")
                }

                override fun onUnityAdsFailedToLoad(placementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {
                    isUnityRewardedLoaded = false
                    PostHogAnalyticsManager.trackAdFailed("REWARDED", placementId ?: UNITY_REWARDED_PLACEMENT_ID, message ?: "Unknown error")
                    DebugLogger.w("UnityAds", "Unity Rewarded failed to load: $message (Error: $error)")
                }
            })
        } catch (e: Throwable) {
            DebugLogger.e("UnityAds", "Exception loading Unity Rewarded: ${e.message}")
        }
    }

    // ----------------------------------------------------
    // Unity Ad Playback
    // ----------------------------------------------------
    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (!isUnityInterstitialLoaded) {
            loadUnityInterstitial(activity)
        }

        if (UnityAds.isInitialized && isUnityInterstitialLoaded) {
            PostHogAnalyticsManager.trackAdDisplayed("INTERSTITIAL", UNITY_INTERSTITIAL_PLACEMENT_ID)
            UnityAds.show(activity, UNITY_INTERSTITIAL_PLACEMENT_ID, object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(placementId: String?, error: UnityAds.UnityAdsShowError?, message: String?) {
                    DebugLogger.e("UnityAds", "Unity Interstitial show failed: $message")
                    PostHogAnalyticsManager.trackAdFailed("INTERSTITIAL", placementId ?: UNITY_INTERSTITIAL_PLACEMENT_ID, message ?: "Show failure")
                    isUnityInterstitialLoaded = false
                    loadUnityInterstitial(activity)
                    onAdDismissed()
                }

                override fun onUnityAdsShowStart(placementId: String?) {
                    DebugLogger.d("UnityAds", "Unity Interstitial Started Showing")
                }

                override fun onUnityAdsShowClick(placementId: String?) {
                    PostHogAnalyticsManager.trackButtonClick("ad_clicked", "InterstitialAd", mapOf("placement_id" to (placementId ?: "")))
                    DebugLogger.d("UnityAds", "Unity Interstitial Clicked")
                }

                override fun onUnityAdsShowComplete(placementId: String?, state: UnityAds.UnityAdsShowCompletionState?) {
                    DebugLogger.d("UnityAds", "Unity Interstitial Closed ($state)")
                    isUnityInterstitialLoaded = false
                    loadUnityInterstitial(activity)
                    onAdDismissed()
                }
            })
        } else {
            DebugLogger.d("UnityAds", "Unity Interstitial not ready yet. Pre-loading for next time.")
            loadUnityInterstitial(activity)
            onAdDismissed()
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdClosed: () -> Unit = {}
    ) {
        if (!UnityAds.isInitialized) {
            initialize(activity)
        }

        if (UnityAds.isInitialized && isUnityRewardedLoaded) {
            PostHogAnalyticsManager.trackAdDisplayed("REWARDED", UNITY_REWARDED_PLACEMENT_ID)
            UnityAds.show(activity, UNITY_REWARDED_PLACEMENT_ID, object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(placementId: String?, error: UnityAds.UnityAdsShowError?, message: String?) {
                    DebugLogger.e("UnityAds", "Unity Rewarded show failed: $message (Error: $error)")
                    PostHogAnalyticsManager.trackAdFailed("REWARDED", placementId ?: UNITY_REWARDED_PLACEMENT_ID, message ?: "Show failure")
                    isUnityRewardedLoaded = false
                    loadUnityRewarded(activity)
                    Toast.makeText(
                        activity,
                        "Ad is preparing, please tap once more in 2 seconds!",
                        Toast.LENGTH_SHORT
                    ).show()
                    onAdClosed()
                }

                override fun onUnityAdsShowStart(placementId: String?) {
                    DebugLogger.d("UnityAds", "Unity Rewarded Started Showing")
                }

                override fun onUnityAdsShowClick(placementId: String?) {
                    PostHogAnalyticsManager.trackButtonClick("ad_clicked", "RewardedAd", mapOf("placement_id" to (placementId ?: "")))
                    DebugLogger.d("UnityAds", "Unity Rewarded Clicked")
                }

                override fun onUnityAdsShowComplete(placementId: String?, state: UnityAds.UnityAdsShowCompletionState?) {
                    isUnityRewardedLoaded = false
                    loadUnityRewarded(activity)
                    if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                        DebugLogger.d("UnityAds", "Unity Rewarded Completed! Granting reward...")
                        PostHogAnalyticsManager.trackRewardedAdCompleted(500, "user_watch_button")
                        onRewardEarned()
                    } else {
                        DebugLogger.d("UnityAds", "Unity Rewarded Skipped ($state)")
                        Toast.makeText(
                            activity,
                            "Ad was skipped. Watch full ad to get reward!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    onAdClosed()
                }
            })
        } else {
            DebugLogger.d("UnityAds", "Unity Rewarded not cached yet. Requesting load now...")
            loadUnityRewarded(activity)
            Toast.makeText(
                activity,
                "Loading video ad... Please tap again in 2 seconds!",
                Toast.LENGTH_SHORT
            ).show()
            onAdClosed()
        }
    }
}

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { context ->
            val frameLayout = android.widget.FrameLayout(context).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val activity = context as? Activity
            if (activity != null) {
                try {
                    val banner = BannerView(activity, AdManager.UNITY_BANNER_PLACEMENT_ID, UnityBannerSize(320, 50))
                    banner.listener = object : BannerView.IListener {
                        override fun onBannerLoaded(bannerView: BannerView?) {
                            DebugLogger.d("UnityAds", "Unity Banner Loaded Successfully")
                        }

                        override fun onBannerFailedToLoad(bannerView: BannerView?, errorInfo: BannerErrorInfo?) {
                            DebugLogger.w("UnityAds", "Unity Banner failed to load: ${errorInfo?.errorMessage} (${errorInfo?.errorCode})")
                        }

                        override fun onBannerClick(bannerView: BannerView?) {
                            DebugLogger.d("UnityAds", "Unity Banner Clicked")
                        }
                        override fun onBannerLeftApplication(bannerView: BannerView?) {}
                        override fun onBannerShown(bannerView: BannerView?) {
                            DebugLogger.d("UnityAds", "Unity Banner Shown")
                        }
                    }
                    frameLayout.addView(banner)
                    banner.load()
                } catch (e: Throwable) {
                    DebugLogger.e("UnityAds", "Error creating Unity banner view: ${e.message}")
                }
            }
            frameLayout
        }
    )
}

