package com.emikhalets.miniweather.domain.model

data class WeatherModel(
    val city: String = "",
    val temperature: Double? = null,
    val feelsLike: Double? = null,
    val humidity: Int? = null,
    val windSpeed: Double? = null,
    val description: String = "",
    val iconUrl: String = "",
    val updatedAt: Long = 0,
    val pressure: Int? = null,
    val sunrise: Long,
    val sunset: Long,
    val timeOffset: Int?,
)