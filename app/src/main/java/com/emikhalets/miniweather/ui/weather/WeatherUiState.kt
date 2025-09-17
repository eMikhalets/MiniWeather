package com.emikhalets.miniweather.ui.weather

import com.emikhalets.miniweather.core.LoadState
import com.emikhalets.miniweather.domain.model.ForecastModel
import com.emikhalets.miniweather.domain.model.PollutionModel
import com.emikhalets.miniweather.domain.model.WeatherModel

data class WeatherUiState(
    val query: String = "",
    val weather: WeatherModel? = null,
    val forecast: ForecastModel? = null,
    val init: Boolean = true,
    val loading: LoadState = LoadState.Idle,
    val refresh: LoadState = LoadState.Idle,
    val savedCities: List<String> = emptyList(),
    val location: Pair<Double, Double>? = null,
    val suggestions: List<String> = emptyList(), // search cities list
    val airPollution: PollutionModel? = null,
)