package com.emikhalets.miniweather.data

import com.emikhalets.miniweather.data.remote.WeatherApi
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
) : Repository {

    // TODO тексты ошибок в обработчик в ui
    private suspend inline fun <T> invoke(crossinline block: suspend () -> T): Result<T> =
        runCatching { withContext(Dispatchers.IO) { block() } }
            .fold(
                onSuccess = { Result.success(it) },
                onFailure = { e ->
                    val msg = when (e) {
                        is HttpException -> when (e.code()) {
                            401 -> "Неверный API ключ"
                            404 -> "Город не найден"
                            else -> "Серверная ошибка (${e.code()})"
                        }

                        is IOException -> "Проблемы с сетью"
                        else -> e.message ?: "Неизвестная ошибка"
                    }
                    Result.failure(Exception(msg, e))
                }
            )

    override suspend fun getByCity(city: String): Result<WeatherModel> = invoke {
        weatherApi.getWeatherByCity(city.trim()).mapToModel()
    }

    override suspend fun getByLocation(
        latitude: Double,
        longitude: Double,
    ): Result<WeatherModel> = invoke {
        weatherApi.getWeatherByLocation(latitude, longitude).mapToModel()
    }
}