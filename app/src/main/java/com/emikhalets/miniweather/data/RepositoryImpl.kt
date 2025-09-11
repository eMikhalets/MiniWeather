package com.emikhalets.miniweather.data

import com.emikhalets.miniweather.data.local.CityIndex
import com.emikhalets.miniweather.data.local.SavedCitiesStore
import com.emikhalets.miniweather.data.remote.WeatherApi
import com.emikhalets.miniweather.domain.model.ForecastModel
import com.emikhalets.miniweather.domain.model.PollutionModel
import com.emikhalets.miniweather.domain.model.Repository
import com.emikhalets.miniweather.domain.model.WeatherModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi,
    private val citiesStore: SavedCitiesStore,
    private val cityIndex: CityIndex,
) : Repository {

    // TODO тексты ошибок в обработчик в ui
    private suspend inline fun <T> invoke(crossinline block: suspend () -> T): Result<T> =
        runCatching { withContext(Dispatchers.IO) { block() } }
            .fold(
                onSuccess = { Result.success(it) },
                onFailure = { exception ->
                    val message = when (exception) {
                        is HttpException -> when (exception.code()) {
                            401 -> "Неверный API ключ"
                            404 -> "Город не найден"
                            else -> "Серверная ошибка (${exception.code()})"
                        }

                        is IOException -> "Проблемы с сетью"
                        else -> exception.message ?: "Неизвестная ошибка"
                    }
                    Result.failure(Exception(message, exception))
                }
            )

    override suspend fun getByCity(city: String): Result<WeatherModel> {
        return invoke { weatherApi.getWeatherByCity(city.trim()).mapToModel() }
    }

    override suspend fun getByLocation(
        latitude: Double,
        longitude: Double,
    ): Result<WeatherModel> {
        return invoke { weatherApi.getWeatherByLocation(latitude, longitude).mapToModel() }
    }

    override suspend fun getForecastByCity(city: String): Result<ForecastModel> {
        return invoke { weatherApi.getForecastByCity(city.trim()).mapToModel() }
    }

    override suspend fun getForecastByLocation(
        latitude: Double,
        longitude: Double,
    ): Result<ForecastModel> {
        return invoke { weatherApi.getForecastByLocation(latitude, longitude).mapToModel() }
    }

    override suspend fun getPollutionByLocation(
        latitude: Double,
        longitude: Double,
    ): Result<PollutionModel> {
        return invoke { weatherApi.getPollutionByLocation(latitude, longitude).mapToModel() }
    }

    override fun getSavedCities(): List<String> {
        return citiesStore.load()
    }

    override fun addOrPromoteCity(value: String): List<String> {
        return citiesStore.addOrPromote(value)
    }

    override fun promoteCity(value: String): List<String> {
        return citiesStore.promote(value)
    }

    override suspend fun searchCities(query: String, limit: Int): List<String> {
        cityIndex.ensureLoaded()
        return withContext(Dispatchers.Default) {
            cityIndex.search(query, limit)
        }
    }
}