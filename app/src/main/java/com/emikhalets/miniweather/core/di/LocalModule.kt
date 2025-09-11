package com.emikhalets.miniweather.core.di

import android.content.Context
import com.emikhalets.miniweather.data.LocationSource
import com.emikhalets.miniweather.data.local.CityIndex
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Provides
    @Singleton
    fun provideLocationSource(
        @ApplicationContext context: Context,
        client: FusedLocationProviderClient,
    ): LocationSource {
        return LocationSource(context, client)
    }

    @Provides
    @Singleton
    fun provideCityIndex(
        @ApplicationContext context: Context,
    ): CityIndex {
        return CityIndex(context)
    }
}