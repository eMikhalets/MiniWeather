package com.emikhalets.miniweather.domain.model

data class ForecastModel(
    val timeOffset: Int,          // смещение города от UTC (секунды)
    val hours: List<Hour>,
) {
    data class Hour(
        val timeEpoch: Long,      // время точки прогноза (epoch sec, UTC)
        val temperature: Double?, // температура
        val iconUrl: String?,     // иконка OWM
        val popPercent: Int?,     // вероятность осадков 0..100
        val precipMm3h: Double?,  // мм осадков за 3 часа (rain)
    )
}