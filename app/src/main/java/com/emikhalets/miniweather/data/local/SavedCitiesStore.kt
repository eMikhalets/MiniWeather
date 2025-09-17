package com.emikhalets.miniweather.data.local

import android.content.SharedPreferences
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedCitiesStore @Inject constructor(
    private val prefs: SharedPreferences
) {
    private val key = "saved_cities_json"

    private fun loadInternal(): List<String> =
        prefs.getString(key, null)
            ?.let { runCatching { JSONArray(it) }.getOrNull() }
            ?.let { json ->
                buildList {
                    for (i in 0 until json.length()) add(json.getString(i))
                }
            }.orEmpty()

    fun load(): List<String> = loadInternal()

    fun addOrPromote(raw: String, limit: Int = 20): List<String> {
        val city = raw.trim().ifEmpty { return loadInternal() }
        val current = loadInternal().toMutableList()
        current.removeAll { it.equals(city, ignoreCase = true) }
        current.add(0, city)
        val clipped = current.take(limit)
        save(clipped)
        return clipped
    }

    fun remove(city: String): List<String> {
        val list = loadInternal().toMutableList().apply {
            removeAll { it.equals(city, ignoreCase = true) }
        }
        save(list)
        return list
    }

    private fun save(list: List<String>) {
        val arr = JSONArray().apply { list.forEach { put(it) } }
        prefs.edit().putString(key, arr.toString()).apply()
    }
}
