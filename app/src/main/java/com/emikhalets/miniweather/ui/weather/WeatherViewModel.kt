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

    enum class Mode { Idle, Load, Refresh }

    private val _uiState: MutableStateFlow<WeatherUiState> = MutableStateFlow(WeatherUiState())
    val uiState get(): StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        _uiState.update { it.copy(savedCities = repository.getSavedCities()) }
    }

    fun setQuery(value: String) {
        _uiState.update {
            it.copy(query = value)
        }
    }

    // запускает индикатор загрузки
    fun search() {
        getWeather(Mode.Load)
    }

    // запускает индикатор pull to refresh
    fun refresh() {
        getWeather(Mode.Refresh)
    }

    private fun getWeather(loadingMode: Mode) {
        val query = _uiState.value.query.trim().ifEmpty { return }

        loadJob?.cancel()

        val (loadingState, refreshingState) = when (loadingMode) {
            Mode.Load -> LoadState.Loading to LoadState.Idle
            Mode.Refresh -> uiState.value.loading to LoadState.Loading
            Mode.Idle -> LoadState.Idle to LoadState.Idle
        }
        _uiState.update { it.copy(loading = loadingState, refreshing = refreshingState) }

        loadJob = viewModelScope.launch {
            repository.getByCity(uiState.value.query)
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            weather = data,
                            loading = LoadState.Idle,
                            refreshing = LoadState.Idle,
                            savedCities = repository.addOrPromoteCity(query)
                        )
                    }
                }
                .onFailure { error ->
                    val state = LoadState.Error(error.message ?: "Ошибка")
                    _uiState.update {
                        when (loadingMode) {
                            Mode.Load -> {
                                it.copy(loading = state, refreshing = LoadState.Idle)
                            }

                            Mode.Refresh -> {
                                it.copy(loading = uiState.value.loading, refreshing = state)
                            }

                            Mode.Idle -> {
                                it.copy(loading = LoadState.Idle, refreshing = LoadState.Idle)
                            }
                        }
                    }
                }
        }
    }
}