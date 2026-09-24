package com.example.logic

import com.example.model.ConfiguredDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class DaySelectionResult(
    val targetDate: LocalDate,
    val targetDayOfWeek: DayOfWeek,
    val isToday: Boolean,
    val isTomorrow: Boolean,
    val viewLabel: String, // "Today's Timetable", "Tomorrow's Timetable", or "${DayName}'s Timetable"
    val reasonDescription: String
)

object TimetableDaySelector {

    /**
     * Determines which day's timetable to display based on current date/time,
     * configured school days, and the cutoff time.
     *
     * Rules:
     * - Before cutoff time:
     *   - If today is a configured day: show today ("Today's Timetable").
     *   - If today is NOT a configured day (e.g. Sunday): search forward to find the next
     *     configured day (e.g. Monday), showing "Tomorrow's Timetable" or "${DayName}'s Timetable".
     * - At or after cutoff time:
     *   - Look at tomorrow. If tomorrow is a configured day: show tomorrow ("Tomorrow's Timetable").
     *   - If tomorrow is NOT a configured day (e.g. Saturday after cutoff -> Sunday is not a school day):
     *     search forward to find the next configured day (e.g. Monday), showing "${DayName}'s Timetable".
     */
    fun selectDay(
        currentDateTime: LocalDateTime,
        configuredDays: List<ConfiguredDay>,
        cutoffTime: LocalTime = LocalTime.of(17, 0)
    ): DaySelectionResult? {
        val enabledDays = configuredDays.filter { it.isEnabled }
        if (enabledDays.isEmpty()) return null

        val enabledDaysSet = enabledDays.map { it.dayOfWeek }.toSet()
        val todayDate = currentDateTime.toLocalDate()
        val currentTime = currentDateTime.toLocalTime()
        val isBeforeCutoff = currentTime.isBefore(cutoffTime)
        val cutoffFormatted = cutoffTime.format(DateTimeFormatter.ofPattern("h:mm a"))

        if (isBeforeCutoff) {
            // Case 1: Before cutoff
            if (todayDate.dayOfWeek in enabledDaysSet) {
                return DaySelectionResult(
                    targetDate = todayDate,
                    targetDayOfWeek = todayDate.dayOfWeek,
                    isToday = true,
                    isTomorrow = false,
                    viewLabel = "Today's Timetable",
                    reasonDescription = "Before $cutoffFormatted cutoff"
                )
            } else {
                // Today is not configured (e.g. Sunday or non-school day) -> next configured day
                val (nextDate, nextDayOfWeek) = findNextConfiguredDay(todayDate.plusDays(1), enabledDaysSet)
                val isNextTomorrow = nextDate == todayDate.plusDays(1)
                val dayConfig = enabledDays.find { it.dayOfWeek == nextDayOfWeek }
                val dayName = dayConfig?.displayName ?: ConfiguredDay.defaultDisplayName(nextDayOfWeek)
                val label = if (isNextTomorrow) "Tomorrow's Timetable" else "$dayName's Timetable"
                return DaySelectionResult(
                    targetDate = nextDate,
                    targetDayOfWeek = nextDayOfWeek,
                    isToday = false,
                    isTomorrow = isNextTomorrow,
                    viewLabel = label,
                    reasonDescription = "Today is not a scheduled day • Next class on $dayName"
                )
            }
        } else {
            // Case 2: At or after cutoff
            val tomorrowDate = todayDate.plusDays(1)
            if (tomorrowDate.dayOfWeek in enabledDaysSet) {
                return DaySelectionResult(
                    targetDate = tomorrowDate,
                    targetDayOfWeek = tomorrowDate.dayOfWeek,
                    isToday = false,
                    isTomorrow = true,
                    viewLabel = "Tomorrow's Timetable",
                    reasonDescription = "After $cutoffFormatted cutoff • Showing tomorrow"
                )
            } else {
                // Tomorrow is not configured (e.g. Saturday cutoff -> Sunday not configured -> Monday)
                val (nextDate, nextDayOfWeek) = findNextConfiguredDay(tomorrowDate.plusDays(1), enabledDaysSet)
                val dayConfig = enabledDays.find { it.dayOfWeek == nextDayOfWeek }
                val dayName = dayConfig?.displayName ?: ConfiguredDay.defaultDisplayName(nextDayOfWeek)
                return DaySelectionResult(
                    targetDate = nextDate,
                    targetDayOfWeek = nextDayOfWeek,
                    isToday = false,
                    isTomorrow = false,
                    viewLabel = "$dayName's Timetable",
                    reasonDescription = "Tomorrow is not a scheduled day • Next class on $dayName"
                )
            }
        }
    }

    /**
     * Search forward starting from startDate to find the earliest date
     * whose DayOfWeek is in enabledDaysSet.
     */
    fun findNextConfiguredDay(
        startDate: LocalDate,
        enabledDaysSet: Set<DayOfWeek>
    ): Pair<LocalDate, DayOfWeek> {
        var curr = startDate
        for (i in 0..14) {
            if (curr.dayOfWeek in enabledDaysSet) {
                return curr to curr.dayOfWeek
            }
            curr = curr.plusDays(1)
        }
        return startDate to startDate.dayOfWeek
    }
}
