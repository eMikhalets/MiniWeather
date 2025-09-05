package com.emikhalets.miniweather.ui.weather

import androidx.compose.ui.graphics.Color
import com.emikhalets.miniweather.domain.model.WeatherModel
import java.util.Calendar

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
