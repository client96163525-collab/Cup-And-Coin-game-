package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DailyChallengeDialog(
    completedDates: Set<String>,
    onPlayToday: () -> Unit,
    onDismiss: () -> Unit
) {
    var animateTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateTrigger = true
    }

    val scaleState by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialog_scale"
    )

    val alphaState by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = EaseOutQuad),
        label = "dialog_alpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            // Elegant Frosted 3D Glowing Container
            Box(
                modifier = Modifier
                    .width(360.dp)
                    .padding(24.dp)
                    .graphicsLayer {
                        scaleX = scaleState
                        scaleY = scaleState
                        alpha = alphaState
                    }
                    .clickable(enabled = false) {} // Prevent click-through
                    .drawBehind {
                        // Ambient dual neon halo behind dialog
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    LavenderAccent.copy(alpha = 0.18f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 4f, size.height / 3f),
                                radius = size.width * 0.9f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    EmeraldGreen.copy(alpha = 0.15f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.75f, size.height * 0.8f),
                                radius = size.width * 0.8f
                            )
                        )
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PurpleNightSurface.copy(alpha = 0.95f),
                                Color.Black.copy(alpha = 0.98f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                LavenderAccent.copy(alpha = 0.4f),
                                EmeraldGreen.copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(20.dp)
            ) {
                // Close button top right
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Dialog",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(6.dp))

                    // Title with trophy illustration
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(GoldAccent.copy(alpha = 0.25f), Color.Transparent)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "DAILY CHALLENGE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.5.sp,
                        style = TextStyle(
                            shadow = Shadow(
                                color = LavenderAccent.copy(alpha = 0.5f),
                                offset = Offset(0f, 2f),
                                blurRadius = 8f
                            )
                        )
                    )

                    val monthName = LocalDate.now().month.name.lowercase().replaceFirstChar { it.uppercase() }
                    val year = LocalDate.now().year
                    Text(
                        text = "$monthName $year",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Colorful Calendar grid
                    CalendarGrid(completedDates)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Animated Glowing Play Button
                    val greenColor = EmeraldGreen
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse_btn")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.03f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "btn_pulse"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .scale(pulseScale)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(greenColor, Color(0xFF2E7D32))
                                )
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.45f), Color.Transparent)
                                ),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { onPlayToday() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PLAY TODAY'S LEVEL",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.4f),
                                        offset = Offset(0f, 1.5f),
                                        blurRadius = 3f
                                    )
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(completedDates: Set<String>) {
    val today = LocalDate.now()
    val firstDayOfMonth = today.withDayOfMonth(1)
    val daysInMonth = today.lengthOfMonth()
    val startDayOfWeek = (firstDayOfMonth.dayOfWeek.value) % 7 // 0 = Sunday

    val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Weekdays Headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekdays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (day == "S") GoldAccent else Color.White.copy(alpha = 0.4f)
                )
            }
        }

        // Calendar Grid System
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(190.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Fill initial blank days
            items(startDayOfWeek) {
                Box(modifier = Modifier.size(36.dp))
            }

            items(daysInMonth) { index ->
                val day = index + 1
                val date = firstDayOfMonth.plusDays(index.toLong())
                val dateStr = date.format(DateTimeFormatter.ISO_DATE)
                val isCompleted = completedDates.contains(dateStr)
                val isToday = date == today

                // Sequential entrance animation with spring
                var animateTile by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    animateTile = true
                }

                val tileScale by animateFloatAsState(
                    targetValue = if (animateTile) 1f else 0.4f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "tile_scale"
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .graphicsLayer {
                            scaleX = tileScale
                            scaleY = tileScale
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val tileColor = if (isCompleted) {
                        EmeraldGreen.copy(alpha = 0.25f)
                    } else if (isToday) {
                        LavenderAccent.copy(alpha = 0.25f)
                    } else {
                        Color.White.copy(alpha = 0.05f)
                    }

                    val borderColor = if (isCompleted) {
                        EmeraldGreen
                    } else if (isToday) {
                        LavenderAccent
                    } else {
                        Color.White.copy(alpha = 0.12f)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        tileColor.copy(alpha = tileColor.alpha + 0.1f),
                                        tileColor.copy(alpha = Math.max(0f, tileColor.alpha - 0.05f))
                                    )
                                )
                            )
                            .border(
                                width = if (isToday) 2.dp else 1.2.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = if (isToday) 0.8f else 0.2f),
                                        borderColor
                                    )
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            // Completed coin sparkle star icon in background
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = EmeraldGreen.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(1.1f)
                            )
                        }

                        if (isToday) {
                            // Today shining aura ring
                            val todayTransition = rememberInfiniteTransition(label = "today")
                            val auraScale by todayTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.3f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "aura"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(auraScale)
                                    .border(0.8.dp, LavenderAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            )
                        }

                        Text(
                            text = "$day",
                            fontSize = 11.sp,
                            fontWeight = if (isToday || isCompleted) FontWeight.Black else FontWeight.Bold,
                            color = if (isCompleted) {
                                EmeraldGreen
                            } else if (isToday) {
                                LavenderAccent
                            } else {
                                Color.White.copy(alpha = 0.75f)
                            },
                            style = TextStyle(
                                shadow = if (isToday || isCompleted) {
                                    Shadow(
                                        color = (if (isCompleted) EmeraldGreen else LavenderAccent).copy(alpha = 0.6f),
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
            }
        }
    }
}
