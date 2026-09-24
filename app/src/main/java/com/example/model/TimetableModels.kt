package com.example.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class ConfiguredDay(
    val dayOfWeek: DayOfWeek,
    val displayName: String = defaultDisplayName(dayOfWeek),
    val isEnabled: Boolean = true,
    val periodCount: Int = 7,
    val orderIndex: Int = dayOfWeek.value
) {
    companion object {
        fun defaultDisplayName(dayOfWeek: DayOfWeek): String =
            when (dayOfWeek) {
                DayOfWeek.MONDAY -> "Monday"
                DayOfWeek.TUESDAY -> "Tuesday"
                DayOfWeek.WEDNESDAY -> "Wednesday"
                DayOfWeek.THURSDAY -> "Thursday"
                DayOfWeek.FRIDAY -> "Friday"
                DayOfWeek.SATURDAY -> "Saturday"
                DayOfWeek.SUNDAY -> "Sunday"
            }

        fun defaultShortName(dayOfWeek: DayOfWeek): String =
            when (dayOfWeek) {
                DayOfWeek.MONDAY -> "Mon"
                DayOfWeek.TUESDAY -> "Tue"
                DayOfWeek.WEDNESDAY -> "Wed"
                DayOfWeek.THURSDAY -> "Thu"
                DayOfWeek.FRIDAY -> "Fri"
                DayOfWeek.SATURDAY -> "Sat"
                DayOfWeek.SUNDAY -> "Sun"
            }
    }
}

data class PeriodEntry(
    val id: Long = 0,
    val dayOfWeek: DayOfWeek,
    val periodNumber: Int,
    val subject: String = "",
    val teacher: String = "",
    val room: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val colorHex: String = "#3B82F6",
    val iconName: String = "book"
) {
    val isAssigned: Boolean get() = subject.isNotBlank()
}

data class UserSettings(
    val isConfigured: Boolean = false,
    val normalDayPeriods: Int = 7,
    val cutoffHour: Int = 17,
    val cutoffMinute: Int = 0,
    val themeMode: String = "SYSTEM" // "LIGHT", "DARK", "SYSTEM"
) {
    val cutoffTime: LocalTime
        get() = LocalTime.of(cutoffHour.coerceIn(0, 23), cutoffMinute.coerceIn(0, 59))
}

data class SubjectPreset(
    val name: String,
    val colorHex: String,
    val iconName: String
)

object TimetablePresets {
    val PRESET_SUBJECTS = listOf(
        SubjectPreset("Mathematics", "#2563EB", "calculator"),
        SubjectPreset("Physics", "#0891B2", "science"),
        SubjectPreset("Chemistry", "#059669", "science"),
        SubjectPreset("Biology", "#16A34A", "science"),
        SubjectPreset("English", "#D97706", "book"),
        SubjectPreset("Computer Science", "#7C3AED", "computer"),
        SubjectPreset("History", "#B45309", "history"),
        SubjectPreset("Geography", "#0D9488", "globe"),
        SubjectPreset("Physical Education", "#DC2626", "sports"),
        SubjectPreset("Art", "#DB2777", "palette"),
        SubjectPreset("Music", "#9333EA", "music"),
        SubjectPreset("Economics", "#4B5563", "chart"),
        SubjectPreset("Free Period / Study", "#64748B", "star")
    )

    val PRESET_COLORS = listOf(
        "#2563EB", // Blue
        "#0891B2", // Cyan
        "#059669", // Emerald
        "#16A34A", // Green
        "#D97706", // Amber
        "#EA580C", // Orange
        "#DC2626", // Red
        "#DB2777", // Pink
        "#7C3AED", // Purple
        "#9333EA", // Violet
        "#0D9488", // Teal
        "#475569"  // Slate
    )

    val PRESET_ICONS = listOf(
        "book",
        "calculator",
        "science",
        "computer",
        "sports",
        "palette",
        "music",
        "globe",
        "history",
        "star"
    )
}
