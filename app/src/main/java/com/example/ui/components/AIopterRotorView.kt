package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AIopterBlue
import com.example.ui.theme.AIopterCyan
import com.example.ui.theme.AIopterCyanLight
import com.example.ui.theme.AIopterKillRed
import com.example.ui.theme.AIopterNavyDark
import com.example.ui.theme.AIopterWarningAmber
import kotlinx.coroutines.isActive

/**
 * AIopter official blue/cyan three-part propeller forming the "O" logo.
 * The propeller itself is the floating overlay object above other apps.
 * Transparent background with subtle shadow and cyan glow for high contrast on any background.
 */
@Composable
fun AIopterRotorView(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    isSpinning: Boolean = false,
    isScreenSharingActive: Boolean = false,
    isListening: Boolean = false,
    isBlockedOrAlert: Boolean = false,
    hasBackingDisk: Boolean = false
) {
    // Smooth rotational animation: ~1.35s per full 360 degree rotation when active,
    // and naturally glides to rest at 0 degrees when processing finishes/stops.
    val rotationAngle = remember { Animatable(0f) }

    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            while (isActive) {
                val current = rotationAngle.value
                val nextTurn = ((current / 360f).toInt() + 1) * 360f
                val remainingDeg = nextTurn - current
                val duration = ((remainingDeg / 360f) * 1350f).toInt().coerceAtLeast(60)
                rotationAngle.animateTo(
                    targetValue = nextTurn,
                    animationSpec = tween(durationMillis = duration, easing = LinearEasing)
                )
            }
        } else {
            // Smoothly decelerate to complete revolution and settle at resting 0 degrees
            val current = rotationAngle.value
            val nextResting = ((current / 360f).toInt() + 1) * 360f
            val remainingDeg = nextResting - current
            if (remainingDeg in 1.0f..359.0f) {
                val settleDuration = ((remainingDeg / 360f) * 550f).toInt().coerceIn(180, 550)
                rotationAngle.animateTo(
                    targetValue = nextResting,
                    animationSpec = tween(durationMillis = settleDuration, easing = FastOutSlowInEasing)
                )
            }
            rotationAngle.snapTo(0f)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rotor_ambient")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size.minDimension
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val outerRadius = canvasSize * 0.44f
            val innerRadius = canvasSize * 0.22f
            val strokeWidth = canvasSize * 0.15f

            // Optional solid dark backing disk only when explicitly requested (e.g. in certain dark cards)
            if (hasBackingDisk) {
                drawCircle(
                    color = AIopterNavyDark,
                    radius = outerRadius + strokeWidth * 0.35f,
                    center = center
                )
            }

            // Status outer glow halo ring when screen sharing or listening
            if (isScreenSharingActive || isListening || isBlockedOrAlert) {
                val glowColor = when {
                    isBlockedOrAlert -> AIopterKillRed
                    isListening -> AIopterWarningAmber
                    else -> AIopterCyan
                }
                drawCircle(
                    color = glowColor.copy(alpha = pulseAlpha * 0.65f),
                    radius = outerRadius + strokeWidth * 0.5f,
                    center = center,
                    style = Stroke(width = strokeWidth * 0.35f)
                )
            }

            // Subtle dark drop shadow layer for visibility against bright/white backgrounds
            val currentRot = rotationAngle.value
            rotate(degrees = currentRot, pivot = center) {
                val bladeSweep = 96f // 3 blades * 96 deg = 288 deg + 3 gaps of 24 deg = 360 deg
                val gap = 24f
                val arcRect = Size(outerRadius * 2f, outerRadius * 2f)
                val shadowOffset = Offset(center.x - outerRadius, center.y - outerRadius + 2f)

                // 1. Drop shadow silhouettes behind blades
                for (i in 0..2) {
                    val startAngle = i * (bladeSweep + gap)
                    drawArc(
                        color = Color.Black.copy(alpha = 0.55f),
                        startAngle = startAngle,
                        sweepAngle = bladeSweep,
                        useCenter = false,
                        topLeft = shadowOffset,
                        size = arcRect,
                        style = Stroke(width = strokeWidth * 1.15f, cap = StrokeCap.Round)
                    )
                }

                // Center core shadow
                drawCircle(
                    color = Color.Black.copy(alpha = 0.5f),
                    radius = innerRadius * 0.6f,
                    center = Offset(center.x, center.y + 2f)
                )

                // 2. Ambient cyan glow underlay around blades
                val arcTopLeft = Offset(center.x - outerRadius, center.y - outerRadius)
                for (i in 0..2) {
                    val startAngle = i * (bladeSweep + gap)
                    drawArc(
                        color = AIopterCyan.copy(alpha = 0.25f),
                        startAngle = startAngle,
                        sweepAngle = bladeSweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcRect,
                        style = Stroke(width = strokeWidth * 1.25f, cap = StrokeCap.Round)
                    )
                }

                // 3. Official 3 aerodynamic rotor blades forming the "O"
                for (i in 0..2) {
                    val startAngle = i * (bladeSweep + gap)
                    val bladeBrush = when (i) {
                        0 -> Brush.sweepGradient(
                            listOf(AIopterBlue, AIopterCyan, AIopterCyanLight),
                            center = center
                        )
                        1 -> Brush.sweepGradient(
                            listOf(AIopterCyan, AIopterBlue, AIopterCyanLight),
                            center = center
                        )
                        else -> Brush.sweepGradient(
                            listOf(AIopterCyanLight, AIopterCyan, AIopterBlue),
                            center = center
                        )
                    }

                    drawArc(
                        brush = bladeBrush,
                        startAngle = startAngle,
                        sweepAngle = bladeSweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcRect,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Center core "O" cutout with metallic cyan ring
                drawCircle(
                    color = AIopterCyanLight.copy(alpha = 0.9f),
                    radius = innerRadius,
                    center = center,
                    style = Stroke(width = strokeWidth * 0.25f)
                )

                // Inner focal node
                drawCircle(
                    color = AIopterCyan,
                    radius = innerRadius * 0.45f,
                    center = center
                )
            }
        }
    }
}
