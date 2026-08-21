package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoinTheme
import com.example.model.CupTheme
import com.example.ui.theme.DarkGold
import com.example.ui.theme.VegasGold

@Composable
fun CupVisual(
    theme: CupTheme,
    modifier: Modifier = Modifier,
    isLifted: Boolean = false,
    liftAmount: Float = 0f, // 0f (on table) to 1f (fully lifted up)
    isHighlighted: Boolean = false,
    highlightColor: Color = VegasGold,
    tiltDegrees: Float = 0f,
    width: Dp = 96.dp,
    height: Dp = 120.dp
) {
    // Dynamic shadow size & alpha based on lift
    val shadowScale = 1f - (liftAmount * 0.45f)
    val shadowAlpha = (0.55f - (liftAmount * 0.35f)).coerceIn(0.1f, 0.6f)

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .graphicsLayer {
                translationY = -liftAmount * 110.dp.toPx()
                rotationZ = tiltDegrees
                rotationX = liftAmount * -45f // 3D tilt/peek suspense effect when cup lifts open
                rotationY = tiltDegrees * 0.4f
                cameraDistance = 16f * density
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Ground Shadow (drawn at base)
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(24.dp)
                .align(Alignment.BottomCenter)
        ) {
            val shadowWidth = size.width * shadowScale
            val shadowHeight = size.height * shadowScale
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = shadowAlpha),
                        Color.Black.copy(alpha = shadowAlpha * 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = shadowWidth / 2f
                ),
                topLeft = Offset((size.width - shadowWidth) / 2f, (size.height - shadowHeight) / 2f),
                size = Size(shadowWidth, shadowHeight)
            )
        }

        // Outer glow behind the cup (representing glowing 3D light from the screenshots)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 6.dp)
        ) {
            val w = size.width
            val h = size.height - 12.dp.toPx()
            val glowColor = theme.primaryColor

            // Ambient background halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = if (isHighlighted) 0.5f else 0.25f),
                        glowColor.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(w / 2f, h / 2f),
                    radius = w * 0.85f
                )
            )
        }

        // 3D Cup Body
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 6.dp)
        ) {
            val w = size.width
            val h = size.height - 12.dp.toPx()

            val topWidth = w * 0.68f
            val bottomWidth = w * 0.88f
            val topX = (w - topWidth) / 2f
            val bottomX = (w - bottomWidth) / 2f
            val topY = 14.dp.toPx()
            val bottomY = h

            // If highlighted, draw outer glowing aura
            if (isHighlighted) {
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            highlightColor.copy(alpha = 0.7f),
                            highlightColor.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    topLeft = Offset(0f, 0f),
                    size = Size(w, h + 14.dp.toPx())
                )
            }

            // Cup Body Path (tapered cylinder with curved sides for 3D realism)
            val cupPath = Path().apply {
                moveTo(topX, topY)
                lineTo(topX + topWidth, topY)
                // Left curve
                quadraticTo(topX + topWidth + 4.dp.toPx(), topY + h * 0.5f, bottomX + bottomWidth, bottomY)
                // Bottom curved edge
                quadraticTo(w / 2f, bottomY + 12.dp.toPx(), bottomX, bottomY)
                // Right curve
                quadraticTo(topX - 4.dp.toPx(), topY + h * 0.5f, topX, topY)
                close()
            }

            // Body Gradient (Shaded 3D cylinder with specular shine on left-center)
            drawPath(
                path = cupPath,
                brush = Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.0f to theme.secondaryColor,
                        0.22f to theme.primaryColor,
                        0.42f to theme.highlightColor.copy(alpha = 0.95f),
                        0.65f to theme.primaryColor,
                        0.88f to theme.secondaryColor,
                        1.0f to theme.secondaryColor.copy(alpha = 0.8f)
                    ),
                    startX = bottomX,
                    endX = bottomX + bottomWidth
                )
            )

            // 3D Spherical Left Edge Ambient Shadow
            val leftShadowPath = Path().apply {
                moveTo(topX, topY)
                lineTo(topX + topWidth * 0.15f, topY)
                quadraticTo(topX + topWidth * 0.18f, topY + h * 0.5f, bottomX + bottomWidth * 0.15f, bottomY)
                lineTo(bottomX, bottomY)
                quadraticTo(topX - 4.dp.toPx(), topY + h * 0.5f, topX, topY)
                close()
            }
            drawPath(
                path = leftShadowPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.45f),
                        Color.Black.copy(alpha = 0.0f)
                    ),
                    startX = topX,
                    endX = topX + topWidth * 0.15f
                )
            )

            // 3D Spherical Right Edge Ambient Shadow
            val rightShadowPath = Path().apply {
                moveTo(topX + topWidth * 0.85f, topY)
                lineTo(topX + topWidth, topY)
                quadraticTo(topX + topWidth + 4.dp.toPx(), topY + h * 0.5f, bottomX + bottomWidth, bottomY)
                lineTo(bottomX + bottomWidth * 0.85f, bottomY)
                quadraticTo(topX + topWidth * 0.82f, topY + h * 0.5f, topX + topWidth * 0.85f, topY)
                close()
            }
            drawPath(
                path = rightShadowPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.0f),
                        Color.Black.copy(alpha = 0.5f)
                    ),
                    startX = topX + topWidth * 0.85f,
                    endX = topX + topWidth
                )
            )

            // Dynamic Sleek Curved Glass Specular Highlight (Curves beautifully with the 3D form)
            val glassHighlightPath = Path().apply {
                moveTo(topX + topWidth * 0.25f, topY)
                lineTo(topX + topWidth * 0.38f, topY)
                quadraticTo(topX + topWidth * 0.41f, topY + h * 0.5f, bottomX + bottomWidth * 0.35f, bottomY)
                lineTo(bottomX + bottomWidth * 0.22f, bottomY)
                quadraticTo(topX + topWidth * 0.28f, topY + h * 0.5f, topX + topWidth * 0.25f, topY)
                close()
            }
            drawPath(
                path = glassHighlightPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.05f)
                    ),
                    startX = topX + topWidth * 0.25f,
                    endX = topX + topWidth * 0.38f
                )
            )

            // Decorative Accent Stripe (Gold/Neon Trim Band - glowing beautifully like the screenshot)
            val stripeY1 = topY + (bottomY - topY) * 0.38f
            val stripeY2 = topY + (bottomY - topY) * 0.46f
            val stripeTopW = topWidth + (bottomWidth - topWidth) * 0.38f
            val stripeBotW = topWidth + (bottomWidth - topWidth) * 0.46f
            val sTopX = (w - stripeTopW) / 2f
            val sBotX = (w - stripeBotW) / 2f

            val stripePath = Path().apply {
                moveTo(sTopX, stripeY1)
                lineTo(sTopX + stripeTopW, stripeY1)
                lineTo(sBotX + stripeBotW, stripeY2)
                lineTo(sBotX, stripeY2)
                close()
            }

            drawPath(
                path = stripePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        theme.rimColor.copy(alpha = 0.4f),
                        theme.rimColor,
                        Color.White.copy(alpha = 0.95f),
                        theme.rimColor,
                        theme.rimColor.copy(alpha = 0.4f)
                    ),
                    startX = sBotX,
                    endX = sBotX + stripeBotW
                )
            )

            // Top Cap Ellipse with inner shading
            drawOval(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        theme.secondaryColor,
                        theme.highlightColor,
                        theme.primaryColor
                    )
                ),
                topLeft = Offset(topX, topY - 7.dp.toPx()),
                size = Size(topWidth, 14.dp.toPx())
            )

            // Bottom Rim (Grip Ring with metallic shimmer)
            drawOval(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        theme.rimColor.copy(alpha = 0.6f),
                        theme.rimColor,
                        Color.White.copy(alpha = 0.7f),
                        theme.rimColor,
                        theme.rimColor.copy(alpha = 0.5f)
                    )
                ),
                topLeft = Offset(bottomX - 2.dp.toPx(), bottomY - 3.dp.toPx()),
                size = Size(bottomWidth + 4.dp.toPx(), 12.dp.toPx())
            )

            // Highlight line (vertical reflection)
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.7f),
                        Color.White.copy(alpha = 0.15f)
                    )
                ),
                start = Offset(w * 0.42f, topY + 4.dp.toPx()),
                end = Offset(w * 0.40f, bottomY - 4.dp.toPx()),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

@Composable
fun CoinVisual(
    theme: CoinTheme,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isSpinning: Boolean = false,
    isGlowing: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "coin_anim")
    val rotationY by if (isSpinning) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "coin_spin"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coin_glow"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 8f * density
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer Radial Aura Glow (3D light shine mimicking screenshot)
        if (isGlowing) {
            Box(
                modifier = Modifier
                    .size(size * 2.2f)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    theme.baseColor.copy(alpha = glowAlpha * 0.7f),
                                    theme.baseColor.copy(alpha = glowAlpha * 0.25f),
                                    theme.accentColor.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                    }
            )
        }

        // Coin Metal Base
        Box(
            modifier = Modifier
                .size(size)
                .shadow(elevation = 12.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            theme.baseColor,
                            theme.accentColor,
                            Color(0xFF4E342E)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Coin Inner Ring and details
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 2.5.dp.toPx()
                // Outer ring
                drawCircle(
                    color = Color.White.copy(alpha = 0.75f),
                    radius = (this.size.minDimension / 2f) - 3.dp.toPx(),
                    style = Stroke(width = strokeW)
                )
                
                // Beveled center divider
                drawCircle(
                    color = theme.accentColor.copy(alpha = 0.5f),
                    radius = (this.size.minDimension / 2f) - 7.dp.toPx(),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Small inner notches/dots around rim (3D coin edge detail)
                val notchCount = 12
                val radius = (this.size.minDimension / 2f) - 5.dp.toPx()
                for (i in 0 until notchCount) {
                    val angle = (i * (360.0 / notchCount)) * (Math.PI / 180.0)
                    val cx = (this.size.width / 2f) + (Math.cos(angle) * radius).toFloat()
                    val cy = (this.size.height / 2f) + (Math.sin(angle) * radius).toFloat()
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = 1.5.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }

            // Coin Symbol (e.g. Star) with custom shadow / glowing text style
            Text(
                text = theme.symbol,
                fontSize = (size.value * 0.48f).sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 1.dp),
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.6f),
                        offset = Offset(0f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}
