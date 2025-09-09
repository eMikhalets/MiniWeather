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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

fun formatDuration(context: Context, seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return if (h > 0) {
        context.getString(R.string.second_format_hour_minute).format(h, m)
    } else {
        context.getString(R.string.second_format_minute).format(m)
    }
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