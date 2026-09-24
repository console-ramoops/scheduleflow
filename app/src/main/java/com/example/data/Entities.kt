package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "day_configs")
data class DayConfigEntity(
    @PrimaryKey val dayOfWeek: Int, // 1 (Monday) .. 7 (Sunday)
    val displayName: String,
    val isEnabled: Boolean,
    val periodCount: Int,
    val orderIndex: Int
)

@Entity(
    tableName = "periods",
    indices = [Index(value = ["dayOfWeek", "periodNumber"], unique = true)]
)
data class PeriodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int, // 1..7
    val periodNumber: Int, // 1..N
    val subject: String = "",
    val teacher: String = "",
    val room: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val colorHex: String = "#2563EB",
    val iconName: String = "book"
)

@Entity(tableName = "app_preferences")
data class AppPreferenceEntity(
    @PrimaryKey val prefKey: String,
    val prefValue: String
)
