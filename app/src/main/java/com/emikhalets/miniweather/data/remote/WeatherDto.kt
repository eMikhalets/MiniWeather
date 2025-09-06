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
    val rain: Rain1HDto? = null,
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
data class Rain1HDto(
    @SerialName("1h")
    val h: Double? = null
)