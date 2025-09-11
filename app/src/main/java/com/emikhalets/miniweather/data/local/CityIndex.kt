package com.emikhalets.miniweather.data.local

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CityIndex @Inject constructor(
    private val context: Context,
) {
    private val namesRef = AtomicReference<List<String>>(emptyList())
    private val normRef = AtomicReference<List<String>>(emptyList())

    suspend fun ensureLoaded(dispatcher: CoroutineDispatcher = Dispatchers.IO) {
        if (namesRef.get().isNotEmpty()) return
        withContext(dispatcher) {
            if (namesRef.get().isNotEmpty()) return@withContext
            val list = context.assets.open("cities_ru.txt")
                .bufferedReader(Charsets.UTF_8)
                .useLines { seq ->
                    seq.map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .toList()
                }
            val normalized = list.map(::normalize)
            namesRef.set(list)
            normRef.set(normalized)
        }
    }

    /**
     * Поиск по prefix/substring (без диакритики, регистронезависимый).
     */
    fun search(query: String, limit: Int = 12): List<String> {
        val q = normalize(query)
        if (q.isBlank()) return emptyList()
        val names = namesRef.get()
        val norms = normRef.get()
        val out = ArrayList<String>(limit)
        for (i in norms.indices) {
            val n = norms[i]
            if (n.startsWith(q) || n.contains(q)) {
                out += names[i]
                if (out.size >= limit) break
            }
        }
        return out
    }

    private fun normalize(s: String): String {
        val noMarks = Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return noMarks
            .lowercase()
            .replace('’', '\'')
            .replace('‘', '\'')
            .replace('`', '\'')
            .trim()
    }
}
