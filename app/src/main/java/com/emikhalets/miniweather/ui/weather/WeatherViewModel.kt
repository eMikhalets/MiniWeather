package com.emikhalets.miniweather.ui.weather

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor() : ViewModel() {

    private val _uiState: MutableStateFlow<WeatherState> = MutableStateFlow(WeatherState.Loading)
    val uiState get(): StateFlow<WeatherState> = _uiState.asStateFlow()
}