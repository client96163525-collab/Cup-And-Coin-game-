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
    
    // Unity Placements with Waterfall Fallbacks
    const val UNITY_BANNER_PLACEMENT_ID = "Banner_Android"
    const val UNITY_BANNER_FALLBACK_ID = "banner"

    const val UNITY_INTERSTITIAL_PLACEMENT_ID = "Interstitial_Android"
    const val UNITY_INTERSTITIAL_FALLBACK_ID = "video"

    const val UNITY_REWARDED_PLACEMENT_ID = "Rewarded_Android"
    const val UNITY_REWARDED_FALLBACK_ID = "rewardedVideo"

    var isUnityInterstitialLoaded = false
        private set
    var isUnityRewardedLoaded = false
        private set

    private var activeInterstitialPlacementId: String = UNITY_INTERSTITIAL_PLACEMENT_ID
    private var activeRewardedPlacementId: String = UNITY_REWARDED_PLACEMENT_ID

    private var currentInterstitialObjectId: String? = null
    private var currentRewardedObjectId: String? = null

    private var isInterstitialLoading = false
    private var isRewardedLoading = false

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

    // ----------------------------------------------------
    // Unity Ads Load Methods (Using UnityAdsLoadOptions)
    // ----------------------------------------------------
    fun loadUnityInterstitial(context: Context, useFallback: Boolean = false) {
        if (!UnityAds.isInitialized) return
        if (isInterstitialLoading) return
        isInterstitialLoading = true

        val placementId = if (useFallback) UNITY_INTERSTITIAL_FALLBACK_ID else UNITY_INTERSTITIAL_PLACEMENT_ID
        val objectId = UUID.randomUUID().toString()
        currentInterstitialObjectId = objectId

        val loadOptions = UnityAdsLoadOptions().apply {
            this.objectId = objectId
        }

        try {
            PostHogAnalyticsManager.trackAdRequested("INTERSTITIAL", placementId, "background_preload")
            DebugLogger.d("UnityAds", "Requesting Interstitial ad ($placementId, objectId=$objectId)")

            UnityAds.load(placementId, loadOptions, object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(loadedPlacementId: String?) {
                    isInterstitialLoading = false
                    isUnityInterstitialLoaded = true
                    activeInterstitialPlacementId = loadedPlacementId ?: placementId
                    PostHogAnalyticsManager.trackAdLoaded("INTERSTITIAL", activeInterstitialPlacementId)
                    DebugLogger.d("UnityAds", "Unity Interstitial Ad Loaded Successfully ($activeInterstitialPlacementId)")
                }

                override fun onUnityAdsFailedToLoad(failedPlacementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {
                    isInterstitialLoading = false
                    isUnityInterstitialLoaded = false
                    PostHogAnalyticsManager.trackAdFailed("INTERSTITIAL", failedPlacementId ?: placementId, message ?: "Unknown error")
                    DebugLogger.w("UnityAds", "Unity Interstitial failed to load on $placementId: $message (Error: $error)")

                    // If primary placement failed (e.g., configured as Header Bidding on dashboard), try waterfall fallback 'video'
                    if (!useFallback && placementId == UNITY_INTERSTITIAL_PLACEMENT_ID) {
                        DebugLogger.i("UnityAds", "Retrying Interstitial with standard waterfall placement: $UNITY_INTERSTITIAL_FALLBACK_ID")
                        loadUnityInterstitial(context, useFallback = true)
                    }
                }
            })
        } catch (e: Throwable) {
            isInterstitialLoading = false
            DebugLogger.e("UnityAds", "Exception loading Unity Interstitial: ${e.message}")
        }
    }

    fun loadUnityRewarded(context: Context, useFallback: Boolean = false) {
        if (!UnityAds.isInitialized) return
        if (isRewardedLoading) return
        isRewardedLoading = true

        val placementId = if (useFallback) UNITY_REWARDED_FALLBACK_ID else UNITY_REWARDED_PLACEMENT_ID
        val objectId = UUID.randomUUID().toString()
        currentRewardedObjectId = objectId

        val loadOptions = UnityAdsLoadOptions().apply {
            this.objectId = objectId
        }

        try {
            PostHogAnalyticsManager.trackAdRequested("REWARDED", placementId, "background_preload")
            DebugLogger.d("UnityAds", "Requesting Rewarded ad ($placementId, objectId=$objectId)")

            UnityAds.load(placementId, loadOptions, object : IUnityAdsLoadListener {
                override fun onUnityAdsAdLoaded(loadedPlacementId: String?) {
                    isRewardedLoading = false
                    isUnityRewardedLoaded = true
                    activeRewardedPlacementId = loadedPlacementId ?: placementId
                    PostHogAnalyticsManager.trackAdLoaded("REWARDED", activeRewardedPlacementId)
                    DebugLogger.d("UnityAds", "Unity Rewarded Ad Loaded Successfully ($activeRewardedPlacementId)")
                }

                override fun onUnityAdsFailedToLoad(failedPlacementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {
                    isRewardedLoading = false
                    isUnityRewardedLoaded = false
                    PostHogAnalyticsManager.trackAdFailed("REWARDED", failedPlacementId ?: placementId, message ?: "Unknown error")
                    DebugLogger.w("UnityAds", "Unity Rewarded failed to load on $placementId: $message (Error: $error)")

                    // If primary placement failed (e.g., configured as Header Bidding on dashboard), try waterfall fallback 'rewardedVideo'
                    if (!useFallback && placementId == UNITY_REWARDED_PLACEMENT_ID) {
                        DebugLogger.i("UnityAds", "Retrying Rewarded with standard waterfall placement: $UNITY_REWARDED_FALLBACK_ID")
                        loadUnityRewarded(context, useFallback = true)
                    }
                }
            })
        } catch (e: Throwable) {
            isRewardedLoading = false
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
            val placementId = activeInterstitialPlacementId
            val showOptions = UnityAdsShowOptions().apply {
                currentInterstitialObjectId?.let { this.objectId = it }
            }

            PostHogAnalyticsManager.trackAdDisplayed("INTERSTITIAL", placementId)
            UnityAds.show(activity, placementId, showOptions, object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(failedPlacementId: String?, error: UnityAds.UnityAdsShowError?, message: String?) {
                    DebugLogger.e("UnityAds", "Unity Interstitial show failed: $message")
                    PostHogAnalyticsManager.trackAdFailed("INTERSTITIAL", failedPlacementId ?: placementId, message ?: "Show failure")
                    isUnityInterstitialLoaded = false
                    loadUnityInterstitial(activity)
                    onAdDismissed()
                }

                override fun onUnityAdsShowStart(startedPlacementId: String?) {
                    DebugLogger.d("UnityAds", "Unity Interstitial Started Showing ($startedPlacementId)")
                }

                override fun onUnityAdsShowClick(clickedPlacementId: String?) {
                    PostHogAnalyticsManager.trackButtonClick("ad_clicked", "InterstitialAd", mapOf("placement_id" to (clickedPlacementId ?: "")))
                    DebugLogger.d("UnityAds", "Unity Interstitial Clicked")
                }

                override fun onUnityAdsShowComplete(completedPlacementId: String?, state: UnityAds.UnityAdsShowCompletionState?) {
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
            val placementId = activeRewardedPlacementId
            val showOptions = UnityAdsShowOptions().apply {
                currentRewardedObjectId?.let { this.objectId = it }
            }

            PostHogAnalyticsManager.trackAdDisplayed("REWARDED", placementId)
            UnityAds.show(activity, placementId, showOptions, object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(failedPlacementId: String?, error: UnityAds.UnityAdsShowError?, message: String?) {
                    DebugLogger.e("UnityAds", "Unity Rewarded show failed: $message (Error: $error)")
                    PostHogAnalyticsManager.trackAdFailed("REWARDED", failedPlacementId ?: placementId, message ?: "Show failure")
                    isUnityRewardedLoaded = false
                    loadUnityRewarded(activity)
                    Toast.makeText(
                        activity,
                        "Ad is loading, please try again in a moment!",
                        Toast.LENGTH_SHORT
                    ).show()
                    onAdClosed()
                }

                override fun onUnityAdsShowStart(startedPlacementId: String?) {
                    DebugLogger.d("UnityAds", "Unity Rewarded Started Showing ($startedPlacementId)")
                }

                override fun onUnityAdsShowClick(clickedPlacementId: String?) {
                    PostHogAnalyticsManager.trackButtonClick("ad_clicked", "RewardedAd", mapOf("placement_id" to (clickedPlacementId ?: "")))
                    DebugLogger.d("UnityAds", "Unity Rewarded Clicked")
                }

                override fun onUnityAdsShowComplete(completedPlacementId: String?, state: UnityAds.UnityAdsShowCompletionState?) {
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
                            if (bannerView?.placementId == AdManager.UNITY_BANNER_PLACEMENT_ID) {
                                DebugLogger.i("UnityAds", "Retrying Banner with fallback placement: ${AdManager.UNITY_BANNER_FALLBACK_ID}")
                                try {
                                    val fallbackBanner = BannerView(activity, AdManager.UNITY_BANNER_FALLBACK_ID, UnityBannerSize(320, 50))
                                    fallbackBanner.listener = object : BannerView.IListener {
                                        override fun onBannerLoaded(b: BannerView?) {
                                            DebugLogger.d("UnityAds", "Unity Fallback Banner Loaded Successfully")
                                        }
                                        override fun onBannerFailedToLoad(b: BannerView?, err: BannerErrorInfo?) {
                                            DebugLogger.w("UnityAds", "Unity Fallback Banner failed: ${err?.errorMessage}")
                                        }
                                        override fun onBannerClick(b: BannerView?) {}
                                        override fun onBannerLeftApplication(b: BannerView?) {}
                                        override fun onBannerShown(b: BannerView?) {}
                                    }
                                    frameLayout.removeAllViews()
                                    frameLayout.addView(fallbackBanner)
                                    fallbackBanner.load()
                                } catch (e: Throwable) {
                                    DebugLogger.e("UnityAds", "Error loading fallback banner: ${e.message}")
                                }
                            }
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

