package com.emikhalets.miniweather.core

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.util.Locale
import kotlin.math.roundToInt

fun Double?.roundToIntOrDash(): String {
    if (this == null) return "—"
    return try {
        roundToInt().toString()
    } catch (e: Exception) {
        "—"
    }
}

fun formatDoubleOneDigit(value: Double): String = String.format(Locale.US, "%.1f", value)

sealed interface LoadState {
    data object Idle : LoadState
    data object Loading : LoadState
    data class Error(val message: String) : LoadState
}

@Composable
fun rememberShimmerBrush(
    baseColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    highlightColor: Color = Color.White.copy(alpha = 0.35f),
    durationMillis: Int = 1100,
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val t by transition.animateFloat(
        initialValue = -600f,
        targetValue = 1600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "translate"
    )

    // проекция движения на оси X/Y по заданному углу
    val rad = Math.toRadians(45.0)
    val dx = kotlin.math.cos(rad).toFloat()
    val dy = kotlin.math.sin(rad).toFloat()

    val band = 400f // ширина «полосы» блика
    val start = Offset(t * dx, t * dy)
    val end = Offset(start.x + band * dx, start.y + band * dy)

    return Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = start,
        end = end
    )
}