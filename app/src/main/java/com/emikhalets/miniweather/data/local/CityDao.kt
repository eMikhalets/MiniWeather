package com.emikhalets.miniweather.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CityDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<CityDb>)

    @Query(
        """
        SELECT name_ru FROM cities
        WHERE name_ru LIKE :query || '%' ESCAPE '\'
        ORDER BY (pop IS NULL), pop DESC
        LIMIT :limit
        """
    )
    suspend fun suggestPrefix(query: String, limit: Int): List<String>
}