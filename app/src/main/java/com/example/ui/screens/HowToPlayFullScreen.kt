package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun HowToPlayFullScreen(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
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
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = LavenderAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "HOW TO PLAY",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Master the Cup & Coin Art",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            Divider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    GuideSectionCard(
                        title = "🎯 Core Gameplay",
                        content = "1. Watch carefully as the coin is shown under one of the three cups.\n" +
                                "2. The cups will shuffle, cross, and slide across the table with smooth 3D animations.\n" +
                                "3. Tap the cup where you believe the coin is hidden!\n" +
                                "4. If correct, celebratory confetti explodes and your level increases! If incorrect, you can retry the exact same level without losing progress."
                    )
                }

                item {
                    GuideSectionCard(
                        title = "🎮 Game Modes Explained",
                        content = "• CLASSIC (🏆): Progressive levels starting from Level 1. Shuffles get faster, cups cross more often, and false shake tricks appear as you level up!\n\n" +
                                "• TIME ATTACK (⚡): You have strictly 5 seconds per round to guess the coin. Urgency sound ticks get faster as time runs out. Test your lightning reflexes!\n\n" +
                                "• ENDLESS (🔥): Survival mode! You have 1 mistake tolerance. How far can you survive under escalating chaos?\n\n" +
                                "• PERFECT RUN (🎯): Chain consecutive correct guesses to build insane score multipliers and streak combos.\n\n" +
                                "• DAILY CHALLENGE (📅): One fixed daily puzzle for all players worldwide. Win unique glory and complete your daily badge!"
                    )
                }

                item {
                    GuideSectionCard(
                        title = "💡 Tips to Become the Best Player",
                        content = "• Track the Center Point: Don't follow the cup directly; track the momentum of the swap trajectory.\n" +
                                "• Audio Cues: Turn on sound in Settings! Our audio engine provides subtle feedback during shuffles.\n" +
                                "• Watch for Fakes: In higher levels, cups perform 'fake-shake' drops to trick your eyes. Stay focused on the swap history!"
                    )
                }

                item {
                    GuideSectionCard(
                        title = "🏆 How Skills & Progression Work",
                        content = "• As you successfully win rounds, your Win Rate %, Best Score, and Highest Level increase.\n" +
                                "• Unlocks: Reaching higher levels and scores unlocks exclusive Cup and Coin themes (like Vegas Gold, Neon Cyber, and Royal Emerald).\n" +
                                "• Consistency is key! Play Daily Challenges every day to keep your streak alive."
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideSectionCard(title: String, content: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.05f))
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LavenderAccent
            )
            Text(
                text = content,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )
        }
    }
}
