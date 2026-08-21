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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class WheelSegment(
    val title: String,
    val subtitle: String,
    val rewardType: String,
    val color: Color,
    val secondaryColor: Color,
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
        WheelSegment("SHIELD", "Endless Retry Shield", "SHIELD", Color(0xFF7C4DFF), Color(0xFF536DFE), "🛡️"),
        WheelSegment("2X MULTI", "Next Round Double", "DOUBLE", Color(0xFF00B0FF), Color(0xFF00E5FF), "🔥"),
        WheelSegment("+250 PTS", "Instant Points", "250PTS", Color(0xFFFF9100), Color(0xFFFFAB00), "🪙"),
        WheelSegment("SUPER SHIELD", "Double Protection", "SHIELD", Color(0xFF00E676), Color(0xFF1DE9B6), "🛡️"),
        WheelSegment("3X BOOST", "Triple Multiplier", "DOUBLE", Color(0xFFFF1744), Color(0xFFFF5252), "⚡"),
        WheelSegment("+500 PTS", "Mega Jackpot", "500PTS", Color(0xFFFFD700), Color(0xFFFFEA00), "👑")
    )

    var isSpinning by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var showRewardDialog by remember { mutableStateOf(false) }
    var selectedSegmentIndex by remember { mutableIntStateOf(0) }
    var pegNeedleVibration by remember { mutableFloatStateOf(0f) }

    // Pointer spring deflection animation
    val needleAngle by animateFloatAsState(
        targetValue = pegNeedleVibration,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "needle_bounce"
    )

    // Smooth realistic multi-stage physics deceleration
    val animatedRotation = animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(
            durationMillis = 4800,
            easing = CubicBezierEasing(0.08f, 0.95f, 0.2f, 1f)
        ),
        label = "wheel_spin_rotation"
    ) { finalValue ->
        if (isSpinning) {
            isSpinning = false
            pegNeedleVibration = 0f
            val degreesPerSegment = 360f / segments.size
            val normalizedAngle = (finalValue % 360f + 360f) % 360f
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
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight()
                    .graphicsLayer {
                        shadowElevation = 24f
                    }
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(GoldAccent, NeonCyan, LavenderAccent)
                        ),
                        shape = RoundedCornerShape(32.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B18)),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Row with 3D Glowing Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = GoldAccent.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🎡", fontSize = 18.sp)
                                }
                            }
                            Column {
                                Text(
                                    text = "3D LUCKY WHEEL",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 1.sp,
                                    style = TextStyle(
                                        shadow = Shadow(
                                            color = GoldAccent.copy(alpha = 0.6f),
                                            offset = Offset(0f, 2f),
                                            blurRadius = 8f
                                        )
                                    )
                                )
                                Text(
                                    text = "DAILY REWARD STAGE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            enabled = !isSpinning,
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.12f))

                    Text(
                        text = "Spin the 3D Cosmic Lucky Wheel daily to unlock powerful protective shields, multipliers, and instant score bonuses!",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // ═══════════════════════════════════════════
                    // 3D WHEEL CONTAINER WITH METALLIC BEVEL & STUDS
                    // ═══════════════════════════════════════════
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .graphicsLayer {
                                rotationX = 12f
                                cameraDistance = 14f
                            }
                            .drawBehind {
                                // 3D Depth Shadow under wheel
                                drawOval(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent),
                                        radius = size.width * 0.7f
                                    ),
                                    topLeft = Offset(0f, 20f),
                                    size = Size(size.width, size.height + 20f)
                                )
                                // Radiant Neon Ambient Halo
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(GoldAccent.copy(alpha = 0.25f), NeonCyan.copy(alpha = 0.15f), Color.Transparent),
                                        radius = size.width * 0.8f
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // 1. Heavy 3D Metallic Outer Bezel
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.sweepGradient(
                                        listOf(
                                            Color(0xFFFFD700),
                                            Color(0xFF8A6200),
                                            Color(0xFFFFF4B8),
                                            Color(0xFF8A6200),
                                            Color(0xFFFFD700)
                                        )
                                    )
                                )
                                .border(3.dp, Color(0xFFFFF4B8), CircleShape)
                                .padding(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF140D22))
                                .border(2.dp, Color.Black.copy(alpha = 0.8f), CircleShape)
                        )

                        // 2. The Rotating 3D Wheel Slices Canvas
                        Canvas(
                            modifier = Modifier
                                .size(246.dp)
                                .rotate(animatedRotation.value)
                        ) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.width / 2f
                            val degreesPerSlice = 360f / segments.size

                            for (i in segments.indices) {
                                val segment = segments[i]
                                val startAngle = i * degreesPerSlice

                                val wedgeBrush = Brush.radialGradient(
                                    colors = listOf(
                                        segment.color.copy(alpha = 0.95f),
                                        segment.secondaryColor.copy(alpha = 0.85f),
                                        Color(0xFF0F0B18).copy(alpha = 0.9f)
                                    ),
                                    center = center,
                                    radius = radius
                                )

                                drawArc(
                                    brush = wedgeBrush,
                                    startAngle = startAngle,
                                    sweepAngle = degreesPerSlice,
                                    useCenter = true,
                                    size = Size(size.width, size.height)
                                )

                                // Golden Separator Rim Ribs
                                drawArc(
                                    color = Color(0xFFFFEA00).copy(alpha = 0.6f),
                                    startAngle = startAngle,
                                    sweepAngle = degreesPerSlice,
                                    useCenter = true,
                                    size = Size(size.width, size.height),
                                    style = Stroke(width = 2.5.dp.toPx())
                                )
                            }

                            // Outer edge metallic inner track
                            drawCircle(
                                color = Color.White.copy(alpha = 0.3f),
                                radius = radius - 2.dp.toPx(),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }

                        // 3. Render High-Contrast 3D Segment Text and Icons
                        for (i in segments.indices) {
                            val segment = segments[i]
                            val segmentAngle = i * (360f / segments.size) + (180f / segments.size)
                            val combinedAngle = animatedRotation.value + segmentAngle

                            Box(
                                modifier = Modifier
                                    .size(246.dp)
                                    .rotate(combinedAngle),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(top = 18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.Black.copy(alpha = 0.65f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, segment.color.copy(alpha = 0.6f)),
                                        modifier = Modifier.padding(2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = segment.iconEmoji,
                                                fontSize = 18.sp,
                                                modifier = Modifier.rotate(-combinedAngle)
                                            )
                                            Text(
                                                text = segment.title,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                letterSpacing = 0.5.sp,
                                                modifier = Modifier.rotate(-combinedAngle)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Perimeter Chrome Studs (24 Metallic Rivets around the wheel)
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(animatedRotation.value)
                        ) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val studRadius = (size.width / 2f) - 6.dp.toPx()
                            val totalStuds = 24

                            for (s in 0 until totalStuds) {
                                val angleRad = Math.toRadians((s * (360.0 / totalStuds)).toDouble())
                                val studX = center.x + (studRadius * cos(angleRad)).toFloat()
                                val studY = center.y + (studRadius * sin(angleRad)).toFloat()

                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    radius = 3.5.dp.toPx(),
                                    center = Offset(studX, studY + 1.5f)
                                )
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        listOf(Color.White, Color(0xFFFFD700), Color(0xFF634A00)),
                                        center = Offset(studX - 1f, studY - 1f),
                                        radius = 3.5.dp.toPx()
                                    ),
                                    radius = 3.dp.toPx(),
                                    center = Offset(studX, studY)
                                )
                            }
                        }

                        // 5. 3D Center Hub Dome (Gold & Crystal Lens)
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .drawBehind {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent),
                                            radius = 34.dp.toPx()
                                        ),
                                        center = Offset(size.width / 2f, size.height / 2f + 4f)
                                    )
                                }
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        listOf(Color(0xFFFFF9C4), Color(0xFFFFD700), Color(0xFF7B5200))
                                    )
                                )
                                .border(2.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(Color(0xFF2A1C4E), Color(0xFF0F0B18))
                                    )
                                )
                                .border(1.5.dp, GoldAccent.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⭐", fontSize = 16.sp)
                        }

                        // 6. 3D Pointer Arrow with Dynamic Spring Needle Physics
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-10).dp)
                                .graphicsLayer {
                                    rotationZ = needleAngle
                                    shadowElevation = 12f
                                }
                                .size(34.dp, 38.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val path = Path().apply {
                                    moveTo(size.width / 2f, size.height)
                                    lineTo(0f, 0f)
                                    lineTo(size.width, 0f)
                                    close()
                                }
                                drawPath(
                                    path = path,
                                    color = Color.Black.copy(alpha = 0.7f)
                                )
                                drawPath(
                                    path = path,
                                    brush = Brush.verticalGradient(
                                        listOf(Color(0xFFFFF9C4), Color(0xFFFFD700), Color(0xFFFF8F00))
                                    )
                                )
                                drawPath(
                                    path = path,
                                    brush = Brush.horizontalGradient(
                                        listOf(Color.White.copy(alpha = 0.8f), Color.Transparent)
                                    ),
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ═══════════════════════════════════════════
                    // 3D INTERACTIVE SPIN TRIGGER BUTTON
                    // ═══════════════════════════════════════════
                    if (isCompletedToday && !showRewardDialog) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White.copy(alpha = 0.06f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("✓", color = EmeraldGreen, fontWeight = FontWeight.Black)
                                    Text(
                                        "SPIN COMPLETED TODAY (COME BACK TOMORROW)",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        val infinitePulse = rememberInfiniteTransition(label = "spin_btn_pulse")
                        val btnGlowAlpha by infinitePulse.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 0.85f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "glow_alpha"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .drawBehind {
                                    if (!isSpinning) {
                                        drawRoundRect(
                                            brush = Brush.radialGradient(
                                                colors = listOf(NeonCyan.copy(alpha = btnGlowAlpha * 0.4f), Color.Transparent),
                                                radius = size.width * 0.6f
                                            )
                                        )
                                    }
                                }
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        if (isSpinning) {
                                            listOf(Color(0xFF2A1C4E), Color(0xFF140D22))
                                        } else {
                                            listOf(NeonCyan, Color(0xFF0091EA))
                                        }
                                    )
                                )
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable(enabled = !isSpinning && !showRewardDialog) {
                                    if (!isSpinning && !showRewardDialog) {
                                        isSpinning = true
                                        onPlaySound("tap")

                                        // 8 to 12 full rotations
                                        val randomSpinDegrees = 360f * (8 + Random.nextInt(5)) + Random.nextInt(360)
                                        rotationAngle += randomSpinDegrees

                                        scope.launch {
                                            for (tick in 1..28) {
                                                val delayTime = (tick * tick * 4L).coerceIn(40L, 340L)
                                                delay(delayTime)
                                                pegNeedleVibration = if (tick % 2 == 0) 12f else -12f
                                                onPlaySound("tap")
                                            }
                                            pegNeedleVibration = 0f
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (isSpinning) "🌀" else "🎡",
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isSpinning) "SPINNING 3D COSMOS..." else "SPIN 3D WHEEL NOW",
                                    color = if (isSpinning) LavenderAccent else Color.Black,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════
            // REWARD NOTIFICATION OVERLAY DIALOG
            // ═══════════════════════════════════════════
            if (showRewardDialog) {
                val winningSegment = segments[selectedSegmentIndex]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.94f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .wrapContentHeight()
                            .border(
                                width = 2.dp,
                                brush = Brush.verticalGradient(
                                    listOf(GoldAccent, NeonCyan)
                                ),
                                shape = RoundedCornerShape(28.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = PurpleNightBg),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "🎉 REWARD UNLOCKED!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldAccent,
                                letterSpacing = 1.sp
                            )

                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(winningSegment.color.copy(alpha = 0.2f))
                                    .border(2.dp, winningSegment.color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = winningSegment.iconEmoji,
                                    fontSize = 42.sp
                                )
                            }

                            Text(
                                text = winningSegment.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )

                            Text(
                                text = winningSegment.subtitle,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )

                            // Interactive details of what was won
                            Surface(
                                color = Color.White.copy(alpha = 0.04f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
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

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = {
                                    showRewardDialog = false
                                    onSpinClaimed(winningSegment.rewardType)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Text(
                                    text = "CLAIM REWARD ➔",
                                    color = Color.Black,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
