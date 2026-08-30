package com.example.util

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

// Unity Ads Imports
import com.unity3d.ads.UnityAds
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize
import com.unity3d.services.banners.BannerErrorInfo

object AdManager {
    // ----------------------------------------------------
    // Unity Ads Settings (Prioritized as requested!)
    // ----------------------------------------------------
    const val PRIORITIZE_UNITY_ADS = true
    
    // Replace with your Unity Game ID from Unity Publisher Dashboard
    const val UNITY_GAME_ID = "800363260" 
    
    // Set to false for live production ads, or true for test ads during development
    const val UNITY_TEST_MODE = false
    
    // Standard Unity Android Placement IDs (Matches default Unity Ads config)
    const val UNITY_BANNER_PLACEMENT_ID = "Banner_Android"
    const val UNITY_INTERSTITIAL_PLACEMENT_ID = "Interstitial_Android"
    const val UNITY_REWARDED_PLACEMENT_ID = "Rewarded_Android"

    var isUnityInterstitialLoaded = false
        private set
    var isUnityRewardedLoaded = false
        private set

    // ----------------------------------------------------
    // Google AdMob Settings
    // ----------------------------------------------------
    const val LIVE_BANNER_AD_UNIT_ID = "ca-app-pub-9235738172114226/3409477527"
    const val LIVE_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-9235738172114226/5846948920"
    const val LIVE_REWARDED_AD_UNIT_ID = "ca-app-pub-9235738172114226/8505007058"

    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private var isInterstitialUsingTest = false
    private var isRewardedUsingTest = false

    fun isEmulator(): Boolean {
        val model = android.os.Build.MODEL ?: ""
        val product = android.os.Build.PRODUCT ?: ""
        val brand = android.os.Build.BRAND ?: ""
        val device = android.os.Build.DEVICE ?: ""
        val fingerprint = android.os.Build.FINGERPRINT ?: ""
        val hardware = android.os.Build.HARDWARE ?: ""

        return (fingerprint.startsWith("generic")
                || fingerprint.startsWith("unknown")
                || model.contains("google_sdk")
                || model.contains("Emulator")
                || model.contains("Android SDK built for x86")
                || (brand.startsWith("generic") && device.startsWith("generic"))
                || "google_sdk" == product
                || hardware.contains("goldfish")
                || hardware.contains("ranchu")
                || hardware.contains("nox")
                || model.contains("Bluestacks")
                || product.contains("sdk_gphone")
                || product.contains("emulator"))
    }

    fun initialize(context: Context) {
        // Initialize Google AdMob SDK
        try {
            MobileAds.initialize(context) { status ->
                DebugLogger.d("AdMob", "AdMob Initialization Complete: $status")
            }
            loadInterstitialAd(context, useTestFallback = isEmulator())
            loadRewardedAd(context, useTestFallback = isEmulator())
        } catch (e: Throwable) {
            DebugLogger.e("AdMob", "Error initializing AdMob: ${e.message}")
        }

        // Initialize Unity Ads SDK
        try {
            val testMode = UNITY_TEST_MODE || isEmulator()
            DebugLogger.d("UnityAds", "Initializing Unity Ads SDK with Game ID: $UNITY_GAME_ID (Test Mode: $testMode)")
            UnityAds.initialize(context, UNITY_GAME_ID, testMode, object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    DebugLogger.d("UnityAds", "Unity Ads SDK Initialized Successfully!")
                    loadUnityInterstitial(context)
                    loadUnityRewarded(context)
                }

                override fun onInitializationFailed(error: UnityAds.UnityAdsInitializationError?, message: String?) {
                    DebugLogger.e("UnityAds", "Unity Ads SDK Initialization Failed: $message ($error)")
                }
            })
        } catch (e: Throwable) {
            DebugLogger.e("UnityAds", "Error initializing Unity Ads: ${e.message}")
        }
    }

    // ----------------------------------------------------
    // Unity Ads Load Methods
    // ----------------------------------------------------
    fun loadUnityInterstitial(context: Context) {
        UnityAds.load(UNITY_INTERSTITIAL_PLACEMENT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                isUnityInterstitialLoaded = true
                DebugLogger.d("UnityAds", "Unity Interstitial Ad Loaded Successfully")
            }

            override fun onUnityAdsFailedToLoad(placementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {
                isUnityInterstitialLoaded = false
                DebugLogger.w("UnityAds", "Unity Interstitial failed to load: $message (Error: $error)")
            }
        })
    }

    fun loadUnityRewarded(context: Context) {
        UnityAds.load(UNITY_REWARDED_PLACEMENT_ID, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String?) {
                isUnityRewardedLoaded = true
                DebugLogger.d("UnityAds", "Unity Rewarded Ad Loaded Successfully")
            }

            override fun onUnityAdsFailedToLoad(placementId: String?, error: UnityAds.UnityAdsLoadError?, message: String?) {
                isUnityRewardedLoaded = false
                DebugLogger.w("UnityAds", "Unity Rewarded failed to load: $message (Error: $error)")
            }
        })
    }

    // ----------------------------------------------------
    // AdMob Load Methods
    // ----------------------------------------------------
    fun loadInterstitialAd(context: Context, useTestFallback: Boolean = false) {
        val forceTest = useTestFallback || isEmulator()
        val adUnitId = if (forceTest) TEST_INTERSTITIAL_AD_UNIT_ID else LIVE_INTERSTITIAL_AD_UNIT_ID
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialUsingTest = forceTest
                    DebugLogger.d("AdMob", "Interstitial Ad Loaded successfully (isTest=$forceTest)")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    DebugLogger.w("AdMob", "Interstitial Ad Failed to load: ${error.message} (code=${error.code})")
                    if (!forceTest) {
                        DebugLogger.d("AdMob", "Retrying Interstitial Ad with Test Ad Unit ID...")
                        loadInterstitialAd(context, useTestFallback = true)
                    }
                }
            }
        )
    }

    fun loadRewardedAd(context: Context, useTestFallback: Boolean = false) {
        val forceTest = useTestFallback || isEmulator()
        val adUnitId = if (forceTest) TEST_REWARDED_AD_UNIT_ID else LIVE_REWARDED_AD_UNIT_ID
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedUsingTest = forceTest
                    DebugLogger.d("AdMob", "Rewarded Ad Loaded successfully (isTest=$forceTest)")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    DebugLogger.w("AdMob", "Rewarded Ad Failed to load: ${error.message} (code=${error.code})")
                    if (!forceTest) {
                        DebugLogger.d("AdMob", "Retrying Rewarded Ad with Test Ad Unit ID...")
                        loadRewardedAd(context, useTestFallback = true)
                    }
                }
            }
        )
    }

    // ----------------------------------------------------
    // Ad Playback (With Unity Ads & AdMob Fail-safe fallback)
    // ----------------------------------------------------
    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (!isUnityInterstitialLoaded) {
            loadUnityInterstitial(activity)
        }
        if (PRIORITIZE_UNITY_ADS && isUnityInterstitialLoaded) {
            // Show Unity Interstitial
            UnityAds.show(activity, UNITY_INTERSTITIAL_PLACEMENT_ID, UnityAdsShowOptions(), object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(placementId: String?, error: UnityAds.UnityAdsShowError?, message: String?) {
                    DebugLogger.e("UnityAds", "Unity Interstitial show failed: $message. Falling back to AdMob Interstitial...")
                    isUnityInterstitialLoaded = false
                    loadUnityInterstitial(activity)
                    // Fallback to AdMob Interstitial Ad
                    showAdMobInterstitial(activity, onAdDismissed)
                }

                override fun onUnityAdsShowStart(placementId: String?) {
                    DebugLogger.d("UnityAds", "Unity Interstitial Started Showing")
                }

                override fun onUnityAdsShowClick(placementId: String?) {}

                override fun onUnityAdsShowComplete(placementId: String?, state: UnityAds.UnityAdsShowCompletionState?) {
                    isUnityInterstitialLoaded = false
                    loadUnityInterstitial(activity)
                    onAdDismissed()
                }
            })
        } else {
            // Show AdMob Interstitial Ad
            showAdMobInterstitial(activity, onAdDismissed)
        }
    }

    private fun showAdMobInterstitial(activity: Activity, onAdDismissed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd(activity, useTestFallback = false)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    loadInterstitialAd(activity, useTestFallback = false)
                    onAdDismissed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            loadInterstitialAd(activity, useTestFallback = false)
            onAdDismissed()
        }
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdClosed: () -> Unit) {
        if (!isUnityRewardedLoaded) {
            loadUnityRewarded(activity)
        }
        if (PRIORITIZE_UNITY_ADS && isUnityRewardedLoaded) {
            // Show Unity Rewarded Ad
            UnityAds.show(activity, UNITY_REWARDED_PLACEMENT_ID, UnityAdsShowOptions(), object : IUnityAdsShowListener {
                override fun onUnityAdsShowFailure(placementId: String?, error: UnityAds.UnityAdsShowError?, message: String?) {
                    DebugLogger.e("UnityAds", "Unity Rewarded show failed: $message. Falling back to AdMob Rewarded...")
                    isUnityRewardedLoaded = false
                    loadUnityRewarded(activity)
                    // Fallback to AdMob
                    showAdMobRewarded(activity, onRewardEarned, onAdClosed)
                }

                override fun onUnityAdsShowStart(placementId: String?) {
                    DebugLogger.d("UnityAds", "Unity Rewarded Started Showing")
                }

                override fun onUnityAdsShowClick(placementId: String?) {}

                override fun onUnityAdsShowComplete(placementId: String?, state: UnityAds.UnityAdsShowCompletionState?) {
                    isUnityRewardedLoaded = false
                    loadUnityRewarded(activity)
                    if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                        DebugLogger.d("UnityAds", "Unity Rewarded completed! Granting reward...")
                        onRewardEarned()
                    }
                    onAdClosed()
                }
            })
        } else {
            // Show AdMob Rewarded Ad
            showAdMobRewarded(activity, onRewardEarned, onAdClosed)
        }
    }

    private fun showAdMobRewarded(activity: Activity, onRewardEarned: () -> Unit, onAdClosed: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd(activity, useTestFallback = false)
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    rewardedAd = null
                    loadRewardedAd(activity, useTestFallback = false)
                    onAdClosed()
                }
            }
            rewardedAd?.show(activity) { rewardItem ->
                DebugLogger.d("AdMob", "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned()
            }
        } else {
            loadRewardedAd(activity, useTestFallback = false)
            onRewardEarned()
            onAdClosed()
        }
    }
}

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            val useTest = AdManager.isEmulator() || AdManager.UNITY_TEST_MODE
            if (AdManager.PRIORITIZE_UNITY_ADS) {
                val activity = context as? Activity
                if (activity != null) {
                    val banner = BannerView(activity, AdManager.UNITY_BANNER_PLACEMENT_ID, UnityBannerSize(320, 50))
                    banner.listener = object : BannerView.IListener {
                        override fun onBannerLoaded(bannerView: BannerView?) {
                            DebugLogger.d("UnityAds", "Unity Banner Loaded Successfully")
                        }

                        override fun onBannerFailedToLoad(bannerView: BannerView?, errorInfo: BannerErrorInfo?) {
                            DebugLogger.w("UnityAds", "Unity Banner failed: ${errorInfo?.errorMessage}. Falling back to AdMob Banner...")
                        }

                        override fun onBannerClick(bannerView: BannerView?) {}
                        override fun onBannerLeftApplication(bannerView: BannerView?) {}
                        override fun onBannerShown(bannerView: BannerView?) {}
                    }
                    banner.load()
                    banner
                } else {
                    // Fallback to AdMob banner if activity context is missing
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = if (useTest) AdManager.TEST_BANNER_AD_UNIT_ID else AdManager.LIVE_BANNER_AD_UNIT_ID
                        loadAd(AdRequest.Builder().build())
                    }
                }
            } else {
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = if (useTest) AdManager.TEST_BANNER_AD_UNIT_ID else AdManager.LIVE_BANNER_AD_UNIT_ID
                    
                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            super.onAdFailedToLoad(error)
                            DebugLogger.w("AdMob", "Banner Ad Live failed: ${error.message}. Switching to Test Banner...")
                            adUnitId = AdManager.TEST_BANNER_AD_UNIT_ID
                            loadAd(AdRequest.Builder().build())
                        }
                    }
                    
                    loadAd(AdRequest.Builder().build())
                }
            }
        }
    )
}
