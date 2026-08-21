package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.exp
import kotlin.random.Random

enum class ConfettiShape {
    CIRCLE,
    RECTANGLE,
    TRIANGLE,
    RIBBON
}

data class Particle(
    val id: Int,
    val startX: Float, // Normalized initial X
    val startY: Float, // Normalized initial Y
    val vx: Float, // Initial X velocity (pixels/sec)
    val vy: Float, // Initial Y velocity (pixels/sec)
    val color: Color,
    val size: Float,
    val shape: ConfettiShape,
    val initialRotation: Float,
    val rotationSpeed: Float,
    val rotationSpeedX: Float, // for 3D rotation simulation on X axis
    val rotationSpeedY: Float, // for 3D rotation simulation on Y axis
    val drag: Float, // air resistance coefficient
    val swayFrequency: Float,
    val swayAmplitude: Float,
    val swayPhase: Float
)

@Composable
fun ParticleBurst(
    trigger: Long,
    modifier: Modifier = Modifier,
    particleCount: Int = 180
) {
    if (trigger == 0L) return

    val progress = remember(trigger) { Animatable(0f) }
    val particles = remember(trigger) {
        val colors = listOf(
            VegasGold, BrightGold, RubyRed, NeonCyan, EmeraldGreen, RoyalPurple,
            Color.White, Color(0xFFFF69B4), Color(0xFF00BFFF), Color(0xFFFF8C00), Color(0xFFADFF2F),
            Color(0xFFFFD700), Color(0xFFFF4500), Color(0xFF00FA9A)
        )
        List(particleCount) { i ->
            // Distribute particles across three emitters:
            // - Left bottom stadium cannon (~35% of particles)
            // - Right bottom stadium cannon (~35% of particles)
            // - Center Winning Cup burst (~30% of particles)
            val emitterType = when {
                i < (particleCount * 0.35f).toInt() -> 0 // Left Cannon
                i < (particleCount * 0.70f).toInt() -> 1 // Right Cannon
                else -> 2 // Center Burst
            }

            val startX: Float
            val startY: Float
            val vx: Float
            val vy: Float

            when (emitterType) {
                0 -> {
                    // Left bottom corner shooting up-right towards center
                    startX = 0.02f
                    startY = 0.95f
                    // Angle between -15 and -65 degrees (in radians: -0.36 * PI to -0.08 * PI)
                    val angle = try {
                        Random.nextDouble(-0.36 * Math.PI, -0.08 * Math.PI)
                    } catch (_: Throwable) { -0.2 * Math.PI }
                    val speed = Random.nextFloat() * 1200f + 800f
                    vx = (cos(angle) * speed).toFloat()
                    vy = (sin(angle) * speed).toFloat()
                }
                1 -> {
                    // Right bottom corner shooting up-left towards center
                    startX = 0.98f
                    startY = 0.95f
                    // Angle between -115 and -165 degrees (in radians: -0.92 * PI to -0.64 * PI)
                    val angle = try {
                        Random.nextDouble(-0.92 * Math.PI, -0.64 * Math.PI)
                    } catch (_: Throwable) { -0.8 * Math.PI }
                    val speed = Random.nextFloat() * 1200f + 800f
                    vx = (cos(angle) * speed).toFloat()
                    vy = (sin(angle) * speed).toFloat()
                }
                else -> {
                    // Center winning cup area burst (radial celebration)
                    startX = 0.5f
                    startY = 0.55f // matches table top level
                    val angle = Random.nextDouble(0.0, 2.0 * Math.PI)
                    val speed = Random.nextFloat() * 800f + 300f
                    vx = (cos(angle) * speed).toFloat()
                    vy = (sin(angle) * speed).toFloat() - 300f // extra vertical upward pop
                }
            }

            val shape = ConfettiShape.entries.random()
            val size = when (shape) {
                ConfettiShape.RIBBON -> Random.nextFloat() * 8f + 5f
                ConfettiShape.CIRCLE -> Random.nextFloat() * 9f + 4f
                else -> Random.nextFloat() * 11f + 5f
            }

            Particle(
                id = i,
                startX = startX,
                startY = startY,
                vx = vx,
                vy = vy,
                color = colors.random(),
                size = size,
                shape = shape,
                initialRotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 400f, // degree rotation per sec
                rotationSpeedX = Random.nextFloat() * 8f + 3f,
                rotationSpeedY = Random.nextFloat() * 8f + 3f,
                drag = Random.nextFloat() * 1.3f + 0.7f, // natural air resistance
                swayFrequency = Random.nextFloat() * 5f + 2f,
                swayAmplitude = Random.nextFloat() * 30f + 10f,
                swayPhase = Random.nextFloat() * 2f * Math.PI.toFloat()
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2800, easing = LinearEasing) // 2.8s total air-time
        )
    }

    if (progress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val t = progress.value
            val duration = 2.8f // duration in seconds
            val s = t * duration // current elapsed seconds

            // Smooth fade-out: starts after 60% of duration
            val alpha = if (t < 0.6f) 1f else (1f - ((t - 0.6f) / 0.4f)).coerceIn(0f, 1f)
            val gravityConst = 1100f // gravity acceleration in pixels/s^2

            particles.forEach { p ->
                val k = p.drag

                // Physical equations with exponential air resistance drag:
                // Velocity v(s) = v0 * e^-ks
                // Distance d(s) = Integral of v(s) = v0 * (1 - e^-ks) / k
                val expTerm = exp(-k * s)
                val dragDistanceFactor = (1f - expTerm) / k

                val velocityXOffset = p.vx * dragDistanceFactor
                val velocityYOffset = p.vy * dragDistanceFactor

                // Gravity effect with drag: y_gravity(s) = (g/k) * s - (g/k^2) * (1 - e^-ks)
                val gravityYOffset = (gravityConst / k) * s - (gravityConst / (k * k)) * (1f - expTerm)

                // Shimmering horizontal fluttering sway (wind turbulence)
                val swayOffset = p.swayAmplitude * sin(p.swayFrequency * s + p.swayPhase) * (s / duration)

                // Normalized start position to actual coordinates
                val px = (size.width * p.startX) + velocityXOffset + swayOffset
                val py = (size.height * p.startY) + velocityYOffset + gravityYOffset

                // 3D scale transformation to simulate fluttering paper flipping on X/Y axes
                val scaleX = cos(p.rotationSpeedX * s).coerceIn(-1f, 1f)
                val scaleY = sin(p.rotationSpeedY * s).coerceIn(-1f, 1f)
                val rotationDegrees = p.initialRotation + (p.rotationSpeed * s)

                // Shimmering shiny color/reflection glitter
                val glitter = 0.75f + 0.25f * sin(24f * s + p.id)
                val particleAlpha = (alpha * glitter).coerceIn(0f, 1f)

                if (px in -100f..(size.width + 100f) && py in -100f..(size.height + 100f)) {
                    withTransform({
                        translate(left = px, top = py)
                        rotate(degrees = rotationDegrees)
                        scale(scaleX = scaleX, scaleY = scaleY)
                    }) {
                        when (p.shape) {
                            ConfettiShape.CIRCLE -> {
                                drawCircle(
                                    color = p.color.copy(alpha = particleAlpha),
                                    radius = p.size,
                                    center = Offset.Zero
                                )
                            }
                            ConfettiShape.RECTANGLE -> {
                                drawRect(
                                    color = p.color.copy(alpha = particleAlpha),
                                    topLeft = Offset(-p.size, -p.size * 0.6f),
                                    size = Size(p.size * 2f, p.size * 1.2f)
                                )
                            }
                            ConfettiShape.TRIANGLE -> {
                                val trianglePath = Path().apply {
                                    moveTo(0f, -p.size)
                                    lineTo(p.size * 0.86f, p.size * 0.5f)
                                    lineTo(-p.size * 0.86f, p.size * 0.5f)
                                    close()
                                }
                                drawPath(
                                    path = trianglePath,
                                    color = p.color.copy(alpha = particleAlpha)
                                )
                            }
                            ConfettiShape.RIBBON -> {
                                val ribbonPath = Path().apply {
                                    moveTo(-p.size, -p.size * 0.4f)
                                    cubicTo(
                                        -p.size * 0.5f, -p.size * 1.2f,
                                        p.size * 0.5f, p.size * 0.4f,
                                        p.size, -p.size * 0.4f
                                    )
                                }
                                drawPath(
                                    path = ribbonPath,
                                    color = p.color.copy(alpha = particleAlpha),
                                    style = Stroke(
                                        width = 3.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
