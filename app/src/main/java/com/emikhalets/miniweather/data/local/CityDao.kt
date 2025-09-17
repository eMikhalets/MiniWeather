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
            SELECT name FROM cities
            WHERE name COLLATE NOCASE LIKE '%' || :query || '%' ESCAPE '\'
            ORDER BY (pop IS NULL), pop DESC
            LIMIT :limit
        """
    )
    suspend fun suggest(query: String, limit: Int): List<String>
}