package com.emikhalets.miniweather.core.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.emikhalets.miniweather.data.LocationSource
import com.emikhalets.miniweather.data.local.AppDatabase
import com.emikhalets.miniweather.data.local.CityDao
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
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "Weather.db")
            .build()
    }

    @Provides
    @Singleton
    fun provideCitiesDao(database: AppDatabase): CityDao {
        return database.cityDao()
    }

    @Provides
    @Singleton
    fun provideCityIndex(
        @ApplicationContext context: Context,
        prefs: SharedPreferences,
        citiesDao: CityDao,
    ): CityIndex {
        return CityIndex(context, prefs, citiesDao)
    }
}