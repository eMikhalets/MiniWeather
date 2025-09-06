package com.emikhalets.miniweather.data

import com.emikhalets.miniweather.data.remote.WeatherDataDto
import com.emikhalets.miniweather.data.remote.WeatherDto
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
    Timber.d("Map Dto to Model: $this")
    val firstWeather = weather?.firstOrNull()
    return WeatherModel(
        city = name.orEmpty(),
        temperature = main?.temp,
        feelsLike = main?.feelsLike,
        humidity = main?.humidity,
        windSpeed = wind?.speed,
        description = firstWeather.safeDescription(),
        iconUrl = firstWeather.safeIconUrl(),
        updatedAt = (dt ?: 0).toLong(),
        pressure = hPaToMmHg(main?.pressure),
    )
}

private fun WeatherDataDto?.safeDescription(): String =
    this?.description ?: ""

private fun WeatherDataDto?.safeIconUrl(): String =
    this?.icon?.let { "https://openweathermap.org/img/wn/${it}@2x.png" }.orEmpty()

private fun hPaToMmHg(hPa: Int?): Int? =
    hPa?.let { (it * 0.75006156).roundToInt() }