package com.example.data

import com.example.model.ConfiguredDay
import com.example.model.PeriodEntry
import com.example.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek

class TimetableRepository(private val dao: TimetableDao) {

    val dayConfigs: Flow<List<ConfiguredDay>> = dao.getAllDayConfigs().map { entities ->
        entities.map { entity ->
            ConfiguredDay(
                dayOfWeek = DayOfWeek.of(entity.dayOfWeek),
                displayName = entity.displayName,
                isEnabled = entity.isEnabled,
                periodCount = entity.periodCount,
                orderIndex = entity.orderIndex
            )
        }
    }

    val allPeriods: Flow<List<PeriodEntry>> = dao.getAllPeriods().map { entities ->
        entities.map { entity ->
            PeriodEntry(
                id = entity.id,
                dayOfWeek = DayOfWeek.of(entity.dayOfWeek),
                periodNumber = entity.periodNumber,
                subject = entity.subject,
                teacher = entity.teacher,
                room = entity.room,
                startTime = entity.startTime,
                endTime = entity.endTime,
                colorHex = entity.colorHex.ifBlank { "#2563EB" },
                iconName = entity.iconName.ifBlank { "book" }
            )
        }
    }

    val settings: Flow<UserSettings> = dao.getAllPreferences().map { prefs ->
        val prefMap = prefs.associate { it.prefKey to it.prefValue }
        UserSettings(
            isConfigured = prefMap["is_configured"] == "true",
            normalDayPeriods = prefMap["normal_day_periods"]?.toIntOrNull() ?: 7,
            cutoffHour = prefMap["cutoff_hour"]?.toIntOrNull() ?: 17,
            cutoffMinute = prefMap["cutoff_minute"]?.toIntOrNull() ?: 0,
            themeMode = prefMap["theme_mode"] ?: "SYSTEM",
            useLiquidGlassBar = prefMap["use_liquid_glass_bar"] != "false"
        )
    }

    suspend fun setupInitialTimetable(
        normalPeriods: Int,
        days: List<ConfiguredDay>,
        cutoffHour: Int = 17,
        cutoffMinute: Int = 0
    ) {
        // Save Day Configs
        val dayEntities = days.map { day ->
            DayConfigEntity(
                dayOfWeek = day.dayOfWeek.value,
                displayName = day.displayName,
                isEnabled = day.isEnabled,
                periodCount = day.periodCount,
                orderIndex = day.orderIndex
            )
        }
        dao.insertDayConfigs(dayEntities)

        // Initialize periods for all enabled days up to their period count
        val periodEntities = mutableListOf<PeriodEntity>()
        for (day in days) {
            if (day.isEnabled) {
                for (p in 1..day.periodCount) {
                    periodEntities.add(
                        PeriodEntity(
                            dayOfWeek = day.dayOfWeek.value,
                            periodNumber = p,
                            subject = "",
                            teacher = "",
                            room = "",
                            startTime = defaultPeriodStartTime(p),
                            endTime = defaultPeriodEndTime(p),
                            colorHex = defaultPeriodColor(p),
                            iconName = "book"
                        )
                    )
                }
            }
        }
        dao.insertPeriods(periodEntities)

        // Save preferences
        val prefs = listOf(
            AppPreferenceEntity("is_configured", "true"),
            AppPreferenceEntity("normal_day_periods", normalPeriods.toString()),
            AppPreferenceEntity("cutoff_hour", cutoffHour.toString()),
            AppPreferenceEntity("cutoff_minute", cutoffMinute.toString()),
            AppPreferenceEntity("theme_mode", "SYSTEM")
        )
        dao.insertPreferences(prefs)
    }

    suspend fun savePeriod(entry: PeriodEntry) {
        dao.insertPeriod(
            PeriodEntity(
                id = entry.id,
                dayOfWeek = entry.dayOfWeek.value,
                periodNumber = entry.periodNumber,
                subject = entry.subject.trim(),
                teacher = entry.teacher.trim(),
                room = entry.room.trim(),
                startTime = entry.startTime.trim(),
                endTime = entry.endTime.trim(),
                colorHex = entry.colorHex,
                iconName = entry.iconName
            )
        )
    }

    suspend fun clearPeriod(dayOfWeek: DayOfWeek, periodNumber: Int) {
        val existing = dao.getPeriod(dayOfWeek.value, periodNumber)
        if (existing != null) {
            dao.insertPeriod(
                existing.copy(
                    subject = "",
                    teacher = "",
                    room = ""
                )
            )
        } else {
            dao.insertPeriod(
                PeriodEntity(
                    dayOfWeek = dayOfWeek.value,
                    periodNumber = periodNumber,
                    subject = "",
                    teacher = "",
                    room = "",
                    startTime = defaultPeriodStartTime(periodNumber),
                    endTime = defaultPeriodEndTime(periodNumber),
                    colorHex = defaultPeriodColor(periodNumber),
                    iconName = "book"
                )
            )
        }
    }

    suspend fun updateDayConfig(day: ConfiguredDay) {
        dao.insertDayConfig(
            DayConfigEntity(
                dayOfWeek = day.dayOfWeek.value,
                displayName = day.displayName,
                isEnabled = day.isEnabled,
                periodCount = day.periodCount,
                orderIndex = day.orderIndex
            )
        )

        if (day.isEnabled) {
            // Trim any periods beyond the new period count
            dao.deletePeriodsBeyondCount(day.dayOfWeek.value, day.periodCount)

            // Ensure all periods up to periodCount exist
            for (p in 1..day.periodCount) {
                val existing = dao.getPeriod(day.dayOfWeek.value, p)
                if (existing == null) {
                    dao.insertPeriod(
                        PeriodEntity(
                            dayOfWeek = day.dayOfWeek.value,
                            periodNumber = p,
                            subject = "",
                            teacher = "",
                            room = "",
                            startTime = defaultPeriodStartTime(p),
                            endTime = defaultPeriodEndTime(p),
                            colorHex = defaultPeriodColor(p),
                            iconName = "book"
                        )
                    )
                }
            }
        }
    }

    suspend fun updateCutoffTime(hour: Int, minute: Int) {
        dao.insertPreference(AppPreferenceEntity("cutoff_hour", hour.toString()))
        dao.insertPreference(AppPreferenceEntity("cutoff_minute", minute.toString()))
    }

    suspend fun updateThemeMode(themeMode: String) {
        dao.insertPreference(AppPreferenceEntity("theme_mode", themeMode))
    }

    suspend fun updateLiquidGlassBar(enabled: Boolean) {
        dao.insertPreference(AppPreferenceEntity("use_liquid_glass_bar", enabled.toString()))
    }

    suspend fun resetTimetable() {
        dao.clearPeriods()
        dao.clearDayConfigs()
        dao.clearPreferences()
    }

    private fun defaultPeriodStartTime(periodNumber: Int): String {
        val startHour = 8
        val totalMinutes = (startHour * 60 + 30) + (periodNumber - 1) * 55
        val h = (totalMinutes / 60)
        val m = totalMinutes % 60
        val amPm = if (h >= 12) "PM" else "AM"
        val displayHour = if (h > 12) h - 12 else if (h == 0) 12 else h
        return String.format("%02d:%02d %s", displayHour, m, amPm)
    }

    private fun defaultPeriodEndTime(periodNumber: Int): String {
        val startHour = 8
        val totalMinutes = (startHour * 60 + 30) + (periodNumber - 1) * 55 + 45
        val h = (totalMinutes / 60)
        val m = totalMinutes % 60
        val amPm = if (h >= 12) "PM" else "AM"
        val displayHour = if (h > 12) h - 12 else if (h == 0) 12 else h
        return String.format("%02d:%02d %s", displayHour, m, amPm)
    }

    private fun defaultPeriodColor(periodNumber: Int): String {
        val colors = listOf(
            "#2563EB", "#0891B2", "#059669", "#16A34A",
            "#D97706", "#7C3AED", "#EA580C", "#DC2626"
        )
        return colors[(periodNumber - 1) % colors.size]
    }
}
