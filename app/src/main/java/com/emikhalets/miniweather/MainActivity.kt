package com.emikhalets.miniweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.emikhalets.miniweather.core.theme.MiniWeatherTheme
import com.emikhalets.miniweather.ui.weather.WeatherScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiniWeatherTheme {
                WeatherScreen()
            }
        }
    }
}