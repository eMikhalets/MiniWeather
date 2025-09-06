package com.emikhalets.miniweather.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CloudsDto(
    @SerialName("all")
    val all: Int? = null,
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
    val tempMin: Double? = null,
)

@Serializable
data class CoordDto(
    @SerialName("lat")
    val lat: Double? = null,
    @SerialName("lon")
    val lon: Double? = null
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