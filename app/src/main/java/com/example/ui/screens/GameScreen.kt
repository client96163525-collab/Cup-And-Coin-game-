package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.CoinVisual
import com.example.ui.components.CupVisual
import com.example.ui.components.ParticleBurst
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameUiState

@Composable
fun GameScreen(
    uiState: GameUiState,
    settings: GameSettings,
    stats: PlayerStats,
    onCupSelected: (Int) -> Unit,
    onNextRound: () -> Unit,
    onRetryRound: () -> Unit,
    onRestartGame: () -> Unit,
    onReturnToHome: () -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Clean Minimalism Top App Bar & Stats Header
            GameHeader(
                uiState = uiState,
                stats = stats,
                settings = settings,
                onBackClick = { showExitConfirmDialog = true },
                onToggleSound = onToggleSound
            )

            // Active Items Indicator Row (Glows when active!)
            if (stats.shieldCount > 0 || stats.doubleScoreActive) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (stats.shieldCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🛡️", fontSize = 12.sp)
                            Text(
                                text = "SHIELD: ${stats.shieldCount}",
                                color = VioletPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    if (stats.doubleScoreActive) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🔥", fontSize = 12.sp)
                            Text(
                                text = "2X ACTIVE",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // Time Attack Progress Bar (if in Time Attack Mode)
            if (uiState.gameMode == GameMode.TIME_ATTACK && uiState.gameState == GameState.WAITING_FOR_GUESS) {
                Spacer(modifier = Modifier.height(10.dp))
                TimeAttackTimerBar(
                    remainingSec = uiState.timeAttackRemainingSec,
                    totalSec = uiState.timeAttackTotalSec
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Combo Banner (Clean Minimalism Hero Stat)
            when (uiState.gameMode) {
                GameMode.PERFECT_RUN -> PerfectStreakCounter(streak = uiState.perfectStreak)
                else -> ComboCounterHeader(combo = uiState.combo)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // The Clean Arena with the 3 Shuffling Cups
            FeltTableArena(
                uiState = uiState,
                settings = settings,
                onCupSelected = onCupSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Suspense Guidance Banner
            StatusPromptBanner(
                text = uiState.roundStatusText,
                gameState = uiState.gameState
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Action Area & Result Sheet
            BottomGameControls(
                uiState = uiState,
                stats = stats,
                onNextRound = onNextRound,
                onRetryRound = onRetryRound,
                onRestartGame = onRestartGame,
                onReturnToHome = onReturnToHome,
                onCupSelected = onCupSelected
            )
        }

        // Win Confetti Particle Burst
        ParticleBurst(
            trigger = uiState.particleTrigger,
            modifier = Modifier.fillMaxSize()
        )

        // Confirm Exit Dialog (3D Frosted Theme)
        if (showExitConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showExitConfirmDialog = false },
                icon = {
                    Surface(
                        shape = CircleShape,
                        color = RubyRed.copy(alpha = 0.2f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = RubyRed,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "Leave Current Game?",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "Your current progress in ${uiState.gameMode.title} mode will be lost if you return to the main menu.",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitConfirmDialog = false
                            onReturnToHome()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RubyDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Yes, Quit", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showExitConfirmDialog = false },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LavenderAccent),
                        border = ButtonDefaults.outlinedButtonBorder().copy(
                            brush = Brush.linearGradient(listOf(LavenderAccent, LavenderAccent))
                        ),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Keep Playing", fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = MinimalSurface,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
private fun ComboCounterHeader(combo: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = "CURRENT COMBO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = LavenderAccent.copy(alpha = 0.85f),
            letterSpacing = 2.sp
        )
        Text(
            text = "x${combo.coerceAtLeast(1)}",
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = (-1.5).sp,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

@Composable
private fun PerfectStreakCounter(streak: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = "PERFECT STREAK",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = VegasGold.copy(alpha = 0.85f),
            letterSpacing = 2.sp
        )
        Text(
            text = "🔥 $streak",
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            color = VegasGold,
            letterSpacing = (-1.5).sp
        )
    }
}

@Composable
private fun GameHeader(
    uiState: GameUiState,
    stats: PlayerStats,
    settings: GameSettings,
    onBackClick: () -> Unit,
    onToggleSound: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Back Icon Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MinimalSurface)
                .border(1.dp, MinimalBorder, RoundedCornerShape(12.dp))
                .testTag("btn_back_to_menu")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = LavenderAccent,
                modifier = Modifier.size(20.dp)
            )
        }

        // High Score / Mode Header Info
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "HIGH SCORE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "${stats.bestScore.coerceAtLeast(uiState.score)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = LavenderAccent,
                letterSpacing = (-0.5).sp
            )
        }

        // Level Pill (Clean Minimalism Badge)
        Surface(
            shape = CircleShape,
            color = MinimalSurface,
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(MinimalBorder, MinimalBorder)))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "LEVEL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LavenderLight,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "${uiState.currentLevel}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LavenderAccent
                )
            }
        }
    }
}

@Composable
private fun TimeAttackTimerBar(
    remainingSec: Float,
    totalSec: Float
) {
    val progress = (remainingSec / totalSec).coerceIn(0f, 1f)
    val barColor = if (progress > 0.4f) EmeraldGreen else if (progress > 0.2f) VegasGold else RubyRed

    Column(
        modifier = Modifier.fillMaxWidth(0.9f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "TIME TO GUESS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            Text(
                text = String.format("%.1fs", remainingSec),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = CardSurfaceElevated
        )
    }
}

@Composable
private fun StatusPromptBanner(
    text: String,
    gameState: GameState
) {
    val isInteractive = gameState == GameState.WAITING_FOR_GUESS

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = when (gameState) {
            GameState.WIN -> WinGreen.copy(alpha = 0.15f)
            GameState.LOSE, GameState.GAME_OVER -> LoseRed.copy(alpha = 0.15f)
            GameState.WAITING_FOR_GUESS -> MinimalSurfaceElevated
            else -> MinimalSurface
        },
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                when (gameState) {
                    GameState.WIN -> listOf(WinGreen, EmeraldGreen)
                    GameState.LOSE, GameState.GAME_OVER -> listOf(LoseRed, RubyRed)
                    GameState.WAITING_FOR_GUESS -> listOf(LavenderAccent, MinimalBorder)
                    else -> listOf(MinimalBorder, MinimalBorder)
                }
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isInteractive) 4.dp else 0.dp, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = when (gameState) {
                    GameState.WIN -> EmeraldGreen
                    GameState.LOSE, GameState.GAME_OVER -> LoseRed
                    GameState.WAITING_FOR_GUESS -> LavenderLight
                    else -> TextPrimary
                },
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeltTableArena(
    uiState: GameUiState,
    settings: GameSettings,
    onCupSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val arenaWidth = maxWidth
        val cupSlotWidth = arenaWidth / uiState.cupCount.toFloat()

        // Physics-based swap progress driven by Compose Animation API
        val swapProgress = remember { Animatable(0f) }
        val shakeProgress = remember { Animatable(0f) }
        val activeSwap = uiState.currentSwap

        LaunchedEffect(activeSwap?.id) {
            if (activeSwap != null) {
                if (activeSwap.isFakeShake) {
                    shakeProgress.snapTo(0f)
                    shakeProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = activeSwap.durationMs.toInt(),
                            easing = LinearEasing
                        )
                    )
                } else {
                    swapProgress.snapTo(0f)
                    swapProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = activeSwap.durationMs.toInt(),
                            easing = CubicBezierEasing(0.35f, 0.0f, 0.25f, 1.0f) // Ultra smooth curve
                        )
                    )
                }
            } else {
                swapProgress.snapTo(0f)
                shakeProgress.snapTo(0f)
            }
        }

        // Clean Minimalist Table Mat Background
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MinimalSurface,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    listOf(
                        MinimalBorder,
                        MinimalSurfaceElevated,
                        MinimalBorder
                    )
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E1D24),
                                Color(0xFF141318)
                            )
                        )
                    )
            ) {
                // Table Felt delicate center border accent
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.92f)
                        .align(Alignment.Center)
                        .border(
                            width = 1.dp,
                            color = MinimalBorder.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(20.dp)
                        )
                )

                // 3 Cup Slots Row
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (slotIndex in 0 until uiState.cupCount) {
                        // Calculate physics-based trajectory offsets & dynamics
                        var computedOffsetX = uiState.cupOffsetXs.getOrElse(slotIndex) { 0f }
                        var computedOffsetY = uiState.cupOffsetYs.getOrElse(slotIndex) { 0f }
                        var computedTilt = uiState.cupTilts.getOrElse(slotIndex) { 0f }
                        var computedZIndex = 1f

                        if (activeSwap != null) {
                            if (activeSwap.isFakeShake && slotIndex == activeSwap.slotA) {
                                val sProg = shakeProgress.value
                                computedTilt = (Math.sin(sProg.toDouble() * Math.PI * 4).toFloat()) * 8f
                                computedZIndex = 2f
                            } else if (!activeSwap.isFakeShake) {
                                val p = swapProgress.value
                                if (slotIndex == activeSwap.slotA || slotIndex == activeSwap.slotB) {
                                    val isSlotA = slotIndex == activeSwap.slotA
                                    val startSlot = if (isSlotA) activeSwap.slotA else activeSwap.slotB
                                    val endSlot = if (isSlotA) activeSwap.slotB else activeSwap.slotA
                                    val distance = (endSlot - startSlot).toFloat()

                                    when (settings.selectedShuffleTheme) {
                                        ShuffleTheme.CLASSIC_SLIDE -> {
                                            computedOffsetX = p * distance
                                            val directionFactor = if (isSlotA) 1f else -1f
                                            val arc = (Math.sin(Math.PI * p).toFloat()) * activeSwap.arcHeightRatio * directionFactor
                                            computedOffsetY = arc * 0.45f
                                            computedTilt = (Math.sin(Math.PI * p).toFloat()) * (if (distance > 0) 12f else -12f)
                                            computedZIndex = if (arc > 0) 3f else 1f
                                        }
                                        ShuffleTheme.DOUBLE_SPIN_WAVE -> {
                                            // Circular loop/orbit
                                            computedOffsetX = p * distance
                                            val directionFactor = if (isSlotA) 1f else -1f
                                            val loopHeight = (Math.sin(Math.PI * p).toFloat()) * 1.5f * activeSwap.arcHeightRatio * directionFactor
                                            computedOffsetY = loopHeight
                                            // Continuous full spin
                                            computedTilt = p * 360f * (if (distance > 0) 1f else -1f)
                                            computedZIndex = if (loopHeight > 0) 3f else 1f
                                        }
                                        ShuffleTheme.COSMIC_ZIG_ZAG -> {
                                            // Rapid bounciness
                                            computedOffsetX = p * distance
                                            val bounce = (Math.sin(Math.PI * p * 3.0).toFloat()) * 0.35f
                                            computedOffsetY = bounce
                                            computedTilt = (Math.sin(Math.PI * p * 3.0).toFloat()) * 15f
                                            computedZIndex = if (bounce > 0) 3f else 1f
                                        }
                                        ShuffleTheme.CHAOS_VORTEX -> {
                                            // Spiral inwards to center, then slide back out
                                            val inwardScale = (Math.sin(Math.PI * p).toFloat())
                                            computedOffsetX = p * distance - (inwardScale * distance * 0.22f)
                                            val spiralHeight = inwardScale * 0.75f * (if (isSlotA) 1f else -1f)
                                            computedOffsetY = spiralHeight
                                            computedTilt = inwardScale * 35f * (if (distance > 0) 1f else -1f)
                                            computedZIndex = if (spiralHeight > 0) 3f else 1f
                                        }
                                    }
                                }
                            }
                        }

                        CupSlotContainer(
                            slotIndex = slotIndex,
                            uiState = uiState,
                            settings = settings,
                            slotWidth = cupSlotWidth,
                            offsetX = computedOffsetX,
                            offsetY = computedOffsetY,
                            tilt = computedTilt,
                            cupZIndex = computedZIndex,
                            onCupSelected = { onCupSelected(slotIndex) }
                        )
                    }
                }

                // 3D Animated WIN/LOSE Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = uiState.gameState == GameState.WIN || uiState.gameState == GameState.LOSE || uiState.gameState == GameState.GAME_OVER,
                    enter = androidx.compose.animation.fadeIn(tween(400)) + androidx.compose.animation.scaleIn(
                        initialScale = 0.3f, 
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                    exit = androidx.compose.animation.fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    val isWin = uiState.gameState == GameState.WIN
                    val overlayColor = if (isWin) WinGreen else LoseRed
                    val overlayText = if (isWin) "YOU WIN!" else "DEFEAT"
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 32.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = overlayText,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black,
                            color = overlayColor,
                            letterSpacing = 2.sp,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black,
                                    offset = androidx.compose.ui.geometry.Offset(0f, 10f),
                                    blurRadius = 16f
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CupSlotContainer(
    slotIndex: Int,
    uiState: GameUiState,
    settings: GameSettings,
    slotWidth: androidx.compose.ui.unit.Dp,
    offsetX: Float,
    offsetY: Float,
    tilt: Float,
    cupZIndex: Float,
    onCupSelected: () -> Unit
) {
    val isCoinAtThisSlot = uiState.coinSlotIndex == slotIndex
    val rawLift = uiState.cupLiftAmounts.getOrElse(slotIndex) { 0f }

    // Physics-based lift animation with subtle overshoot
    val animatedLiftAmount by animateFloatAsState(
        targetValue = rawLift,
        animationSpec = tween(
            durationMillis = 450,
            easing = CubicBezierEasing(0.34f, 1.1f, 0.24f, 1.05f) // Smooth with a tiny bounce/overshoot
        ),
        label = "cup_lift_$slotIndex"
    )

    val isSelected = uiState.selectedSlotIndex == slotIndex
    val isWinning = isCoinAtThisSlot && (uiState.gameState == GameState.WIN || uiState.isRevealingWinningSlot)

    // Calculate interactive readiness via strictly enforced GameUiState rule
    val isSelectable = uiState.isInputAllowed

    // Animate subtle hover pulse when waiting for guess
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_guess_$slotIndex")
    val pulseScale by if (isSelectable) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    Box(
        modifier = Modifier
            .width(slotWidth)
            .fillMaxHeight()
            .clickable(
                enabled = isSelectable,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onCupSelected
            )
            .testTag("cup_slot_$slotIndex"),
        contentAlignment = Alignment.Center
    ) {
        // Floor Spot / Target Ring (visible when waiting for guess)
        if (isSelectable) {
            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 22.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 26.dp)
                    .border(
                        width = 1.dp,
                        brush = Brush.radialGradient(listOf(LavenderAccent.copy(alpha = 0.7f), Color.Transparent)),
                        shape = CircleShape
                    )
            )
        }

        val density = LocalDensity.current
        val slotWidthPx = with(density) { slotWidth.toPx() }
        val liftHeightPx = with(density) { 60.dp.toPx() }

        // The 3D Cup with dynamic Z-Index, Centrifugal Tilt, Physics Arc Offsets, and properly bound Coin
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp)
                .graphicsLayer {
                    translationX = offsetX * slotWidthPx
                    translationY = offsetY * liftHeightPx
                    scaleX = pulseScale
                    scaleY = pulseScale
                    shadowElevation = if (offsetY != 0f) 8.dp.toPx() else 0f
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // The Hidden Coin (placed on the table surface, moving seamlessly with the cup container)
            if (isCoinAtThisSlot) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 6.dp) // Adjust base offset to align with cup bottom
                        .graphicsLayer {
                            alpha = animatedLiftAmount.coerceIn(0f, 1f)
                            scaleX = 0.8f + (animatedLiftAmount * 0.2f)
                            scaleY = 0.8f + (animatedLiftAmount * 0.2f)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    CoinVisual(
                        theme = settings.selectedCoinTheme,
                        size = 46.dp,
                        isSpinning = uiState.gameState == GameState.WIN || uiState.gameState == GameState.SHOW_COIN,
                        isGlowing = true
                    )
                }
            }

            CupVisual(
                theme = settings.selectedCupTheme,
                liftAmount = animatedLiftAmount,
                tiltDegrees = tilt,
                isHighlighted = isSelected || isWinning,
                highlightColor = if (isWinning) LavenderAccent else if (isSelected) LavenderLight else LavenderAccent,
                width = 86.dp,
                height = 114.dp
            )
        }
    }
}

@Composable
private fun BottomGameControls(
    uiState: GameUiState,
    stats: PlayerStats,
    onNextRound: () -> Unit,
    onRetryRound: () -> Unit,
    onRestartGame: () -> Unit,
    onReturnToHome: () -> Unit,
    onCupSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState.gameState) {
            GameState.SHOW_COIN, GameState.HIDE_COIN, GameState.SHUFFLING -> {
                Text(
                    text = "Track the cup carefully...",
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(14.dp))
                CleanFooterMetadataRow(score = uiState.score, bestScore = stats.bestScore)
            }

            GameState.WAITING_FOR_GUESS -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "SELECT THE WINNING CUP",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LavenderAccent,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Tap cup 1, 2, or 3 to guess",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                CleanFooterMetadataRow(score = uiState.score, bestScore = stats.bestScore)
            }

            GameState.REVEALING -> {
                CircularProgressIndicator(
                    color = LavenderAccent,
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                CleanFooterMetadataRow(score = uiState.score, bestScore = stats.bestScore)
            }

            GameState.WIN -> {
                RoundResultCard(
                    title = uiState.roundResultTitle,
                    message = uiState.roundResultMessage,
                    isWin = true,
                    actionButtonText = if (uiState.gameMode == GameMode.DAILY_CHALLENGE) "FINISH CHALLENGE" else "NEXT ROUND ➔",
                    onAction = {
                        if (uiState.gameMode == GameMode.DAILY_CHALLENGE) {
                            onReturnToHome()
                        } else {
                            onNextRound()
                        }
                    },
                    onMenu = onReturnToHome
                )
            }

            GameState.LOSE -> {
                RoundResultCard(
                    title = uiState.roundResultTitle,
                    message = uiState.roundResultMessage,
                    isWin = false,
                    actionButtonText = "TRY AGAIN ↻",
                    onAction = onRetryRound,
                    onMenu = onReturnToHome
                )
            }

            GameState.GAME_OVER -> {
                RoundResultCard(
                    title = "GAME OVER",
                    message = "Final Score: ${uiState.score} • Reached Level ${uiState.currentLevel}",
                    isWin = false,
                    actionButtonText = "PLAY AGAIN ↻",
                    onAction = onRestartGame,
                    onMenu = onReturnToHome
                )
            }

            GameState.HOME, GameState.ROUND_RESULT -> {}
        }
    }
}

@Composable
private fun CleanFooterMetadataRow(score: Int, bestScore: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "CURRENT SCORE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Text(
                text = "$score",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MinimalSurface,
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(MinimalBorder, MinimalBorder)))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "BEST",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Text(
                    text = "$bestScore",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LavenderAccent
                )
            }
        }
    }
}

@Composable
private fun RoundResultCard(
    title: String,
    message: String,
    isWin: Boolean,
    actionButtonText: String,
    onAction: () -> Unit,
    onMenu: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MinimalSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isWin) listOf(LavenderAccent, MinimalBorder) else listOf(RubyRed, MinimalBorder)
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isWin) LavenderLight else LoseRed,
                letterSpacing = 0.5.sp
            )

            Text(
                text = message,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onMenu,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.linearGradient(listOf(MinimalBorder, MinimalBorder))),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_result_menu")
                ) {
                    Text("MENU", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isWin) LavenderAccent else RubyRed
                    ),
                    modifier = Modifier
                        .weight(1.6f)
                        .height(48.dp)
                        .testTag("btn_result_action")
                ) {
                    Text(
                        text = actionButtonText,
                        color = if (isWin) VioletDark else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
