package com.emikhalets.miniweather.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject

class LocationSource @Inject constructor(
    private val fused: FusedLocationProviderClient,
    @field:ApplicationContext private val context: Context,
) {

    /**
     * Возвращает приблизительные координаты (lat, lon) или ошибку.
     * Требует выданного COARSE разрешения.
     */
    suspend fun getLocation(): Result<Pair<Double, Double>> {
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasCoarse) {
            return Result.failure(IllegalStateException("No location permission"))
        }

        return try {
            val location = try {
                withTimeout(3000) {
                    fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .await()
                }
            } catch (e: TimeoutCancellationException) {
                Timber.e(e)
                null
            }

            val lastLocation = location ?: fused.lastLocation.await()
            if (lastLocation != null) {
                Result.success(lastLocation.latitude to lastLocation.longitude)
            } else {
                Result.failure(IllegalStateException("Location unavailable"))
            }
        } catch (t: Throwable) {
            Timber.e(t)
            Result.failure(t)
        }
    }
}