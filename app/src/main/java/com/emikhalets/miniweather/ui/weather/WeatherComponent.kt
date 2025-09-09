package com.emikhalets.miniweather.ui.weather

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.emikhalets.miniweather.R
import com.emikhalets.miniweather.core.toast
import com.emikhalets.miniweather.domain.model.WeatherModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// Простой градиент: тепло — оранжевый, холод — синий, ночь — темнее
fun heroGradient(model: WeatherModel): List<Color> {
    val hour = Calendar.getInstance().apply { timeInMillis = model.updatedAt * 1000 }
        .get(Calendar.HOUR_OF_DAY)
    val isNight = hour < 6 || hour >= 21
    val temperature = model.temperature ?: 0.0
    return if (isNight) {
        listOf(Color(0xFF0F172A), Color(0xFF1E293B))
    } else if (temperature >= 20) {
        listOf(Color(0xFFFFB74D), Color(0xFFFF8A65))
    } else {
        listOf(Color(0xFF64B5F6), Color(0xFF4FC3F7))
    }
}

@Composable
fun DaylightArc(
    sunriseEpochSec: Long,
    sunsetEpochSec: Long,
    modifier: Modifier = Modifier,
    timezoneOffset: Int? = null,
    nowEpochSec: Long = System.currentTimeMillis() / 1000,
) {
    if (sunriseEpochSec == 0L || sunsetEpochSec == 0L) return

    val context = LocalContext.current

    // валидация (на случай полярного дня/ночи)
    val isValid = sunsetEpochSec > sunriseEpochSec
    val dayLength = (sunsetEpochSec - sunriseEpochSec).coerceAtLeast(1)
    val rawProgress = ((nowEpochSec - sunriseEpochSec).toFloat() / dayLength.toFloat())
    val clamped = rawProgress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = if (isValid) clamped else 0f,
        animationSpec = tween(700, easing = LinearEasing),
        label = "sun-progress"
    )

    val arcColorBg = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val arcColorFg = MaterialTheme.colorScheme.primary
    val sunColor = Color(0xFFFFCC33)

    val strokeWidth = with(LocalDensity.current) { 6.dp.toPx() }
    val sunRadiusPx = with(LocalDensity.current) { 10.dp.toPx() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Полукруг: сверху, левый край = рассвет, правый = закат
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 16.dp)
        ) {
            // диаметр дуги ограничим шириной холста и высотой*2 (чтобы влезла верхняя полусфера)
            val diameter = min(size.width, size.height * 2f)
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, radius) // центр окружности выше baseline

            val rect = Rect(
                left = center.x - radius,
                top = center.y - radius,
                right = center.x + radius,
                bottom = center.y + radius
            )

            // фон дуги (вся световая дуга)
            drawArc(
                color = arcColorBg,
                startAngle = 180f,      // слева
                sweepAngle = 180f,      // до права
                useCenter = false,
                topLeft = rect.topLeft,
                size = rect.size,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            if (isValid) {
                // прогресс по дуге
                drawArc(
                    color = arcColorFg,
                    startAngle = 180f,
                    sweepAngle = 180f * animated,
                    useCenter = false,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // текущая позиция "солнца" (точка на дуге)
                val angleDeg = 180f + 180f * animated
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val sunX = center.x + radius * cos(angleRad).toFloat()
                val sunY = center.y + radius * sin(angleRad).toFloat()
                drawCircle(
                    color = sunColor,
                    radius = sunRadiusPx + 2, // тонкая "аура"
                    center = Offset(sunX, sunY)
                )
            }
        }

        // подписи времени
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(sunriseEpochSec, timezoneOffset),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Start
            )
            val centerLabel = if (!isValid) "—"
            else when {
                rawProgress < 0f -> stringResource(R.string.before_sunrise)
                rawProgress > 1f -> stringResource(R.string.after_sunset)
                else -> {
                    // сколько до заката
                    val leftSec = sunsetEpochSec - nowEpochSec
                    stringResource(R.string.before_sunset_value) +
                            formatDuration(context, leftSec.coerceAtLeast(0))
                }
            }
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatTime(sunsetEpochSec, timezoneOffset),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.End
            )
        }
    }
}

fun formatTime(epochSec: Long, tzOffsetSec: Int?): String {
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    val tz = tzOffsetSec?.let { seconds ->
        val sign = if (seconds >= 0) "+" else "-"
        val abs = abs(seconds)
        val h = abs / 3600
        val m = (abs % 3600) / 60
        TimeZone.getTimeZone(String.format(Locale.US, "GMT%s%02d:%02d", sign, h, m))
    } ?: TimeZone.getDefault()
    fmt.timeZone = tz
    return fmt.format(Date(epochSec * 1000))
}

/**
 * Возвращает лямбду, которую можно повесить на кнопку "геолокация".
 * Внутри хук сам показывает rational/настройки/диалог включения локации
 * и вызывает onReady() только когда всё ок.
 */
@Composable
fun rememberLocationAccess(
    onReady: () -> Unit,
    rationaleText: String,
    permissionDeniedTitle: String,
    permissionDeniedHint: String,
    allowText: String,
    cancelText: String,
    openSettingsText: String,
    servicesDisabledToast: String,
): () -> Unit {
    val context = LocalContext.current
    val activity = LocalActivity.current

    var showRationale by remember { mutableStateOf(false) }
    var showGoToSettings by remember { mutableStateOf(false) }

    val resolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onReady()
        } else {
            context.toast(servicesDisabledToast)
        }
    }

    val permission = Manifest.permission.ACCESS_COARSE_LOCATION
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            resolveLocationSettings(activity, onReady, servicesDisabledToast, resolutionLauncher)
        } else {
            val needRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
            } ?: false
            if (needRationale) {
                showRationale = true
            } else {
                showGoToSettings = true
            }
        }
    }

    val onLocationClick: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
        if (granted) {
            resolveLocationSettings(activity, onReady, servicesDisabledToast, resolutionLauncher)
        } else {
            val needRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
            } ?: false
            if (needRationale) {
                showRationale = true
            } else {
                permissionLauncher.launch(permission)
            }
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(text = permissionDeniedTitle) },
            text = { Text(text = rationaleText) },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    permissionLauncher.launch(permission)
                }) { Text(allowText) }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) { Text(cancelText) }
            }
        )
    }
    if (showGoToSettings) {
        AlertDialog(
            onDismissRequest = { showGoToSettings = false },
            title = { Text(text = permissionDeniedTitle) },
            text = { Text(text = permissionDeniedHint) },
            confirmButton = {
                TextButton(onClick = {
                    showGoToSettings = false
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                }) { Text(openSettingsText) }
            },
            dismissButton = {
                TextButton(onClick = { showGoToSettings = false }) { Text(cancelText) }
            }
        )
    }

    return onLocationClick
}

private fun resolveLocationSettings(
    activity: Activity?,
    onReady: () -> Unit,
    servicesDisabledToast: String,
    resolutionLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>?,
) {
    activity ?: return

    val client = LocationServices.getSettingsClient(activity)
    val request = LocationRequest.Builder(
        Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10_000L
    ).build()
    val settingsRequest = LocationSettingsRequest.Builder()
        .addLocationRequest(request)
        .build()

    client.checkLocationSettings(settingsRequest)
        .addOnSuccessListener { onReady() }
        .addOnFailureListener { ex ->
            val rae = ex as? ResolvableApiException
            if (rae != null) {
                resolutionLauncher
                    ?.launch(IntentSenderRequest.Builder(rae.resolution).build())
                    ?: rae.startResolutionForResult(activity, 1001)
            } else {
                activity.toast(servicesDisabledToast)
            }
        }
}

private fun formatDuration(context: Context, seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return if (h > 0) {
        context.getString(R.string.second_format_hour_minute).format(h, m)
    } else {
        context.getString(R.string.second_format_minute).format(m)
    }
}