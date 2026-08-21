package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Visual Theme definition specific to each Game Mode to give EVERY mode
 * a completely distinct, immersive, premium aesthetic, table mat, HUD, and atmosphere!
 */
data class ModeVisualTheme(
    val mode: GameMode,
    val modeBadgeTitle: String,
    val modeBadgeIcon: String,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val glowColor: Color,
    val backgroundBrush: Brush,
    val arenaBgBrush: Brush,
    val arenaBorderBrush: Brush,
    val arenaSurfaceColor: Color,
    val pedestalColor: Color,
    val statusBannerBg: Color,
    val statusBannerBorder: Brush,
    val statusTextColor: Color
)

object ModeThemeFactory {
    fun getTheme(mode: GameMode): ModeVisualTheme {
        return when (mode) {
            GameMode.TUTORIAL -> ModeVisualTheme(
                mode = mode,
                modeBadgeTitle = "TRAINING ACADEMY",
                modeBadgeIcon = "🎓",
                primaryAccent = Color(0xFF00E5FF), // Cyan Academy Blue
                secondaryAccent = Color(0xFFFFD54F), // Amber Gold
                glowColor = Color(0xFF00E5FF).copy(alpha = 0.25f),
                backgroundBrush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF041824),
                        Color(0xFF020D14),
                        Color(0xFF01060A)
                    )
                ),
                arenaBgBrush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF09293C),
                        Color(0xFF04141E)
                    )
                ),
                arenaBorderBrush = Brush.linearGradient(
                    listOf(
                        Color(0xFF00E5FF),
                        Color(0xFFFFD54F),
                        Color(0xFF00E5FF)
                    )
                ),
                arenaSurfaceColor = Color(0xFF061B28),
                pedestalColor = Color(0xFF00E5FF),
                statusBannerBg = Color(0xFF082C40),
                statusBannerBorder = Brush.horizontalGradient(
                    listOf(Color(0xFF00E5FF), Color(0xFFFFD54F))
                ),
                statusTextColor = Color(0xFFE0F7FA)
            )

            GameMode.CLASSIC -> ModeVisualTheme(
                mode = mode,
                modeBadgeTitle = "CLASSIC ARENA",
                modeBadgeIcon = "🏆",
                primaryAccent = Color(0xFF9C27B0), // Royal Purple
                secondaryAccent = Color(0xFFFFD700), // Vegas Gold
                glowColor = Color(0xFF7B1FA2).copy(alpha = 0.25f),
                backgroundBrush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF140D24),
                        Color(0xFF0C0717),
                        Color(0xFF080410)
                    )
                ),
                arenaBgBrush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF24193D),
                        Color(0xFF160E27)
                    )
                ),
                arenaBorderBrush = Brush.linearGradient(
                    listOf(
                        Color(0xFFFFD700).copy(alpha = 0.6f),
                        Color(0xFF9C27B0).copy(alpha = 0.5f),
                        Color(0xFFFFD700).copy(alpha = 0.3f)
                    )
                ),
                arenaSurfaceColor = Color(0xFF1B112E),
                pedestalColor = Color(0xFFFFD700),
                statusBannerBg = Color(0xFF22153B),
                statusBannerBorder = Brush.horizontalGradient(
                    listOf(Color(0xFFBA68C8), Color(0xFFFFD700).copy(alpha = 0.4f))
                ),
                statusTextColor = Color(0xFFE1BEE7)
            )

            GameMode.TIME_ATTACK -> ModeVisualTheme(
                mode = mode,
                modeBadgeTitle = "CYBER BLITZ",
                modeBadgeIcon = "⚡",
                primaryAccent = Color(0xFF00F2FE), // Neon Cyan
                secondaryAccent = Color(0xFF4FACFE), // Electric Blue
                glowColor = Color(0xFF00F2FE).copy(alpha = 0.3f),
                backgroundBrush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF041324),
                        Color(0xFF020C17),
                        Color(0xFF01060D)
                    )
                ),
                arenaBgBrush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A223B),
                        Color(0xFF051322)
                    )
                ),
                arenaBorderBrush = Brush.linearGradient(
                    listOf(
                        Color(0xFF00F2FE),
                        Color(0xFF4FACFE),
                        Color(0xFF00F2FE)
                    )
                ),
                arenaSurfaceColor = Color(0xFF061A2E),
                pedestalColor = Color(0xFF00F2FE),
                statusBannerBg = Color(0xFF082744),
                statusBannerBorder = Brush.horizontalGradient(
                    listOf(Color(0xFF00F2FE), Color(0xFF4FACFE))
                ),
                statusTextColor = Color(0xFF80D8FF)
            )

            GameMode.ENDLESS -> ModeVisualTheme(
                mode = mode,
                modeBadgeTitle = "SURVIVAL GAUNTLET",
                modeBadgeIcon = "🔥",
                primaryAccent = Color(0xFFFF3D00), // Lava Crimson
                secondaryAccent = Color(0xFFFF9100), // Molten Amber
                glowColor = Color(0xFFFF3D00).copy(alpha = 0.3f),
                backgroundBrush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF200705),
                        Color(0xFF130403),
                        Color(0xFF0A0202)
                    )
                ),
                arenaBgBrush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF380F0B),
                        Color(0xFF1E0604)
                    )
                ),
                arenaBorderBrush = Brush.linearGradient(
                    listOf(
                        Color(0xFFFF3D00),
                        Color(0xFFFF9100),
                        Color(0xFFFF3D00)
                    )
                ),
                arenaSurfaceColor = Color(0xFF260A07),
                pedestalColor = Color(0xFFFF3D00),
                statusBannerBg = Color(0xFF3B0F0B),
                statusBannerBorder = Brush.horizontalGradient(
                    listOf(Color(0xFFFF5722), Color(0xFFFF9100))
                ),
                statusTextColor = Color(0xFFFFAB91)
            )

            GameMode.PERFECT_RUN -> ModeVisualTheme(
                mode = mode,
                modeBadgeTitle = "FLAWLESS RUN",
                modeBadgeIcon = "👑",
                primaryAccent = Color(0xFFFFD700), // Pure Gold
                secondaryAccent = Color(0xFFE040FB), // Diamond Magenta
                glowColor = Color(0xFFFFD700).copy(alpha = 0.25f),
                backgroundBrush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF17130A),
                        Color(0xFF0E0B05),
                        Color(0xFF080602)
                    )
                ),
                arenaBgBrush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF2C2413),
                        Color(0xFF181308)
                    )
                ),
                arenaBorderBrush = Brush.linearGradient(
                    listOf(
                        Color(0xFFFFD700),
                        Color(0xFFFFF9C4),
                        Color(0xFFFFD700)
                    )
                ),
                arenaSurfaceColor = Color(0xFF201A0D),
                pedestalColor = Color(0xFFFFD700),
                statusBannerBg = Color(0xFF2E2410),
                statusBannerBorder = Brush.horizontalGradient(
                    listOf(Color(0xFFFFD700), Color(0xFFFFF176))
                ),
                statusTextColor = Color(0xFFFFF59D)
            )

            GameMode.DAILY_CHALLENGE -> ModeVisualTheme(
                mode = mode,
                modeBadgeTitle = "DAILY QUEST",
                modeBadgeIcon = "📅",
                primaryAccent = Color(0xFF00E676), // Emerald Green
                secondaryAccent = Color(0xFF00B0FF), // Celestial Teal
                glowColor = Color(0xFF00E676).copy(alpha = 0.25f),
                backgroundBrush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF051C15),
                        Color(0xFF03100C),
                        Color(0xFF010A07)
                    )
                ),
                arenaBgBrush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A3326),
                        Color(0xFF041C14)
                    )
                ),
                arenaBorderBrush = Brush.linearGradient(
                    listOf(
                        Color(0xFF00E676),
                        Color(0xFF69F0AE),
                        Color(0xFF00E676)
                    )
                ),
                arenaSurfaceColor = Color(0xFF07271D),
                pedestalColor = Color(0xFF00E676),
                statusBannerBg = Color(0xFF0B3B2B),
                statusBannerBorder = Brush.horizontalGradient(
                    listOf(Color(0xFF00E676), Color(0xFF69F0AE))
                ),
                statusTextColor = Color(0xFFA7F3D0)
            )
        }
    }
}

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
    val visualTheme = remember(uiState.gameMode) { ModeThemeFactory.getTheme(uiState.gameMode) }

    // Intercept back button during gameplay to safely confirm quit
    BackHandler(enabled = true) {
        showExitConfirmDialog = !showExitConfirmDialog
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(visualTheme.backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Mode Ambient Glow Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            visualTheme.glowColor,
                            Color.Transparent
                        ),
                        radius = 600f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Distinct Dynamic Top Header Per Game Mode
            GameModeHeader(
                uiState = uiState,
                stats = stats,
                visualTheme = visualTheme,
                onBackClick = { showExitConfirmDialog = true },
                onToggleSound = onToggleSound
            )

            // Active Boosters Pill Row (Shield & 2X Multiplier)
            if (stats.shieldCount > 0 || stats.doubleScoreActive) {
                Spacer(modifier = Modifier.height(4.dp))
                ActiveBoostersPillRow(
                    shieldCount = stats.shieldCount,
                    doubleScoreActive = stats.doubleScoreActive,
                    visualTheme = visualTheme
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Specialized Hero Center Section per Game Mode
            GameModeHeroSection(
                uiState = uiState,
                stats = stats,
                visualTheme = visualTheme
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Specialized 3D Arena with Mode-Distinctive Table, Pedestals & Accents
            GameModeTableArena(
                uiState = uiState,
                settings = settings,
                visualTheme = visualTheme,
                onCupSelected = onCupSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Suspense Guidance Banner
            ModeStatusPromptBanner(
                text = uiState.roundStatusText,
                gameState = uiState.gameState,
                visualTheme = visualTheme
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Action Area & Result Sheet
            GameModeBottomControls(
                uiState = uiState,
                stats = stats,
                visualTheme = visualTheme,
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

        // Confirm Exit Dialog
        if (showExitConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showExitConfirmDialog = false },
                icon = {
                    Surface(
                        shape = CircleShape,
                        color = visualTheme.primaryAccent.copy(alpha = 0.2f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = visualTheme.primaryAccent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "Leave ${uiState.gameMode.title}?",
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
                        colors = ButtonDefaults.buttonColors(containerColor = visualTheme.primaryAccent),
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
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = visualTheme.secondaryAccent),
                        border = BorderStroke(1.dp, visualTheme.secondaryAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Keep Playing", fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = visualTheme.arenaSurfaceColor,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

// ----------------------------------------------------------------------------
// TOP HEADER - UNIQUELY CUSTOMIZED FOR EACH GAME MODE
// ----------------------------------------------------------------------------

@Composable
private fun GameModeHeader(
    uiState: GameUiState,
    stats: PlayerStats,
    visualTheme: ModeVisualTheme,
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
        // Mode-Styled Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(visualTheme.arenaSurfaceColor)
                .border(1.dp, visualTheme.primaryAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .testTag("btn_back_to_menu")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = visualTheme.primaryAccent,
                modifier = Modifier.size(20.dp)
            )
        }

        // Center Distinct Mode Title Badge
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = visualTheme.arenaSurfaceColor,
            border = BorderStroke(1.dp, visualTheme.arenaBorderBrush),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(visualTheme.modeBadgeIcon, fontSize = 15.sp)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = visualTheme.modeBadgeTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = visualTheme.primaryAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = when (uiState.gameMode) {
                            GameMode.CLASSIC -> "LEVEL ${uiState.currentLevel}"
                            GameMode.TIME_ATTACK -> "5.0s BLITZ"
                            GameMode.ENDLESS -> "ROUND ${uiState.currentLevel}"
                            GameMode.PERFECT_RUN -> "FLAWLESS"
                            GameMode.DAILY_CHALLENGE -> java.text.SimpleDateFormat("MMM dd", java.util.Locale.US).format(java.util.Date())
                            GameMode.TUTORIAL -> "STEP ${uiState.currentLevel}"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = visualTheme.secondaryAccent,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Right Quick Stat Pill
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = visualTheme.arenaSurfaceColor,
            border = BorderStroke(1.dp, visualTheme.primaryAccent.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when (uiState.gameMode) {
                    GameMode.TUTORIAL -> {
                        Text("🎓", fontSize = 11.sp)
                        Text(
                            text = "STEP ${uiState.currentLevel}/3",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = visualTheme.primaryAccent
                        )
                    }
                    GameMode.CLASSIC -> {
                        Text("⭐", fontSize = 11.sp)
                        Text(
                            text = "${stats.bestScore.coerceAtLeast(uiState.score)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = visualTheme.secondaryAccent
                        )
                    }
                    GameMode.TIME_ATTACK -> {
                        Text("⏱️", fontSize = 11.sp)
                        Text(
                            text = "${uiState.score}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = visualTheme.primaryAccent
                        )
                    }
                    GameMode.ENDLESS -> {
                        Text("🔥", fontSize = 11.sp)
                        Text(
                            text = "R${uiState.currentLevel}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = visualTheme.primaryAccent
                        )
                    }
                    GameMode.PERFECT_RUN -> {
                        Text("👑", fontSize = 11.sp)
                        Text(
                            text = "${uiState.perfectStreak}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = visualTheme.secondaryAccent
                        )
                    }
                    GameMode.DAILY_CHALLENGE -> {
                        Text("📅", fontSize = 11.sp)
                        Text(
                            text = "TODAY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = visualTheme.primaryAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveBoostersPillRow(
    shieldCount: Int,
    doubleScoreActive: Boolean,
    visualTheme: ModeVisualTheme
) {
    Row(
        modifier = Modifier
            .background(visualTheme.arenaSurfaceColor.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .border(1.dp, visualTheme.primaryAccent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (shieldCount > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("🛡️", fontSize = 11.sp)
                Text(
                    text = "SHIELD: $shieldCount",
                    color = visualTheme.primaryAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
        if (doubleScoreActive) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("⚡", fontSize = 11.sp)
                Text(
                    text = "2X BOOST ACTIVE",
                    color = visualTheme.secondaryAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// CENTER HERO SECTION - 5 FULLY DISTINCT MODES
// ----------------------------------------------------------------------------

@Composable
private fun GameModeHeroSection(
    uiState: GameUiState,
    stats: PlayerStats,
    visualTheme: ModeVisualTheme
) {
    when (uiState.gameMode) {
        GameMode.TUTORIAL -> TutorialHeroWidget(uiState, visualTheme)
        GameMode.CLASSIC -> ClassicHeroWidget(uiState, stats, visualTheme)
        GameMode.TIME_ATTACK -> TimeAttackHeroWidget(uiState, visualTheme)
        GameMode.ENDLESS -> EndlessSurvivalHeroWidget(uiState, stats, visualTheme)
        GameMode.PERFECT_RUN -> PerfectRunHeroWidget(uiState, visualTheme)
        GameMode.DAILY_CHALLENGE -> DailyChallengeHeroWidget(uiState, visualTheme)
    }
}

/** 0. TUTORIAL MODE: Interactive Step-by-Step Training Academy HUD */
@Composable
private fun TutorialHeroWidget(
    uiState: GameUiState,
    visualTheme: ModeVisualTheme
) {
    val level = uiState.currentLevel

    val coachHint = when (level) {
        1 -> "Follow the shadow of the middle cup as it makes 1 single swap."
        2 -> "Two cups are about to swap twice. Keep your eyes centered."
        else -> "Final mastery! 3 fluid swaps across all slots. Spot the winner!"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .background(visualTheme.arenaSurfaceColor.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
            .border(1.dp, visualTheme.primaryAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("🎓", fontSize = 14.sp)
                Text(
                    text = "BEGINNER TRAINING ACADEMY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = visualTheme.primaryAccent,
                    letterSpacing = 1.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = visualTheme.secondaryAccent.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, visualTheme.secondaryAccent.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "STEP $level OF 3",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = visualTheme.secondaryAccent,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3-Step Interactive Breadcrumb Stepper
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                1 to "1 SWAP",
                2 to "2 SWAPS",
                3 to "3 SWAPS"
            ).forEach { (step, label) ->
                val isCurrent = level == step
                val isCompleted = level > step

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isCompleted -> Color(0xFF00E676).copy(alpha = 0.2f)
                        isCurrent -> visualTheme.primaryAccent.copy(alpha = 0.22f)
                        else -> Color.White.copy(alpha = 0.05f)
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            isCompleted -> Color(0xFF00E676).copy(alpha = 0.6f)
                            isCurrent -> visualTheme.primaryAccent
                            else -> Color.White.copy(alpha = 0.1f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isCompleted) "✓" else "$step.",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = when {
                                isCompleted -> Color(0xFF00E676)
                                isCurrent -> visualTheme.primaryAccent
                                else -> TextSecondary
                            }
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                            color = if (isCurrent) Color.White else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("💡", fontSize = 11.sp)
            Text(
                text = coachHint,
                fontSize = 10.sp,
                color = visualTheme.statusTextColor,
                fontWeight = FontWeight.Medium,
                lineHeight = 13.sp
            )
        }
    }
}

/** 1. CLASSIC MODE: Royal Level Stage Progress & Win Streak Multiplier */
@Composable
private fun ClassicHeroWidget(
    uiState: GameUiState,
    stats: PlayerStats,
    visualTheme: ModeVisualTheme
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (uiState.winStreak >= 2) "🔥 ${uiState.winStreak}-WIN STREAK" else "WIN STREAK MULTIPLIER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (uiState.winStreak >= 2) visualTheme.secondaryAccent else TextSecondary,
                letterSpacing = 1.2.sp
            )
            if (uiState.winStreak >= 2) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = visualTheme.secondaryAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, visualTheme.secondaryAccent.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "ACTIVE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = visualTheme.secondaryAccent,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${String.format("%.1f", uiState.streakMultiplier)}x",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = if (uiState.winStreak >= 2) visualTheme.secondaryAccent else Color.White,
                letterSpacing = (-1).sp
            )
            Text(
                text = "SCORE BOOST",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Text(
            text = if (uiState.winStreak == 0) "Win 2 rounds in a row for score multiplier" else "Consecutive wins increase your multiplier!",
            fontSize = 10.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

/** 2. TIME ATTACK MODE: Cyber Countdown Stopwatch & Speedometer */
@Composable
private fun TimeAttackHeroWidget(
    uiState: GameUiState,
    visualTheme: ModeVisualTheme
) {
    val remainingSec = uiState.timeAttackRemainingSec
    val totalSec = uiState.timeAttackTotalSec
    val progress = (remainingSec / totalSec).coerceIn(0f, 1f)

    val timerColor = when {
        progress > 0.5f -> visualTheme.primaryAccent
        progress > 0.25f -> Color(0xFFFFD54F)
        else -> Color(0xFFFF5252)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cyber_timer_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .background(visualTheme.arenaSurfaceColor.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .border(1.dp, timerColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("⚡", fontSize = 14.sp)
                Text(
                    text = "BLITZ TIMER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = timerColor,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = String.format("%.2fs", remainingSec),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = timerColor,
                modifier = if (progress <= 0.25f) Modifier.graphicsLayer { scaleX = pulseGlow; scaleY = pulseGlow } else Modifier
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Glowing Cyber Progress Bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape),
            color = timerColor,
            trackColor = Color(0xFF041324)
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "FAST GUESS = +250 RAPID REACTION BONUS!",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = visualTheme.secondaryAccent,
            letterSpacing = 0.5.sp
        )
    }
}

/** 3. ENDLESS MODE: High-Stakes Volcanic Survival Hearts & Danger Gauge */
@Composable
private fun EndlessSurvivalHeroWidget(
    uiState: GameUiState,
    stats: PlayerStats,
    visualTheme: ModeVisualTheme
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ember_flicker")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .background(visualTheme.arenaSurfaceColor.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
            .border(1.dp, visualTheme.primaryAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "🔥",
                    fontSize = 14.sp,
                    modifier = Modifier.graphicsLayer { scaleX = flameScale; scaleY = flameScale }
                )
                Text(
                    text = "SURVIVAL INTENSITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = visualTheme.primaryAccent,
                    letterSpacing = 1.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = visualTheme.primaryAccent.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, visualTheme.primaryAccent.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "1 LIFE AT STAKE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = visualTheme.primaryAccent,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ROUND SURVIVED", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Text(
                    text = "Round ${uiState.currentLevel}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(visualTheme.primaryAccent.copy(alpha = 0.3f)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SHUFFLE CHAOS", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Text(
                    text = "${(1.0f + (uiState.currentLevel * 0.08f)).coerceAtMost(3.0f)}x SPEED",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = visualTheme.secondaryAccent
                )
            }
        }
    }
}

/** 4. PERFECT RUN MODE: Crystalline Diamond Crown & Flawless Gauntlet */
@Composable
private fun PerfectRunHeroWidget(
    uiState: GameUiState,
    visualTheme: ModeVisualTheme
) {
    val streak = uiState.perfectStreak
    val multiplier = uiState.streakMultiplier

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .background(visualTheme.arenaSurfaceColor.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
            .border(1.dp, visualTheme.primaryAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("👑", fontSize = 16.sp)
            Text(
                text = "FLAWLESS GAUNTLET",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = visualTheme.primaryAccent,
                letterSpacing = 1.2.sp
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = visualTheme.primaryAccent.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, visualTheme.primaryAccent.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "${String.format("%.1f", multiplier)}x BOOST",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = visualTheme.primaryAccent,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = "🔥 $streak PERFECT STREAK",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = visualTheme.primaryAccent,
            letterSpacing = (-0.5).sp
        )

        Text(
            text = "Zero mistakes permitted • Chain wins for diamond glory",
            fontSize = 9.sp,
            color = visualTheme.statusTextColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/** 5. DAILY CHALLENGE MODE: Cosmic Nebula & Star Quest Card */
@Composable
private fun DailyChallengeHeroWidget(
    uiState: GameUiState,
    visualTheme: ModeVisualTheme
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .background(visualTheme.arenaSurfaceColor.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
            .border(1.dp, visualTheme.primaryAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("🌟", fontSize = 14.sp)
                Text(
                    text = "TODAY'S SPECIAL PUZZLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = visualTheme.primaryAccent,
                    letterSpacing = 1.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = visualTheme.primaryAccent.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, visualTheme.primaryAccent.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "+500 COINS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = visualTheme.primaryAccent,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DAILY TARGET", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Text("Spot Golden Coin", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Box(modifier = Modifier.width(1.dp).height(20.dp).background(visualTheme.primaryAccent.copy(alpha = 0.3f)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("GLOBAL SEED", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Text("#${SimpleDateFormat("MMdd", Locale.US).format(Date())}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = visualTheme.secondaryAccent)
            }
        }
    }
}

// ----------------------------------------------------------------------------
// 3D TABLE ARENA - CUSTOM STYLED PER MODE (CYBER GRID, LAVA EMBERS, CRYSTAL, ETC.)
// ----------------------------------------------------------------------------

@Composable
private fun GameModeTableArena(
    uiState: GameUiState,
    settings: GameSettings,
    visualTheme: ModeVisualTheme,
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
                            easing = CubicBezierEasing(0.35f, 0.0f, 0.25f, 1.0f)
                        )
                    )
                }
            } else {
                swapProgress.snapTo(0f)
                shakeProgress.snapTo(0f)
            }
        }

        // Mode Distinctive Table Arena Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = visualTheme.arenaSurfaceColor,
            border = BorderStroke(1.5.dp, visualTheme.arenaBorderBrush),
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .shadow(10.dp, RoundedCornerShape(24.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(visualTheme.arenaBgBrush)
            ) {
                // Specialized Decorative Table Pattern Per Mode
                when (uiState.gameMode) {
                    GameMode.TUTORIAL -> {
                        // Training Arena Guideline Tracks Canvas
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val trackY = size.height * 0.78f
                            val cyanLine = Color(0xFF00E5FF).copy(alpha = 0.25f)
                            val goldDash = Color(0xFFFFD54F).copy(alpha = 0.35f)

                            // Horizontal guide track
                            drawLine(
                                color = cyanLine,
                                start = Offset(size.width * 0.1f, trackY),
                                end = Offset(size.width * 0.9f, trackY),
                                strokeWidth = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )
                            // Inset Training Border
                            drawRoundRect(
                                color = cyanLine,
                                topLeft = Offset(size.width * 0.04f, size.height * 0.08f),
                                size = androidx.compose.ui.geometry.Size(size.width * 0.92f, size.height * 0.84f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(36f, 36f),
                                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
                            )
                        }
                    }
                    GameMode.TIME_ATTACK -> {
                        // Cyber Grid Lines Canvas
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 1f
                            val gridStep = 40f
                            val cyanGrid = Color(0xFF00F2FE).copy(alpha = 0.08f)
                            for (x in 0..size.width.toInt() step gridStep.toInt()) {
                                drawLine(cyanGrid, Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height), strokeWidth)
                            }
                            for (y in 0..size.height.toInt() step gridStep.toInt()) {
                                drawLine(cyanGrid, Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()), strokeWidth)
                            }
                        }
                    }
                    GameMode.ENDLESS -> {
                        // Volcanic Hazard Floor Stripes
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.92f)
                                .align(Alignment.Center)
                                .border(
                                    width = 1.dp,
                                    color = visualTheme.primaryAccent.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                        )
                    }
                    GameMode.PERFECT_RUN -> {
                        // Diamond Bevel Inset Line
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.92f)
                                .align(Alignment.Center)
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        listOf(
                                            Color(0xFFFFD700).copy(alpha = 0.5f),
                                            Color.White.copy(alpha = 0.1f),
                                            Color(0xFFFFD700).copy(alpha = 0.4f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                )
                        )
                    }
                    GameMode.DAILY_CHALLENGE -> {
                        // Cosmic Star Constellation Frame
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.92f)
                                .align(Alignment.Center)
                                .border(
                                    width = 1.dp,
                                    color = visualTheme.primaryAccent.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                        )
                    }
                    GameMode.CLASSIC -> {
                        // Royal Velvet Gold Stitched Accent
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.92f)
                                .align(Alignment.Center)
                                .border(
                                    width = 1.dp,
                                    color = visualTheme.secondaryAccent.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                        )
                    }
                }

                // Arena Skeleton Loading State
                if (uiState.gameState == GameState.PREPARING || uiState.isArenaPreparing) {
                    ArenaSkeletonLoading(
                        cupCount = uiState.cupCount,
                        visualTheme = visualTheme,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // 3D Cup Slots
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (slotIndex in 0 until uiState.cupCount) {
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
                                                computedOffsetX = p * distance
                                                val directionFactor = if (isSlotA) 1f else -1f
                                                val loopHeight = (Math.sin(Math.PI * p).toFloat()) * 1.5f * activeSwap.arcHeightRatio * directionFactor
                                                computedOffsetY = loopHeight
                                                computedTilt = p * 360f * (if (distance > 0) 1f else -1f)
                                                computedZIndex = if (loopHeight > 0) 3f else 1f
                                            }
                                            ShuffleTheme.COSMIC_ZIG_ZAG -> {
                                                computedOffsetX = p * distance
                                                val bounce = (Math.sin(Math.PI * p * 3.0).toFloat()) * 0.35f
                                                computedOffsetY = bounce
                                                computedTilt = (Math.sin(Math.PI * p * 3.0).toFloat()) * 15f
                                                computedZIndex = if (bounce > 0) 3f else 1f
                                            }
                                            ShuffleTheme.CHAOS_VORTEX -> {
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

                            ModeCupSlotContainer(
                                slotIndex = slotIndex,
                                uiState = uiState,
                                settings = settings,
                                visualTheme = visualTheme,
                                slotWidth = cupSlotWidth,
                                offsetX = computedOffsetX,
                                offsetY = computedOffsetY,
                                tilt = computedTilt,
                                cupZIndex = computedZIndex,
                                onCupSelected = { onCupSelected(slotIndex) }
                            )
                        }
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
                    val overlayColor = if (isWin) Color(0xFF00E676) else Color(0xFFFF3D00)
                    val overlayText = if (isWin) "VICTORY!" else "MISSED!"

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
                            .border(1.5.dp, overlayColor.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 32.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = overlayText,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = overlayColor,
                            letterSpacing = 2.sp,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black,
                                    offset = androidx.compose.ui.geometry.Offset(0f, 8f),
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
private fun ArenaSkeletonLoading(
    cupCount: Int,
    visualTheme: ModeVisualTheme,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(cupCount) { index ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 80.dp, height = 106.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        visualTheme.primaryAccent.copy(alpha = 0.15f * shimmerAlpha),
                                        Color.White.copy(alpha = 0.02f * shimmerAlpha)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        visualTheme.primaryAccent.copy(alpha = 0.45f * shimmerAlpha),
                                        Color.White.copy(alpha = 0.08f * shimmerAlpha)
                                    )
                                ),
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(visualTheme.primaryAccent.copy(alpha = 0.2f * shimmerAlpha), CircleShape)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 64.dp, height = 18.dp)
                            .border(
                                width = 1.dp,
                                brush = Brush.radialGradient(
                                    listOf(
                                        visualTheme.pedestalColor.copy(alpha = 0.5f * shimmerAlpha),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeCupSlotContainer(
    slotIndex: Int,
    uiState: GameUiState,
    settings: GameSettings,
    visualTheme: ModeVisualTheme,
    slotWidth: androidx.compose.ui.unit.Dp,
    offsetX: Float,
    offsetY: Float,
    tilt: Float,
    cupZIndex: Float,
    onCupSelected: () -> Unit
) {
    val isCoinAtThisSlot = uiState.coinSlotIndex == slotIndex
    val rawLift = uiState.cupLiftAmounts.getOrElse(slotIndex) { 0f }

    val animatedLiftAmount by animateFloatAsState(
        targetValue = rawLift,
        animationSpec = tween(
            durationMillis = 450,
            easing = CubicBezierEasing(0.34f, 1.1f, 0.24f, 1.05f)
        ),
        label = "cup_lift_$slotIndex"
    )

    val isSelected = uiState.selectedSlotIndex == slotIndex
    val isWinning = isCoinAtThisSlot && (uiState.gameState == GameState.WIN || uiState.isRevealingWinningSlot)
    val isSelectable = uiState.isInputAllowed

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
        // Mode-Themed Pedestal Ring on Table Surface
        if (isSelectable) {
            Box(
                modifier = Modifier
                    .size(width = 68.dp, height = 22.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 26.dp)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.radialGradient(
                            listOf(
                                visualTheme.pedestalColor.copy(alpha = 0.8f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        val density = LocalDensity.current
        val slotWidthPx = with(density) { slotWidth.toPx() }
        val liftHeightPx = with(density) { 60.dp.toPx() }

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
            // Hidden Coin Visual
            if (isCoinAtThisSlot) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
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
                highlightColor = if (isWinning) visualTheme.primaryAccent else if (isSelected) visualTheme.secondaryAccent else visualTheme.primaryAccent,
                width = 86.dp,
                height = 114.dp
            )
        }
    }
}

// ----------------------------------------------------------------------------
// STATUS PROMPT BANNER - THEMED PER GAME MODE
// ----------------------------------------------------------------------------

@Composable
private fun ModeStatusPromptBanner(
    text: String,
    gameState: GameState,
    visualTheme: ModeVisualTheme
) {
    val isInteractive = gameState == GameState.WAITING_FOR_GUESS

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = when (gameState) {
            GameState.WIN -> Color(0xFF00E676).copy(alpha = 0.15f)
            GameState.LOSE, GameState.GAME_OVER -> Color(0xFFFF3D00).copy(alpha = 0.15f)
            GameState.WAITING_FOR_GUESS -> visualTheme.statusBannerBg
            else -> visualTheme.arenaSurfaceColor
        },
        border = BorderStroke(
            width = 1.dp,
            brush = when (gameState) {
                GameState.WIN -> Brush.horizontalGradient(listOf(Color(0xFF00E676), Color(0xFF69F0AE)))
                GameState.LOSE, GameState.GAME_OVER -> Brush.horizontalGradient(listOf(Color(0xFFFF3D00), Color(0xFFFF9100)))
                GameState.WAITING_FOR_GUESS -> visualTheme.statusBannerBorder
                else -> Brush.horizontalGradient(listOf(visualTheme.primaryAccent.copy(alpha = 0.3f), visualTheme.primaryAccent.copy(alpha = 0.3f)))
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isInteractive) 4.dp else 0.dp, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = when (gameState) {
                    GameState.WIN -> Color(0xFF00E676)
                    GameState.LOSE, GameState.GAME_OVER -> Color(0xFFFF5252)
                    GameState.WAITING_FOR_GUESS -> visualTheme.statusTextColor
                    else -> TextPrimary
                },
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ----------------------------------------------------------------------------
// BOTTOM CONTROLS & RESULT SHEETS - THEMED PER GAME MODE
// ----------------------------------------------------------------------------

@Composable
private fun GameModeBottomControls(
    uiState: GameUiState,
    stats: PlayerStats,
    visualTheme: ModeVisualTheme,
    onNextRound: () -> Unit,
    onRetryRound: () -> Unit,
    onRestartGame: () -> Unit,
    onReturnToHome: () -> Unit,
    onCupSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState.gameState) {
            GameState.SHOW_COIN, GameState.HIDE_COIN, GameState.SHUFFLING, GameState.PREPARING -> {
                Text(
                    text = "Track the winning cup...",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                ModeFooterMetadataRow(
                    score = uiState.score,
                    bestScore = stats.bestScore,
                    winStreak = uiState.winStreak,
                    multiplier = uiState.streakMultiplier,
                    visualTheme = visualTheme
                )
            }

            GameState.WAITING_FOR_GUESS -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "SELECT THE WINNING CUP",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = visualTheme.primaryAccent,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Tap cup 1, 2, or 3 to guess",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                ModeFooterMetadataRow(
                    score = uiState.score,
                    bestScore = stats.bestScore,
                    winStreak = uiState.winStreak,
                    multiplier = uiState.streakMultiplier,
                    visualTheme = visualTheme
                )
            }

            GameState.REVEALING -> {
                CircularProgressIndicator(
                    color = visualTheme.primaryAccent,
                    modifier = Modifier.size(26.dp),
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(10.dp))
                ModeFooterMetadataRow(
                    score = uiState.score,
                    bestScore = stats.bestScore,
                    winStreak = uiState.winStreak,
                    multiplier = uiState.streakMultiplier,
                    visualTheme = visualTheme
                )
            }

            GameState.WIN -> {
                val winButtonText = when {
                    uiState.gameMode == GameMode.DAILY_CHALLENGE -> "FINISH CHALLENGE"
                    uiState.gameMode == GameMode.TUTORIAL && uiState.currentLevel >= 3 -> "START MAIN GAME 🏆"
                    uiState.gameMode == GameMode.TUTORIAL -> "NEXT TRAINING STEP ➔"
                    else -> "NEXT ROUND ➔"
                }

                ModeRoundResultCard(
                    title = uiState.roundResultTitle,
                    message = uiState.roundResultMessage,
                    isWin = true,
                    multiplier = uiState.streakMultiplier,
                    winStreak = uiState.winStreak,
                    actionButtonText = winButtonText,
                    visualTheme = visualTheme,
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
                val loseButtonText = when {
                    uiState.gameMode == GameMode.ENDLESS -> "RESTART SURVIVAL ↻"
                    uiState.gameMode == GameMode.TUTORIAL -> "RETRY STEP ${uiState.currentLevel} ↻"
                    else -> "TRY AGAIN ↻"
                }

                ModeRoundResultCard(
                    title = uiState.roundResultTitle,
                    message = uiState.roundResultMessage,
                    isWin = false,
                    multiplier = 1.0f,
                    winStreak = 0,
                    actionButtonText = loseButtonText,
                    visualTheme = visualTheme,
                    onAction = if (uiState.gameMode == GameMode.ENDLESS) onRestartGame else onRetryRound,
                    onMenu = onReturnToHome
                )
            }

            GameState.GAME_OVER -> {
                ModeRoundResultCard(
                    title = "GAME OVER",
                    message = "Final Score: ${uiState.score} • Reached Round ${uiState.currentLevel}",
                    isWin = false,
                    multiplier = 1.0f,
                    winStreak = 0,
                    actionButtonText = "PLAY AGAIN ↻",
                    visualTheme = visualTheme,
                    onAction = onRestartGame,
                    onMenu = onReturnToHome
                )
            }

            GameState.HOME, GameState.ROUND_RESULT, GameState.PREPARING -> {}
        }
    }
}

@Composable
private fun ModeFooterMetadataRow(
    score: Int,
    bestScore: Int,
    winStreak: Int = 0,
    multiplier: Float = 1.0f,
    visualTheme: ModeVisualTheme
) {
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
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Text(
                text = "$score",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
        }

        if (winStreak >= 2) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = visualTheme.secondaryAccent.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, visualTheme.secondaryAccent.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🔥", fontSize = 11.sp)
                    Text(
                        text = "${String.format("%.1f", multiplier)}x BOOST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = visualTheme.secondaryAccent
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = visualTheme.arenaSurfaceColor,
            border = BorderStroke(1.dp, visualTheme.primaryAccent.copy(alpha = 0.3f))
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
                    color = TextSecondary
                )
                Text(
                    text = "$bestScore",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = visualTheme.primaryAccent
                )
            }
        }
    }
}

@Composable
private fun ModeRoundResultCard(
    title: String,
    message: String,
    isWin: Boolean,
    actionButtonText: String,
    multiplier: Float = 1.0f,
    winStreak: Int = 0,
    visualTheme: ModeVisualTheme,
    onAction: () -> Unit,
    onMenu: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = visualTheme.arenaSurfaceColor),
        border = BorderStroke(
            width = 1.5.dp,
            brush = if (isWin) Brush.horizontalGradient(listOf(visualTheme.primaryAccent, visualTheme.secondaryAccent))
                    else Brush.horizontalGradient(listOf(Color(0xFFFF3D00), Color(0xFFFF9100)))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = if (isWin) visualTheme.primaryAccent else Color(0xFFFF5252),
                letterSpacing = 0.5.sp
            )

            if (isWin && multiplier > 1.0f) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = visualTheme.secondaryAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, visualTheme.secondaryAccent.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🔥", fontSize = 12.sp)
                        Text(
                            text = "${String.format("%.1fx", multiplier)} WIN-STREAK MULTIPLIER APPLIED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = visualTheme.secondaryAccent
                        )
                    }
                }
            }

            Text(
                text = message,
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onMenu,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = BorderStroke(1.dp, visualTheme.primaryAccent.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("btn_result_menu")
                ) {
                    Text("MENU", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isWin) visualTheme.primaryAccent else Color(0xFFFF3D00)
                    ),
                    modifier = Modifier
                        .weight(1.6f)
                        .height(46.dp)
                        .testTag("btn_result_action")
                ) {
                    Text(
                        text = actionButtonText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
