package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.CoinVisual
import com.example.ui.components.CupVisual
import com.example.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.content.Intent
import android.net.Uri
import android.widget.Toast

enum class HomeTab {
    HOME, STATS, THEMES, SETTINGS
}

@Composable
fun AnimatedMeshBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh_bg")
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mesh_offset_1"
    )
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 1500f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mesh_offset_2"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .background(
                Brush.radialGradient(
                    colors = listOf(LavenderAccent.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(offset1, offset2),
                    radius = 2000f
                )
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(NeonCyan.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(offset2, offset1),
                    radius = 1600f
                )
            )
    )
}

@Composable
fun borderStrokeGradient() = androidx.compose.foundation.BorderStroke(
    width = 1.dp,
    brush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.25f),
            Color.White.copy(alpha = 0.05f)
        )
    )
)

@Composable
fun AnimatedEntrance(
    isVisible: Boolean,
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = delayMillis, easing = FastOutSlowInEasing),
        label = "entrance_alpha"
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 40f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "entrance_offset_y"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                alpha = animatedAlpha
                translationY = if (!isVisible) 40f else animatedOffsetY
            }
    ) {
        content()
    }
}

@Composable
fun Glowing3DCard(
    modifier: Modifier = Modifier,
    glowColor: Color = LavenderAccent,
    isGlowing: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_glow")
    val glowIntensity by if (isGlowing) {
        infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.75f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "intensity"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Box(
        modifier = modifier
            .drawBehind {
                if (isGlowing && glowIntensity > 0f) {
                    val w = size.width
                    val h = size.height
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = glowIntensity * 0.22f),
                                Color.Transparent
                            ),
                            center = Offset(w / 2f, h / 2f),
                            radius = w * 0.85f
                        )
                    )
                }
            }
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PurpleNightSurface.copy(alpha = 0.85f),
                        Color.Black.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.28f),
                        glowColor.copy(alpha = 0.65f),
                        Color.Transparent
                    )
                ),
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.5.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    shape = shape
                )
        ) {
            content()
        }
    }
}

@Composable
fun HomeScreen(
    stats: PlayerStats,
    settings: GameSettings,
    isDailyCompleted: Boolean,
    isLuckySpinCompleted: Boolean,
    onSpinClaimed: (String) -> Unit,
    onStartGame: (GameMode) -> Unit,
    onOpenHowToPlay: () -> Unit,
    onSelectCupTheme: (CupTheme) -> Unit,
    onSelectCoinTheme: (CoinTheme) -> Unit,
    onSelectShuffleTheme: (ShuffleTheme) -> Unit,
    onToggleSound: () -> Unit,
    onToggleVibration: () -> Unit,
    onToggleReducedMotion: () -> Unit,
    onResetProgress: () -> Unit,
    onPlaySound: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    var showDailyChallengeDialog by remember { mutableStateOf(false) }
    var showLuckySpinDialog by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(HomeTab.HOME) }

    // Intercept Back button within HomeScreen: close open dialogs or return to HOME tab
    BackHandler(enabled = showDailyChallengeDialog || showLuckySpinDialog || currentTab != HomeTab.HOME) {
        when {
            showDailyChallengeDialog -> {
                onPlaySound("tap")
                showDailyChallengeDialog = false
            }
            showLuckySpinDialog -> {
                onPlaySound("tap")
                showLuckySpinDialog = false
            }
            currentTab != HomeTab.HOME -> {
                onPlaySound("tab")
                currentTab = HomeTab.HOME
            }
        }
    }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AnimatedMeshBackground()

        // Page content container
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (currentTab) {
                HomeTab.HOME -> {
                    HomeTabContent(
                        stats = stats,
                        settings = settings,
                        isDailyCompleted = isDailyCompleted,
                        isLuckySpinCompleted = isLuckySpinCompleted,
                        isVisible = isVisible,
                        onStartGame = { mode -> 
                            onPlaySound("tap")
                            onStartGame(mode) 
                        },
                        onOpenSettings = { 
                            onPlaySound("tab")
                            currentTab = HomeTab.SETTINGS 
                        },
                        onDailyTrigger = { 
                            onPlaySound("tap")
                            showDailyChallengeDialog = true 
                        },
                        onLuckySpinTrigger = {
                            onPlaySound("tap")
                            showLuckySpinDialog = true
                        }
                    )
                }
                HomeTab.STATS -> {
                    StatsTabContent(stats = stats)
                }
                HomeTab.THEMES -> {
                    ThemesTabContent(
                        settings = settings,
                        stats = stats,
                        onSelectCupTheme = { theme ->
                            onPlaySound("unlock")
                            onSelectCupTheme(theme)
                        },
                        onSelectCoinTheme = { theme ->
                            onPlaySound("unlock")
                            onSelectCoinTheme(theme)
                        },
                        onSelectShuffleTheme = { theme ->
                            onPlaySound("unlock")
                            onSelectShuffleTheme(theme)
                        }
                    )
                }
                HomeTab.SETTINGS -> {
                    SettingsTabContent(
                        settings = settings,
                        onToggleSound = {
                            onToggleSound()
                            onPlaySound("tap")
                        },
                        onToggleVibration = {
                            onToggleVibration()
                            onPlaySound("tap")
                        },
                        onToggleReducedMotion = {
                            onToggleReducedMotion()
                            onPlaySound("tap")
                        },
                        onResetProgress = {
                            onResetProgress()
                            onPlaySound("lose")
                        }
                    )
                }
            }
        }

        // Floating Bottom Navigation Bar (Persistent across all tabs!)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, PurpleNightBg.copy(alpha = 0.95f))
                    )
                )
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
                .padding(horizontal = 24.dp)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(PurpleNightSurface.copy(alpha = 0.95f), Color.Black.copy(alpha = 0.97f))
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            LavenderAccent.copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = "HOME",
                icon = Icons.Default.Home,
                isActive = currentTab == HomeTab.HOME,
                onClick = { 
                    if (currentTab != HomeTab.HOME) {
                        onPlaySound("tab")
                        currentTab = HomeTab.HOME 
                    }
                }
            )
            BottomNavItem(
                label = "STATS",
                icon = Icons.Outlined.Leaderboard,
                isActive = currentTab == HomeTab.STATS,
                onClick = { 
                    if (currentTab != HomeTab.STATS) {
                        onPlaySound("tab")
                        currentTab = HomeTab.STATS 
                    }
                }
            )
            BottomNavItem(
                label = "THEMES",
                icon = Icons.Outlined.Palette,
                isActive = currentTab == HomeTab.THEMES,
                onClick = { 
                    if (currentTab != HomeTab.THEMES) {
                        onPlaySound("tab")
                        currentTab = HomeTab.THEMES 
                    }
                }
            )
            BottomNavItem(
                label = "SETTINGS",
                icon = Icons.Outlined.Settings,
                isActive = currentTab == HomeTab.SETTINGS,
                onClick = { 
                    if (currentTab != HomeTab.SETTINGS) {
                        onPlaySound("tab")
                        currentTab = HomeTab.SETTINGS 
                    }
                }
            )
        }

        if (showDailyChallengeDialog) {
            DailyChallengeDialog(
                completedDates = stats.completedDailyDates,
                dailyStreak = stats.dailyStreak,
                onPlayToday = {
                    onPlaySound("jackpot")
                    showDailyChallengeDialog = false
                    onStartGame(GameMode.DAILY_CHALLENGE)
                },
                onDismiss = { 
                    onPlaySound("tap")
                    showDailyChallengeDialog = false 
                }
            )
        }

        if (showLuckySpinDialog) {
            LuckySpinDialog(
                stats = stats,
                isCompletedToday = isLuckySpinCompleted,
                onSpinClaimed = onSpinClaimed,
                onPlaySound = onPlaySound,
                onDismiss = {
                    onPlaySound("tap")
                    showLuckySpinDialog = false
                }
            )
        }
    }
}

@Composable
fun HomeTabContent(
    stats: PlayerStats,
    settings: GameSettings,
    isDailyCompleted: Boolean,
    isLuckySpinCompleted: Boolean,
    isVisible: Boolean,
    onStartGame: (GameMode) -> Unit,
    onOpenSettings: () -> Unit,
    onDailyTrigger: () -> Unit,
    onLuckySpinTrigger: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(GameMode.CLASSIC) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 180.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar: CUP & COIN title left, Settings right
        // Top Bar: CUP & COIN title left, Highest Score & Streak Header Pill center, Settings right
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "CUP & COIN",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 1.2.sp
                    )
                }

                // Header Badges for Best Streak & Highest Score (Loaded from Local Storage)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(PurpleNightSurfaceElevated, Color.Black.copy(alpha = 0.6f))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    // Highest Score Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🏆", fontSize = 11.sp)
                        Text(
                            text = String.format("%,d", stats.bestScore),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldAccent
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(12.dp)
                            .background(Color.White.copy(alpha = 0.2f))
                    )

                    // Highest Streak Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🔥", fontSize = 11.sp)
                        Text(
                            text = "${stats.bestStreak}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = RubyRed
                        )
                    }
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Hero Section: Beautiful 3D cups and title
        item {
            AnimatedEntrance(isVisible = isVisible, delayMillis = 100) {
                HeroSection(settings)
            }
        }

        // Best Stats Cards Row (Mockup Style with 3D and glow effects)
        item {
            AnimatedEntrance(isVisible = isVisible, delayMillis = 200) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BestStatCard(
                        label = "BEST SCORE",
                        value = String.format("%,d", stats.bestScore),
                        icon = Icons.Default.EmojiEvents,
                        iconColor = GoldAccent,
                        modifier = Modifier.weight(1f)
                    )
                    BestStatCard(
                        label = "BEST LEVEL",
                        value = "${stats.highestLevel}",
                        icon = Icons.Outlined.Leaderboard,
                        iconColor = EmeraldGreen,
                        modifier = Modifier.weight(1f)
                    )
                    BestStatCard(
                        label = "BEST STREAK",
                        value = "${stats.bestStreak}",
                        icon = Icons.Default.Whatshot,
                        iconColor = RubyRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Modes Selection Row with 3D scale and glow
        item {
            AnimatedEntrance(isVisible = isVisible, delayMillis = 200) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModeQuickButton(
                        label = "CLASSIC",
                        icon = Icons.Default.EmojiEvents,
                        iconColor = GoldAccent,
                        isSelected = selectedMode == GameMode.CLASSIC,
                        onClick = { selectedMode = GameMode.CLASSIC }
                    )
                    ModeQuickButton(
                        label = "TIME",
                        icon = Icons.Default.FlashOn,
                        iconColor = Color(0xFF29B6F6),
                        isSelected = selectedMode == GameMode.TIME_ATTACK,
                        onClick = { selectedMode = GameMode.TIME_ATTACK }
                    )
                    ModeQuickButton(
                        label = "ENDLESS",
                        icon = Icons.Default.Whatshot,
                        iconColor = Color(0xFFFF7043),
                        isSelected = selectedMode == GameMode.ENDLESS,
                        onClick = { selectedMode = GameMode.ENDLESS }
                    )
                    ModeQuickButton(
                        label = "PERFECT",
                        icon = Icons.Default.Adjust,
                        iconColor = Color(0xFFFFCA28),
                        isSelected = selectedMode == GameMode.PERFECT_RUN,
                        onClick = { selectedMode = GameMode.PERFECT_RUN }
                    )
                    ModeQuickButton(
                        label = "DAILY",
                        icon = Icons.Default.DateRange,
                        iconColor = Color(0xFF66BB6A),
                        isSelected = selectedMode == GameMode.DAILY_CHALLENGE,
                        onClick = { selectedMode = GameMode.DAILY_CHALLENGE }
                    )
                }
            }
        }

        // ═══════════════════════════════════════════
        // MODE-SPECIFIC SHOWCASE CARD (CUSTOM 3D UI FOR EACH MODE)
        // ═══════════════════════════════════════════
        item {
            AnimatedEntrance(isVisible = isVisible, delayMillis = 240) {
                ModeShowcaseCard3D(
                    mode = selectedMode,
                    stats = stats,
                    isDailyCompleted = isDailyCompleted,
                    onPlay = {
                        if (selectedMode == GameMode.DAILY_CHALLENGE) {
                            onDailyTrigger()
                        } else {
                            onStartGame(selectedMode)
                        }
                    }
                )
            }
        }

        // TODAY'S CHALLENGE Card: Glowing Green 3D Accent Card
        item {
            AnimatedEntrance(isVisible = isVisible, delayMillis = 350) {
                TodayChallengeCard(
                    isCompleted = isDailyCompleted,
                    onClick = onDailyTrigger
                )
            }
        }

        // LUCKY SPIN WHEEL Card: Glowing Cyan 3D Card
        item {
            AnimatedEntrance(isVisible = isVisible, delayMillis = 370) {
                LuckySpinWheelCard(
                    isCompleted = isLuckySpinCompleted,
                    stats = stats,
                    onClick = onLuckySpinTrigger
                )
            }
        }

        // TODAY Stats Row Section
        item {
            AnimatedEntrance(isVisible = isVisible, delayMillis = 400) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "TODAY'S PROGRESS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TodayStatItem(
                            label = "Best Streak",
                            value = "${stats.currentStreak}",
                            icon = Icons.Default.Whatshot,
                            iconColor = Color(0xFFFF7043),
                            modifier = Modifier.weight(1f)
                        )
                        TodayStatItem(
                            label = "Best Level",
                            value = "${stats.highestLevel}",
                            icon = Icons.Outlined.Leaderboard,
                            iconColor = Color(0xFF29B6F6),
                            modifier = Modifier.weight(1f)
                        )
                        TodayStatItem(
                            label = "Coins Found",
                            value = "${(stats.classicStats.totalCorrect + stats.endlessStats.totalCorrect + stats.timeAttackStats.totalCorrect) * 10}",
                            icon = Icons.Default.MonetizationOn,
                            iconColor = GoldAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsTabContent(stats: PlayerStats) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 180.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = EmeraldGreen.copy(alpha = 0.2f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "CAREER STATS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Real-time gaming performance analytics",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
        }

        // Highlight Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatHighlightCard3D(
                    title = "WIN RATE",
                    value = "${stats.winRatePercent}%",
                    subtitle = "${stats.gamesWon} Wins of ${stats.gamesPlayed}",
                    accent = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
                StatHighlightCard3D(
                    title = "BEST SCORE",
                    value = "${stats.bestScore}",
                    subtitle = "All-Time Points",
                    accent = VegasGold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Match breakdown
        item {
            Glowing3DCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = LavenderAccent.copy(alpha = 0.5f),
                isGlowing = false
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "MATCH BREAKDOWN",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LavenderAccent,
                        letterSpacing = 1.sp
                    )
                    Divider(color = Color.White.copy(alpha = 0.1f))

                    StatDetailRow("Total Games Played", "${stats.gamesPlayed}")
                    StatDetailRow("Victories (Won)", "${stats.gamesWon}", EmeraldGreen)
                    StatDetailRow("Defeats (Missed)", "${stats.gamesLost}", RubyRed)
                    StatDetailRow("Highest Level Cleared", "Level ${stats.highestLevel}", LavenderLight)
                    StatDetailRow("Best Combo Multiplier", "🔥 x${stats.bestCombo}", VegasGold)
                    StatDetailRow("Longest Win Streak", "⚡ ${stats.bestStreak} Wins", NeonCyan)
                    if (stats.dailyStreak > 0) {
                        StatDetailRow("Daily Challenge Streak", "📅 ${stats.dailyStreak} Days", EmeraldGreen)
                    }
                }
            }
        }

        // Share Stats Action Button
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = {
                    val appUrl = "https://ais-pre-zd2ct6cs36h4qk7rq4htax-95295274561.asia-southeast1.run.app"
                    val shareMsg = """
                        🏆 *Cup Shuffle 3D Career Stats! Can you beat me?* 🧠
                        
                        ⭐ *Best Score*: ${stats.bestScore} pts
                        ⚡ *Highest Level Cleared*: Level ${stats.highestLevel}
                        🔥 *Win Rate*: ${stats.winRatePercent}%
                        📅 *Daily Challenge Streak*: ${stats.dailyStreak} Days
                        ⚔️ *Total Games Played*: ${stats.gamesPlayed} Matches
                        
                        🌀 Master 3D vertical orbital orbits, helical twists, and rapid cup vortex shuffles!
                        📥 Download the official Android game directly here:
                        $appUrl
                    """.trimIndent()
                    com.example.util.ShareUtils.shareText(context, shareMsg, "Share Career Stats")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(EmeraldGreen, NeonCyan)
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(EmeraldGreen.copy(alpha = 0.15f), NeonCyan.copy(alpha = 0.15f))
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("⚔️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SHARE CAREER ACHIEVEMENTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Quote badge
        item {
            Glowing3DCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = GoldAccent.copy(alpha = 0.6f),
                isGlowing = true,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "🏆", fontSize = 28.sp)
                    Column {
                        Text(
                            text = "Keep Pushing Higher!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Unlock exclusive golden themes by beating Level 20+ and scoring 500+ points.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemesTabContent(
    settings: GameSettings,
    stats: PlayerStats,
    onSelectCupTheme: (CupTheme) -> Unit,
    onSelectCoinTheme: (CoinTheme) -> Unit,
    onSelectShuffleTheme: (ShuffleTheme) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Cups, 1 = Coins, 2 = Shuffle
    var previewCupTheme by remember(settings.selectedCupTheme) { mutableStateOf(settings.selectedCupTheme) }
    var previewCoinTheme by remember(settings.selectedCoinTheme) { mutableStateOf(settings.selectedCoinTheme) }
    var previewShuffleTheme by remember(settings.selectedShuffleTheme) { mutableStateOf(settings.selectedShuffleTheme) }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 180.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = LavenderAccent.copy(alpha = 0.2f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = LavenderAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "3D THEMES VAULT",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Inspect all 3D skins • Unlock by leveling up & scoring",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
        }

        // Tab selection (Double bezel 3D style)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MinimalSurfaceElevated,
                contentColor = LavenderAccent,
                indicator = {},
                divider = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "🥤 CUPS",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) LavenderAccent else TextMuted,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 0) MinimalSurface else Color.Transparent)
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "🪙 COINS",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) LavenderAccent else TextMuted,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 1) MinimalSurface else Color.Transparent)
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "🌪️ SHUFFLE",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 2) LavenderAccent else TextMuted,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 2) MinimalSurface else Color.Transparent)
                )
            }
        }

        // ═══════════════════════════════════════════
        // 3D LIVE PREVIEW SHOWCASE STAGE
        // ═══════════════════════════════════════════
        item {
            when (selectedTab) {
                0 -> {
                    val isUnlocked = stats.highestLevel >= previewCupTheme.unlockLevel
                    val isEquipped = settings.selectedCupTheme == previewCupTheme
                    ThemeLivePreviewStage(
                        title = previewCupTheme.displayName,
                        category = "3D CUP SKIN",
                        accentColor = previewCupTheme.primaryColor,
                        isUnlocked = isUnlocked,
                        isEquipped = isEquipped,
                        unlockRequirementText = if (isUnlocked) "UNLOCKED (Req. Lvl ${previewCupTheme.unlockLevel})" else "LOCK: Reach Level ${previewCupTheme.unlockLevel} (Current: Lvl ${stats.highestLevel})",
                        onEquip = {
                            if (isUnlocked) {
                                onSelectCupTheme(previewCupTheme)
                                Toast.makeText(context, "Equipped ${previewCupTheme.displayName}!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Locked! Reach Level ${previewCupTheme.unlockLevel} to equip.", Toast.LENGTH_LONG).show()
                            }
                        },
                        previewContent = {
                            CupVisual(
                                theme = previewCupTheme,
                                width = 72.dp,
                                height = 88.dp,
                                isLifted = true,
                                liftAmount = 0.45f
                            )
                        }
                    )
                }
                1 -> {
                    val isUnlocked = stats.bestScore >= previewCoinTheme.unlockScore
                    val isEquipped = settings.selectedCoinTheme == previewCoinTheme
                    ThemeLivePreviewStage(
                        title = "${previewCoinTheme.displayName} (${previewCoinTheme.symbol})",
                        category = "3D COIN ARTIFACT",
                        accentColor = previewCoinTheme.baseColor,
                        isUnlocked = isUnlocked,
                        isEquipped = isEquipped,
                        unlockRequirementText = if (isUnlocked) "UNLOCKED (Req. ${previewCoinTheme.unlockScore} Pts)" else "LOCK: Reach ${previewCoinTheme.unlockScore} Pts (Best: ${stats.bestScore})",
                        onEquip = {
                            if (isUnlocked) {
                                onSelectCoinTheme(previewCoinTheme)
                                Toast.makeText(context, "Equipped ${previewCoinTheme.displayName}!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Locked! Reach score ${previewCoinTheme.unlockScore} to equip.", Toast.LENGTH_LONG).show()
                            }
                        },
                        previewContent = {
                            CoinVisual(theme = previewCoinTheme, size = 68.dp, isSpinning = true)
                        }
                    )
                }
                2 -> {
                    val isUnlocked = stats.highestLevel >= previewShuffleTheme.unlockLevel
                    val isEquipped = settings.selectedShuffleTheme == previewShuffleTheme
                    val accentColor = when (previewShuffleTheme) {
                        ShuffleTheme.CLASSIC_SLIDE -> Color(0xFF00E5FF)
                        ShuffleTheme.DOUBLE_SPIN_WAVE -> Color(0xFFE040FB)
                        ShuffleTheme.COSMIC_ZIG_ZAG -> Color(0xFFFFD700)
                        ShuffleTheme.CHAOS_VORTEX -> Color(0xFFFF3D00)
                    }
                    ThemeLivePreviewStage(
                        title = previewShuffleTheme.displayName,
                        category = "3D SHUFFLE PATTERN",
                        accentColor = accentColor,
                        isUnlocked = isUnlocked,
                        isEquipped = isEquipped,
                        unlockRequirementText = if (isUnlocked) "UNLOCKED (Req. Lvl ${previewShuffleTheme.unlockLevel})" else "LOCK: Reach Level ${previewShuffleTheme.unlockLevel} (Current: Lvl ${stats.highestLevel})",
                        onEquip = {
                            if (isUnlocked) {
                                onSelectShuffleTheme(previewShuffleTheme)
                                Toast.makeText(context, "Equipped ${previewShuffleTheme.displayName}!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Locked! Reach Level ${previewShuffleTheme.unlockLevel} to equip.", Toast.LENGTH_LONG).show()
                            }
                        },
                        previewContent = {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(accentColor.copy(alpha = 0.2f), CircleShape)
                                    .border(2.dp, accentColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = previewShuffleTheme.effectEmoji,
                                    fontSize = 36.sp
                                )
                            }
                        }
                    )
                }
            }
        }

        // Section Title for Vault Catalog
        item {
            Text(
                text = "SKIN VAULT CATALOG (TAP TO PREVIEW)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Theme list
        if (selectedTab == 0) {
            items(CupTheme.entries) { theme ->
                val isUnlocked = stats.highestLevel >= theme.unlockLevel
                val isSelected = settings.selectedCupTheme == theme
                val isInspected = previewCupTheme == theme

                ThemeItemRow3D(
                    title = theme.displayName,
                    subtitle = if (isUnlocked) {
                        if (isSelected) "Active Equipped Theme ✓" else "Unlocked • Tap to Inspect"
                    } else {
                        "🔒 Requires Level ${theme.unlockLevel}"
                    },
                    isUnlocked = isUnlocked,
                    isSelected = isSelected,
                    isInspected = isInspected,
                    accentColor = theme.primaryColor,
                    onSelect = {
                        previewCupTheme = theme
                        if (isUnlocked) onSelectCupTheme(theme)
                    },
                    iconContent = {
                        CupVisual(theme = theme, width = 36.dp, height = 44.dp)
                    }
                )
            }
        } else if (selectedTab == 1) {
            items(CoinTheme.entries) { theme ->
                val isUnlocked = stats.bestScore >= theme.unlockScore
                val isSelected = settings.selectedCoinTheme == theme
                val isInspected = previewCoinTheme == theme

                ThemeItemRow3D(
                    title = "${theme.displayName} (${theme.symbol})",
                    subtitle = if (isUnlocked) {
                        if (isSelected) "Active Equipped Coin ✓" else "Unlocked • Tap to Inspect"
                    } else {
                        "🔒 Requires ${theme.unlockScore} Points"
                    },
                    isUnlocked = isUnlocked,
                    isSelected = isSelected,
                    isInspected = isInspected,
                    accentColor = theme.baseColor,
                    onSelect = {
                        previewCoinTheme = theme
                        if (isUnlocked) onSelectCoinTheme(theme)
                    },
                    iconContent = {
                        CoinVisual(theme = theme, size = 36.dp, isSpinning = false)
                    }
                )
            }
        } else {
            items(ShuffleTheme.entries) { theme ->
                val isUnlocked = stats.highestLevel >= theme.unlockLevel
                val isSelected = settings.selectedShuffleTheme == theme
                val isInspected = previewShuffleTheme == theme
                val accentColor = when (theme) {
                    ShuffleTheme.CLASSIC_SLIDE -> Color(0xFF00E5FF)
                    ShuffleTheme.DOUBLE_SPIN_WAVE -> Color(0xFFE040FB)
                    ShuffleTheme.COSMIC_ZIG_ZAG -> Color(0xFFFFD700)
                    ShuffleTheme.CHAOS_VORTEX -> Color(0xFFFF3D00)
                }

                ThemeItemRow3D(
                    title = theme.displayName,
                    subtitle = if (isUnlocked) {
                        if (isSelected) "Active Shuffle Style ✓" else "Unlocked • Tap to Inspect"
                    } else {
                        "🔒 Requires Level ${theme.unlockLevel}"
                    },
                    isUnlocked = isUnlocked,
                    isSelected = isSelected,
                    isInspected = isInspected,
                    accentColor = accentColor,
                    onSelect = {
                        previewShuffleTheme = theme
                        if (isUnlocked) onSelectShuffleTheme(theme)
                    },
                    iconContent = {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = theme.effectEmoji,
                                fontSize = 22.sp
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ThemeLivePreviewStage(
    title: String,
    category: String,
    accentColor: Color,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    unlockRequirementText: String,
    onEquip: () -> Unit,
    previewContent: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.45f),
                        radius = size.width * 0.6f
                    )
                )
            }
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E172F),
                        Color(0xFF0F0B18)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.5f),
                        accentColor.copy(alpha = 0.6f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Category Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = category,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    letterSpacing = 1.2.sp
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isEquipped) EmeraldGreen.copy(alpha = 0.2f) else if (isUnlocked) GoldAccent.copy(alpha = 0.2f) else RubyRed.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isEquipped) EmeraldGreen.copy(alpha = 0.5f) else if (isUnlocked) GoldAccent.copy(alpha = 0.5f) else RubyRed.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = if (isEquipped) "EQUIPPED" else if (isUnlocked) "UNLOCKED" else "LOCKED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isEquipped) EmeraldGreen else if (isUnlocked) GoldAccent else RubyRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3D Visual Floating Display Stage
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(accentColor.copy(alpha = 0.45f), Color.Transparent),
                                radius = size.width * 0.7f
                            )
                        )
                    }
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .border(1.5.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                previewContent()
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title & Unlock Info
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = unlockRequirementText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) EmeraldGreen else RubyRed,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3D Equip / Locked Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isEquipped) {
                                listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
                            } else if (isUnlocked) {
                                listOf(accentColor, accentColor.copy(alpha = 0.75f))
                            } else {
                                listOf(Color(0xFF37474F), Color(0xFF212121))
                            }
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(enabled = isUnlocked && !isEquipped, onClick = onEquip),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isEquipped) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CURRENTLY EQUIPPED ✓", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                    } else if (isUnlocked) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EQUIP THIS SKIN ➔", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.Black)
                    } else {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LEVEL REQUIREMENT LOCKED", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsTabContent(
    settings: GameSettings,
    onToggleSound: () -> Unit,
    onToggleVibration: () -> Unit,
    onToggleReducedMotion: () -> Unit,
    onResetProgress: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 180.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "GAME SETTINGS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Customize audio, haptics and motion",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
        }

        // Toggles Box
        item {
            Glowing3DCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = LavenderAccent.copy(alpha = 0.5f),
                isGlowing = false
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "PREFERENCES",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LavenderAccent,
                        letterSpacing = 1.sp
                    )
                    Divider(color = Color.White.copy(alpha = 0.1f))

                    SettingToggleRow3D(
                        title = "Sound Effects",
                        subtitle = "Procedural sound generation",
                        checked = settings.soundEnabled,
                        onCheckedChange = { onToggleSound() }
                    )
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    SettingToggleRow3D(
                        title = "Haptic Vibration",
                        subtitle = "Tactile feedback on taps & shuffles",
                        checked = settings.vibrationEnabled,
                        onCheckedChange = { onToggleVibration() }
                    )
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    SettingToggleRow3D(
                        title = "Reduced Motion",
                        subtitle = "Slower, gentler cup transitions",
                        checked = settings.reducedMotion,
                        onCheckedChange = { onToggleReducedMotion() }
                    )
                }
            }
        }

        // Information & Legal Card
        item {
            var showAboutModal by remember { mutableStateOf(false) }
            var showSupportModal by remember { mutableStateOf(false) }
            var showPrivacyModal by remember { mutableStateOf(false) }
            var showTermsModal by remember { mutableStateOf(false) }

            val context = LocalContext.current
            val clipboardManager = LocalClipboardManager.current

            Glowing3DCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = NeonCyan.copy(alpha = 0.5f),
                isGlowing = false
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "INFORMATION & LEGAL",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Divider(color = Color.White.copy(alpha = 0.1f))

                    // Row 1: About Us
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAboutModal = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("About Developer & Game", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("App version, credits & technology", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                    }
                    Divider(color = Color.White.copy(alpha = 0.05f))

                    // Row 2: Help & Support
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSupportModal = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Help & Support", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Email support, FAQs & feedback", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                    }
                    Divider(color = Color.White.copy(alpha = 0.05f))

                    // Row 3: Privacy Policy
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrivacyModal = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Privacy Policy", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("User data protection details (100% offline)", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                    }
                    Divider(color = Color.White.copy(alpha = 0.05f))

                    // Row 4: Terms & Conditions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTermsModal = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Terms & Conditions", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Usage terms & license agreements", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                    }
                }
            }

            // About Us Dialog Modal
            if (showAboutModal) {
                AlertDialog(
                    onDismissRequest = { showAboutModal = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("ℹ️", fontSize = 24.sp)
                            Text("About Cup Shuffle 3D", fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Cup Shuffle 3D: Brain Trainer is a premium concentration and memory building shell game designed to test your focus limits.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 14.sp
                            )
                            Divider(color = Color.White.copy(alpha = 0.1f))
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Developer Studio:", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 13.sp)
                                Text("Abhix Official", color = Color.White, fontSize = 13.sp)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Creator Username:", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 13.sp)
                                Text("Abhixofficial01", color = Color.White, fontSize = 13.sp)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Version Info:", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 13.sp)
                                Text("v1.0.0 (Production-Ready)", color = Color.White, fontSize = 13.sp)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Tech Stack:", fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 13.sp)
                                Text("Kotlin, Jetpack Compose", color = Color.White, fontSize = 13.sp)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showAboutModal = false },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                        ) {
                            Text("Got it!", color = MidnightNavy, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = MidnightNavy,
                    shape = RoundedCornerShape(22.dp)
                )
            }

            // Help & Support Modal with Intent & Copy Clipboard Actions
            if (showSupportModal) {
                AlertDialog(
                    onDismissRequest = { showSupportModal = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("💬", fontSize = 24.sp)
                            Text("Help & Support", fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)
                        }
                    },
                    text = {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    "We are here to help! If you experience any bugs, have questions, or want to share feedback, feel free to contact us directly.",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.sp
                                )
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                        .border(1.dp, LavenderAccent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("OFFICIAL CONTACT EMAIL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LavenderAccent)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Abhixofficial01@gmail.com", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString("Abhixofficial01@gmail.com"))
                                                    Toast.makeText(context, "Email copied to clipboard!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = LavenderAccent, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("FREQUENTLY ASKED QUESTIONS (FAQ)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LavenderAccent)
                            }
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f))) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Q: How do I unlock new Shuffling Animations?", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                        Text("A: Your shuffle types unlock automatically as your career highest level increases! (Orbit at Lvl 4, Zig-Zag at Lvl 8, Vortex at Lvl 12).", color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp, lineHeight = 14.sp)
                                    }
                                }
                            }
                            item {
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f))) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Q: Will my high score data be deleted if I uninstall?", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                        Text("A: Yes. Because this is a 100% offline game that protects your privacy, all progress is stored strictly on your phone's memory.", color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp, lineHeight = 14.sp)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { showSupportModal = false }) {
                                Text("Close", color = Color.White.copy(alpha = 0.6f))
                            }
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:")
                                            putExtra(Intent.EXTRA_EMAIL, arrayOf("Abhixofficial01@gmail.com"))
                                            putExtra(Intent.EXTRA_SUBJECT, "Cup Shuffle 3D Support Request")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No email client installed! Copied to clipboard.", Toast.LENGTH_LONG).show()
                                        clipboardManager.setText(AnnotatedString("Abhixofficial01@gmail.com"))
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent)
                            ) {
                                Text("Send Email", color = MidnightNavy, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    containerColor = MidnightNavy,
                    shape = RoundedCornerShape(22.dp)
                )
            }

            // Privacy Policy Scrollable Modal
            if (showPrivacyModal) {
                AlertDialog(
                    onDismissRequest = { showPrivacyModal = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("🛡️", fontSize = 24.sp)
                            Text("Privacy Policy", fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)
                        }
                    },
                    text = {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text("Last Updated: August 20, 2026", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
                            }
                            item {
                                Text(
                                    "Your privacy is absolutely vital to us. Cup Shuffle 3D has been designed with strict safety regulations in mind:",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                            item {
                                Text(
                                    "1. 100% OFFLINE:\nThis game does NOT connect to any servers, API endpoints, or databases. It works fully offline.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                            item {
                                Text(
                                    "2. NO DATA COLLECTION:\nWe do not gather, store, request, or share any personal identifiable data. No email addresses, phone numbers, location, or device telemetry are tracked.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                            item {
                                Text(
                                    "3. NO THIRD PARTY SDKs:\nWe contain zero ad network SDKs, tracking cookies, or analytic pixels. Your child is perfectly safe from target marketing.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                            item {
                                Text(
                                    "4. LOCAL STORAGE:\nScores, unlocked game levels, settings, and coins are kept strictly on your own device using Android secure preferences storage.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showPrivacyModal = false },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("I Understand", color = MidnightNavy, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = MidnightNavy,
                    shape = RoundedCornerShape(22.dp)
                )
            }

            // Terms & Conditions Scrollable Modal
            if (showTermsModal) {
                AlertDialog(
                    onDismissRequest = { showTermsModal = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("📜", fontSize = 24.sp)
                            Text("Terms & Conditions", fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp)
                        }
                    },
                    text = {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text("Last Updated: August 20, 2026", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
                            }
                            item {
                                Text(
                                    "By using or playing Cup Shuffle 3D, you consent and agree to the following terms of usage:",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                            item {
                                Text(
                                    "1. INTELLECTUAL PROPERTY:\nAll visual art, custom 3D vector cup assets, synthesized sound effects, graphics and code blocks belong to developer Abhix Official (Abhixofficial01). Direct cloning, distribution, or decompilation is strictly prohibited.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                            item {
                                Text(
                                    "2. LOCAL SAVE LIABILITY:\nBecause progress resides strictly inside your phone's memory, we hold no liability for lost scores or career themes caused by phone factory resets, app cache cleans, or uninstallation.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                            item {
                                Text(
                                    "3. LICENSE:\nWe grant you a non-commercial, non-exclusive revocable license to install and play the game for personal gaming amusement.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showTermsModal = false },
                            colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent)
                        ) {
                            Text("Accept Terms", color = MidnightNavy, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = MidnightNavy,
                    shape = RoundedCornerShape(22.dp)
                )
            }
        }

        // Danger Zone
        item {
            Glowing3DCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = RubyRed.copy(alpha = 0.7f),
                isGlowing = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "DANGER ZONE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RubyRed,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Permanently clear your high scores, career records, unlocked themes, and stats. This cannot be undone.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RubyRed),
                        border = ButtonDefaults.outlinedButtonBorder().copy(
                            brush = Brush.linearGradient(listOf(RubyRed, RubyRed))
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("RESET ALL PROGRESS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Offline Info
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldGreen.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "100% Offline Ready • Zero Internet Required",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldGreen
                        )
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Progress?", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = { Text("This action cannot be undone. All your high scores and unlocked themes will be cleared.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        onResetProgress()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RubyDark)
                ) {
                    Text("Yes, Reset", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel", color = TextPrimary)
                }
            },
            containerColor = MinimalSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ThemeItemRow3D(
    title: String,
    subtitle: String,
    isUnlocked: Boolean,
    isSelected: Boolean,
    isInspected: Boolean = false,
    accentColor: Color,
    onSelect: () -> Unit,
    iconContent: @Composable () -> Unit
) {
    Glowing3DCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        glowColor = if (isInspected) accentColor else if (isSelected) LavenderAccent else accentColor.copy(alpha = 0.4f),
        isGlowing = isInspected || isSelected,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = if (isInspected) 0.3f else 0.15f))
                    .border(
                        width = if (isInspected) 2.dp else 1.dp,
                        color = if (isInspected) accentColor else accentColor.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                iconContent()
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.7f)
                    )
                    if (!isUnlocked) {
                        Text("🔒", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (isSelected) LavenderAccent else if (isUnlocked) Color.White.copy(alpha = 0.7f) else RubyRed
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = EmeraldGreen,
                    modifier = Modifier.size(26.dp)
                )
            } else if (isInspected) {
                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .border(1.dp, accentColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("INSPECTING", fontSize = 9.sp, fontWeight = FontWeight.Black, color = accentColor)
                }
            }
        }
    }
}

@Composable
fun ModeShowcaseCard3D(
    mode: GameMode,
    stats: PlayerStats,
    isDailyCompleted: Boolean,
    onPlay: () -> Unit
) {
    val modeAccent = when (mode) {
        GameMode.CLASSIC -> GoldAccent
        GameMode.TIME_ATTACK -> Color(0xFF00E5FF)
        GameMode.ENDLESS -> Color(0xFFFF5722)
        GameMode.PERFECT_RUN -> Color(0xFFFFD54F)
        GameMode.DAILY_CHALLENGE -> EmeraldGreen
    }

    val modeGradient = when (mode) {
        GameMode.CLASSIC -> listOf(Color(0xFF2C2205), Color(0xFF130E01))
        GameMode.TIME_ATTACK -> listOf(Color(0xFF072138), Color(0xFF020B14))
        GameMode.ENDLESS -> listOf(Color(0xFF33140A), Color(0xFF140502))
        GameMode.PERFECT_RUN -> listOf(Color(0xFF2F240A), Color(0xFF120E02))
        GameMode.DAILY_CHALLENGE -> listOf(Color(0xFF0A2B1D), Color(0xFF02120C))
    }

    val modeIcon = when (mode) {
        GameMode.CLASSIC -> Icons.Default.EmojiEvents
        GameMode.TIME_ATTACK -> Icons.Default.FlashOn
        GameMode.ENDLESS -> Icons.Default.Whatshot
        GameMode.PERFECT_RUN -> Icons.Default.Adjust
        GameMode.DAILY_CHALLENGE -> Icons.Default.DateRange
    }

    val modeTagline = when (mode) {
        GameMode.CLASSIC -> "PROGRESSIVE LEVEL CHALLENGE"
        GameMode.TIME_ATTACK -> "30-SECOND HIGH-SPEED BLITZ"
        GameMode.ENDLESS -> "ENDLESS SURVIVAL ARENA • 3 LIVES"
        GameMode.PERFECT_RUN -> "FLAWLESS RUN • ZERO MISTAKES"
        GameMode.DAILY_CHALLENGE -> "DAILY PUZZLE SEED ARENA"
    }

    val modeDescription = when (mode) {
        GameMode.CLASSIC -> "Start from Level 1, track accelerating cup shuffles, and level up with consecutive wins!"
        GameMode.TIME_ATTACK -> "Race against the clock! Fast correct guesses add +3.0s extra time to your countdown."
        GameMode.ENDLESS -> "Infinite survival run! Speed multiplies every 3 rounds. Shields absorb incorrect guesses."
        GameMode.PERFECT_RUN -> "Strict perfection gauntlet! A single incorrect guess ends your run immediately."
        GameMode.DAILY_CHALLENGE -> "Unique daily seed for all players worldwide. Solve daily to stack continuous streaks!"
    }

    val buttonText = when (mode) {
        GameMode.CLASSIC -> "PLAY CLASSIC (LEVEL ${stats.highestLevel}) ➔"
        GameMode.TIME_ATTACK -> "START TIME ATTACK BLITZ ⚡"
        GameMode.ENDLESS -> "ENTER ENDLESS SURVIVAL 🔥"
        GameMode.PERFECT_RUN -> "LAUNCH PERFECT RUN 💎"
        GameMode.DAILY_CHALLENGE -> if (isDailyCompleted) "REPLAY DAILY CHALLENGE 📅" else "PLAY TODAY'S PUZZLE ➔"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_mode_btn")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mode_btn_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(modeAccent.copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.35f),
                        radius = size.width * 0.75f
                    )
                )
            }
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.verticalGradient(modeGradient))
            .border(
                width = 1.8.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.55f), modeAccent.copy(alpha = 0.7f), Color.Transparent)
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mode Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(modeAccent.copy(alpha = 0.2f), CircleShape)
                            .border(1.5.dp, modeAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = modeIcon,
                            contentDescription = null,
                            tint = modeAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = mode.title.uppercase(),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = modeTagline,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = modeAccent,
                            letterSpacing = 0.6.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, modeAccent.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = when (mode) {
                            GameMode.CLASSIC -> "LVL ${stats.highestLevel}"
                            GameMode.TIME_ATTACK -> "${stats.timeAttackStats.bestScore} PTS"
                            GameMode.ENDLESS -> "🛡️ ${stats.shieldCount} SHIELDS"
                            GameMode.PERFECT_RUN -> "BEST ${stats.bestStreak}"
                            GameMode.DAILY_CHALLENGE -> "${stats.dailyStreak}D STREAK"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = modeAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = modeDescription,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.78f),
                lineHeight = 16.sp
            )

            // Dynamic Mode Highlights Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (mode) {
                    GameMode.CLASSIC -> {
                        ModeStatBadge("CLEAR RECORD", "Lvl ${stats.highestLevel}", GoldAccent)
                        ModeStatBadge("WIN RATE", "${stats.winRatePercent}%", EmeraldGreen)
                        ModeStatBadge("COMBO MULTI", "x${stats.bestCombo}", VegasGold)
                    }
                    GameMode.TIME_ATTACK -> {
                        ModeStatBadge("TIME BEST", "${stats.timeAttackStats.bestScore} Pts", Color(0xFF00E5FF))
                        ModeStatBadge("GAMES WON", "${stats.timeAttackStats.totalWins}", EmeraldGreen)
                        ModeStatBadge("TIME BONUS", "+3.0s / Win", GoldAccent)
                    }
                    GameMode.ENDLESS -> {
                        ModeStatBadge("LIVES", "3 Hearts", RubyRed)
                        ModeStatBadge("ACTIVE SHIELDS", "${stats.shieldCount} 🛡️", Color(0xFF00E5FF))
                        ModeStatBadge("ENDLESS RECORD", "${stats.endlessStats.bestScore} Pts", Color(0xFFFF5722))
                    }
                    GameMode.PERFECT_RUN -> {
                        ModeStatBadge("TOLERANCE", "0 Errors", RubyRed)
                        ModeStatBadge("STRIKE RECORD", "${stats.bestStreak} Wins", GoldAccent)
                        ModeStatBadge("DIFFICULTY", "INSANE", RubyRed)
                    }
                    GameMode.DAILY_CHALLENGE -> {
                        ModeStatBadge("ACTIVE STREAK", "${stats.dailyStreak} Days", GoldAccent)
                        ModeStatBadge("TODAY'S PUZZLE", if (isDailyCompleted) "SOLVED ✓" else "READY ⚡", if (isDailyCompleted) EmeraldGreen else Color(0xFF00E5FF))
                        ModeStatBadge("REWARDS", "Badge + Streak", EmeraldGreen)
                    }
                }
            }

            // ═══════════════════════════════════════════
            // MASSIVE 3D EMBOSSED MODE-SPECIFIC PLAY BUTTON
            // ═══════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                modeAccent,
                                modeAccent.copy(alpha = 0.75f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.8f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buttonText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        letterSpacing = 1.sp,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.White.copy(alpha = 0.4f),
                                offset = Offset(0f, 1f),
                                blurRadius = 2f
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeStatBadge(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Black, color = valueColor)
    }
}

@Composable
private fun SettingToggleRow3D(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = LavenderAccent,
                checkedTrackColor = LavenderAccent.copy(alpha = 0.35f),
                uncheckedThumbColor = Color.White.copy(alpha = 0.5f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
fun StatHighlightCard3D(
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Glowing3DCard(
        modifier = modifier,
        glowColor = accent,
        isGlowing = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                style = TextStyle(
                    shadow = Shadow(
                        color = accent.copy(alpha = 0.5f),
                        offset = Offset(0f, 2f),
                        blurRadius = 6f
                    )
                )
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun StatDetailRow(label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun BestStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Glowing3DCard(
        modifier = modifier,
        glowColor = iconColor,
        isGlowing = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = 0.45f),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, iconColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                style = TextStyle(
                    shadow = Shadow(
                        color = iconColor.copy(alpha = 0.5f),
                        offset = Offset(0f, 2f),
                        blurRadius = 6f
                    )
                )
            )
        }
    }
}

@Composable
fun ModeQuickButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else if (isSelected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                iconColor.copy(alpha = if (isSelected) 0.45f else 0.25f),
                                Color.Transparent
                            ),
                            radius = size.width * (if (isSelected) 0.9f else 0.7f)
                        )
                    )
                }
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PurpleNightSurface.copy(alpha = if (isSelected) 0.95f else 0.8f),
                            if (isSelected) iconColor.copy(alpha = 0.25f) else Color.Black
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = if (isSelected) 2.5.dp else 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = if (isSelected) {
                            listOf(Color.White, iconColor)
                        } else {
                            listOf(Color.White.copy(alpha = 0.3f), iconColor.copy(alpha = 0.8f))
                        }
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) iconColor else iconColor.copy(alpha = 0.85f),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.ExtraBold,
            color = if (isSelected) iconColor else Color.White.copy(alpha = 0.65f),
            letterSpacing = 0.5.sp,
            style = if (isSelected) {
                TextStyle(
                    shadow = Shadow(
                        color = iconColor.copy(alpha = 0.5f),
                        blurRadius = 4f
                    )
                )
            } else {
                TextStyle.Default
            }
        )
    }
}

@Composable
fun TodayChallengeCard(
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val greenAccent = Color(0xFF4CAF50)
    val greenDark = Color(0xFF1B5E20)
    val infiniteTransition = rememberInfiniteTransition(label = "challenge_pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            greenAccent.copy(alpha = borderAlpha * 0.18f),
                            Color.Transparent
                        ),
                        radius = size.width * 0.6f
                    )
                )
            }
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        greenDark.copy(alpha = 0.25f),
                        Color.Black.copy(alpha = 0.8f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        greenAccent.copy(alpha = borderAlpha),
                        greenAccent.copy(alpha = borderAlpha * 0.3f),
                        greenAccent.copy(alpha = borderAlpha)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(greenAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, greenAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = greenAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "TODAY'S CHALLENGE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = greenAccent,
                        letterSpacing = 0.5.sp,
                        style = TextStyle(
                            shadow = Shadow(
                                color = greenAccent.copy(alpha = 0.5f),
                                offset = Offset(0f, 1f),
                                blurRadius = 4f
                            )
                        )
                    )
                    Text(
                        text = if (isCompleted) "Challenge completed! ✓" else "One puzzle. One chance.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(greenAccent, CircleShape)
                    .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun TodayStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Glowing3DCard(
        modifier = modifier,
        glowColor = iconColor.copy(alpha = 0.5f),
        isGlowing = false,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
fun BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val activeColor = when (label) {
        "HOME" -> Color(0xFF00E5FF)       // Glowing Neon Cyan
        "STATS" -> Color(0xFF00E676)      // Victory Green
        "THEMES" -> Color(0xFFFFD700)     // Shiny Gold
        "SETTINGS" -> Color(0xFFE040FB)   // Bright Magenta
        else -> LavenderAccent
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Magnetic scale and spring bouncing transition
    val scaleFactor by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else if (isActive) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nav_item_scale"
    )

    // Glow background animation for active tabs
    val glowAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.15f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "glow_alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scaleFactor
                scaleY = scaleFactor
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp, horizontal = 10.dp)
    ) {
        // Glowing 3D background behind icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .drawBehind {
                    if (glowAlpha > 0f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(activeColor.copy(alpha = glowAlpha), Color.Transparent),
                                radius = size.width * 0.75f
                            )
                        )
                    }
                }
                .background(
                    if (isActive) activeColor.copy(alpha = 0.12f) else Color.Transparent,
                    RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    brush = if (isActive) {
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.4f), activeColor.copy(alpha = 0.6f))
                        )
                    } else {
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                    },
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else Color.White.copy(alpha = 0.45f),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isActive) activeColor else Color.White.copy(alpha = 0.45f),
            letterSpacing = 0.5.sp,
            style = TextStyle(
                shadow = if (isActive) {
                    Shadow(
                        color = activeColor.copy(alpha = 0.6f),
                        offset = Offset(0f, 1f),
                        blurRadius = 3f
                    )
                } else {
                    null
                }
            )
        )
    }
}

@Composable
fun HeroSection(settings: GameSettings) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_anim")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_float"
    )
    
    val coinSpin by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hero_coin_spin"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .graphicsLayer { translationY = floatAnim }
        ) {
            CupVisual(
                theme = settings.selectedCupTheme,
                width = 46.dp,
                height = 54.dp,
                tiltDegrees = -10f
            )
            Box(contentAlignment = Alignment.Center) {
                CupVisual(
                    theme = settings.selectedCupTheme,
                    width = 60.dp,
                    height = 72.dp,
                    isLifted = true,
                    liftAmount = 0.55f,
                    tiltDegrees = (floatAnim / 3)
                )
                Box(modifier = Modifier.graphicsLayer { rotationY = coinSpin }) {
                    CoinVisual(
                        theme = settings.selectedCoinTheme,
                        size = 28.dp,
                        isSpinning = false,
                        isGlowing = true
                    )
                }
            }
            CupVisual(
                theme = settings.selectedCupTheme,
                width = 46.dp,
                height = 54.dp,
                tiltDegrees = 10f
            )
        }

        Text(
            text = "CUP & COIN",
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 5.sp,
            style = TextStyle(
                shadow = Shadow(
                    color = LavenderAccent.copy(alpha = 0.6f),
                    offset = Offset(0f, 6f),
                    blurRadius = 12f
                )
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Trust your eyes. Find the gold.",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun LuckySpinWheelCard(
    isCompleted: Boolean,
    stats: PlayerStats,
    onClick: () -> Unit
) {
    val cyanAccent = Color(0xFF00E5FF)
    val cyanDark = Color(0xFF006064)
    val infiniteTransition = rememberInfiniteTransition(label = "spin_pulse")
    
    // Smooth infinite rotation for the Wheel Emoji
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulse animation for 3D border glow
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            cyanAccent.copy(alpha = borderAlpha * 0.25f),
                            Color.Transparent
                        ),
                        radius = size.width * 0.7f
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx(), 20.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A192F), // Deep space blue/cyan
                        Color(0xFF020813)
                    )
                )
            )
            .border(
                width = 2.dp, // Thicker border for better 3D definition
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.4f),
                        cyanAccent.copy(alpha = borderAlpha),
                        cyanAccent.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(18.dp), // Generous padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Rotatable Wheel Emoji with 3D Inner Shadow / Ring
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(cyanAccent.copy(alpha = 0.25f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                        .border(1.5.dp, cyanAccent.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.graphicsLayer {
                            rotationZ = rotationAngle
                        }
                    ) {
                        Text("🎡", fontSize = 26.sp)
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "LUCKY SPIN WHEEL",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 0.7.sp,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = cyanAccent.copy(alpha = 0.5f),
                                    offset = Offset(0f, 0f),
                                    blurRadius = 6f
                                )
                            )
                        )
                        if (isCompleted) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("COMPLETED", color = Color.White.copy(alpha = 0.7f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .background(cyanAccent.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                    .border(1.dp, cyanAccent.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("READY", color = cyanAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isCompleted) 
                            "Spun! Active Shields: ${stats.shieldCount} 🛡️ | 2X Multi: ${if (stats.doubleScoreActive) "Active 🔥" else "Inactive"}"
                            else "Win free Endless Mode Shields & 2X Score Multipliers!",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f), // Clearer color
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Premium 3D Pill Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isCompleted) {
                                listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
                            } else {
                                listOf(cyanAccent, Color(0xFF0091EA))
                            }
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = if (isCompleted) Color.White.copy(alpha = 0.2f) else Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isCompleted) "CLAIMED" else "SPIN ➔",
                    color = if (isCompleted) Color.White.copy(alpha = 0.5f) else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.4f),
                            offset = Offset(0f, 1f),
                            blurRadius = 2f
                        )
                    )
                )
            }
        }
    }
}
