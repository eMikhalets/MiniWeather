package com.emikhalets.miniweather.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emikhalets.miniweather.core.LoadState
import com.emikhalets.miniweather.data.LocationSource
import com.emikhalets.miniweather.domain.model.ForecastModel
import com.emikhalets.miniweather.domain.model.PollutionModel
import com.emikhalets.miniweather.domain.model.Repository
import com.emikhalets.miniweather.domain.model.WeatherModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: Repository,
    private val locationSource: LocationSource,
) : ViewModel() {

    enum class LoadingMode { Idle, Load, Refresh }

    private val _uiState: MutableStateFlow<WeatherUiState> = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> get() = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        _uiState.update { it.copy(savedCities = repository.getSavedCities()) }
    }

    fun consumeRefreshState() {
        _uiState.update { it.copy(refreshing = LoadState.Idle) }
    }

    fun setQuery(value: String) {
        _uiState.update {
            it.copy(query = value)
        }
    }

    // Реквест погоды запускает индикатор загрузки
    fun search() {
        getWeather(LoadingMode.Load)
    }

    // Реквест погоды по координатам запускает индикатор загрузки
    fun searchLocation() {
        getLocation(LoadingMode.Load)
    }

    // Реквест погоды запускает индикатор pull to refresh
    fun refresh() {
        if (uiState.value.location == null) {
            getWeather(LoadingMode.Refresh)
        } else {
            getLocation(LoadingMode.Refresh)
        }
    }

    private fun getWeather(loadingMode: LoadingMode) {
        val query = _uiState.value.query.trim().ifEmpty { return }
        _uiState.update { it.copy(location = null) }

        loadJob?.cancel()
        prepareLoadingState(loadingMode)

        loadJob = viewModelScope.launch {
            supervisorScope {
                val weatherDef = async { repository.getByCity(query) }
                val forecastDef = async { repository.getForecastByCity(query) }
                val pollutionDef = async { repository.getPollutionByCity(query) }

                weatherDef.await()
                    .onSuccess { weather ->
                        handleForecastDef(
                            weather = weather,
                            forecastDef = forecastDef,
                            pollutionDef = pollutionDef,
                            loadingMode = loadingMode,
                            query = query
                        )
                    }
                    .onFailure { error ->
                        forecastDef.cancel()
                        handleError(error, "Ошибка", loadingMode)
                    }
            }
        }
    }

    private fun getLocation(loadingMode: LoadingMode) {
        loadJob?.cancel()
        prepareLoadingState(loadingMode)

        loadJob = viewModelScope.launch {
            locationSource.getLocation()
                .onSuccess { (latitude, longitude) ->
                    _uiState.update { it.copy(location = Pair(latitude, longitude)) }
                    getWeatherByLocation(loadingMode, latitude, longitude)
                }
                .onFailure { error ->
                    handleError(error, "Геолокация недоступна", loadingMode)
                }
        }
    }

    private suspend fun getWeatherByLocation(
        loadingMode: LoadingMode,
        latitude: Double,
        longitude: Double,
    ) {
        supervisorScope {
            val weatherDef = async { repository.getByLocation(latitude, longitude) }
            val forecastDef = async { repository.getForecastByLocation(latitude, longitude) }
            val pollutionDef = async { repository.getPollutionByLocation(latitude, longitude) }

            weatherDef.await()
                .onSuccess { weather ->
                    handleForecastDef(
                        weather = weather,
                        forecastDef = forecastDef,
                        pollutionDef = pollutionDef,
                        loadingMode = loadingMode
                    )
                }
                .onFailure { error ->
                    handleError(error, "Ошибка", loadingMode)
                }
        }
    }

    private suspend fun handleForecastDef(
        weather: WeatherModel,
        forecastDef: Deferred<Result<ForecastModel>>,
        pollutionDef: Deferred<Result<PollutionModel>>,
        loadingMode: LoadingMode,
        query: String = "",
    ) {
        val cities = if (query.isNotBlank()) {
            repository.addOrPromoteCity(query)
        } else {
            uiState.value.savedCities
        }

        forecastDef.await()
            .onSuccess { forecast ->
                handlePollutionDef(
                    weather = weather,
                    forecast = forecast,
                    pollutionDef = pollutionDef,
                    loadingMode = loadingMode,
                    savedCities = cities
                )
            }
            .onFailure { error ->
                handleError(error, "Ошибка", loadingMode)
            }
    }

    private suspend fun handlePollutionDef(
        weather: WeatherModel,
        forecast: ForecastModel,
        pollutionDef: Deferred<Result<PollutionModel>>,
        loadingMode: LoadingMode,
        savedCities: List<String>,
    ) {
        pollutionDef.await()
            .onSuccess { pollution ->
                _uiState.update {
                    it.copy(
                        weather = weather,
                        forecast = forecast,
                        airPollution = pollution,
                        loading = LoadState.Idle,
                        refreshing = LoadState.Idle,
                        savedCities = savedCities,
                    )
                }
            }
            .onFailure { error ->
                handleError(error, "Ошибка", loadingMode)
            }
    }

    private fun handleError(
        error: Throwable,
        defaultError: String,
        loadingMode: LoadingMode,
    ) {
        val state = LoadState.Error(error.message ?: defaultError)
        _uiState.update {
            when (loadingMode) {
                LoadingMode.Load -> {
                    it.copy(loading = state, refreshing = LoadState.Idle)
                }

                LoadingMode.Refresh -> {
                    it.copy(loading = uiState.value.loading, refreshing = state)
                }

                LoadingMode.Idle -> {
                    it.copy(loading = LoadState.Idle, refreshing = LoadState.Idle)
                }
            }
        }
    }

    private fun prepareLoadingState(loadingMode: LoadingMode) {
        val (loadingState, refreshState) = when (loadingMode) {
            LoadingMode.Load -> LoadState.Loading to LoadState.Idle
            LoadingMode.Refresh -> uiState.value.loading to LoadState.Loading
            LoadingMode.Idle -> LoadState.Idle to LoadState.Idle
        }
        _uiState.update { it.copy(loading = loadingState, refreshing = refreshState) }
    }
}