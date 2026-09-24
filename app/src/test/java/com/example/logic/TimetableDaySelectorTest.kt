package com.example.logic

import com.example.model.ConfiguredDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TimetableDaySelectorTest {

    // Standard 6-day schedule (Monday to Saturday enabled, Sunday disabled)
    private val standard6Days = listOf(
        ConfiguredDay(DayOfWeek.MONDAY, "Monday", isEnabled = true, periodCount = 7),
        ConfiguredDay(DayOfWeek.TUESDAY, "Tuesday", isEnabled = true, periodCount = 7),
        ConfiguredDay(DayOfWeek.WEDNESDAY, "Wednesday", isEnabled = true, periodCount = 6),
        ConfiguredDay(DayOfWeek.THURSDAY, "Thursday", isEnabled = true, periodCount = 7),
        ConfiguredDay(DayOfWeek.FRIDAY, "Friday", isEnabled = true, periodCount = 7),
        ConfiguredDay(DayOfWeek.SATURDAY, "Saturday", isEnabled = true, periodCount = 5),
        ConfiguredDay(DayOfWeek.SUNDAY, "Sunday", isEnabled = false, periodCount = 0)
    )

    // Standard cutoff: 5:00 PM (17:00)
    private val cutoff5PM = LocalTime.of(17, 0)

    // Reference dates (2026-09-28 is a Monday)
    private val mondayDate = LocalDate.of(2026, 9, 28)
    private val fridayDate = LocalDate.of(2026, 10, 2)
    private val saturdayDate = LocalDate.of(2026, 10, 3)
    private val sundayDate = LocalDate.of(2026, 10, 4)

    @Test
    fun `Monday at 4 59 PM returns Monday timetable`() {
        val dt = LocalDateTime.of(mondayDate, LocalTime.of(16, 59))
        val result = TimetableDaySelector.selectDay(dt, standard6Days, cutoff5PM)

        assertNotNull(result)
        assertEquals(DayOfWeek.MONDAY, result!!.targetDayOfWeek)
        assertEquals(mondayDate, result.targetDate)
        assertTrue(result.isToday)
        assertFalse(result.isTomorrow)
        assertEquals("Today's Timetable", result.viewLabel)
    }

    @Test
    fun `Monday at 5 00 PM returns Tuesday timetable`() {
        val dt = LocalDateTime.of(mondayDate, LocalTime.of(17, 0))
        val result = TimetableDaySelector.selectDay(dt, standard6Days, cutoff5PM)

        assertNotNull(result)
        assertEquals(DayOfWeek.TUESDAY, result!!.targetDayOfWeek)
        assertEquals(mondayDate.plusDays(1), result.targetDate)
        assertFalse(result.isToday)
        assertTrue(result.isTomorrow)
        assertEquals("Tomorrow's Timetable", result.viewLabel)
    }

    @Test
    fun `Friday at 6 00 PM returns Saturday timetable`() {
        val dt = LocalDateTime.of(fridayDate, LocalTime.of(18, 0))
        val result = TimetableDaySelector.selectDay(dt, standard6Days, cutoff5PM)

        assertNotNull(result)
        assertEquals(DayOfWeek.SATURDAY, result!!.targetDayOfWeek)
        assertEquals(saturdayDate, result.targetDate)
        assertFalse(result.isToday)
        assertTrue(result.isTomorrow)
        assertEquals("Tomorrow's Timetable", result.viewLabel)
    }

    @Test
    fun `Saturday after 5 00 PM returns Monday timetable`() {
        val dt = LocalDateTime.of(saturdayDate, LocalTime.of(17, 1))
        val result = TimetableDaySelector.selectDay(dt, standard6Days, cutoff5PM)

        assertNotNull(result)
        // Tomorrow is Sunday (not enabled), next configured day is Monday
        assertEquals(DayOfWeek.MONDAY, result!!.targetDayOfWeek)
        assertEquals(mondayDate.plusDays(7), result.targetDate)
        assertFalse(result.isToday)
        assertFalse(result.isTomorrow)
        assertEquals("Monday's Timetable", result.viewLabel)
    }

    @Test
    fun `Sunday returns Monday timetable`() {
        // Sunday morning (before cutoff)
        val dtMorning = LocalDateTime.of(sundayDate, LocalTime.of(10, 0))
        val resultMorning = TimetableDaySelector.selectDay(dtMorning, standard6Days, cutoff5PM)

        assertNotNull(resultMorning)
        assertEquals(DayOfWeek.MONDAY, resultMorning!!.targetDayOfWeek)
        assertEquals(mondayDate.plusDays(7), resultMorning.targetDate)
        assertFalse(resultMorning.isToday)
        assertTrue(resultMorning.isTomorrow)
        assertEquals("Tomorrow's Timetable", resultMorning.viewLabel)

        // Sunday evening (after cutoff)
        val dtEvening = LocalDateTime.of(sundayDate, LocalTime.of(19, 0))
        val resultEvening = TimetableDaySelector.selectDay(dtEvening, standard6Days, cutoff5PM)

        assertNotNull(resultEvening)
        assertEquals(DayOfWeek.MONDAY, resultEvening!!.targetDayOfWeek)
        assertEquals(mondayDate.plusDays(7), resultEvening.targetDate)
        assertFalse(resultEvening.isToday)
        assertTrue(resultEvening.isTomorrow)
    }

    @Test
    fun `Day with no timetable returns next configured timetable day`() {
        // Schedule where Wednesday is also off (Mon, Tue, Thu, Fri only)
        val customDays = listOf(
            ConfiguredDay(DayOfWeek.MONDAY, "Monday", isEnabled = true, periodCount = 7),
            ConfiguredDay(DayOfWeek.TUESDAY, "Tuesday", isEnabled = true, periodCount = 7),
            ConfiguredDay(DayOfWeek.WEDNESDAY, "Wednesday", isEnabled = false, periodCount = 0),
            ConfiguredDay(DayOfWeek.THURSDAY, "Thursday", isEnabled = true, periodCount = 7),
            ConfiguredDay(DayOfWeek.FRIDAY, "Friday", isEnabled = true, periodCount = 7),
            ConfiguredDay(DayOfWeek.SATURDAY, "Saturday", isEnabled = false, periodCount = 0),
            ConfiguredDay(DayOfWeek.SUNDAY, "Sunday", isEnabled = false, periodCount = 0)
        )

        val tuesdayDate = LocalDate.of(2026, 9, 29)
        // Tuesday evening after cutoff: tomorrow is Wednesday (disabled) -> skips to Thursday!
        val dt = LocalDateTime.of(tuesdayDate, LocalTime.of(17, 30))
        val result = TimetableDaySelector.selectDay(dt, customDays, cutoff5PM)

        assertNotNull(result)
        assertEquals(DayOfWeek.THURSDAY, result!!.targetDayOfWeek)
        assertEquals(tuesdayDate.plusDays(2), result.targetDate)
        assertEquals("Thursday's Timetable", result.viewLabel)

        // Wednesday midday (today is non-school day) -> next configured day is Thursday
        val wedDate = LocalDate.of(2026, 9, 30)
        val wedDt = LocalDateTime.of(wedDate, LocalTime.of(12, 0))
        val wedResult = TimetableDaySelector.selectDay(wedDt, customDays, cutoff5PM)

        assertNotNull(wedResult)
        assertEquals(DayOfWeek.THURSDAY, wedResult!!.targetDayOfWeek)
        assertEquals(wedDate.plusDays(1), wedResult.targetDate)
        assertTrue(wedResult.isTomorrow)
    }

    @Test
    fun `Different period counts per day are preserved`() {
        val monConfig = standard6Days.first { it.dayOfWeek == DayOfWeek.MONDAY }
        val wedConfig = standard6Days.first { it.dayOfWeek == DayOfWeek.WEDNESDAY }
        val satConfig = standard6Days.first { it.dayOfWeek == DayOfWeek.SATURDAY }

        assertEquals(7, monConfig.periodCount)
        assertEquals(6, wedConfig.periodCount)
        assertEquals(5, satConfig.periodCount)
    }

    @Test
    fun `Custom cutoff time behaves accurately`() {
        val customCutoff = LocalTime.of(14, 30) // 2:30 PM cutoff

        // 2:29 PM -> Today (Monday)
        val dtBefore = LocalDateTime.of(mondayDate, LocalTime.of(14, 29))
        val resultBefore = TimetableDaySelector.selectDay(dtBefore, standard6Days, customCutoff)

        assertNotNull(resultBefore)
        assertEquals(DayOfWeek.MONDAY, resultBefore!!.targetDayOfWeek)
        assertTrue(resultBefore.isToday)
        assertEquals("Today's Timetable", resultBefore.viewLabel)

        // 2:30 PM -> Tomorrow (Tuesday)
        val dtAt = LocalDateTime.of(mondayDate, LocalTime.of(14, 30))
        val resultAt = TimetableDaySelector.selectDay(dtAt, standard6Days, customCutoff)

        assertNotNull(resultAt)
        assertEquals(DayOfWeek.TUESDAY, resultAt!!.targetDayOfWeek)
        assertTrue(resultAt.isTomorrow)
        assertEquals("Tomorrow's Timetable", resultAt.viewLabel)
    }
}
