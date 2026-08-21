package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CoinTheme
import com.example.model.CupTheme
import com.example.model.GameMode
import com.example.model.GameSettings
import com.example.model.PlayerStats
import com.example.model.ShuffleTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("three_cup_coin_prefs", Context.MODE_PRIVATE)

    private val _stats = MutableStateFlow(loadStats())
    val stats: StateFlow<PlayerStats> = _stats.asStateFlow()

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    private fun loadStats(): PlayerStats {
        return PlayerStats(
            gamesPlayed = prefs.getInt("gamesPlayed", 0),
            gamesWon = prefs.getInt("gamesWon", 0),
            gamesLost = prefs.getInt("gamesLost", 0),
            bestScore = prefs.getInt("bestScore", 0),
            highestLevel = prefs.getInt("highestLevel", 1),
            bestCombo = prefs.getInt("bestCombo", 0),
            currentStreak = prefs.getInt("currentStreak", 0),
            bestStreak = prefs.getInt("bestStreak", 0),
            lastDailyDate = prefs.getString("lastDailyDate", "") ?: "",
            dailyStreak = prefs.getInt("dailyStreak", 0),
            completedDailyDates = prefs.getStringSet("completedDailyDates", emptySet()) ?: emptySet(),
            bestPerfectStreak = prefs.getInt("bestPerfectStreak", 0),
            shieldCount = prefs.getInt("shieldCount", 0),
            doubleScoreActive = prefs.getBoolean("doubleScoreActive", false),
            lastSpinDate = prefs.getString("lastSpinDate", "") ?: ""
            // ModeStats persistence would go here in a full implementation
        )
    }

    private fun loadSettings(): GameSettings {
        val cupThemeId = prefs.getString("selectedCupTheme", CupTheme.ROYAL_CRIMSON.id) ?: CupTheme.ROYAL_CRIMSON.id
        val coinThemeId = prefs.getString("selectedCoinTheme", CoinTheme.GOLD_STAR.id) ?: CoinTheme.GOLD_STAR.id
        val shuffleThemeId = prefs.getString("selectedShuffleTheme", ShuffleTheme.CLASSIC_SLIDE.id) ?: ShuffleTheme.CLASSIC_SLIDE.id

        val cupTheme = CupTheme.entries.find { it.id == cupThemeId } ?: CupTheme.ROYAL_CRIMSON
        val coinTheme = CoinTheme.entries.find { it.id == coinThemeId } ?: CoinTheme.GOLD_STAR
        val shuffleTheme = ShuffleTheme.entries.find { it.id == shuffleThemeId } ?: ShuffleTheme.CLASSIC_SLIDE

        return GameSettings(
            soundEnabled = prefs.getBoolean("soundEnabled", true),
            vibrationEnabled = prefs.getBoolean("vibrationEnabled", true),
            reducedMotion = prefs.getBoolean("reducedMotion", false),
            selectedCupTheme = cupTheme,
            selectedCoinTheme = coinTheme,
            selectedShuffleTheme = shuffleTheme
        )
    }

    fun recordGameRound(isWin: Boolean, score: Int, level: Int, combo: Int, mode: GameMode = GameMode.CLASSIC, streak: Int = 0) {
        val current = _stats.value
        val newPlayed = current.gamesPlayed + 1
        val newWon = if (isWin) current.gamesWon + 1 else current.gamesWon
        val newLost = if (!isWin) current.gamesLost + 1 else current.gamesLost
        val newBestScore = maxOf(current.bestScore, score)
        val newHighestLevel = maxOf(current.highestLevel, level)
        val newBestCombo = maxOf(current.bestCombo, combo)
        val newCurrentStreak = if (isWin) current.currentStreak + 1 else 0
        val newBestStreak = maxOf(current.bestStreak, newCurrentStreak)
        val newBestPerfectStreak = if (mode == GameMode.PERFECT_RUN) maxOf(current.bestPerfectStreak, streak) else current.bestPerfectStreak

        val updated = current.copy(
            gamesPlayed = newPlayed,
            gamesWon = newWon,
            gamesLost = newLost,
            bestScore = newBestScore,
            highestLevel = newHighestLevel,
            bestCombo = newBestCombo,
            currentStreak = newCurrentStreak,
            bestStreak = newBestStreak,
            bestPerfectStreak = newBestPerfectStreak
        )

        prefs.edit()
            .putInt("gamesPlayed", newPlayed)
            .putInt("gamesWon", newWon)
            .putInt("gamesLost", newLost)
            .putInt("bestScore", newBestScore)
            .putInt("highestLevel", newHighestLevel)
            .putInt("bestCombo", newBestCombo)
            .putInt("currentStreak", newCurrentStreak)
            .putInt("bestStreak", newBestStreak)
            .putInt("bestPerfectStreak", newBestPerfectStreak)
            .apply()

        _stats.value = updated
    }

    fun recordDailyChallengeCompletion() {
        val todayStr = getTodayDateString()
        val current = _stats.value
        val newDailyStreak = if (isConsecutiveDay(current.lastDailyDate, todayStr)) current.dailyStreak + 1 else if (current.lastDailyDate == todayStr) current.dailyStreak else 1
        
        val newCompletedDates = current.completedDailyDates.toMutableSet()
        newCompletedDates.add(todayStr)

        val updated = current.copy(
            lastDailyDate = todayStr,
            dailyStreak = newDailyStreak,
            completedDailyDates = newCompletedDates
        )

        prefs.edit()
            .putString("lastDailyDate", todayStr)
            .putInt("dailyStreak", newDailyStreak)
            .putStringSet("completedDailyDates", newCompletedDates)
            .apply()

        _stats.value = updated
    }

    fun isDailyChallengeCompletedToday(): Boolean {
        return _stats.value.lastDailyDate == getTodayDateString()
    }

    fun isLuckySpinAvailableToday(): Boolean {
        return _stats.value.lastSpinDate != getTodayDateString()
    }

    fun recordLuckySpin(rewardType: String) {
        val todayStr = getTodayDateString()
        val current = _stats.value
        var newShieldCount = current.shieldCount
        var newDoubleScoreActive = current.doubleScoreActive
        var newBestScore = current.bestScore

        when (rewardType) {
            "SHIELD" -> newShieldCount++
            "DOUBLE" -> newDoubleScoreActive = true
            "250PTS" -> newBestScore += 250
            "500PTS" -> newBestScore += 500
        }

        val updated = current.copy(
            lastSpinDate = todayStr,
            shieldCount = newShieldCount,
            doubleScoreActive = newDoubleScoreActive,
            bestScore = newBestScore
        )

        prefs.edit()
            .putString("lastSpinDate", todayStr)
            .putInt("shieldCount", newShieldCount)
            .putBoolean("doubleScoreActive", newDoubleScoreActive)
            .putInt("bestScore", newBestScore)
            .apply()

        _stats.value = updated
    }

    fun useShield() {
        val current = _stats.value
        val newShieldCount = (current.shieldCount - 1).coerceAtLeast(0)
        val updated = current.copy(shieldCount = newShieldCount)
        prefs.edit().putInt("shieldCount", newShieldCount).apply()
        _stats.value = updated
    }

    fun consumeDoubleScore() {
        val current = _stats.value
        val updated = current.copy(doubleScoreActive = false)
        prefs.edit().putBoolean("doubleScoreActive", false).apply()
        _stats.value = updated
    }

    fun updateSettings(newSettings: GameSettings) {
        prefs.edit()
            .putBoolean("soundEnabled", newSettings.soundEnabled)
            .putBoolean("vibrationEnabled", newSettings.vibrationEnabled)
            .putBoolean("reducedMotion", newSettings.reducedMotion)
            .putString("selectedCupTheme", newSettings.selectedCupTheme.id)
            .putString("selectedCoinTheme", newSettings.selectedCoinTheme.id)
            .putString("selectedShuffleTheme", newSettings.selectedShuffleTheme.id)
            .apply()

        _settings.value = newSettings
    }

    fun resetProgress() {
        prefs.edit().clear().apply()
        _stats.value = PlayerStats()
        _settings.value = GameSettings()
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    private fun isConsecutiveDay(lastDateStr: String, todayStr: String): Boolean {
        if (lastDateStr.isEmpty()) return false
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val lastDate = sdf.parse(lastDateStr) ?: return false
            val today = sdf.parse(todayStr) ?: return false
            val diffMs = today.time - lastDate.time
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            diffDays == 1L
        } catch (_: Exception) {
            false
        }
    }
}
