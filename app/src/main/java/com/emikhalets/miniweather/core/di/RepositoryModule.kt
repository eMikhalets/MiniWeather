package com.emikhalets.miniweather.core.di

import com.emikhalets.miniweather.data.RepositoryImpl
import com.emikhalets.miniweather.data.local.SavedCitiesStore
import com.emikhalets.miniweather.data.remote.WeatherApi
import com.emikhalets.miniweather.domain.model.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRepository(
        weatherApi: WeatherApi,
        citiesStore: SavedCitiesStore,
    ): Repository {
        return RepositoryImpl(weatherApi, citiesStore)
    }
}