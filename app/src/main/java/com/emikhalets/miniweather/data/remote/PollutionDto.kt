package com.emikhalets.miniweather.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PollutionDto(
    @SerialName("coord")
    val coordinates: CoordDto? = null,
    @SerialName("list")
    val list: List<PollutionDataDto?>? = null,
)

@Serializable
data class PollutionDataDto(
    @SerialName("components")
    val components: PollutionComponents? = null,
    @SerialName("dt")
    val dt: Int? = null,
    @SerialName("main")
    val main: PollutionMain? = null,
)

@Serializable
data class PollutionComponents(
    @SerialName("co")
    val co: Double? = null,
    @SerialName("nh3")
    val nh3: Double? = null,
    @SerialName("no")
    val no: Double? = null,
    @SerialName("no2")
    val no2: Double? = null,
    @SerialName("o3")
    val o3: Double? = null,
    @SerialName("pm10")
    val pm10: Double? = null,
    @SerialName("pm2_5")
    val pm25: Double? = null,
    @SerialName("so2")
    val so2: Double? = null,
)

@Serializable
data class PollutionMain(
    @SerialName("aqi")
    val aqi: Int? = null,
)