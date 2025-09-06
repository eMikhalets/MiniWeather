package com.emikhalets.miniweather.data.remote


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastDto(
    @SerialName("city")
    val city: CityDto? = null,
    @SerialName("cnt")
    val cnt: Int? = null,
    @SerialName("cod")
    val cod: String? = null,
    @SerialName("list")
    val list: List<ForecastItemDto?>? = null,
    @SerialName("message")
    val message: Int? = null,
)

@Serializable
data class ForecastItemDto(
    @SerialName("clouds")
    val clouds: CloudsDto? = null,
    @SerialName("dt")
    val dt: Int? = null,
    @SerialName("dt_txt")
    val dtTxt: String? = null,
    @SerialName("main")
    val main: MainDto? = null,
    @SerialName("pop")
    val pop: Double? = null,
    @SerialName("rain")
    val rain: Rain3HDto? = null,
    @SerialName("snow")
    val snow: Snow3HDto? = null,
    @SerialName("sys")
    val sys: ForecastSysDto? = null,
    @SerialName("visibility")
    val visibility: Int? = null,
    @SerialName("weather")
    val weather: List<WeatherDataDto?>? = null,
    @SerialName("wind")
    val wind: WindDto? = null,
)

@Serializable
data class Rain3HDto(
    @SerialName("3h")
    val h: Double? = null,
)

@Serializable
data class Snow3HDto(
    @SerialName("3h")
    val h: Double? = null,
)

@Serializable
data class ForecastSysDto(
    @SerialName("pod")
    val pod: String? = null,
)

@Serializable
data class CityDto(
    @SerialName("coord")
    val coord: CoordDto? = null,
    @SerialName("country")
    val country: String? = null,
    @SerialName("id")
    val id: Int? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("population")
    val population: Int? = null,
    @SerialName("sunrise")
    val sunrise: Int? = null,
    @SerialName("sunset")
    val sunset: Int? = null,
    @SerialName("timezone")
    val timezone: Int? = null,
)