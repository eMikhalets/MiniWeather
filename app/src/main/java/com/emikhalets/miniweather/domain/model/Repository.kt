package com.emikhalets.miniweather.domain.model

interface Repository {

    suspend fun getByCity(city: String): Result<WeatherModel>

    suspend fun getByLocation(latitude: Double, longitude: Double): Result<WeatherModel>

    fun getSavedCities(): List<String>

    fun addOrPromoteCity(value: String): List<String>

    fun promoteCity(value: String): List<String>
}