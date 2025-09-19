package com.emikhalets.miniweather.ui.weather

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emikhalets.miniweather.R
import com.emikhalets.miniweather.core.theme.MiniWeatherTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DaylightArc(
    sunriseEpochSec: Long,
    sunsetEpochSec: Long,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    previewProgress: Float? = null,
) {
    if (sunriseEpochSec == 0L || sunsetEpochSec == 0L) return

    // валидация (на случай полярного дня/ночи)
    val isValid = sunsetEpochSec > sunriseEpochSec
    val nowEpochSec = remember { System.currentTimeMillis() / 1000 }
    val dayLength = (sunsetEpochSec - sunriseEpochSec).coerceAtLeast(1)
    val raw = (nowEpochSec - sunriseEpochSec).toFloat() / dayLength
    val clamped = raw.coerceIn(0f, 1f)

    val arcColorBackground = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val arcColorForeground = MaterialTheme.colorScheme.primary
    val sunColor = Color(0xFFFFCC33)
    val stroke = with(LocalDensity.current) { 6.dp.toPx() }
    val sunRadius = with(LocalDensity.current) { 10.dp.toPx() }

    val inPreview = LocalInspectionMode.current
    val progressValue = when {
        inPreview && previewProgress != null -> previewProgress.coerceIn(0f, 1f)
        !animate || !isValid -> clamped
        else -> {
            val anim = remember { Animatable(if (clamped in 0f..1f) 0f else clamped) }
            LaunchedEffect(sunriseEpochSec, sunsetEpochSec, nowEpochSec) {
                if (clamped in 0f..1f) {
                    anim.snapTo(0f)
                    anim.animateTo(clamped, tween(500, easing = LinearEasing))
                } else {
                    anim.snapTo(clamped)
                }
            }
            anim.value
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            // степень сплющенности
            val verticalRadius = (size.height * 0.44f).coerceAtLeast(stroke)
            val horizontalPadding = sunRadius * 1.5f
            val rect = Rect(
                left = horizontalPadding,
                top = size.height - 2f * verticalRadius,
                right = size.width - horizontalPadding,
                bottom = size.height * 1.65f
            )

            // фон дуги
            drawArc(
                color = arcColorBackground,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = rect.topLeft,
                size = rect.size,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            if (isValid) {
                // прогресс дуги
                drawArc(
                    color = arcColorForeground,
                    startAngle = 180f,
                    sweepAngle = 180f * progressValue,
                    useCenter = false,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )

                // позиция «солнца» на полуэллипсе
                val angleDeg = 180f + 180f * progressValue
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val sunX = rect.center.x + (rect.width / 2f) * cos(angleRad).toFloat()
                val sunY = rect.center.y + (rect.height / 2f) * sin(angleRad).toFloat()

                drawCircle(
                    color = sunColor,
                    radius = sunRadius + 2f,
                    center = Offset(sunX, sunY)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = formatTimeDeviceTZ(sunriseEpochSec),
                style = MaterialTheme.typography.labelMedium
            )
            val centerLabel = if (!isValid) {
                "—"
            } else {
                when {
                    raw < 0f -> stringResource(R.string.before_sunrise)
                    raw > 1f -> stringResource(R.string.after_sunset)
                    else -> {
                        val leftSec = (sunsetEpochSec - nowEpochSec).coerceAtLeast(0)
                        stringResource(R.string.before_sunset_value) +
                                formatDuration(LocalContext.current, leftSec)
                    }
                }
            }
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatTimeDeviceTZ(sunsetEpochSec),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

private fun formatTimeDeviceTZ(epochSec: Long): String {
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    fmt.timeZone = TimeZone.getDefault()
    return fmt.format(Date(epochSec * 1000))
}

// ==============
//  Preview Data
// ==============

private val startTime = System.currentTimeMillis() - 1000 * 60 * 60 * 3
private val endTime = System.currentTimeMillis() + 1000 * 60 * 60 * 8

@Preview(showBackground = true)
@Composable
private fun PreviewData() {
    MiniWeatherTheme {
        DaylightArc(
            sunriseEpochSec = startTime,
            sunsetEpochSec = endTime,
            previewProgress = 0.5f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDataPadding50() {
    MiniWeatherTheme {
        DaylightArc(
            sunriseEpochSec = startTime,
            sunsetEpochSec = endTime,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDataPadding100() {
    MiniWeatherTheme {
        DaylightArc(
            sunriseEpochSec = startTime,
            sunsetEpochSec = endTime,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 100.dp)
        )
    }
}