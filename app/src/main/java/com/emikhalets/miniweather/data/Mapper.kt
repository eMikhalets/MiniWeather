package com.emikhalets.miniweather.data

import com.emikhalets.miniweather.data.remote.ForecastDto
import com.emikhalets.miniweather.data.remote.ForecastItemDto
import com.emikhalets.miniweather.data.remote.WeatherDataDto
import com.emikhalets.miniweather.data.remote.WeatherDto
import com.emikhalets.miniweather.domain.model.ForecastModel
import com.emikhalets.miniweather.domain.model.WeatherModel
import timber.log.Timber
import kotlin.math.roundToInt

/**
 * OpenWeather 'current weather' → domain WeatherModel
 * - updatedAt: epoch seconds (под UI: Date(updatedAt * 1000))
 * - pressure: конвертируем из hPa в мм рт. ст.
 * - dewPoint: в этом эндпоинте нет → оставляем null
 */
fun WeatherDto.mapToModel(): WeatherModel {
    Timber.d("Map current weather dto to model: $this")
    val firstWeather = weather?.firstOrNull()
    return WeatherModel(
        city = name.orEmpty(),
        temperature = main?.temp,
        feelsLike = main?.feelsLike,
        humidity = main?.humidity,
        windSpeed = wind?.speed,
        description = firstWeather.safeDescription(),
        iconUrl = firstWeather?.icon.toIconUrl(),
        updatedAt = (dt ?: 0).toLong(),
        pressure = hPaToMmHg(main?.pressure),
        sunset = sys?.sunset?.toLong() ?: 0,
        sunrise = sys?.sunrise?.toLong() ?: 0,
        timeOffset = timezone
    )
}

fun ForecastDto.mapToModel(): ForecastModel {
    val tz = city?.timezone ?: 0
    val hours = list.orEmpty()
        .mapNotNull { it }
        .map { it.toHour() }

    return ForecastModel(
        timeOffset = tz,
        hours = hours
    )
}

private fun ForecastItemDto.toHour(): ForecastModel.Hour {
    val icon = weather.orEmpty().firstOrNull()?.icon
    return ForecastModel.Hour(
        timeEpoch = (dt ?: 0).toLong(),
        temperature = main?.temp,
        iconUrl = icon.toIconUrl(),
        popPercent = pop?.let { (it * 100).roundToInt().coerceIn(0, 100) },
        precipMm3h = rain?.h ?: snow?.h
    )
}

private fun WeatherDataDto?.safeDescription(): String =
    this?.description ?: ""

private fun String?.toIconUrl(): String =
    this?.let { "https://openweathermap.org/img/wn/${it}@2x.png" }.orEmpty()

private fun hPaToMmHg(hPa: Int?): Int? =
    hPa?.let { (it * 0.75006156).roundToInt() }