package com.emikhalets.miniweather.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("data/2.5/weather")
    suspend fun getWeatherByCity(
        @Query("q") city: String,
    ): WeatherDto

    @GET("data/2.5/weather")
    suspend fun getWeatherByLocation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): WeatherDto

    @GET("data/2.5/forecast")
    suspend fun getForecastByCity(
        @Query("q") city: String,
    ): ForecastDto

    @GET("data/2.5/forecast")
    suspend fun getForecastByLocation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): ForecastDto

    @GET("data/2.5/air_pollution")
    suspend fun getPollutionByLocation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): PollutionDto
}