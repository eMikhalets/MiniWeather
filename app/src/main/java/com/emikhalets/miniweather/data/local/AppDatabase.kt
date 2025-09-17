package com.emikhalets.miniweather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CityDb::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cityDao(): CityDao
}