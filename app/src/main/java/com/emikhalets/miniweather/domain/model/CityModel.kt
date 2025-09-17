package com.emikhalets.miniweather.domain.model

import kotlinx.serialization.Serializable

// Для парсинга городов из assets
@Serializable
data class CityModel(
    val name: String,
    val pop: Int? = null
)