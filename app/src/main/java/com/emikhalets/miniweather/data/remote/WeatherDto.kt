package com.emikhalets.miniweather.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDto(
    @SerialName("base")
    val base: String? = null,
    @SerialName("clouds")
    val clouds: CloudsDto? = null,
    @SerialName("cod")
    val cod: Int? = null,
    @SerialName("coord")
    val coord: CoordDto? = null,
    @SerialName("dt")
    val dt: Int? = null,
    @SerialName("id")
    val id: Int? = null,
    @SerialName("main")
    val main: MainDto? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("rain")
    val rain: RainDto? = null,
    @SerialName("sys")
    val sys: SysDto? = null,
    @SerialName("timezone")
    val timezone: Int? = null,
    @SerialName("visibility")
    val visibility: Int? = null,
    @SerialName("weather")
    val weather: List<WeatherDataDto?>? = null,
    @SerialName("wind")
    val wind: WindDto? = null
)

@Serializable
data class WindDto(
    @SerialName("deg")
    val deg: Int? = null,
    @SerialName("gust")
    val gust: Double? = null,
    @SerialName("speed")
    val speed: Double? = null
)

@Serializable
data class WeatherDataDto(
    @SerialName("description")
    val description: String? = null,
    @SerialName("icon")
    val icon: String? = null,
    @SerialName("id")
    val id: Int? = null,
    @SerialName("main")
    val main: String? = null
)

@Serializable
data class SysDto(
    @SerialName("country")
    val country: String? = null,
    @SerialName("id")
    val id: Int? = null,
    @SerialName("sunrise")
    val sunrise: Int? = null,
    @SerialName("sunset")
    val sunset: Int? = null,
    @SerialName("type")
    val type: Int? = null
)

@Serializable
data class RainDto(
    @SerialName("1h")
    val h: Double? = null
)

@Serializable
data class MainDto(
    @SerialName("feels_like")
    val feelsLike: Double? = null,
    @SerialName("grnd_level")
    val grndLevel: Int? = null,
    @SerialName("humidity")
    val humidity: Int? = null,
    @SerialName("pressure")
    val pressure: Int? = null,
    @SerialName("sea_level")
    val seaLevel: Int? = null,
    @SerialName("temp")
    val temp: Double? = null,
    @SerialName("temp_max")
    val tempMax: Double? = null,
    @SerialName("temp_min")
    val tempMin: Double? = null
)

@Serializable
data class CoordDto(
    @SerialName("lat")
    val lat: Double? = null,
    @SerialName("lon")
    val lon: Double? = null
)

@Serializable
data class CloudsDto(
    @SerialName("all")
    val all: Int? = null
)