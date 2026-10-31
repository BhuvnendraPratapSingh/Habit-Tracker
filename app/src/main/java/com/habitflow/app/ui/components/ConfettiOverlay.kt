package com.habitflow.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val color: Color,
    val size: Float,
    val shape: Int // 0 = rect, 1 = circle
)

private val confettiColors = listOf(
    Color(0xFFFF6B35),
    Color(0xFFFFD700),
    Color(0xFF34D399),
    Color(0xFF60A5FA),
    Color(0xFFF472B6),
    Color(0xFFA78BFA),
    Color(0xFFFBBF24),
    Color(0xFFFF3B30),
)

/**
 * Full-screen confetti particle overlay.
 *
 * BUG-19 FIX: Canvas does NOT consume touch events.
 * No clickable/pointerInput modifier — touches pass through
 * to the content underneath (habit cards, buttons, etc.).
 */
@Composable
fun ConfettiOverlay(
    trigger: Boolean,
    onFinished: () -> Unit = {},
    particleCount: Int = 120,
    durationMs: Long = 3000L
) {
    if (!trigger) return

    val triggerKey = remember(trigger) { System.nanoTime() }

    key(triggerKey) {
        var particles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }
        val progress = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            particles = List(particleCount) {
                ConfettiParticle(
                    x = Random.nextFloat(),
                    y = Random.nextFloat() * -0.3f - 0.1f,
                    velocityX = Random.nextFloat() * 0.4f - 0.2f,
                    velocityY = Random.nextFloat() * 0.3f + 0.2f,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = Random.nextFloat() * 8f - 4f,
                    color = confettiColors.random(),
                    size = Random.nextFloat() * 8f + 4f,
                    shape = Random.nextInt(2)
                )
            }
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMs.toInt(), easing = LinearEasing)
            )
            delay(100)
            onFinished()
        }

        val t = progress.value

        // fillMaxSize draws over everything visually,
        // but without clickable/pointerInput, touches pass through.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val alpha = if (t > 0.7f) 1f - ((t - 0.7f) / 0.3f) else 1f

            particles.forEach { p ->
                val gravity = 0.5f
                val x = (p.x + p.velocityX * t) * size.width +
                        sin(t * 3f + p.rotation) * 20f
                val y = (p.y + p.velocityY * t + gravity * t * t) * size.height
                val rotation = p.rotation + p.rotationSpeed * t * 60f

                if (y < size.height + 50 && y > -50) {
                    rotate(rotation, pivot = Offset(x, y)) {
                        when (p.shape) {
                            0 -> drawRect(
                                color = p.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                                topLeft = Offset(x - p.size / 2, y - p.size),
                                size = Size(p.size, p.size * 1.5f)
                            )
                            1 -> drawCircle(
                                color = p.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                                radius = p.size / 2,
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }
        }
    }
}
