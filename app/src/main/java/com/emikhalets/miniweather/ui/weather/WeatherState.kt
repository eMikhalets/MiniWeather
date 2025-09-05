package com.emikhalets.miniweather.ui.weather

import com.emikhalets.miniweather.domain.model.WeatherModel

sealed interface WeatherState {
    data object Empty : WeatherState
    data object Loading : WeatherState
    data class Content(val data: WeatherModel) : WeatherState
    data class Error(val message: String) : WeatherState
}