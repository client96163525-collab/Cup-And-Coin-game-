package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.components.CoinVisual
import com.example.ui.components.CupVisual
import com.example.ui.theme.*

@Composable
fun HowToPlayDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MinimalSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LavenderAccent.copy(alpha = 0.6f), MinimalBorder))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .shadow(12.dp, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "HOW TO PLAY",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.2.sp
                )

                RuleStepCard(
                    stepNumber = "1",
                    title = "Watch the Coin 🪙",
                    description = "At the start of every round, the winning cup lifts to reveal the coin."
                )

                RuleStepCard(
                    stepNumber = "2",
                    title = "Follow the Shuffle 👁️",
                    description = "Cups swap positions at high speed. Keep your focus locked on the target cup!"
                )

                RuleStepCard(
                    stepNumber = "3",
                    title = "Pick the Cup 🎯",
                    description = "Tap your guess after shuffling stops. Chain consecutive wins to trigger combo multipliers!"
                )

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_close_how_to_play")
                ) {
                    Text("GOT IT", color = VioletDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun RuleStepCard(stepNumber: String, title: String, description: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MinimalSurfaceElevated,
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(MinimalBorder, MinimalBorder))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(LavenderAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LavenderAccent
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ThemesDialog(
    currentCupTheme: CupTheme,
    currentCoinTheme: CoinTheme,
    highestLevel: Int,
    bestScore: Int,
    onSelectCupTheme: (CupTheme) -> Unit,
    onSelectCoinTheme: (CoinTheme) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Cups, 1 = Coins

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MinimalSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LavenderAccent.copy(alpha = 0.6f), MinimalBorder))),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .shadow(12.dp, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CUSTOMIZE THEMES",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Cup Skins vs Coin Skins
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MinimalSurfaceElevated,
                    contentColor = LavenderAccent,
                    indicator = {},
                    divider = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "🥤 CUPS",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 0) LavenderAccent else TextMuted
                            )
                        },
                        modifier = Modifier.background(if (selectedTab == 0) MinimalSurface else Color.Transparent)
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "🪙 COINS",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 1) LavenderAccent else TextMuted
                            )
                        },
                        modifier = Modifier.background(if (selectedTab == 1) MinimalSurface else Color.Transparent)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (selectedTab == 0) {
                        items(CupTheme.entries) { theme ->
                            val isUnlocked = highestLevel >= theme.unlockLevel
                            val isSelected = currentCupTheme == theme

                            CupThemeItem(
                                theme = theme,
                                isUnlocked = isUnlocked,
                                isSelected = isSelected,
                                onSelect = { if (isUnlocked) onSelectCupTheme(theme) }
                            )
                        }
                    } else {
                        items(CoinTheme.entries) { theme ->
                            val isUnlocked = bestScore >= theme.unlockScore
                            val isSelected = currentCoinTheme == theme

                            CoinThemeItem(
                                theme = theme,
                                isUnlocked = isUnlocked,
                                isSelected = isSelected,
                                onSelect = { if (isUnlocked) onSelectCoinTheme(theme) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("DONE", color = VioletDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CupThemeItem(
    theme: CupTheme,
    isUnlocked: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MinimalSurfaceElevated else MinimalSurface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isSelected) listOf(LavenderAccent, theme.primaryColor) else listOf(MinimalBorder, MinimalBorder)
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked, onClick = onSelect)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CupVisual(
                theme = theme,
                width = 38.dp,
                height = 46.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = theme.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) TextPrimary else TextMuted
                )
                Text(
                    text = if (isUnlocked) {
                        if (isSelected) "Active Theme ✓" else "Tap to Select"
                    } else {
                        "🔒 Unlocks at Level ${theme.unlockLevel}"
                    },
                    fontSize = 12.sp,
                    color = if (isSelected) LavenderAccent else if (isUnlocked) TextSecondary else RubyRed
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = LavenderAccent
                )
            }
        }
    }
}

@Composable
private fun CoinThemeItem(
    theme: CoinTheme,
    isUnlocked: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MinimalSurfaceElevated else MinimalSurface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (isSelected) listOf(LavenderAccent, theme.baseColor) else listOf(MinimalBorder, MinimalBorder)
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked, onClick = onSelect)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CoinVisual(
                theme = theme,
                size = 36.dp,
                isSpinning = false
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${theme.displayName} (${theme.symbol})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) TextPrimary else TextMuted
                )
                Text(
                    text = if (isUnlocked) {
                        if (isSelected) "Active Coin ✓" else "Tap to Select"
                    } else {
                        "🔒 Unlocks at ${theme.unlockScore} Best Score"
                    },
                    fontSize = 12.sp,
                    color = if (isSelected) LavenderAccent else if (isUnlocked) TextSecondary else RubyRed
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = LavenderAccent
                )
            }
        }
    }
}

@Composable
fun StatsDialog(
    stats: PlayerStats,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MinimalSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(EmeraldGreen, MinimalBorder))),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "CAREER STATS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGreen,
                    letterSpacing = 1.sp
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MinimalSurfaceElevated,
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(MinimalBorder, MinimalBorder))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatRow("Total Games Played", "${stats.gamesPlayed}")
                        StatRow("Games Won", "${stats.gamesWon}", EmeraldGreen)
                        StatRow("Games Missed", "${stats.gamesLost}", RubyRed)
                        StatRow("Win Accuracy Rate", "${stats.winRatePercent}%", LavenderAccent)
                        Divider(color = MinimalBorder, thickness = 1.dp)
                        StatRow("Highest Score", "${stats.bestScore}", LavenderAccent)
                        StatRow("Highest Level Cleared", "Lvl ${stats.highestLevel}", LavenderLight)
                        StatRow("Best Combo Multiplier", "🔥 x${stats.bestCombo}")
                        StatRow("Best Win Streak", "⚡ ${stats.bestStreak} Wins")
                        if (stats.dailyStreak > 0) {
                            StatRow("Daily Challenge Streak", "📅 ${stats.dailyStreak} Days", EmeraldGreen)
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_close_stats")
                ) {
                    Text("CLOSE STATS", color = VioletDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun SettingsDialog(
    settings: GameSettings,
    onToggleSound: () -> Unit,
    onToggleVibration: () -> Unit,
    onToggleReducedMotion: () -> Unit,
    onResetProgress: () -> Unit,
    onDismiss: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MinimalSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(MinimalBorder, MinimalBorder))),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "SETTINGS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MinimalSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SettingToggleRow(
                            title = "Sound Effects",
                            subtitle = "Dynamic procedural game audio",
                            checked = settings.soundEnabled,
                            onCheckedChange = { onToggleSound() }
                        )
                        Divider(color = MinimalBorder)
                        SettingToggleRow(
                            title = "Haptic Vibration",
                            subtitle = "Tactile feedback on shuffles & taps",
                            checked = settings.vibrationEnabled,
                            onCheckedChange = { onToggleVibration() }
                        )
                        Divider(color = MinimalBorder)
                        SettingToggleRow(
                            title = "Reduced Motion",
                            subtitle = "Slightly gentler shuffle animations",
                            checked = settings.reducedMotion,
                            onCheckedChange = { onToggleReducedMotion() }
                        )
                    }
                }

                // Reset Progress Button
                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LoseRed),
                    border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.linearGradient(listOf(RubyDark, RubyDark))),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_reset_progress")
                ) {
                    Text("RESET ALL PROGRESS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Offline badge info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Offline Ready",
                        tint = EmeraldGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "100% Offline • Zero Internet Required",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MinimalBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("CLOSE", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = { Text("Reset Progress?", fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = { Text("This will permanently clear your high scores, best combos, and statistics.", color = TextSecondary) },
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
}

@Composable
private fun SettingToggleRow(
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
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = LavenderAccent,
                checkedTrackColor = LavenderAccent.copy(alpha = 0.35f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = MinimalBorder
            )
        )
    }
}
