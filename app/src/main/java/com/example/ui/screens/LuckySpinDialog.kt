package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.PlayerStats
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

val CyanAccentLocal = Color(0xFF00E5FF)
val OrangeAccentLocal = Color(0xFFFF9800)
val VioletLocal = Color(0xFFD500F9)
val YellowAccentLocal = Color(0xFFFFEA00)
val RubyRedLocal = Color(0xFFFF1744)

data class WheelSegment(
    val title: String,
    val subtitle: String,
    val rewardType: String,
    val color: Color,
    val iconEmoji: String
)

@Composable
fun LuckySpinDialog(
    stats: PlayerStats,
    isCompletedToday: Boolean,
    onSpinClaimed: (String) -> Unit,
    onPlaySound: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val segments = listOf(
        WheelSegment("🛡️ SHIELD", "Endless Retry", "SHIELD", VioletLocal, "🛡️"),
        WheelSegment("🔥 2X MULTI", "Next Round Win", "DOUBLE", CyanAccentLocal, "🔥"),
        WheelSegment("🪙 +250 PTS", "Immediate score", "250PTS", OrangeAccentLocal, "🪙"),
        WheelSegment("🛡️ SHIELD", "Endless Retry", "SHIELD", YellowAccentLocal, "🛡️"),
        WheelSegment("🔥 2X MULTI", "Next Round Win", "DOUBLE", CyanAccentLocal, "🔥"),
        WheelSegment("🪙 +500 PTS", "Mega score boost", "500PTS", RubyRedLocal, "🪙")
    )

    var isSpinning by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var showRewardDialog by remember { mutableStateOf(false) }
    var selectedSegmentIndex by remember { mutableIntStateOf(0) }

    // Animation state
    val animatedRotation = animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(
            durationMillis = 4000,
            easing = CubicBezierEasing(0.15f, 0.85f, 0.38f, 1f)
        ),
        label = "wheel_spin_rotation"
    ) { finalValue ->
        if (isSpinning) {
            isSpinning = false
            // Calculate which segment was hit
            // Invert final angle because the wheel spins clockwise but the segments are oriented counterclockwise
            val degreesPerSegment = 360f / segments.size
            val normalizedAngle = (finalValue % 360f + 360f) % 360f
            // Pointer is at the top (270 degrees)
            val pointerOffsetAngle = (270f - normalizedAngle + 360f) % 360f
            val hitIndex = (pointerOffsetAngle / degreesPerSegment).toInt() % segments.size
            selectedSegmentIndex = hitIndex
            showRewardDialog = true
            onPlaySound("unlock")
        }
    }

    Dialog(
        onDismissRequest = { if (!isSpinning) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight()
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(NeonCyan, Color.Transparent, LavenderAccent)
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = PurpleNightBg),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LUCKY SPIN 🎡",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = NeonCyan.copy(alpha = 0.5f),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 8f
                                )
                            )
                        )
                        IconButton(
                            onClick = onDismiss,
                            enabled = !isSpinning,
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.05f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.1f))

                    Text(
                        text = "Spin the 3D Cosmic Wheel daily to earn powerful shields or double-score multipliers!",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // The Spinning Wheel Frame
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .drawBehind {
                                // Draw an outer neon aura shadow
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(NeonCyan.copy(alpha = 0.15f), Color.Transparent),
                                        radius = size.width * 0.7f
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing the wheel slices using Compose Canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(animatedRotation.value)
                        ) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val radius = size.width / 2
                            val degreesPerSlice = 360f / segments.size

                            for (i in segments.indices) {
                                val segment = segments[i]
                                val startAngle = i * degreesPerSlice

                                // Draw segment wedge
                                drawArc(
                                    color = segment.color,
                                    startAngle = startAngle,
                                    sweepAngle = degreesPerSlice,
                                    useCenter = true,
                                    size = Size(size.width, size.height)
                                )

                                // Draw a neon divider line
                                drawArc(
                                    color = Color.Black.copy(alpha = 0.25f),
                                    startAngle = startAngle,
                                    sweepAngle = degreesPerSlice,
                                    useCenter = true,
                                    size = Size(size.width, size.height),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }

                            // Draw central core
                            drawCircle(
                                color = Color.Black,
                                radius = 24.dp.toPx()
                            )
                            drawCircle(
                                brush = Brush.verticalGradient(
                                    listOf(GoldAccent, BrightGold)
                                ),
                                radius = 18.dp.toPx()
                            )
                        }

                        // Drawing segment emojis on top at precise rotational offsets
                        for (i in segments.indices) {
                            val segment = segments[i]
                            val segmentAngle = i * (360f / segments.size) + (180f / segments.size)
                            val combinedAngle = animatedRotation.value + segmentAngle

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(combinedAngle),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(top = 28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = segment.iconEmoji,
                                        fontSize = 24.sp,
                                        modifier = Modifier.rotate(-combinedAngle) // Keep emojis upright!
                                    )
                                }
                            }
                        }

                        // Wheel pointer arrow at the TOP center pointing DOWN
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-14).dp)
                                .size(28.dp)
                                .background(BrightGold, shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                                .border(1.5.dp, Color.White, shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▼", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buttons
                    if (isCompletedToday && !showRewardDialog) {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.1f),
                                disabledContainerColor = Color.White.copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("SPIN COMPLETED TODAY 📅", color = Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (!isSpinning && !showRewardDialog) {
                                    isSpinning = true
                                    onPlaySound("tap")
                                    // Generate massive extra rotation angle (6 to 10 full turns) plus offset
                                    val randomSpinDegrees = 360f * (6 + Random.nextInt(4)) + Random.nextInt(360)
                                    rotationAngle += randomSpinDegrees
                                    
                                    // Simulated ticking sound during spin
                                    scope.launch {
                                        for (tick in 1..24) {
                                            delay((tick * tick * 5L).coerceAtMost(300L))
                                            onPlaySound("tap")
                                        }
                                    }
                                }
                            },
                            enabled = !isSpinning && !showRewardDialog,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                disabledContainerColor = NeonCyan.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(
                                text = if (isSpinning) "SPINNING COSMOS..." else "SPIN NOW! 🎡",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // Reward Notification Overlay!
            if (showRewardDialog) {
                val winningSegment = segments[selectedSegmentIndex]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.92f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp)
                            .border(2.dp, BrightGold, shape = RoundedCornerShape(28.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF070B19)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "COSMIC WIN! 🎉",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = BrightGold,
                                textAlign = TextAlign.Center
                            )

                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .background(winningSegment.color.copy(alpha = 0.15f), shape = CircleShape)
                                    .border(2.dp, winningSegment.color, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    winningSegment.iconEmoji,
                                    fontSize = 44.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = winningSegment.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = winningSegment.subtitle,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Interactive details of what was won
                            Surface(
                                color = Color.White.copy(alpha = 0.03f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = when (winningSegment.rewardType) {
                                            "SHIELD" -> Icons.Default.Shield
                                            "DOUBLE" -> Icons.Default.Whatshot
                                            else -> Icons.Default.MonetizationOn
                                        },
                                        contentDescription = null,
                                        tint = winningSegment.color,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = when (winningSegment.rewardType) {
                                            "SHIELD" -> "Gives you a second chance in Endless and Perfect runs! Activates automatically."
                                            "DOUBLE" -> "Saves a 2X Multiplier for your next winning round. Double points instantly!"
                                            else -> "Instantly boosts your career score statistics. Show off your grand total!"
                                        },
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.75f),
                                        lineHeight = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    showRewardDialog = false
                                    onSpinClaimed(winningSegment.rewardType)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrightGold),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("CLAIM MY REWARD! 🎁", color = Color.Black, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
