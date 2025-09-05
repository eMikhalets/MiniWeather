package com.emikhalets.miniweather.core

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