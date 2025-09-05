package com.emikhalets.miniweather.domain.model

interface Repository {

    suspend fun getByCity(city: String): Result<WeatherModel>

    suspend fun getByLocation(latitude: Double, longitude: Double): Result<WeatherModel>
}