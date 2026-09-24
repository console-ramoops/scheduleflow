package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {

    // Day configs
    @Query("SELECT * FROM day_configs ORDER BY orderIndex ASC")
    fun getAllDayConfigs(): Flow<List<DayConfigEntity>>

    @Query("SELECT * FROM day_configs WHERE dayOfWeek = :dayOfWeek LIMIT 1")
    suspend fun getDayConfig(dayOfWeek: Int): DayConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayConfigs(configs: List<DayConfigEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayConfig(config: DayConfigEntity)

    // Periods
    @Query("SELECT * FROM periods ORDER BY dayOfWeek ASC, periodNumber ASC")
    fun getAllPeriods(): Flow<List<PeriodEntity>>

    @Query("SELECT * FROM periods WHERE dayOfWeek = :dayOfWeek ORDER BY periodNumber ASC")
    fun getPeriodsForDay(dayOfWeek: Int): Flow<List<PeriodEntity>>

    @Query("SELECT * FROM periods WHERE dayOfWeek = :dayOfWeek AND periodNumber = :periodNumber LIMIT 1")
    suspend fun getPeriod(dayOfWeek: Int, periodNumber: Int): PeriodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriod(period: PeriodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriods(periods: List<PeriodEntity>)

    @Query("DELETE FROM periods WHERE dayOfWeek = :dayOfWeek AND periodNumber = :periodNumber")
    suspend fun deletePeriod(dayOfWeek: Int, periodNumber: Int)

    @Query("DELETE FROM periods WHERE dayOfWeek = :dayOfWeek AND periodNumber > :maxPeriod")
    suspend fun deletePeriodsBeyondCount(dayOfWeek: Int, maxPeriod: Int)

    // App Preferences
    @Query("SELECT * FROM app_preferences")
    fun getAllPreferences(): Flow<List<AppPreferenceEntity>>

    @Query("SELECT * FROM app_preferences WHERE prefKey = :key LIMIT 1")
    suspend fun getPreference(key: String): AppPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(pref: AppPreferenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(prefs: List<AppPreferenceEntity>)

    @Transaction
    @Query("DELETE FROM periods")
    suspend fun clearPeriods()

    @Transaction
    @Query("DELETE FROM day_configs")
    suspend fun clearDayConfigs()

    @Transaction
    @Query("DELETE FROM app_preferences")
    suspend fun clearPreferences()
}
