package com.emikhalets.miniweather.domain.model

data class PollutionModel(
    val aqi: Int,
    val updatedAt: Long,
    val co: Double?,
    val no: Double?,
    val no2: Double?,
    val o3: Double?,
    val so2: Double?,
    val pm2_5: Double?,
    val pm10: Double?,
    val nh3: Double?,
)