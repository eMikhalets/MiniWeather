package com.emikhalets.miniweather.ui.weather

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor() : ViewModel() {

    private val _uiState: MutableStateFlow<WeatherUiState> = MutableStateFlow(WeatherUiState())
    val uiState get(): StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun setQuery(value: String) {
        _uiState.update {
            it.copy(query = value)
            // launch network request
        }
    }

    fun getWeather() {
        _uiState.update {
            it
            // launch network request
        }
    }

//    private fun normalize(state: WeatherUiState) = state.copy(
//        loading = if (state.weather == null) state.loading else LoadState.Idle,
//        refreshing = if (state.weather != null) state.refreshing else LoadState.Idle
//    )
}