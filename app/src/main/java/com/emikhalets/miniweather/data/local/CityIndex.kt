package com.emikhalets.miniweather.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.emikhalets.miniweather.domain.model.CityModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CityIndex @Inject constructor(
    private val context: Context,
    private val prefs: SharedPreferences,
    private val cityDao: CityDao,
) {
    private val savedKey = "cities_saved"

    suspend fun suggestions(query: String, limit: Int = 12): List<String> {
        if (query.isBlank()) return emptyList()
        saveIfNeeded()
        return cityDao.suggestPrefix(query, limit)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun saveIfNeeded() {
        if (prefs.getBoolean(savedKey, false)) return
        withContext(Dispatchers.IO) {
            context.assets.open("cities_ru.json").use { input ->
                val json = Json { ignoreUnknownKeys = true }
                val list = json.decodeFromStream<List<CityModel>>(input)
                list.chunked(1000).forEach { chunk ->
                    cityDao.insertAll(chunk.map { CityDb(0, it.name, it.pop) })
                }
            }
            prefs.edit { putBoolean(savedKey, true) }
        }
    }
}