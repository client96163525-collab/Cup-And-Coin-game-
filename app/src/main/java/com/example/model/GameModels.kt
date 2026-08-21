package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class GameMode(
    val title: String,
    val subtitle: String,
    val description: String,
    val iconName: String
) {
    CLASSIC(
        title = "Classic",
        subtitle = "Levels 1 to 50+",
        description = "Progressive speed & swaps. Master the tracking art!",
        iconName = "🏆"
    ),
    TIME_ATTACK(
        title = "Time Attack",
        subtitle = "5 Seconds to Guess",
        description = "Quick reflexes needed! Guess before time expires.",
        iconName = "⚡"
    ),
    ENDLESS(
        title = "Endless",
        subtitle = "1 Mistake = Game Over",
        description = "How far can you survive under escalating chaos?",
        iconName = "🔥"
    ),
    PERFECT_RUN(
        title = "Perfect Run",
        subtitle = "Streak Master",
        description = "Chain consecutive correct answers for insane combos.",
        iconName = "🎯"
    ),
    DAILY_CHALLENGE(
        title = "Daily Challenge",
        subtitle = "Today's Puzzle",
        description = "One fixed daily puzzle for all players. Win unique glory!",
        iconName = "📅"
    )
}

enum class GameState {
    HOME,
    PREPARING,
    SHOW_COIN,
    HIDE_COIN,
    SHUFFLING,
    WAITING_FOR_GUESS,
    REVEALING,
    WIN,
    LOSE,
    GAME_OVER,
    ROUND_RESULT
}

/**
 * High-level phase classification for state transitions & rule enforcement
 */
enum class GamePhase {
    HOME,
    PREPARING,
    SHUFFLING,
    GUESSING,
    RESULT
}

data class SwapMove(
    val cup1: Int,
    val cup2: Int,
    val durationMs: Long,
    val isFakeShake: Boolean = false,
    val arcHeightRatio: Float = 0.35f // Positive for top arc, negative for bottom arc
)

data class ActiveSwap(
    val id: Long = System.currentTimeMillis(),
    val slotA: Int,
    val slotB: Int,
    val durationMs: Long,
    val arcHeightRatio: Float = 0.35f,
    val isFakeShake: Boolean = false
)

enum class CupTheme(
    val id: String,
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val rimColor: Color,
    val highlightColor: Color,
    val unlockLevel: Int
) {
    MINIMAL_VIOLET(
        id = "minimal_violet",
        displayName = "Minimal Violet",
        primaryColor = VioletPrimary,
        secondaryColor = VioletDeep,
        rimColor = LavenderAccent,
        highlightColor = LavenderLight,
        unlockLevel = 1
    ),
    ROYAL_CRIMSON(
        id = "royal_crimson",
        displayName = "Royal Crimson",
        primaryColor = RubyRed,
        secondaryColor = RubyDark,
        rimColor = VegasGold,
        highlightColor = BrightGold,
        unlockLevel = 5
    ),
    CYBER_NEON(
        id = "cyber_neon",
        displayName = "Cyber Neon",
        primaryColor = Color(0xFF0D47A1),
        secondaryColor = Color(0xFF001064),
        rimColor = NeonCyan,
        highlightColor = Color(0xFF80D8FF),
        unlockLevel = 10
    ),
    EMERALD_LUXE(
        id = "emerald_luxe",
        displayName = "Emerald Luxe",
        primaryColor = Color(0xFF1B5E20),
        secondaryColor = Color(0xFF003300),
        rimColor = VegasGold,
        highlightColor = EmeraldGreen,
        unlockLevel = 15
    ),
    SOLID_GOLD(
        id = "solid_gold",
        displayName = "Vegas Gold",
        primaryColor = VegasGold,
        secondaryColor = DarkGold,
        rimColor = Color(0xFFFFF9C4),
        highlightColor = Color(0xFFFFFFFF),
        unlockLevel = 20
    )
}

enum class CoinTheme(
    val id: String,
    val displayName: String,
    val symbol: String,
    val baseColor: Color,
    val accentColor: Color,
    val unlockScore: Int
) {
    GOLD_STAR(
        id = "gold_star",
        displayName = "Lucky Star",
        symbol = "★",
        baseColor = VegasGold,
        accentColor = DarkGold,
        unlockScore = 0
    ),
    DIAMOND(
        id = "diamond",
        displayName = "Diamond Gem",
        symbol = "💎",
        baseColor = Color(0xFF80DEEA),
        accentColor = Color(0xFF0097A7),
        unlockScore = 500
    ),
    BITCOIN(
        id = "bitcoin",
        displayName = "Crypto Coin",
        symbol = "₿",
        baseColor = Color(0xFFFFB300),
        accentColor = Color(0xFFFF6F00),
        unlockScore = 1500
    ),
    LUCKY_CLOVER(
        id = "lucky_clover",
        displayName = "Four-Leaf Clover",
        symbol = "☘",
        baseColor = Color(0xFF66BB6A),
        accentColor = Color(0xFF2E7D32),
        unlockScore = 3000
    ),
    ROYAL_CROWN(
        id = "royal_crown",
        displayName = "Royal Crown",
        symbol = "👑",
        baseColor = Color(0xFFFFD54F),
        accentColor = Color(0xFFFF8F00),
        unlockScore = 5000
    )
}

enum class ShuffleTheme(
    val id: String,
    val displayName: String,
    val description: String,
    val unlockLevel: Int,
    val effectEmoji: String
) {
    CLASSIC_SLIDE(
        id = "classic_slide",
        displayName = "Classic Slide",
        description = "Smooth horizontal arcs.",
        unlockLevel = 1,
        effectEmoji = "↔️"
    ),
    DOUBLE_SPIN_WAVE(
        id = "double_spin_wave",
        displayName = "3D Orbit Loop",
        description = "360° loops and circles.",
        unlockLevel = 4,
        effectEmoji = "🌀"
    ),
    COSMIC_ZIG_ZAG(
        id = "cosmic_zig_zag",
        displayName = "Zig-Zag Bounce",
        description = "Ultra rapid bouncing slides.",
        unlockLevel = 8,
        effectEmoji = "⚡"
    ),
    CHAOS_VORTEX(
        id = "chaos_vortex",
        displayName = "Chaos Vortex",
        description = "Suck inside spiral and explode.",
        unlockLevel = 12,
        effectEmoji = "🌪️"
    )
}

data class PlayerStats(
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val gamesLost: Int = 0,
    val bestScore: Int = 0,
    val highestLevel: Int = 1,
    val bestCombo: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastDailyDate: String = "",
    val dailyStreak: Int = 0,
    val completedDailyDates: Set<String> = emptySet(),
    val bestPerfectStreak: Int = 0,
    val shieldCount: Int = 0,
    val doubleScoreActive: Boolean = false,
    val lastSpinDate: String = "",
    // Mode-specific granular statistics
    val endlessStats: ModeStats = ModeStats(),
    val classicStats: ModeStats = ModeStats(),
    val timeAttackStats: ModeStats = ModeStats(),
    val perfectRunStats: ModeStats = ModeStats(),
    val dailyStats: ModeStats = ModeStats()
) {
    val winRatePercent: Int
        get() = if (gamesPlayed > 0) ((gamesWon.toDouble() / gamesPlayed) * 100).toInt() else 0
}

data class ModeStats(
    val totalRuns: Int = 0,
    val totalWins: Int = 0,
    val bestLevel: Int = 0,
    val bestScore: Int = 0,
    val bestStreak: Int = 0,
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val totalPlayTimeSec: Long = 0,
    val recentRuns: List<RunRecord> = emptyList()
) {
    val accuracyPercent: Int
        get() = if (totalCorrect + totalWrong > 0) 
            ((totalCorrect.toDouble() / (totalCorrect + totalWrong)) * 100).toInt() else 0
}

data class RunRecord(
    val level: Int,
    val score: Int,
    val timestamp: Long
)

data class GameSettings(
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val selectedCupTheme: CupTheme = CupTheme.MINIMAL_VIOLET,
    val selectedCoinTheme: CoinTheme = CoinTheme.GOLD_STAR,
    val selectedShuffleTheme: ShuffleTheme = ShuffleTheme.CLASSIC_SLIDE
)
