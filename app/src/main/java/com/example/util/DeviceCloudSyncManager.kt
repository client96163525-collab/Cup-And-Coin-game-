package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.example.model.PlayerStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Automatically syncs and restores player scores, coins, shields, and levels
 * across app uninstalls and reinstalls using the physical device's unique Android ID.
 * NO user login, password, or account registration required!
 */
object DeviceCloudSyncManager {
    private const val TAG = "DeviceCloudSync"
    private const val SYNC_PREFS = "device_cloud_sync_prefs"
    private const val KEY_LAST_SYNC_TIME = "last_cloud_sync_time"
    private const val BACKUP_API_URL = "https://cupandcoin.vercel.app/api/user-data"

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return try {
            val id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            if (!id.isNullOrBlank()) id else "DEV_FALLBACK_DEFAULT"
        } catch (_: Exception) {
            "DEV_FALLBACK_DEFAULT"
        }
    }

    /**
     * Pushes the latest player stats to the device cloud backup in background
     */
    fun syncStatsToCloud(context: Context, stats: PlayerStats) {
        val deviceId = getDeviceId(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(BACKUP_API_URL)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("Accept", "application/json")
                    doOutput = true
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                val payload = JSONObject().apply {
                    put("deviceId", deviceId)
                    put("bestScore", stats.bestScore)
                    put("highestLevel", stats.highestLevel)
                    put("gamesPlayed", stats.gamesPlayed)
                    put("gamesWon", stats.gamesWon)
                    put("shieldCount", stats.shieldCount)
                    put("dailyStreak", stats.dailyStreak)
                    put("bestStreak", stats.bestStreak)
                    put("bestCombo", stats.bestCombo)
                    put("isTutorialCompleted", stats.isTutorialCompleted)
                    put("updatedAt", System.currentTimeMillis())
                }

                conn.outputStream.use { os ->
                    OutputStreamWriter(os, "UTF-8").use { writer ->
                        writer.write(payload.toString())
                        writer.flush()
                    }
                }

                val code = conn.responseCode
                if (code in 200..299) {
                    DebugLogger.d(TAG, "Device stats successfully backed up to cloud for device: $deviceId")
                    context.getSharedPreferences(SYNC_PREFS, Context.MODE_PRIVATE)
                        .edit()
                        .putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
                        .apply()
                }
                conn.disconnect()
            } catch (e: Exception) {
                DebugLogger.w(TAG, "Cloud sync deferred (offline or network unavailable): ${e.message}")
            }
        }
    }

    /**
     * Checks if cloud backup exists for this device and restores data if local data is empty/new
     */
    fun restoreStatsFromCloudIfAvailable(context: Context, onRestored: (PlayerStats) -> Unit) {
        val deviceId = getDeviceId(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$BACKUP_API_URL?deviceId=$deviceId")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                val code = conn.responseCode
                if (code in 200..299) {
                    val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    if (response.isNotBlank()) {
                        val json = JSONObject(response)
                        val data = json.optJSONObject("data") ?: json
                        if (data.has("bestScore") || data.has("highestLevel")) {
                            val restored = PlayerStats(
                                bestScore = data.optInt("bestScore", 0),
                                highestLevel = data.optInt("highestLevel", 1).coerceAtLeast(1),
                                gamesPlayed = data.optInt("gamesPlayed", 0),
                                gamesWon = data.optInt("gamesWon", 0),
                                shieldCount = data.optInt("shieldCount", 0),
                                dailyStreak = data.optInt("dailyStreak", 0),
                                bestStreak = data.optInt("bestStreak", 0),
                                bestCombo = data.optInt("bestCombo", 0),
                                isTutorialCompleted = data.optBoolean("isTutorialCompleted", false)
                            )
                            DebugLogger.i(TAG, "Successfully restored device backup from cloud! Level: ${restored.highestLevel}, Score: ${restored.bestScore}")
                            launch(Dispatchers.Main) {
                                onRestored(restored)
                            }
                        }
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                DebugLogger.w(TAG, "Cloud restore check completed: ${e.message}")
            }
        }
    }
}
