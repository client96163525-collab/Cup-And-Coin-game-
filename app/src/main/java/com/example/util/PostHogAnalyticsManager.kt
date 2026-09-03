package com.example.util

import android.content.Context
import android.content.res.Resources
import android.os.Build
import com.example.model.CoinTheme
import com.example.model.CupTheme
import com.example.model.GameMode
import com.posthog.PostHog
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random

/**
 * 🦔 PostHog Analytics Manager for Cup & Coin
 * Full-scale production telemetry tracking:
 * - Device & Hardware specifications (Phone model, Android version, screen size)
 * - Active users & app install reporting
 * - Screen views & session durations
 * - Game modes, level progress, wins, losses, streaks
 * - Power-up purchases & usage
 * - Ads impressions & rewarded conversions
 * - Lucky Spin & Daily Challenge completions
 * - Custom themes & user preferences
 */
object PostHogAnalyticsManager {

    private const val POSTHOG_API_KEY = "phc_oELYum75W4Y5KTrtd9MR4BiM4Djzecnq8e7oQHv59577"
    private const val POSTHOG_HOST = "https://us.i.posthog.com"
    private const val PROJECT_ID = "591554"

    private const val PREFS_NAME = "cup_posthog_prefs"
    private const val KEY_DISTINCT_ID = "posthog_distinct_id"
    private const val KEY_USERNAME = "posthog_username"
    private const val KEY_FIRST_OPEN = "posthog_first_open_time"
    private const val KEY_INSTALL_TRACKED = "posthog_install_tracked"

    private var isInitialized = false
    private var sessionStartTimeMs = 0L
    private var currentUserId: String = ""

    fun initialize(context: Context) {
        if (isInitialized) return

        try {
            val config = PostHogAndroidConfig(
                apiKey = POSTHOG_API_KEY,
                host = POSTHOG_HOST
            ).apply {
                captureApplicationLifecycleEvents = true
                captureScreenViews = true
            }

            PostHogAndroid.setup(context.applicationContext, config)
            isInitialized = true
            sessionStartTimeMs = System.currentTimeMillis()

            // Setup persistent User Identification and Super Properties
            setupUserIdentity(context)
            DebugLogger.d("PostHog", "PostHog Analytics successfully initialized with Project ID: $PROJECT_ID")
        } catch (e: Throwable) {
            DebugLogger.e("PostHog", "PostHog initialization error: ${e.message}")
        }
    }

    private fun setupUserIdentity(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            var distinctId = prefs.getString(KEY_DISTINCT_ID, null)
            var username = prefs.getString(KEY_USERNAME, null)
            val isFirstInstall = !prefs.getBoolean(KEY_INSTALL_TRACKED, false)

            if (distinctId.isNullOrEmpty()) {
                distinctId = "user_" + System.currentTimeMillis() + "_" + Random.nextInt(100000, 999999)
                prefs.edit().putString(KEY_DISTINCT_ID, distinctId).apply()
            }

            if (username.isNullOrEmpty()) {
                val brands = listOf("Pixel", "Galaxy", "OnePlus", "Redmi", "Vivo", "Oppo", "Realme", "Moto", "Xiaomi", "Nothing")
                val brand = brands.find { Build.MANUFACTURER.contains(it, ignoreCase = true) } ?: Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
                username = "${brand}_Player_${Random.nextInt(1000, 9999)}"
                prefs.edit().putString(KEY_USERNAME, username).apply()
            }

            val displayMetrics = Resources.getSystem().displayMetrics
            val screenResolution = "${displayMetrics.widthPixels}x${displayMetrics.heightPixels} (${displayMetrics.densityDpi} dpi)"
            val installDate = if (isFirstInstall) {
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
                prefs.edit().putString(KEY_FIRST_OPEN, now).apply()
                now
            } else {
                prefs.getString(KEY_FIRST_OPEN, "Unknown") ?: "Unknown"
            }

            val userProperties = mutableMapOf<String, Any>(
                "username" to username,
                "phone_brand" to Build.BRAND,
                "phone_manufacturer" to Build.MANUFACTURER,
                "phone_model" to Build.MODEL,
                "phone_device" to Build.DEVICE,
                "phone_hardware" to Build.HARDWARE,
                "android_version" to Build.VERSION.RELEASE,
                "android_sdk_int" to Build.VERSION.SDK_INT,
                "screen_resolution" to screenResolution,
                "device_locale" to Locale.getDefault().toLanguageTag(),
                "timezone" to TimeZone.getDefault().id,
                "app_version" to "1.0.5",
                "app_name" to "Cup & Coin",
                "first_installed_at" to installDate,
                "posthog_project_id" to PROJECT_ID
            )

            // Register super properties sent with every event
            PostHog.register("app_name", "Cup & Coin")
            PostHog.register("device_model", "${Build.MANUFACTURER} ${Build.MODEL}")
            PostHog.register("os_version", "Android ${Build.VERSION.RELEASE}")

            currentUserId = distinctId

            // Identify user on PostHog
            PostHog.identify(
                distinctId = distinctId,
                userProperties = userProperties
            )

            // If first launch, send install event
            if (isFirstInstall) {
                PostHog.capture(
                    event = "app_installed",
                    properties = userProperties
                )
                prefs.edit().putBoolean(KEY_INSTALL_TRACKED, true).apply()
                DebugLogger.d("PostHog", "Captured app_installed event for $username on ${Build.MODEL}")
            }

            // Capture App Opened / Active Session Start
            PostHog.capture(
                event = "app_opened",
                properties = mapOf(
                    "username" to username,
                    "phone_model" to Build.MODEL,
                    "timestamp" to System.currentTimeMillis()
                )
            )
        } catch (e: Throwable) {
            DebugLogger.e("PostHog", "Failed to setup identity: ${e.message}")
        }
    }

    // =========================================================================
    // 📱 SCREEN & NAVIGATION TELEMETRY
    // =========================================================================

    fun trackScreen(screenName: String, extraProps: Map<String, Any> = emptyMap()) {
        if (!isInitialized) return
        try {
            val props = mutableMapOf<String, Any>(
                "screen_name" to screenName,
                "device_model" to Build.MODEL
            )
            props.putAll(extraProps)
            PostHog.screen(screenTitle = screenName, properties = props)
            PostHog.capture(event = "screen_viewed", properties = props)
            DebugLogger.d("PostHog", "Tracked Screen: $screenName")
        } catch (e: Throwable) {
            DebugLogger.e("PostHog", "trackScreen error: ${e.message}")
        }
    }

    // =========================================================================
    // 🎮 GAMEPLAY & MINI-GAME TELEMETRY
    // =========================================================================

    fun trackGameStarted(
        mode: GameMode,
        level: Int,
        cupCount: Int,
        shuffleSpeed: Float,
        currentScore: Int,
        currentCoins: Int
    ) {
        captureEvent("game_round_started", mapOf(
            "game_mode" to mode.name,
            "level" to level,
            "cup_count" to cupCount,
            "shuffle_speed" to shuffleSpeed,
            "player_score" to currentScore,
            "player_coins" to currentCoins
        ))
    }

    fun trackCupSelected(
        selectedSlot: Int,
        correctSlot: Int,
        isWin: Boolean,
        mode: GameMode,
        level: Int,
        streak: Int,
        reactionTimeMs: Long
    ) {
        captureEvent("cup_selected", mapOf(
            "selected_slot" to selectedSlot,
            "correct_slot" to correctSlot,
            "is_win" to isWin,
            "game_mode" to mode.name,
            "level" to level,
            "streak" to streak,
            "reaction_time_ms" to reactionTimeMs
        ))
    }

    fun trackGameFinished(
        mode: GameMode,
        level: Int,
        isWin: Boolean,
        scoreEarned: Int,
        coinsEarned: Int,
        streak: Int,
        multiplier: Float
    ) {
        val eventName = if (isWin) "game_round_won" else "game_round_lost"
        captureEvent(eventName, mapOf(
            "game_mode" to mode.name,
            "level" to level,
            "score_earned" to scoreEarned,
            "coins_earned" to coinsEarned,
            "streak" to streak,
            "multiplier" to multiplier
        ))
    }

    // =========================================================================
    // ⚡ POWER-UPS & IN-GAME UTILITIES
    // =========================================================================

    fun trackPowerUpPurchased(powerUpName: String, costCoins: Int, currentBalance: Int) {
        captureEvent("powerup_purchased", mapOf(
            "powerup_name" to powerUpName,
            "cost_coins" to costCoins,
            "remaining_coins" to currentBalance
        ))
    }

    fun trackPowerUpUsed(powerUpName: String, level: Int, mode: GameMode) {
        captureEvent("powerup_used", mapOf(
            "powerup_name" to powerUpName,
            "level" to level,
            "game_mode" to mode.name
        ))
    }

    // =========================================================================
    // 🎡 LUCKY SPIN & REWARDS TELEMETRY
    // =========================================================================

    fun trackLuckySpinTriggered(spinType: String, costOrAd: String) {
        captureEvent("lucky_spin_triggered", mapOf(
            "spin_type" to spinType,
            "trigger_method" to costOrAd
        ))
    }

    fun trackLuckySpinRewardClaimed(rewardType: String, amount: Int, method: String) {
        captureEvent("lucky_spin_reward_claimed", mapOf(
            "reward_type" to rewardType,
            "amount" to amount,
            "claim_method" to method
        ))
    }

    // =========================================================================
    // 📅 DAILY CHALLENGE & STREAKS
    // =========================================================================

    fun trackDailyChallengeCompleted(dayNumber: Int, rewardCoins: Int, streakDays: Int) {
        captureEvent("daily_challenge_completed", mapOf(
            "day_number" to dayNumber,
            "reward_coins" to rewardCoins,
            "streak_days" to streakDays
        ))
    }

    // =========================================================================
    // 📺 MONETIZATION & ADS TELEMETRY (UNITY ADS)
    // =========================================================================

    fun trackAdRequested(adType: String, placementId: String, triggerSource: String) {
        captureEvent("ad_requested", mapOf(
            "ad_type" to adType,
            "placement_id" to placementId,
            "trigger_source" to triggerSource
        ))
    }

    fun trackAdLoaded(adType: String, placementId: String) {
        captureEvent("ad_loaded", mapOf(
            "ad_type" to adType,
            "placement_id" to placementId
        ))
    }

    fun trackAdDisplayed(adType: String, placementId: String) {
        captureEvent("ad_displayed", mapOf(
            "ad_type" to adType,
            "placement_id" to placementId
        ))
    }

    fun trackRewardedAdCompleted(rewardPoints: Int, triggerSource: String) {
        captureEvent("rewarded_ad_completed", mapOf(
            "reward_points" to rewardPoints,
            "trigger_source" to triggerSource
        ))
    }

    fun trackAdFailed(adType: String, placementId: String, errorMessage: String) {
        captureEvent("ad_failed", mapOf(
            "ad_type" to adType,
            "placement_id" to placementId,
            "error_message" to errorMessage
        ))
    }

    // =========================================================================
    // 🎨 CUSTOMIZATION & THEMES TELEMETRY
    // =========================================================================

    fun trackThemeChanged(themeCategory: String, themeName: String) {
        captureEvent("theme_changed", mapOf(
            "category" to themeCategory,
            "theme_name" to themeName
        ))
    }

    fun trackSettingToggled(settingName: String, isEnabled: Boolean) {
        captureEvent("setting_toggled", mapOf(
            "setting_name" to settingName,
            "is_enabled" to isEnabled
        ))
    }

    // =========================================================================
    // 🔘 BUTTON CLICKS & INTERACTIONS
    // =========================================================================

    fun trackButtonClick(buttonName: String, screen: String, metadata: Map<String, Any> = emptyMap()) {
        val props = mutableMapOf<String, Any>(
            "button_name" to buttonName,
            "screen" to screen
        )
        props.putAll(metadata)
        captureEvent("button_clicked", props)
    }

    // =========================================================================
    // ⚠️ CRASH & ERROR TELEMETRY
    // =========================================================================

    fun trackError(errorCategory: String, errorMessage: String, details: String = "") {
        captureEvent("app_error", mapOf(
            "error_category" to errorCategory,
            "error_message" to errorMessage,
            "details" to details,
            "device_model" to Build.MODEL,
            "android_version" to Build.VERSION.RELEASE
        ))
    }

    // =========================================================================
    // 🔄 USER STATE UPDATE
    // =========================================================================

    fun updateUserProgressProperties(
        totalScore: Int,
        totalCoins: Int,
        highestStreak: Int,
        gamesPlayed: Int,
        selectedCup: CupTheme,
        selectedCoin: CoinTheme
    ) {
        if (!isInitialized) return
        try {
            val userProps = mapOf(
                "total_score" to totalScore,
                "total_coins" to totalCoins,
                "highest_streak" to highestStreak,
                "games_played" to gamesPlayed,
                "selected_cup" to selectedCup.name,
                "selected_coin" to selectedCoin.name,
                "last_active_time" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            )
            if (currentUserId.isNotEmpty()) {
                PostHog.identify(
                    distinctId = currentUserId,
                    userProperties = userProps
                )
            }
        } catch (e: Throwable) {
            DebugLogger.e("PostHog", "Failed to update person properties: ${e.message}")
        }
    }

    // =========================================================================
    // 🛠️ INTERNAL DISPATCHER
    // =========================================================================

    private fun captureEvent(eventName: String, properties: Map<String, Any>) {
        if (!isInitialized) return
        try {
            val enrichedProps = properties.toMutableMap()
            enrichedProps["device_model"] = Build.MODEL
            enrichedProps["device_brand"] = Build.BRAND
            enrichedProps["android_version"] = Build.VERSION.RELEASE
            enrichedProps["timestamp"] = System.currentTimeMillis()
            
            PostHog.capture(
                event = eventName,
                properties = enrichedProps
            )
            DebugLogger.d("PostHog", "Event Captured [$eventName]: $properties")
        } catch (e: Throwable) {
            DebugLogger.e("PostHog", "Failed to capture event $eventName: ${e.message}")
        }
    }

    fun flush() {
        if (!isInitialized) return
        try {
            PostHog.flush()
        } catch (_: Throwable) {}
    }
}
