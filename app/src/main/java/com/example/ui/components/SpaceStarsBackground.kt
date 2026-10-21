package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CyberBackground
import kotlin.math.sin
import kotlin.random.Random

data class StarParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val baseAlpha: Float,
    val twinkleSpeed: Int,
    val phaseShift: Float,
    val color: Color
)

@Composable
fun SpaceStarsBackground(
    modifier: Modifier = Modifier,
    starCount: Int = 85,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "StarsTwinkle")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starPulse"
    )

    // Generate static star coordinates once
    val stars = remember(starCount) {
        val colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFE2E8F0),
            Color(0xFF00F0FF),
            Color(0xFFA855F7),
            Color(0xFF93C5FD),
            Color(0xFFFDE68A)
        )
        List(starCount) {
            StarParticle(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat(),
                radius = Random.nextFloat() * 1.8f + 0.6f,
                baseAlpha = Random.nextFloat() * 0.5f + 0.3f,
                twinkleSpeed = Random.nextInt(1, 4),
                phaseShift = Random.nextFloat() * 6.28f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0D1527), // Deep cosmic navy center
                        Color(0xFF060913), // Mid space
                        Color(0xFF020408)  // Obsidian deep void
                    ),
                    radius = 1600f
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Draw celestial nebula haze clouds
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF6B21A8).copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.18f),
                    radius = w * 0.6f
                ),
                radius = w * 0.6f,
                center = Offset(w * 0.85f, h * 0.18f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF0284C7).copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.75f),
                    radius = w * 0.7f
                ),
                radius = w * 0.7f,
                center = Offset(w * 0.15f, h * 0.75f)
            )

            // Draw twinkling stars
            for (star in stars) {
                val twinkle = (sin(pulse * star.twinkleSpeed + star.phaseShift) * 0.35f).toFloat()
                val alpha = (star.baseAlpha + twinkle).coerceIn(0.15f, 1.0f)
                val center = Offset(star.xRatio * w, star.yRatio * h)

                drawCircle(
                    color = star.color.copy(alpha = alpha),
                    radius = star.radius,
                    center = center
                )

                // Add subtle glow aura to larger stars
                if (star.radius > 1.8f) {
                    drawCircle(
                        color = star.color.copy(alpha = alpha * 0.25f),
                        radius = star.radius * 2.8f,
                        center = center
                    )
                }
            }
        }

        content()
    }
}
