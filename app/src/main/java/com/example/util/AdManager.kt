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

object AdManager {
    // User Live AdMob Ad Unit IDs
    const val LIVE_BANNER_AD_UNIT_ID = "ca-app-pub-9235738172114226/3409477527"
    const val LIVE_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-9235738172114226/5846948920"
    const val LIVE_REWARDED_AD_UNIT_ID = "ca-app-pub-9235738172114226/8505007058"

    // Google AdMob Official Test Ad Unit IDs (for development/testing or unapproved account fallback)
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private var isInterstitialUsingTest = false
    private var isRewardedUsingTest = false

    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context) { status ->
                DebugLogger.d("AdMob", "AdMob Initialization Complete: $status")
            }
            loadInterstitialAd(context, useTestFallback = false)
            loadRewardedAd(context, useTestFallback = false)
        } catch (e: Throwable) {
            DebugLogger.e("AdMob", "Error initializing AdMob: ${e.message}")
        }
    }

    fun loadInterstitialAd(context: Context, useTestFallback: Boolean = false) {
        val adUnitId = if (useTestFallback) TEST_INTERSTITIAL_AD_UNIT_ID else LIVE_INTERSTITIAL_AD_UNIT_ID
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialUsingTest = useTestFallback
                    DebugLogger.d("AdMob", "Interstitial Ad Loaded successfully (isTest=$useTestFallback)")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    DebugLogger.w("AdMob", "Interstitial Ad Failed to load: ${error.message} (code=${error.code})")
                    // If live ad failed (e.g. account review pending), fallback to test ad so UI/test runs smoothly
                    if (!useTestFallback) {
                        DebugLogger.d("AdMob", "Retrying Interstitial Ad with Test Ad Unit ID...")
                        loadInterstitialAd(context, useTestFallback = true)
                    }
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
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

    fun loadRewardedAd(context: Context, useTestFallback: Boolean = false) {
        val adUnitId = if (useTestFallback) TEST_REWARDED_AD_UNIT_ID else LIVE_REWARDED_AD_UNIT_ID
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedUsingTest = useTestFallback
                    DebugLogger.d("AdMob", "Rewarded Ad Loaded successfully (isTest=$useTestFallback)")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    DebugLogger.w("AdMob", "Rewarded Ad Failed to load: ${error.message} (code=${error.code})")
                    // Fallback to test unit if live account is pending approval
                    if (!useTestFallback) {
                        DebugLogger.d("AdMob", "Retrying Rewarded Ad with Test Ad Unit ID...")
                        loadRewardedAd(context, useTestFallback = true)
                    }
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdClosed: () -> Unit) {
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
            // Fallback reward if ad is not ready so game play continues smoothly
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
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdManager.LIVE_BANNER_AD_UNIT_ID
                
                adListener = object : com.google.android.gms.ads.AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        super.onAdFailedToLoad(error)
                        DebugLogger.w("AdMob", "Banner Ad Live failed: ${error.message}. Switching to Test Banner...")
                        // Fallback to test banner if live banner fails during review
                        adUnitId = AdManager.TEST_BANNER_AD_UNIT_ID
                        loadAd(AdRequest.Builder().build())
                    }
                }
                
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
