package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.GameAudioEngine
import com.example.data.GameRepository
import com.example.model.*
import com.example.util.DebugLogger
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
    val winStreak: Int = 0,
    val streakMultiplier: Float = 1.0f,
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
    val isArenaPreparing: Boolean = false,
    val roundStatusText: String = "Watch the coin! 👀",
    val timeAttackRemainingSec: Float = 5.0f,
    val timeAttackTotalSec: Float = 5.0f,
    val particleTrigger: Long = 0L,
    val roundResultTitle: String = "",
    val roundResultMessage: String = "",
    val roundScoreEarned: Int = 0,
    val roundMultiplierEarned: Float = 1.0f,
    val isDailyCompletedToday: Boolean = false,
    val isLuckySpinCompletedToday: Boolean = false,
    val perfectStreak: Int = 0,
    val cupCount: Int = 3
) {
    /**
     * High-level phase classification: Home, Preparing, Shuffling, Guessing, Result
     */
    val currentPhase: GamePhase
        get() = when (gameState) {
            GameState.HOME -> GamePhase.HOME
            GameState.PREPARING -> GamePhase.PREPARING
            GameState.SHOW_COIN, GameState.HIDE_COIN, GameState.SHUFFLING -> GamePhase.SHUFFLING
            GameState.WAITING_FOR_GUESS -> GamePhase.GUESSING
            GameState.REVEALING, GameState.WIN, GameState.LOSE, GameState.GAME_OVER, GameState.ROUND_RESULT -> GamePhase.RESULT
        }

    /**
     * Strict rule enforcement: Input is only allowed during WAITING_FOR_GUESS and when not shuffling
     */
    val isInputAllowed: Boolean
        get() = gameState == GameState.WAITING_FOR_GUESS && !isShuffling && currentSwap == null && !isArenaPreparing
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
    private var selectCupJob: Job? = null

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
        DebugLogger.i("GAME_VM", "startGame requested for mode=$mode")
        audioEngine.playTap()
        selectCupJob?.cancel()
        gameLoopJob?.cancel()
        timerJob?.cancel()

        val currentState = _uiState.value
        val resumeLevel = when (mode) {
            GameMode.CLASSIC -> repository.stats.value.highestLevel.coerceAtLeast(1)
            GameMode.TUTORIAL -> 1
            else -> 1
        }
        
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
                winStreak = 0,
                streakMultiplier = 1.0f,
                perfectStreak = if (mode == GameMode.PERFECT_RUN) 0 else currentState.perfectStreak,
                timeAttackRemainingSec = if (mode == GameMode.DAILY_CHALLENGE) 10f else 5f,
                gameState = GameState.PREPARING,
                isArenaPreparing = true,
                selectedSlotIndex = null,
                isRevealingWinningSlot = false,
                cupLiftAmounts = List(cupCount) { 0f },
                cupOffsetXs = List(cupCount) { 0f },
                cupOffsetYs = List(cupCount) { 0f },
                cupTilts = List(cupCount) { 0f },
                activeSwapPair = null,
                currentSwap = null,
                isShuffling = false,
                roundScoreEarned = 0,
                roundMultiplierEarned = 1.0f
            )
        }

        startRound(isRetry = false)
    }

    fun nextRound() {
        audioEngine.playTap()
        selectCupJob?.cancel()
        val currentMode = _uiState.value.gameMode
        val currentLevel = _uiState.value.currentLevel

        // If completed tutorial round 3, graduate player to Classic Mode
        if (currentMode == GameMode.TUTORIAL && currentLevel >= 3) {
            repository.recordTutorialCompleted()
            startGame(GameMode.CLASSIC)
            return
        }

        val nextLevel = currentLevel + 1
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
                currentSwap = null,
                isShuffling = false,
                roundScoreEarned = 0
            )
        }
        
        // Time Attack next round should be faster/instant
        val isTimeAttack = _uiState.value.gameMode == GameMode.TIME_ATTACK
        startRound(isRetry = false, skipInitialCoinShow = isTimeAttack)
    }

    fun retryRound() {
        audioEngine.playTap()
        selectCupJob?.cancel()
        val cupCount = _uiState.value.cupCount
        _uiState.update {
            it.copy(
                gameState = GameState.SHOW_COIN,
                selectedSlotIndex = null,
                isRevealingWinningSlot = false,
                cupLiftAmounts = List(cupCount) { 0f },
                cupOffsetXs = List(cupCount) { 0f },
                cupOffsetYs = List(cupCount) { 0f },
                cupTilts = List(cupCount) { 0f },
                activeSwapPair = null,
                currentSwap = null,
                isShuffling = false,
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
        selectCupJob?.cancel()
        gameLoopJob?.cancel()
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                gameState = GameState.HOME,
                selectedSlotIndex = null,
                isShuffling = false,
                activeSwapPair = null,
                currentSwap = null,
                isDailyCompletedToday = repository.isDailyChallengeCompletedToday()
            )
        }
    }

    private fun calculateStreakMultiplier(streak: Int): Float {
        return when {
            streak <= 1 -> 1.0f
            streak == 2 -> 1.5f
            streak == 3 -> 2.0f
            streak == 4 -> 2.5f
            streak == 5 -> 3.0f
            streak == 6 -> 3.5f
            streak == 7 -> 4.0f
            else -> (4.0f + (streak - 7) * 0.5f).coerceAtMost(5.0f)
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

            // If retry, randomize a fresh coin slot different from previous slot
            val initialCoinSlot = when {
                isRetry -> {
                    val prevSlot = currentState.coinSlotIndex
                    var newSlot = kotlin.random.Random.nextInt(cupCount)
                    if (cupCount > 1 && newSlot == prevSlot) {
                        newSlot = (prevSlot + 1) % cupCount
                    }
                    newSlot
                }
                mode == GameMode.TUTORIAL -> when (level) {
                    1 -> 1 // Middle cup for first tutorial step
                    2 -> 0 // Left cup for second tutorial step
                    else -> 1 // Middle cup for 3rd step
                }
                else -> kotlin.random.Random.nextInt(cupCount)
            }

            val tutorialShowText = when (level) {
                1 -> "👀 STEP 1/3: Watch the middle cup closely!"
                2 -> "👀 STEP 2/3: Now track 2 smooth swaps!"
                else -> "👀 STEP 3/3: Final Training! Track 3 fluid swaps!"
            }

            if (!skipInitialCoinShow) {
                // Subtle skeleton loading staging state before the first reveal/shuffle
                _uiState.update {
                    it.copy(
                        gameState = GameState.PREPARING,
                        isArenaPreparing = true,
                        coinSlotIndex = initialCoinSlot,
                        selectedSlotIndex = null,
                        isRevealingWinningSlot = false,
                        roundStatusText = if (mode == GameMode.TUTORIAL) "GETTING PRACTICE READY..." else "PREPARING ARENA...",
                        cupLiftAmounts = List(cupCount) { 0f },
                        cupOffsetXs = List(cupCount) { 0f },
                        cupOffsetYs = List(cupCount) { 0f },
                        cupTilts = List(cupCount) { 0f }
                    )
                }
                delay(450)
            }

            _uiState.update {
                it.copy(
                    gameState = if (skipInitialCoinShow) GameState.HIDE_COIN else GameState.SHOW_COIN,
                    isArenaPreparing = false,
                    coinSlotIndex = initialCoinSlot,
                    selectedSlotIndex = null,
                    isRevealingWinningSlot = false,
                    roundStatusText = if (skipInitialCoinShow) "GET READY..." else (if (mode == GameMode.TUTORIAL) tutorialShowText else "WATCH THE COIN! 👀"),
                    cupLiftAmounts = List(cupCount) { i -> if (!skipInitialCoinShow && initialCoinSlot == i) 1f else 0f },
                    cupOffsetXs = List(cupCount) { 0f },
                    cupOffsetYs = List(cupCount) { 0f },
                    cupTilts = List(cupCount) { 0f }
                )
            }

            if (!skipInitialCoinShow) {
                audioEngine.playCoinReveal()
                // Coin is visible longer in tutorial for comfortable learning
                val showDelay = if (mode == GameMode.TUTORIAL) 1700L else 1200L
                delay(showDelay)
            }

            // Step: Smooth coin sliding/dropping animation phase before cup covers it
            _uiState.update {
                it.copy(
                    gameState = GameState.HIDE_COIN,
                    roundStatusText = if (mode == GameMode.TUTORIAL) "COIN IS HIDDEN INSIDE..." else "COIN DROPPING..."
                )
            }
            delay(350)

            // Step: Drop cup to cover coin
            _uiState.update {
                it.copy(
                    cupLiftAmounts = List(cupCount) { 0f },
                    roundStatusText = if (mode == GameMode.TUTORIAL) "PREPARE TO TRACK..." else "GET READY..."
                )
            }
            audioEngine.playCupLand()
            delay(400)

            // Step: Perform Shuffles
            _uiState.update {
                it.copy(
                    gameState = GameState.SHUFFLING,
                    isShuffling = true,
                    roundStatusText = if (mode == GameMode.TUTORIAL) "👀 FOLLOW THE MOVING CUP..." else "FOLLOW THE COIN 👁️"
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

            val tutorialGuessText = when (level) {
                1 -> "👉 STEP 1/3: Tap the cup where the coin moved!"
                2 -> "👉 STEP 2/3: Followed both moves? Tap your pick!"
                else -> "👉 STEP 3/3: Where is the coin hidden? Make your pick!"
            }

            // Shuffle finished!
            _uiState.update {
                it.copy(
                    gameState = GameState.WAITING_FOR_GUESS,
                    isShuffling = false,
                    coinSlotIndex = currentCoinPos,
                    roundStatusText = if (mode == GameMode.TUTORIAL) tutorialGuessText else "WHERE IS THE COIN? 🤔",
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
        audioEngine.playShuffle(_uiState.value.gameMode)

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
        // Special case: 3 slow, crystal-clear practice rounds for Tutorial Mode
        if (mode == GameMode.TUTORIAL) {
            return when (level) {
                1 -> listOf(
                    // Round 1: 1 slow, deliberate swap from Middle (1) to Right (2)
                    SwapMove(
                        cup1 = 1,
                        cup2 = 2,
                        durationMs = 950L,
                        isFakeShake = false,
                        arcHeightRatio = 0.35f
                    )
                )
                2 -> listOf(
                    // Round 2: 2 clear sequential swaps (Left 0 <-> Middle 1, then Middle 1 <-> Right 2)
                    SwapMove(
                        cup1 = 0,
                        cup2 = 1,
                        durationMs = 850L,
                        isFakeShake = false,
                        arcHeightRatio = 0.35f
                    ),
                    SwapMove(
                        cup1 = 1,
                        cup2 = 2,
                        durationMs = 850L,
                        isFakeShake = false,
                        arcHeightRatio = -0.35f
                    )
                )
                else -> listOf(
                    // Round 3: 3 smooth 3D swaps to master all cup positions
                    SwapMove(
                        cup1 = 1,
                        cup2 = 0,
                        durationMs = 780L,
                        isFakeShake = false,
                        arcHeightRatio = 0.38f
                    ),
                    SwapMove(
                        cup1 = 0,
                        cup2 = 2,
                        durationMs = 780L,
                        isFakeShake = false,
                        arcHeightRatio = -0.38f
                    ),
                    SwapMove(
                        cup1 = 2,
                        cup2 = 1,
                        durationMs = 780L,
                        isFakeShake = false,
                        arcHeightRatio = 0.35f
                    )
                )
            }
        }

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
        var currentCoinTracking = initialCoin

        for (i in 0 until swapCount) {
            // Pick a pair that isn't identical to the immediately preceding one
            val availablePairs = pairs.filter { it != lastPair }
            val chosenPair = if (availablePairs.isNotEmpty()) {
                availablePairs[rnd.nextInt(availablePairs.size)]
            } else if (pairs.isNotEmpty()) {
                pairs[rnd.nextInt(pairs.size)]
            } else {
                0 to 1
            }
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

            currentCoinTracking = when (currentCoinTracking) {
                chosenPair.first -> chosenPair.second
                chosenPair.second -> chosenPair.first
                else -> currentCoinTracking
            }
        }

        // Guarantee coin finishes in a shuffled position different from starting position
        if (currentCoinTracking == initialCoin && cupCount > 1) {
            val altCup = (initialCoin + 1) % cupCount
            moves.add(
                SwapMove(
                    cup1 = initialCoin,
                    cup2 = altCup,
                    durationMs = baseDurationMs,
                    isFakeShake = false,
                    arcHeightRatio = 0.35f
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
        DebugLogger.d("GAME_VM", "onCupSelected: slotIndex=$slotIndex, isTimeout=$isTimeout, isInputAllowed=${currentState.isInputAllowed}, gameState=${currentState.gameState}")
        if (!currentState.isInputAllowed && !isTimeout) {
            DebugLogger.w("GAME_VM", "onCupSelected IGNORED: input not allowed")
            return
        }
        if (currentState.gameState != GameState.WAITING_FOR_GUESS) {
            DebugLogger.w("GAME_VM", "onCupSelected IGNORED: wrong gameState (${currentState.gameState})")
            return
        }
        if (selectCupJob?.isActive == true) {
            DebugLogger.w("GAME_VM", "onCupSelected IGNORED: selectCupJob already active")
            return
        }

        timerJob?.cancel()
        selectCupJob?.cancel()
        audioEngine.playTap()

        val cupCount = currentState.cupCount
        val safeSlotIndex = if (slotIndex in 0 until cupCount) slotIndex else -1
        val winningSlot = (currentState.coinSlotIndex % cupCount).coerceAtLeast(0)
        val isWin = safeSlotIndex == winningSlot && !isTimeout
        val mode = currentState.gameMode
        val level = currentState.currentLevel

        DebugLogger.i("GAME_RESULT", "Tapped Slot: $safeSlotIndex | Winning Slot: $winningSlot | Mode: $mode | Level: $level | Result: ${if (isWin) "WIN 🏆" else "LOSE ❌"}")

        // Synchronously update UI state immediately to REVEALING and set selectedSlotIndex.
        // This IMMEDIATELY sets isInputAllowed = false so no rapid duplicate taps can execute.
        val initialLifts = MutableList(cupCount) { 0f }
        if (safeSlotIndex in 0 until cupCount) {
            initialLifts[safeSlotIndex] = 1f
        }

        // Mode specific scoring and streak logic
        var newCombo = currentState.combo
        var newPerfectStreak = currentState.perfectStreak
        var score = currentState.score
        var isGameOver = false
        var isShieldUsed = false
        var roundScore = 0
        var newWinStreak = currentState.winStreak
        var newMultiplier = currentState.streakMultiplier

        if (isWin) {
            newCombo++
            newPerfectStreak++
            newWinStreak++
            newMultiplier = calculateStreakMultiplier(newWinStreak)

            val basePoints = 100 + (level * 10)
            val comboBonus = (newCombo - 1).coerceAtLeast(0) * 25
            val timeBonus = if (mode == GameMode.TIME_ATTACK) (currentState.timeAttackRemainingSec * 30).toInt() else 0
            val powerupMultiplier = if (repository.stats.value.doubleScoreActive) 2 else 1
            
            roundScore = (((basePoints + comboBonus + timeBonus) * newMultiplier) * powerupMultiplier).toInt()
            score += roundScore
            
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
                _uiState.update { it.copy(timeAttackRemainingSec = (it.timeAttackRemainingSec - 3f).coerceAtLeast(0f)) }
            }
            
            if (!isShieldUsed) {
                newCombo = 0
                newPerfectStreak = 0
                newWinStreak = 0
                newMultiplier = 1.0f
            }
        }

        _uiState.update {
            it.copy(
                gameState = GameState.REVEALING,
                selectedSlotIndex = if (safeSlotIndex in 0 until cupCount) safeSlotIndex else null,
                cupLiftAmounts = initialLifts,
                roundStatusText = if (isWin) "YOU FOUND IT! 🎉" else if (isTimeout) "TIME EXPIRED! ⏱️" else "REVEALING... 🪙"
            )
        }

        selectCupJob = viewModelScope.launch {
            if (isWin) {
                audioEngine.playWin(mode)
                delay(120)
                audioEngine.playCoinCollect()
                if (newWinStreak >= 2) {
                    delay(150)
                    audioEngine.playComboMultiplier()
                }
            }

            val resultTitle = when {
                mode == GameMode.TUTORIAL -> when {
                    isWin && level == 1 -> "STEP 1 COMPLETE! 🎯"
                    isWin && level == 2 -> "STEP 2 COMPLETE! 🌟"
                    isWin && level >= 3 -> "🎓 TRAINING COMPLETED!"
                    level == 1 -> "STEP 1: NICE TRY!"
                    level == 2 -> "STEP 2: SO CLOSE!"
                    else -> "STEP 3: ALMOST THERE!"
                }
                isGameOver -> "GAME OVER"
                isWin && newWinStreak >= 2 -> "🔥 ${newMultiplier}x STREAK MULTIPLIER!"
                isWin -> "ROUND COMPLETE!"
                isTimeout -> "TIME EXPIRED!"
                else -> "MISS!"
            }

            val resultMessage = when {
                mode == GameMode.TUTORIAL -> when {
                    isWin && level == 1 -> "Great eye! You tracked 1 slow swap. Ready for Step 2 with 2 swaps?"
                    isWin && level == 2 -> "Awesome focus! You tracked two consecutive swaps. Final test next!"
                    isWin && level >= 3 -> "Mastered! You've learned how to track moving cups and spot the coin. +300 Beginner Coins rewarded!"
                    level == 1 -> "Keep your eyes locked on the moving cup base. Tap Retry to try Step 1 again!"
                    level == 2 -> "Watch the trajectory of both swaps closely. Tap Retry to practice Step 2!"
                    else -> "Take a deep breath and follow the 3D orbital swaps. Tap Retry to master Step 3!"
                }
                isWin && newWinStreak >= 2 -> "+$roundScore PTS • $newWinStreak-Win Streak (${newMultiplier}x Boost)"
                isWin -> "+$roundScore PTS Earned • Great guess!"
                isShieldUsed -> "Shield protected your streak! Safe to continue."
                isGameOver -> "Final Score: $score • Reached Level $level"
                else -> "The coin was under Cup ${winningSlot + 1}"
            }

            _uiState.update {
                it.copy(
                    gameState = if (isGameOver) GameState.GAME_OVER else (if (isWin) GameState.WIN else GameState.LOSE),
                    selectedSlotIndex = if (safeSlotIndex in 0 until cupCount) safeSlotIndex else null,
                    cupLiftAmounts = initialLifts,
                    combo = newCombo,
                    perfectStreak = newPerfectStreak,
                    winStreak = newWinStreak,
                    streakMultiplier = newMultiplier,
                    roundScoreEarned = roundScore,
                    roundMultiplierEarned = newMultiplier,
                    roundResultTitle = resultTitle,
                    roundResultMessage = resultMessage,
                    score = score,
                    particleTrigger = if (isWin) System.currentTimeMillis() else it.particleTrigger,
                    roundStatusText = when {
                        isShieldUsed -> "🛡️ SHIELD SAVED YOU! 🛡️"
                        isGameOver -> "GAME OVER!"
                        isWin && newWinStreak >= 2 -> "🔥 ${newWinStreak}X STREAK! +$roundScore PTS"
                        isWin -> "YOU FOUND IT! 🎉" 
                        isTimeout -> "TIME EXPIRED! ⏱️" 
                        else -> "THE COIN WAS HERE! 🪙"
                    }
                )
            }

            // If user lost, reveal the actual winning cup after a brief suspense pause!
            if (!isWin) {
                delay(350)
                audioEngine.playLose(mode)
                val revealLifts = initialLifts.toMutableList()
                if (winningSlot in 0 until cupCount) {
                    revealLifts[winningSlot] = 1f
                }
                _uiState.update { it.copy(cupLiftAmounts = revealLifts) }
                audioEngine.playCoinReveal()
            }
            
            if (isWin && mode == GameMode.TIME_ATTACK) {
                // Add 2 seconds
                _uiState.update { it.copy(timeAttackRemainingSec = it.timeAttackRemainingSec + 2f) }
            }
            
            if (isWin && mode == GameMode.DAILY_CHALLENGE) {
                repository.recordDailyChallengeCompletion()
                _uiState.update { it.copy(isDailyCompletedToday = true) }
            }

            if (isWin && mode == GameMode.TUTORIAL && level >= 3) {
                repository.recordTutorialCompleted()
            }
            
            // Record game round stats in local storage
            repository.recordGameRound(
                isWin = isWin,
                score = score,
                level = level,
                combo = newCombo,
                mode = mode,
                streak = if (mode == GameMode.PERFECT_RUN) newPerfectStreak else (if (isWin) repository.stats.value.currentStreak + 1 else 0)
            )
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

    fun selectAppTheme(theme: com.example.model.AppTheme) {
        val updated = settings.value.copy(appTheme = theme)
        repository.updateSettings(updated)
        audioEngine.playTap()
    }

    fun spinLuckyWheel(rewardType: String) {
        repository.recordLuckySpin(rewardType)
        _uiState.update { it.copy(isLuckySpinCompletedToday = true) }
        audioEngine.playRewardClaim()
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
