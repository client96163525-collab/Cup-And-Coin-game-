package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.util.ShareUtils
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

@Composable
fun DailyChallengeDialog(
    completedDates: Set<String>,
    dailyStreak: Int = 0,
    onPlayToday: () -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(enabled = true) {
        onDismiss()
    }

    var animateTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateTrigger = true
    }

    val scaleState by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialog_scale"
    )

    val alphaState by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = EaseOutQuad),
        label = "dialog_alpha"
    )

    val today = remember { LocalDate.now() }
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(today) }

    val isCurrentMonth = displayedMonth == YearMonth.now()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.78f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            // ═══════════════════════════════════════════
            // 3D FROSTED CALENDAR STAGE CONTAINER
            // ═══════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.93f)
                    .widthIn(max = 420.dp)
                    .padding(vertical = 12.dp)
                    .graphicsLayer {
                        scaleX = scaleState
                        scaleY = scaleState
                        alpha = alphaState
                        shadowElevation = 30f
                    }
                    .clickable(enabled = false) {}
                    .drawBehind {
                        // Dual 3D ambient neon glows
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(EmeraldGreen.copy(alpha = 0.22f), Color.Transparent),
                                center = Offset(size.width * 0.2f, size.height * 0.3f),
                                radius = size.width * 0.85f
                            )
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(VioletPrimary.copy(alpha = 0.22f), Color.Transparent),
                                center = Offset(size.width * 0.8f, size.height * 0.7f),
                                radius = size.width * 0.85f
                            )
                        )
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF191328),
                                Color(0xFF0C0914)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.45f),
                                EmeraldGreen.copy(alpha = 0.4f),
                                VioletPrimary.copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                // Close button top right
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(34.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Dialog",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 3D Header Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Text("📅", fontSize = 14.sp)
                            Text(
                                text = "3D DAILY CHALLENGE ARENA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldAccent,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // 3D Month Navigator Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { displayedMonth = displayedMonth.minusMonths(1) },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Month",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val monthName = displayedMonth.month.getDisplayName(JavaTextStyle.FULL, Locale.US)
                            Text(
                                text = "$monthName ${displayedMonth.year}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.8.sp,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = EmeraldGreen.copy(alpha = 0.5f),
                                        offset = Offset(0f, 1f),
                                        blurRadius = 4f
                                    )
                                )
                            )

                            if (!isCurrentMonth) {
                                Text(
                                    text = "Tap to jump to Today",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen,
                                    modifier = Modifier
                                        .clickable {
                                            displayedMonth = YearMonth.now()
                                            selectedDate = today
                                        }
                                        .padding(top = 1.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { displayedMonth = displayedMonth.plusMonths(1) },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Month",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3D Streak & Solved Count Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF221A33), Color(0xFF0F0B18))
                                ),
                                RoundedCornerShape(14.dp)
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text("🔥", fontSize = 15.sp)
                            Column {
                                Text("ACTIVE STREAK", fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "$dailyStreak DAYS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldAccent
                                )
                            }
                        }

                        val completedInMonthCount = (1..displayedMonth.lengthOfMonth()).count { day ->
                            val dateStr = displayedMonth.atDay(day).format(DateTimeFormatter.ISO_DATE)
                            completedDates.contains(dateStr)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text("🏆", fontSize = 15.sp)
                            Column {
                                Text("SOLVED PUZZLES", fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "$completedInMonthCount / ${displayedMonth.lengthOfMonth()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ═══════════════════════════════════════════
                    // 3D CALENDAR GRID (ELEVATED TILES & BEVELS)
                    // ═══════════════════════════════════════════
                    Calendar3DGrid(
                        yearMonth = displayedMonth,
                        completedDates = completedDates,
                        selectedDate = selectedDate,
                        onDateSelected = { date -> selectedDate = date }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Color Legend Guide
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Legend3DPill(color = EmeraldGreen, icon = "✓", label = "Solved")
                        Legend3DPill(color = RubyRed, icon = "✕", label = "Missed")
                        Legend3DPill(color = VioletPrimary, icon = "⚡", label = "Today")
                        Legend3DPill(color = Color.White.copy(alpha = 0.35f), icon = "🔒", label = "Locked")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Selected Date Detail Card
                    val selectedDateStr = selectedDate.format(DateTimeFormatter.ISO_DATE)
                    val isSelectedCompleted = completedDates.contains(selectedDateStr)
                    val isSelectedToday = selectedDate == today
                    val isSelectedPast = selectedDate.isBefore(today) && !isSelectedCompleted

                    val statusBgColor = when {
                        isSelectedCompleted -> EmeraldGreen.copy(alpha = 0.15f)
                        isSelectedPast -> RubyRed.copy(alpha = 0.15f)
                        isSelectedToday -> EmeraldGreen.copy(alpha = 0.18f)
                        else -> Color.White.copy(alpha = 0.04f)
                    }

                    val statusBorderColor = when {
                        isSelectedCompleted -> EmeraldGreen.copy(alpha = 0.5f)
                        isSelectedPast -> RubyRed.copy(alpha = 0.5f)
                        isSelectedToday -> EmeraldGreen.copy(alpha = 0.6f)
                        else -> Color.White.copy(alpha = 0.1f)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(statusBgColor, RoundedCornerShape(12.dp))
                            .border(1.dp, statusBorderColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = selectedDate.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.US)),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = when {
                                    isSelectedCompleted -> "🏆 Solved! Streak bonus secured."
                                    isSelectedPast -> "❌ Missed Challenge (Expired)"
                                    isSelectedToday -> if (isSelectedCompleted) "⭐ Solved Today!" else "⚡ Today's Challenge Ready"
                                    else -> "🔒 Unlocks on this date"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = when {
                                    isSelectedCompleted -> EmeraldGreen
                                    isSelectedPast -> RubyRed
                                    isSelectedToday -> EmeraldGreen
                                    else -> Color.White.copy(alpha = 0.5f)
                                }
                            )
                        }

                        Text(
                            text = when {
                                isSelectedCompleted -> "SOLVED"
                                isSelectedPast -> "MISSED"
                                isSelectedToday -> "READY"
                                else -> "LOCKED"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = when {
                                isSelectedCompleted -> EmeraldGreen
                                isSelectedPast -> RubyRed
                                isSelectedToday -> GoldAccent
                                else -> Color.White.copy(alpha = 0.4f)
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ═══════════════════════════════════════════
                    // 3D ENHANCED PLAY BUTTON (MASSIVE EMBOSSED CAPSULE)
                    // ═══════════════════════════════════════════
                    val isTodayDone = completedDates.contains(today.format(DateTimeFormatter.ISO_DATE))
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse_btn")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.025f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "btn_pulse"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .scale(pulseScale)
                            .drawBehind {
                                drawRoundRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            (if (isTodayDone) VioletPrimary else EmeraldGreen).copy(alpha = 0.4f),
                                            Color.Transparent
                                        ),
                                        radius = size.width * 0.6f
                                    )
                                )
                            }
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = if (isTodayDone) {
                                        listOf(Color(0xFF8E24AA), Color(0xFF4A148C))
                                    } else {
                                        listOf(Color(0xFF00E676), Color(0xFF00897B))
                                    }
                                )
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.7f), Color.Transparent)
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
                                imageVector = if (isTodayDone) Icons.Default.Refresh else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isTodayDone) Color.White else Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTodayDone) "REPLAY TODAY'S PUZZLE ➔" else "PLAY TODAY'S CHALLENGE ➔",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isTodayDone) Color.White else Color.Black,
                                letterSpacing = 1.sp,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.35f),
                                        offset = Offset(0f, 1.5f),
                                        blurRadius = 3f
                                    )
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Share Progress Button with Website URL Included
                    val context = LocalContext.current
                    OutlinedButton(
                        onClick = {
                            val appUrl = "https://ais-pre-zd2ct6cs36h4qk7rq4htax-95295274561.asia-southeast1.run.app"
                            val completedCount = completedDates.size
                            val shareMsg = """
                                📅 *Cup & Coin 3D Daily Challenge!* 🏆
                                
                                🧠 I am training my focus and tracking high-speed orbital shuffles!
                                🔥 Active Streak: $dailyStreak Days
                                ⭐ Total Daily Puzzles Solved: $completedCount
                                
                                🥤 Track the coin, beat the shuffle, and keep your strike alive!
                                🌐 Play online or download here:
                                $appUrl
                            """.trimIndent()
                            ShareUtils.shareText(context, shareMsg, "Share Daily Streak")
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LavenderAccent),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, LavenderAccent.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("📤", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "SHARE STREAK & WEBSITE LINK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = LavenderLight
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Legend3DPill(color: Color, icon: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
            .border(0.8.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Text(
            text = "$icon $label",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun Calendar3DGrid(
    yearMonth: YearMonth,
    completedDates: Set<String>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val startDayOfWeek = (firstDayOfMonth.dayOfWeek.value) % 7 // 0 = Sunday

    val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF130E1F))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                    color = if (day == "S") GoldAccent else Color.White.copy(alpha = 0.5f)
                )
            }
        }

        // 3D Calendar Grid Matrix
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(190.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(startDayOfWeek) {
                Box(modifier = Modifier.size(36.dp))
            }

            items(daysInMonth) { index ->
                val day = index + 1
                val date = yearMonth.atDay(day)
                val dateStr = date.format(DateTimeFormatter.ISO_DATE)
                val isCompleted = completedDates.contains(dateStr)
                val isToday = date == today
                val isPast = date.isBefore(today) && !isCompleted
                val isFuture = date.isAfter(today)
                val isSelected = date == selectedDate

                // Dynamic 3D Elevated Tile Styling
                val tileGradient = when {
                    isCompleted -> listOf(
                        Color(0xFF00E676),
                        Color(0xFF00796B)
                    )
                    isToday -> listOf(
                        Color(0xFF7C4DFF),
                        Color(0xFF311B92)
                    )
                    isPast -> listOf(
                        Color(0xFFD50000).copy(alpha = 0.5f),
                        Color(0xFF3E0A0A)
                    )
                    isSelected -> listOf(
                        Color(0xFFFFD700).copy(alpha = 0.4f),
                        Color(0xFF2B2200)
                    )
                    else -> listOf(
                        Color(0xFF221A35),
                        Color(0xFF130E20)
                    )
                }

                val tileBorder = when {
                    isSelected -> GoldAccent
                    isCompleted -> Color(0xFFB9F6CA)
                    isToday -> Color(0xFFB388FF)
                    isPast -> RubyRed.copy(alpha = 0.6f)
                    else -> Color.White.copy(alpha = 0.12f)
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .graphicsLayer {
                            shadowElevation = if (isSelected || isToday || isCompleted) 8f else 2f
                        }
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.verticalGradient(tileGradient))
                        .border(
                            width = if (isSelected) 2.dp else 1.2.dp,
                            color = tileBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Text("⭐", fontSize = 10.sp, modifier = Modifier.align(Alignment.TopEnd).padding(2.dp))
                    } else if (isFuture) {
                        Text("🔒", fontSize = 8.sp, modifier = Modifier.align(Alignment.TopEnd).padding(2.dp), color = Color.White.copy(alpha = 0.3f))
                    }

                    Text(
                        text = "$day",
                        fontSize = 12.sp,
                        fontWeight = if (isToday || isCompleted || isSelected) FontWeight.Black else FontWeight.Bold,
                        color = if (isCompleted) Color.White else if (isPast) Color.White.copy(alpha = 0.7f) else Color.White,
                        style = TextStyle(
                            shadow = if (isCompleted || isToday) {
                                Shadow(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    offset = Offset(0f, 1f),
                                    blurRadius = 2f
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
