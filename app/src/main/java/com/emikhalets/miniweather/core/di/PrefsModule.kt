package com.emikhalets.miniweather.core.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PrefsModule {

    @Provides
    @Singleton
    fun providePrefs(@ApplicationContext ctx: Context): SharedPreferences =
        ctx.getSharedPreferences("mini_weather_prefs", Context.MODE_PRIVATE)
}