package com.emikhalets.miniweather.domain.model

interface Repository {

    // Current weather

    suspend fun getByCity(city: String): Result<WeatherModel>

    suspend fun getByLocation(latitude: Double, longitude: Double): Result<WeatherModel>

    // Forecast weather

    suspend fun getForecastByCity(city: String): Result<ForecastModel>

    suspend fun getForecastByLocation(latitude: Double, longitude: Double): Result<ForecastModel>

    // Air pollution

    suspend fun getPollutionByLocation(latitude: Double, longitude: Double): Result<PollutionModel>

    // Local cities

    suspend fun getSavedCities(): List<String>

    suspend fun addOrPromoteCity(value: String): List<String>

    suspend fun promoteCity(value: String): List<String>

    suspend fun searchCities(query: String, limit: Int = 12): List<String>
}