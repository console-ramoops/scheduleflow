package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.TimetableRepository
import com.example.logic.DaySelectionResult
import com.example.logic.TimetableDaySelector
import com.example.model.ConfiguredDay
import com.example.model.PeriodEntry
import com.example.model.UserSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class ViewFilterMode {
    SMART_AUTO, // Automatically chooses Today or Tomorrow based on cutoff
    TODAY,      // Explicit Today
    TOMORROW,   // Explicit Tomorrow
    SPECIFIC    // Specific day of week selected by user
}

data class TimetableUiState(
    val isConfigured: Boolean = false,
    val isLoading: Boolean = true,
    val settings: UserSettings = UserSettings(),
    val dayConfigs: List<ConfiguredDay> = emptyList(),
    val allPeriods: List<PeriodEntry> = emptyList(),
    val currentDateTime: LocalDateTime = LocalDateTime.now(),
    val filterMode: ViewFilterMode = ViewFilterMode.SMART_AUTO,
    val specificDay: DayOfWeek = DayOfWeek.MONDAY,
    val editingPeriod: PeriodEntry? = null,
    val isEditorOpen: Boolean = false
) {
    val enabledDays: List<ConfiguredDay>
        get() = dayConfigs.filter { it.isEnabled }

    val autoSelection: DaySelectionResult?
        get() = TimetableDaySelector.selectDay(
            currentDateTime = currentDateTime,
            configuredDays = dayConfigs,
            cutoffTime = settings.cutoffTime
        )

    val activeDayOfWeek: DayOfWeek
        get() = when (filterMode) {
            ViewFilterMode.SMART_AUTO -> autoSelection?.targetDayOfWeek ?: DayOfWeek.MONDAY
            ViewFilterMode.TODAY -> currentDateTime.dayOfWeek
            ViewFilterMode.TOMORROW -> currentDateTime.toLocalDate().plusDays(1).dayOfWeek
            ViewFilterMode.SPECIFIC -> specificDay
        }

    val activeDate: LocalDate
        get() = when (filterMode) {
            ViewFilterMode.SMART_AUTO -> autoSelection?.targetDate ?: currentDateTime.toLocalDate()
            ViewFilterMode.TODAY -> currentDateTime.toLocalDate()
            ViewFilterMode.TOMORROW -> currentDateTime.toLocalDate().plusDays(1)
            ViewFilterMode.SPECIFIC -> {
                // Calculate date of specific day relative to current week
                val today = currentDateTime.toLocalDate()
                val diff = specificDay.value - today.dayOfWeek.value
                today.plusDays(diff.toLong())
            }
        }

    val headerTitle: String
        get() = when (filterMode) {
            ViewFilterMode.SMART_AUTO -> autoSelection?.viewLabel ?: "Timetable"
            ViewFilterMode.TODAY -> "Today's Timetable"
            ViewFilterMode.TOMORROW -> "Tomorrow's Timetable"
            ViewFilterMode.SPECIFIC -> {
                val dayConfig = dayConfigs.find { it.dayOfWeek == specificDay }
                val name = dayConfig?.displayName ?: ConfiguredDay.defaultDisplayName(specificDay)
                "$name's Timetable"
            }
        }

    val activeDayConfig: ConfiguredDay?
        get() = dayConfigs.find { it.dayOfWeek == activeDayOfWeek }

    val periodsForActiveDay: List<PeriodEntry>
        get() {
            val maxPeriods = activeDayConfig?.periodCount ?: 7
            val entriesForDay = allPeriods.filter { it.dayOfWeek == activeDayOfWeek }
            // Ensure every period from 1..maxPeriods is represented
            return (1..maxPeriods).map { pNum ->
                entriesForDay.find { it.periodNumber == pNum } ?: PeriodEntry(
                    dayOfWeek = activeDayOfWeek,
                    periodNumber = pNum
                )
            }
        }
}

private data class UiControls(
    val currentDateTime: LocalDateTime,
    val filterMode: ViewFilterMode,
    val specificDay: DayOfWeek,
    val editingPeriod: PeriodEntry?,
    val isEditorOpen: Boolean
)

class TimetableViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimetableRepository
    private val _currentDateTime = MutableStateFlow(LocalDateTime.now())
    private val _filterMode = MutableStateFlow(ViewFilterMode.SMART_AUTO)
    private val _specificDay = MutableStateFlow(DayOfWeek.MONDAY)
    private val _editingPeriod = MutableStateFlow<PeriodEntry?>(null)
    private val _isEditorOpen = MutableStateFlow(false)

    val uiState: StateFlow<TimetableUiState>

    init {
        val database = AppDatabase.getInstance(application)
        repository = TimetableRepository(database.timetableDao())

        val repoFlow = combine(
            repository.settings,
            repository.dayConfigs,
            repository.allPeriods
        ) { settings, dayConfigs, allPeriods ->
            Triple(settings, dayConfigs, allPeriods)
        }

        val controlsFlow = combine(
            _currentDateTime,
            _filterMode,
            _specificDay,
            _editingPeriod,
            _isEditorOpen
        ) { currentDt, mode, specificDay, editingEntry, isOpen ->
            UiControls(currentDt, mode, specificDay, editingEntry, isOpen)
        }

        uiState = combine(repoFlow, controlsFlow) { (settings, dayConfigs, allPeriods), controls ->
            TimetableUiState(
                isConfigured = settings.isConfigured,
                isLoading = false,
                settings = settings,
                dayConfigs = dayConfigs,
                allPeriods = allPeriods,
                currentDateTime = controls.currentDateTime,
                filterMode = controls.filterMode,
                specificDay = controls.specificDay,
                editingPeriod = controls.editingPeriod,
                isEditorOpen = controls.isEditorOpen
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TimetableUiState(isLoading = true)
        )

        // Clock timer ticker to automatically update time and evaluate cutoffs/date changes
        viewModelScope.launch {
            while (isActive) {
                _currentDateTime.value = LocalDateTime.now()
                delay(15000) // Update every 15s to keep clock current and trigger 5pm cutoff seamlessly
            }
        }
    }

    fun completeSetup(normalPeriods: Int, days: List<ConfiguredDay>) {
        viewModelScope.launch {
            repository.setupInitialTimetable(
                normalPeriods = normalPeriods,
                days = days
            )
            _filterMode.value = ViewFilterMode.SMART_AUTO
        }
    }

    fun setFilterMode(mode: ViewFilterMode) {
        _filterMode.value = mode
    }

    fun selectSpecificDay(day: DayOfWeek) {
        _specificDay.value = day
        _filterMode.value = ViewFilterMode.SPECIFIC
    }

    fun openPeriodEditor(period: PeriodEntry) {
        _editingPeriod.value = period
        _isEditorOpen.value = true
    }

    fun closePeriodEditor() {
        _isEditorOpen.value = false
        _editingPeriod.value = null
    }

    fun savePeriod(period: PeriodEntry) {
        viewModelScope.launch {
            repository.savePeriod(period)
            closePeriodEditor()
        }
    }

    fun clearPeriod(dayOfWeek: DayOfWeek, periodNumber: Int) {
        viewModelScope.launch {
            repository.clearPeriod(dayOfWeek, periodNumber)
            closePeriodEditor()
        }
    }

    fun updateCutoffTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.updateCutoffTime(hour, minute)
        }
    }

    fun updateDayConfig(day: ConfiguredDay) {
        viewModelScope.launch {
            repository.updateDayConfig(day)
        }
    }

    fun updateTheme(themeMode: String) {
        viewModelScope.launch {
            repository.updateThemeMode(themeMode)
        }
    }

    fun updateLiquidGlassBar(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateLiquidGlassBar(enabled)
        }
    }

    fun updateMonet(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateMonetTheming(enabled)
        }
    }

    fun resetTimetable() {
        viewModelScope.launch {
            repository.resetTimetable()
            _filterMode.value = ViewFilterMode.SMART_AUTO
        }
    }
}
