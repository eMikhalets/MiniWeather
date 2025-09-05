package com.emikhalets.miniweather.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emikhalets.miniweather.core.LoadState
import com.emikhalets.miniweather.domain.model.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: Repository,
) : ViewModel() {

    private val _uiState: MutableStateFlow<WeatherUiState> = MutableStateFlow(WeatherUiState())
    val uiState get(): StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun setQuery(value: String) {
        _uiState.update {
            it.copy(query = value)
        }
    }

    fun getWeather() {
        val hasData = _uiState.value.weather != null
        loadJob?.cancel()

        _uiState.update {
            normalize(
                it.copy(
                    loading = if (!hasData) LoadState.Loading else LoadState.Idle,
                    refreshing = if (hasData) LoadState.Loading else LoadState.Idle
                )
            )
        }
        loadJob = viewModelScope.launch {
            repository.getByCity(uiState.value.query)
                .onSuccess { data ->
                    _uiState.update {
                        normalize(
                            it.copy(
                                weather = data,
                                loading = LoadState.Idle,
                                refreshing = LoadState.Idle
                            )
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        normalize(
                            it.copy(
                                loading = if (!hasData) {
                                    LoadState.Error(error.message ?: "Ошибка")
                                } else {
                                    LoadState.Idle
                                },
                                refreshing = if (hasData) {
                                    LoadState.Error(error.message ?: "Ошибка")
                                } else {
                                    LoadState.Idle
                                }
                            )
                        )
                    }
                }
        }
    }

    private fun normalize(state: WeatherUiState) = state.copy(
        loading = if (state.weather == null) state.loading else LoadState.Idle,
        refreshing = if (state.weather != null) state.refreshing else LoadState.Idle
    )
}