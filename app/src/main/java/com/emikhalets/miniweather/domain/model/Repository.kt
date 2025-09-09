package com.emikhalets.miniweather.domain.model

interface Repository {

    // Current weather

    suspend fun getByCity(city: String): Result<WeatherModel>

    suspend fun getByLocation(latitude: Double, longitude: Double): Result<WeatherModel>

    // Forecast weather

    suspend fun getForecastByCity(city: String): Result<ForecastModel>

    suspend fun getForecastByLocation(latitude: Double, longitude: Double): Result<ForecastModel>

    // Air pollution

    suspend fun getPollutionByCity(city: String): Result<PollutionModel>

    suspend fun getPollutionByLocation(latitude: Double, longitude: Double): Result<PollutionModel>

    // Saved cities

    fun getSavedCities(): List<String>

    fun addOrPromoteCity(value: String): List<String>

    fun promoteCity(value: String): List<String>
}