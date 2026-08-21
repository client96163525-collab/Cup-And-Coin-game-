package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.GameAudioEngine
import com.example.data.GameRepository
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Random

data class GameUiState(
    val gameState: GameState = GameState.HOME,
    val gameMode: GameMode = GameMode.CLASSIC,
    val currentLevel: Int = 1,
    val score: Int = 0,
    val combo: Int = 0,
    val coinSlotIndex: Int = 1, // 0 = Left, 1 = Center, 2 = Right
    val selectedSlotIndex: Int? = null,
    val isRevealingWinningSlot: Boolean = false,
    val cupLiftAmounts: List<Float> = listOf(0f, 0f, 0f), // 0 to 1 for each slot
    val cupOffsetXs: List<Float> = listOf(0f, 0f, 0f), // Horizontal shift in DP/fraction
    val cupOffsetYs: List<Float> = listOf(0f, 0f, 0f), // Arc lift in DP
    val cupTilts: List<Float> = listOf(0f, 0f, 0f),
    val activeSwapPair: Pair<Int, Int>? = null,
    val currentSwap: ActiveSwap? = null,
    val isShuffling: Boolean = false,
    val roundStatusText: String = "Watch the coin! 👀",
    val timeAttackRemainingSec: Float = 5.0f,
    val timeAttackTotalSec: Float = 5.0f,
    val particleTrigger: Long = 0L,
    val roundResultTitle: String = "",
    val roundResultMessage: String = "",
    val roundScoreEarned: Int = 0,
    val isDailyCompletedToday: Boolean = false,
    val isLuckySpinCompletedToday: Boolean = false,
    val perfectStreak: Int = 0,
    val cupCount: Int = 3
) {
    /**
     * High-level phase classification: Home, Shuffling, Guessing, Result
     */
    val currentPhase: GamePhase
        get() = when (gameState) {
            GameState.HOME -> GamePhase.HOME
            GameState.SHOW_COIN, GameState.HIDE_COIN, GameState.SHUFFLING -> GamePhase.SHUFFLING
            GameState.WAITING_FOR_GUESS -> GamePhase.GUESSING
            GameState.REVEALING, GameState.WIN, GameState.LOSE, GameState.GAME_OVER, GameState.ROUND_RESULT -> GamePhase.RESULT
        }

    /**
     * Strict rule enforcement: Input is only allowed during WAITING_FOR_GUESS and when not shuffling
     */
    val isInputAllowed: Boolean
        get() = gameState == GameState.WAITING_FOR_GUESS && !isShuffling && currentSwap == null
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val repository = GameRepository(application.applicationContext)
    val audioEngine = GameAudioEngine(application.applicationContext)

    val stats: StateFlow<PlayerStats> = repository.stats
    val settings: StateFlow<GameSettings> = repository.settings

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var dailyChallengeTimerJob: Job? = null
    private var gameLoopJob: Job? = null

    init {
        viewModelScope.launch {
            settings.collect { s ->
                audioEngine.isSoundEnabled = s.soundEnabled
                audioEngine.isVibrationEnabled = s.vibrationEnabled
            }
        }
        _uiState.update { 
            it.copy(
                isDailyCompletedToday = repository.isDailyChallengeCompletedToday(),
                isLuckySpinCompletedToday = !repository.isLuckySpinAvailableToday()
            )
        }
    }

    fun startGame(mode: GameMode) {
        audioEngine.playTap()
        gameLoopJob?.cancel()
        timerJob?.cancel()

        val currentState = _uiState.value
        val resumeLevel = if (mode == GameMode.CLASSIC) repository.stats.value.highestLevel.coerceAtLeast(1) else 1
        
        val cupCount = when {
            mode == GameMode.ENDLESS && resumeLevel > 50 -> 5
            mode == GameMode.ENDLESS && resumeLevel > 20 -> 4
            else -> 3
        }

        _uiState.update {
            it.copy(
                gameMode = mode,
                currentLevel = resumeLevel,
                cupCount = cupCount,
                score = 0,
                combo = 0,
                perfectStreak = if (mode == GameMode.PERFECT_RUN) 0 else currentState.perfectStreak,
                timeAttackRemainingSec = if (mode == GameMode.DAILY_CHALLENGE) 10f else 5f,
                gameState = GameState.SHOW_COIN,
                selectedSlotIndex = null,
                isRevealingWinningSlot = false,
                cupLiftAmounts = List(cupCount) { 0f },
                cupOffsetXs = List(cupCount) { 0f },
                cupOffsetYs = List(cupCount) { 0f },
                cupTilts = List(cupCount) { 0f },
                activeSwapPair = null,
                roundScoreEarned = 0
            )
        }

        startRound(isRetry = false)
    }

    fun nextRound() {
        audioEngine.playTap()
        val nextLevel = _uiState.value.currentLevel + 1
        val cupCount = when {
            _uiState.value.gameMode == GameMode.ENDLESS && nextLevel > 50 -> 5
            _uiState.value.gameMode == GameMode.ENDLESS && nextLevel > 20 -> 4
            else -> 3
        }

        _uiState.update {
            it.copy(
                currentLevel = nextLevel,
                cupCount = cupCount,
                gameState = GameState.SHOW_COIN,
                selectedSlotIndex = null,
                isRevealingWinningSlot = false,
                cupLiftAmounts = List(cupCount) { 0f },
                cupOffsetXs = List(cupCount) { 0f },
                cupOffsetYs = List(cupCount) { 0f },
                cupTilts = List(cupCount) { 0f },
                activeSwapPair = null,
                roundScoreEarned = 0
            )
        }
        
        // Time Attack next round should be faster/instant
        val isTimeAttack = _uiState.value.gameMode == GameMode.TIME_ATTACK
        startRound(isRetry = false, skipInitialCoinShow = isTimeAttack)
    }

    fun retryRound() {
        audioEngine.playTap()
        _uiState.update {
            it.copy(
                gameState = GameState.SHOW_COIN,
                selectedSlotIndex = null,
                isRevealingWinningSlot = false,
                cupOffsetXs = listOf(0f, 0f, 0f),
                cupOffsetYs = listOf(0f, 0f, 0f),
                cupTilts = listOf(0f, 0f, 0f),
                activeSwapPair = null,
                roundScoreEarned = 0
            )
        }
        startRound(isRetry = true)
    }

    fun restartCurrentGame() {
        startGame(_uiState.value.gameMode)
    }

    fun returnToHome() {
        audioEngine.playTap()
        gameLoopJob?.cancel()
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                gameState = GameState.HOME,
                isDailyCompletedToday = repository.isDailyChallengeCompletedToday()
            )
        }
    }

    private fun startRound(isRetry: Boolean = false, skipInitialCoinShow: Boolean = false) {
        gameLoopJob?.cancel()
        timerJob?.cancel()

        gameLoopJob = viewModelScope.launch {
            val currentState = _uiState.value
            val level = currentState.currentLevel
            val mode = currentState.gameMode
            val cupCount = currentState.cupCount

            // If retry, keep exact same winning coin slot so the coin stays in the same box!
            val initialCoinSlot = if (isRetry) currentState.coinSlotIndex else kotlin.random.Random.nextInt(cupCount)

            _uiState.update {
                it.copy(
                    gameState = if (skipInitialCoinShow) GameState.HIDE_COIN else GameState.SHOW_COIN,
                    coinSlotIndex = initialCoinSlot,
                    selectedSlotIndex = null,
                    isRevealingWinningSlot = false,
                    roundStatusText = if (skipInitialCoinShow) "GET READY..." else "WATCH THE COIN! 👀",
                    cupLiftAmounts = List(cupCount) { i -> if (!skipInitialCoinShow && initialCoinSlot == i) 1f else 0f },
                    cupOffsetXs = List(cupCount) { 0f },
                    cupOffsetYs = List(cupCount) { 0f },
                    cupTilts = List(cupCount) { 0f }
                )
            }

            if (!skipInitialCoinShow) {
                audioEngine.playCoinReveal()
                // Coin is visible for ~1.2 seconds
                delay(1200)
            }

            // Step: Smooth coin sliding/dropping animation phase before cup covers it
            _uiState.update {
                it.copy(
                    gameState = GameState.HIDE_COIN,
                    roundStatusText = "COIN DROPPING..."
                )
            }
            delay(350)

            // Step: Drop cup to cover coin
            _uiState.update {
                it.copy(
                    cupLiftAmounts = listOf(0f, 0f, 0f),
                    roundStatusText = "GET READY..."
                )
            }
            audioEngine.playCupLand()
            delay(400)

            // Step: Perform Shuffles
            _uiState.update {
                it.copy(
                    gameState = GameState.SHUFFLING,
                    isShuffling = true,
                    roundStatusText = "FOLLOW THE COIN 👁️"
                )
            }

            val moves = generateShuffleSequence(level, mode, cupCount, initialCoinSlot)
            var currentCoinPos = initialCoinSlot

            for (move in moves) {
                if (move.isFakeShake) {
                    performFakeShake(move.cup1)
                } else {
                    currentCoinPos = performSwap(move, currentCoinPos)
                }
            }

            // Shuffle finished!
            _uiState.update {
                it.copy(
                    gameState = GameState.WAITING_FOR_GUESS,
                    isShuffling = false,
                    coinSlotIndex = currentCoinPos,
                    roundStatusText = "WHERE IS THE COIN? 🤔",
                    timeAttackRemainingSec = 5.0f
                )
            }

            // If Time Attack mode, start countdown
            if (mode == GameMode.TIME_ATTACK) {
                startTimeAttackCountdown()
            }
        }
    }

    private suspend fun performSwap(move: SwapMove, currentCoinPos: Int): Int {
        val (slotA, slotB) = move.cup1 to move.cup2
        val duration = if (settings.value.reducedMotion) (move.durationMs * 1.3f).toLong() else move.durationMs
        audioEngine.playCupMove()

        val swap = ActiveSwap(
            id = System.nanoTime(),
            slotA = slotA,
            slotB = slotB,
            durationMs = duration,
            arcHeightRatio = move.arcHeightRatio,
            isFakeShake = false
        )

        _uiState.update {
            it.copy(
                activeSwapPair = slotA to slotB,
                currentSwap = swap
            )
        }

        // Wait for the Compose animation duration
        delay(duration)

        // Update coin position mathematically
        val newCoinPos = when (currentCoinPos) {
            slotA -> slotB
            slotB -> slotA
            else -> currentCoinPos
        }

        // Reset active swap state after transition completes, and tightly bind the coin index
        _uiState.update { state ->
            val cupCount = state.cupCount
            state.copy(
                cupOffsetXs = List(cupCount) { 0f },
                cupOffsetYs = List(cupCount) { 0f },
                cupTilts = List(cupCount) { 0f },
                activeSwapPair = null,
                currentSwap = null,
                coinSlotIndex = newCoinPos
            )
        }
        audioEngine.playCupLand()

        // Small inter-swap breath
        delay((duration * 0.15f).toLong().coerceIn(15, 60))
        return newCoinPos
    }

    private suspend fun performFakeShake(slot: Int) {
        audioEngine.playCupMove()
        val swap = ActiveSwap(
            id = System.nanoTime(),
            slotA = slot,
            slotB = slot,
            durationMs = 200L,
            arcHeightRatio = 0f,
            isFakeShake = true
        )
        _uiState.update {
            it.copy(
                currentSwap = swap
            )
        }
        delay(200L)
        _uiState.update { state ->
            val cupCount = state.cupCount
            state.copy(
                currentSwap = null,
                cupTilts = List(cupCount) { 0f }
            )
        }
        audioEngine.playCupLand()
        delay(40L)
    }

    private fun generateShuffleSequence(level: Int, mode: GameMode, cupCount: Int, initialCoin: Int): List<SwapMove> {
        val moves = mutableListOf<SwapMove>()
        
        // Use date-based seed for Daily Challenge
        val seed = if (mode == GameMode.DAILY_CHALLENGE) {
            val date = java.time.LocalDate.now().toEpochDay()
            date
        } else {
            System.currentTimeMillis()
        }
        val rnd = Random(seed)

        // Difficulty scaling for different modes
        val swapCount = when (mode) {
            GameMode.DAILY_CHALLENGE -> 7 + (level % 3)
            GameMode.ENDLESS -> when {
                level in 1..5 -> 2 + rnd.nextInt(4) // 2-5 swaps
                level in 6..10 -> 4 + rnd.nextInt(4) // 4-7 swaps
                level in 11..15 -> 6 + rnd.nextInt(4) // 6-9 swaps
                level in 16..20 -> 7 + rnd.nextInt(5) // 7-11 swaps
                level in 21..30 -> 9 + rnd.nextInt(5) // 9-13 swaps
                level in 31..40 -> 10 + rnd.nextInt(6) // 10-15 swaps
                level in 41..50 -> 12 + rnd.nextInt(6) // 12-17 swaps
                else -> 12 + rnd.nextInt(6) // Controlled random for 50+
            }
            else -> when { // Classic/Other
                level in 1..3 -> 3 + level
                level in 4..7 -> 7 + (level - 3)
                level in 8..12 -> 11 + (level - 7)
                level in 13..20 -> 16 + (level - 12)
                else -> (20 + rnd.nextInt(8)).coerceAtMost(35)
            }
        }

        val baseDurationMs = when {
            mode == GameMode.DAILY_CHALLENGE -> 380L
            level in 1..3 -> (650L - (level * 30L)).coerceAtLeast(520L) // Smooth & comfortable tracking
            level in 4..7 -> (520L - ((level - 3) * 20L)).coerceAtLeast(420L)
            level in 8..12 -> (410L - ((level - 7) * 15L)).coerceAtLeast(320L)
            level in 13..20 -> (310L - ((level - 12) * 8L)).coerceAtLeast(240L)
            else -> (230L - (level * 2L)).coerceAtLeast(160L)
        }

        val pairs = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until cupCount) {
            for (j in i + 1 until cupCount) {
                pairs.add(i to j)
            }
        }

        var lastPair: Pair<Int, Int>? = null

        for (i in 0 until swapCount) {
            // Pick a pair that isn't identical to the immediately preceding one
            val availablePairs = pairs.filter { it != lastPair }
            val chosenPair = availablePairs[rnd.nextInt(availablePairs.size)]
            lastPair = chosenPair

            // Add occasional fake shake on higher levels (>10)
            val shouldFake = level >= 10 && i > 0 && i % 4 == 0 && rnd.nextBoolean()
            if (shouldFake) {
                moves.add(
                    SwapMove(
                        cup1 = rnd.nextInt(cupCount),
                        cup2 = rnd.nextInt(cupCount),
                        durationMs = 180L,
                        isFakeShake = true
                    )
                )
            }

            val varDuration = (baseDurationMs + rnd.nextInt(60) - 30).coerceAtLeast(140L)
            val arcDir = if (i % 2 == 0) 0.35f else -0.35f

            moves.add(
                SwapMove(
                    cup1 = chosenPair.first,
                    cup2 = chosenPair.second,
                    durationMs = varDuration,
                    isFakeShake = false,
                    arcHeightRatio = arcDir
                )
            )
        }

        return moves
    }

    private fun startTimeAttackCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val totalSteps = 50
            val stepTime = 100L // 5.0 seconds total

            for (i in totalSteps downTo 0) {
                val remaining = i * 0.1f
                _uiState.update { it.copy(timeAttackRemainingSec = remaining) }
                if (i in 1..30 && i % 3 == 0) {
                    audioEngine.playCountdownTick()
                }
                delay(stepTime)
            }

            // Timeout occurred!
            if (_uiState.value.gameState == GameState.WAITING_FOR_GUESS) {
                onCupSelected(slotIndex = -1, isTimeout = true)
            }
        }
    }

    fun onCupSelected(slotIndex: Int, isTimeout: Boolean = false) {
        val currentState = _uiState.value
        if (!currentState.isInputAllowed && !isTimeout) return
        if (currentState.gameState != GameState.WAITING_FOR_GUESS) return

        timerJob?.cancel()
        audioEngine.playTap()

        val isWin = slotIndex == currentState.coinSlotIndex && !isTimeout
        val mode = currentState.gameMode
        val level = currentState.currentLevel

        // Mode specific scoring and streak logic
        var newCombo = currentState.combo
        var newPerfectStreak = currentState.perfectStreak
        var score = currentState.score
        var isGameOver = false
        var isShieldUsed = false

        if (isWin) {
            newCombo++
            newPerfectStreak++
            val basePoints = 100
            val multiplier = if (repository.stats.value.doubleScoreActive) 2 else 1
            val comboBonus = (newCombo - 1).coerceAtLeast(0) * 50
            val timeBonus = if (mode == GameMode.TIME_ATTACK) (currentState.timeAttackRemainingSec * 30).toInt() else 0
            score += (basePoints + comboBonus + timeBonus) * multiplier
            
            if (repository.stats.value.doubleScoreActive) {
                repository.consumeDoubleScore()
            }
        } else {
            // Endless/Perfect Run/Time Attack specific failure rules
            if (mode == GameMode.ENDLESS || mode == GameMode.PERFECT_RUN) {
                if (repository.stats.value.shieldCount > 0) {
                    isShieldUsed = true
                    repository.useShield()
                } else {
                    isGameOver = true
                }
            }
            // Time Attack penalty
            if (mode == GameMode.TIME_ATTACK) {
                // Deduct 3 seconds
                _uiState.update { it.copy(timeAttackRemainingSec = (it.timeAttackRemainingSec - 3f).coerceAtLeast(0f)) }
            }
            newCombo = 0
            newPerfectStreak = 0
        }

        viewModelScope.launch {
            // Lift the selected cup (or timeout auto-reveal)
            val lifts = MutableList(3) { 0f }
            if (slotIndex in 0..2) {
                lifts[slotIndex] = 1f
            }

            _uiState.update {
                it.copy(
                    gameState = if (isGameOver) GameState.GAME_OVER else (if (isWin) GameState.WIN else GameState.LOSE),
                    selectedSlotIndex = if (slotIndex in 0..2) slotIndex else null,
                    cupLiftAmounts = lifts,
                    combo = newCombo,
                    perfectStreak = newPerfectStreak,
                    score = score,
                    roundStatusText = when {
                        isShieldUsed -> "🛡️ SHIELD SAVED YOU! 🛡️"
                        isGameOver -> "GAME OVER!"
                        isWin -> "YOU FOUND IT! 🎉" 
                        isTimeout -> "TIME EXPIRED! ⏱️" 
                        else -> "MISS! ❌"
                    }
                )
            }
            
            if (isWin && mode == GameMode.TIME_ATTACK) {
                // Add 2 seconds
                _uiState.update { it.copy(timeAttackRemainingSec = it.timeAttackRemainingSec + 2f) }
            }
            
            if (isWin && mode == GameMode.DAILY_CHALLENGE) {
                repository.recordDailyChallengeCompletion()
                _uiState.update { it.copy(isDailyCompletedToday = true) }
            }
            
            if (!isGameOver) {
                // Keep the result visible briefly before potentially moving to next round
            } else {
                // Record Game Over stats for Endless/Perfect Run
                repository.recordGameRound(isWin = false, score = score, level = level, combo = 0, mode = mode, streak = newPerfectStreak)
            }
        }
    }

    fun toggleSound() {
        val updated = settings.value.copy(soundEnabled = !settings.value.soundEnabled)
        repository.updateSettings(updated)
        audioEngine.playTap()
    }

    fun toggleVibration() {
        val updated = settings.value.copy(vibrationEnabled = !settings.value.vibrationEnabled)
        repository.updateSettings(updated)
        audioEngine.playTap()
    }

    fun toggleReducedMotion() {
        val updated = settings.value.copy(reducedMotion = !settings.value.reducedMotion)
        repository.updateSettings(updated)
        audioEngine.playTap()
    }

    fun selectCupTheme(theme: CupTheme) {
        val updated = settings.value.copy(selectedCupTheme = theme)
        repository.updateSettings(updated)
        audioEngine.playTap()
    }

    fun selectCoinTheme(theme: CoinTheme) {
        val updated = settings.value.copy(selectedCoinTheme = theme)
        repository.updateSettings(updated)
        audioEngine.playTap()
    }

    fun selectShuffleTheme(theme: ShuffleTheme) {
        val updated = settings.value.copy(selectedShuffleTheme = theme)
        repository.updateSettings(updated)
        audioEngine.playTap()
    }

    fun spinLuckyWheel(rewardType: String) {
        repository.recordLuckySpin(rewardType)
        _uiState.update { it.copy(isLuckySpinCompletedToday = true) }
        audioEngine.playCoinReveal()
    }

    fun resetProgress() {
        repository.resetProgress()
        audioEngine.playTap()
        _uiState.update {
            it.copy(
                score = 0,
                combo = 0,
                currentLevel = 1,
                isDailyCompletedToday = false,
                isLuckySpinCompletedToday = false
            )
        }
    }
}
