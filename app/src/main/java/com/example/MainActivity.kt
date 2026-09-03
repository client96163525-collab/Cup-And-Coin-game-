package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.CoinTheme
import com.example.model.CupTheme
import com.example.model.GameState
import com.example.model.ShuffleTheme
import com.example.ui.components.CoinVisual
import com.example.ui.components.CupVisual
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Unity Ads SDK
        com.example.util.AdManager.initialize(this)

        // Report app install to official website backend
        com.example.util.AppInstallReporter.reportInstallIfNeeded(this)

        // Global Crash Reporting & Logging Handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val crashInfo = "FATAL CRASH in thread [${thread.name}]: ${throwable.javaClass.name} - ${throwable.message}\n" +
                    throwable.stackTrace.take(10).joinToString("\n") { "  at $it" }
            com.example.util.DebugLogger.e("CRASH_HANDLER", crashInfo)
            try {
                getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("last_crash_log", crashInfo)
                    .commit()
            } catch (_: Throwable) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }

        setContent {
            val viewModel: GameViewModel = viewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val appTheme = settings.appTheme
            
            val backgroundColor = when (appTheme) {
                com.example.model.AppTheme.BLACK -> Color.Black
                com.example.model.AppTheme.WHITE -> Color.White
                else -> MidnightNavy
            }

            MyApplicationTheme(appTheme = appTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSplash by remember { mutableStateOf(true) }

                    LaunchedEffect(Unit) {
                        delay(2500)
                        showSplash = false
                    }

                    if (showSplash) {
                        SplashScreen()
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                            val stats by viewModel.stats.collectAsStateWithLifecycle()

                            var showHowToPlay by remember { mutableStateOf(false) }
                            var showAppExitConfirm by remember { mutableStateOf(false) }

                            val context = androidx.compose.ui.platform.LocalContext.current
                            var previousCrashLog by remember { mutableStateOf<String?>(null) }

                            LaunchedEffect(Unit) {
                                try {
                                    val prefs = context.getSharedPreferences("game_prefs", android.content.Context.MODE_PRIVATE)
                                    val crash = prefs.getString("last_crash_log", null)
                                    if (!crash.isNullOrBlank()) {
                                        previousCrashLog = crash
                                        com.example.util.DebugLogger.e("PREVIOUS_CRASH", crash)
                                        prefs.edit().remove("last_crash_log").apply()
                                    }
                                } catch (_: Throwable) {}
                            }

                            if (previousCrashLog != null) {
                                AlertDialog(
                                    onDismissRequest = { previousCrashLog = null },
                                    title = { Text("Previous Session Crash Report", color = RubyRed, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                                    text = {
                                        Column {
                                            Text(
                                                text = "The app recovered from a previous crash. Details logged to console:",
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.8f),
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = previousCrashLog ?: "",
                                                    fontSize = 10.sp,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    color = NeonCyan,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = { previousCrashLog = null },
                                            colors = ButtonDefaults.buttonColors(containerColor = RubyDark)
                                        ) {
                                            Text("Dismiss & Play")
                                        }
                                    },
                                    containerColor = MinimalSurfaceElevated,
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }

                            // System Back Button Handling
                            BackHandler(enabled = true) {
                                when {
                                    showAppExitConfirm -> showAppExitConfirm = false
                                    showHowToPlay -> showHowToPlay = false
                                    uiState.gameState != GameState.HOME -> {
                                        viewModel.returnToHome()
                                    }
                                    else -> {
                                        showAppExitConfirm = true
                                    }
                                }
                            }

                            if (uiState.gameState == GameState.HOME) {
                                HomeScreen(
                                    stats = stats,
                                    settings = settings,
                                    isDailyCompleted = uiState.isDailyCompletedToday,
                                    isLuckySpinCompleted = uiState.isLuckySpinCompletedToday,
                                    onSpinClaimed = { rewardType -> viewModel.spinLuckyWheel(rewardType) },
                                    onStartGame = { mode -> viewModel.startGame(mode) },
                                    onOpenHowToPlay = { showHowToPlay = true },
                                    onSelectCupTheme = { theme -> viewModel.selectCupTheme(theme) },
                                    onSelectCoinTheme = { theme -> viewModel.selectCoinTheme(theme) },
                                    onSelectShuffleTheme = { theme -> viewModel.selectShuffleTheme(theme) },
                                    onToggleSound = { viewModel.toggleSound() },
                                    onToggleVibration = { viewModel.toggleVibration() },
                                    onToggleReducedMotion = { viewModel.toggleReducedMotion() },
                                    onAppThemeChange = { theme -> viewModel.selectAppTheme(theme) },
                                    onResetProgress = { viewModel.resetProgress() },
                                    onPlaySound = { sound ->
                                        when (sound) {
                                            "tab" -> viewModel.audioEngine.playTabSwitch()
                                            "unlock" -> viewModel.audioEngine.playThemeUnlock()
                                            "tap" -> viewModel.audioEngine.playTap()
                                            "lose" -> viewModel.audioEngine.playLose()
                                            "win" -> viewModel.audioEngine.playWin()
                                            "reward", "bonus" -> viewModel.audioEngine.playRewardClaim()
                                            "jackpot" -> viewModel.audioEngine.playJackpot()
                                            "wheel_tick" -> viewModel.audioEngine.playWheelTick()
                                            "coin" -> viewModel.audioEngine.playCoinCollect()
                                            "shuffle" -> viewModel.audioEngine.playShuffle()
                                        }
                                    }
                                )
                            } else {
                                GameScreen(
                                    uiState = uiState,
                                    settings = settings,
                                    stats = stats,
                                    onCupSelected = { slotIndex -> viewModel.onCupSelected(slotIndex) },
                                    onNextRound = { viewModel.nextRound() },
                                    onRetryRound = { viewModel.retryRound() },
                                    onRestartGame = { viewModel.restartCurrentGame() },
                                    onReturnToHome = { viewModel.returnToHome() },
                                    onToggleSound = { viewModel.toggleSound() }
                                )
                            }

                            // Full Screen Modals
                            if (showHowToPlay) {
                                HowToPlayFullScreen(onDismiss = { showHowToPlay = false })
                            }

                            // App Exit Confirmation Dialog (3D Frosted Theme)
                            if (showAppExitConfirm) {
                                AlertDialog(
                                    onDismissRequest = { showAppExitConfirm = false },
                                    icon = {
                                        Surface(
                                            shape = CircleShape,
                                            color = RubyRed.copy(alpha = 0.2f),
                                            modifier = Modifier.size(52.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.ExitToApp,
                                                    contentDescription = null,
                                                    tint = RubyRed,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }
                                    },
                                    title = {
                                        Text(
                                            text = "Exit Cup & Coin?",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 20.sp,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = "Are you sure you want to exit the application? Your career stats and unlocked themes are safely saved.",
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.75f),
                                            textAlign = TextAlign.Center,
                                            lineHeight = 20.sp
                                        )
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                showAppExitConfirm = false
                                                finish()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = RubyDark),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.height(48.dp)
                                        ) {
                                            Text("Yes, Exit", fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    },
                                    dismissButton = {
                                        OutlinedButton(
                                            onClick = { showAppExitConfirm = false },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LavenderAccent),
                                            border = ButtonDefaults.outlinedButtonBorder().copy(
                                                brush = Brush.linearGradient(listOf(LavenderAccent, LavenderAccent))
                                            ),
                                            modifier = Modifier.height(48.dp)
                                        ) {
                                            Text("Stay & Play", fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    containerColor = MinimalSurfaceElevated,
                                    shape = RoundedCornerShape(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_3d_anim")

    // 3D Orbiting / Floating Animation for Cups
    val cupFloatAnim by infiniteTransition.animateFloat(
        initialValue = -22f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cup_float"
    )

    // 3D Tilting / Rotation for Cups
    val cupTiltAnim by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cup_tilt"
    )

    // Pulsing 3D Coin Scale & Spin
    val coinScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coin_pulse"
    )

    // Glowing Radial background expansion
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy),
        contentAlignment = Alignment.Center
    ) {
        // Glowing Ambient Background Orb
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(glowScale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(LavenderAccent.copy(alpha = 0.25f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 3D Interactive Stage Preview Graphic
            Box(
                modifier = Modifier
                    .height(180.dp)
                    .width(300.dp),
                contentAlignment = Alignment.Center
            ) {
                // Central 3D Glowing Coin with 3D depth
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                        .graphicsLayer {
                            scaleX = coinScale
                            scaleY = coinScale
                            rotationY = cupFloatAnim * 2f
                            cameraDistance = 16f * density
                        },
                    contentAlignment = Alignment.Center
                ) {
                    CoinVisual(
                        theme = CoinTheme.GOLD_STAR,
                        size = 64.dp,
                        isSpinning = true
                    )
                }

                // Three 3D Tilting & Floating Cups
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    CupVisual(
                        theme = CupTheme.MINIMAL_VIOLET,
                        width = 62.dp,
                        height = 78.dp,
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = cupFloatAnim
                                rotationZ = cupTiltAnim
                                rotationX = 15f
                                cameraDistance = 20f * density
                            }
                    )
                    CupVisual(
                        theme = CupTheme.ROYAL_CRIMSON,
                        width = 70.dp,
                        height = 88.dp,
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = -cupFloatAnim * 1.3f
                                rotationZ = -cupTiltAnim
                                rotationX = 20f
                                cameraDistance = 20f * density
                            }
                    )
                    CupVisual(
                        theme = CupTheme.CYBER_NEON,
                        width = 62.dp,
                        height = 78.dp,
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = cupFloatAnim
                                rotationZ = cupTiltAnim
                                rotationX = 15f
                                cameraDistance = 20f * density
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "CUP & COIN",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = LavenderAccent.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "🌟 3D Master Tracking Arcade 🌟",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LavenderAccent,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
