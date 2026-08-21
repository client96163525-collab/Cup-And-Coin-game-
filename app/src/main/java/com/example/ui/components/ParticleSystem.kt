package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val id: Int,
    val startX: Float,
    val startY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val size: Float,
    val isCircle: Boolean,
    val initialRotation: Float,
    val rotationSpeed: Float
)

@Composable
fun ParticleBurst(
    trigger: Long,
    modifier: Modifier = Modifier,
    particleCount: Int = 120
) {
    if (trigger == 0L) return

    val progress = remember(trigger) { Animatable(0f) }
    val particles = remember(trigger) {
        val colors = listOf(
            VegasGold, BrightGold, RubyRed, NeonCyan, EmeraldGreen, RoyalPurple, Color.White, Color(0xFFFF69B4), Color(0xFF00BFFF)
        )
        List(particleCount) { i ->
            val angle = Random.nextDouble(0.0, 2.0 * Math.PI)
            // Explode outward with much more power
            val speed = Random.nextFloat() * 1200f + 300f
            Particle(
                id = i,
                startX = 0.5f,
                startY = 0.55f, // slightly lower to match table
                velocityX = (cos(angle) * speed).toFloat(),
                velocityY = (sin(angle) * speed).toFloat() - 300f, // strong initial upward pop
                color = colors.random(),
                size = Random.nextFloat() * 14f + 6f, // bigger pieces
                isCircle = Random.nextBoolean(),
                initialRotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 1000f // fast spinning
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2200, easing = LinearEasing) // longer duration
        )
    }

    if (progress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val t = progress.value
            // Fade out starts later
            val alpha = if (t < 0.6f) 1f else (1f - ((t - 0.6f) / 0.4f)).coerceIn(0f, 1f)
            
            // Gravity gets stronger over time to pull them down
            val gravity = 1800f * t * t

            particles.forEach { p ->
                // apply friction to horizontal velocity
                val friction = (1f - t).coerceAtLeast(0.1f)
                val currentVx = p.velocityX * friction
                
                val px = (size.width * p.startX) + (currentVx * t)
                val py = (size.height * p.startY) + (p.velocityY * t) + gravity
                val currentRotation = p.initialRotation + (p.rotationSpeed * t)

                if (px in -100f..(size.width + 100f) && py in -100f..(size.height + 100f)) {
                    withTransform({
                        translate(left = px, top = py)
                        rotate(degrees = currentRotation)
                    }) {
                        if (p.isCircle) {
                            drawCircle(
                                color = p.color.copy(alpha = alpha),
                                radius = p.size * (1f - t * 0.2f),
                                center = Offset.Zero
                            )
                        } else {
                            drawRect(
                                color = p.color.copy(alpha = alpha),
                                topLeft = Offset(-p.size / 2, -p.size / 2),
                                size = androidx.compose.ui.geometry.Size(p.size, p.size * 1.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}
