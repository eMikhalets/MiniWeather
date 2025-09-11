package com.emikhalets.miniweather.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cities",
    indices = [Index("name_ru"), Index("pop")]
)
data class CityDb(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name_ru")
    val nameRu: String,
    @ColumnInfo(name = "pop")
    val pop: Int?
)