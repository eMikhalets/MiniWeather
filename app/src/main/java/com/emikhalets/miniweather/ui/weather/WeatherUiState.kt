package com.emikhalets.miniweather.ui.weather

import com.emikhalets.miniweather.core.LoadState
import com.emikhalets.miniweather.domain.model.WeatherModel

data class WeatherUiState(
    val query: String = "",
    val weather: WeatherModel? = null,
    val loading: LoadState = LoadState.Idle,
    val refreshing: LoadState = LoadState.Idle,
    val savedCities: List<String> = emptyList()
)