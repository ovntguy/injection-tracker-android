package com.deeeelay.injectiontracker.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deeeelay.injectiontracker.data.repo.TrackerRepository
import com.deeeelay.injectiontracker.domain.InjectionLog
import com.deeeelay.injectiontracker.domain.LogType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val marksByDate: Map<LocalDate, DayMarks> = emptyMap(),
    val selectedEntries: List<CalendarEntry> = emptyList(),
)

data class DayMarks(
    val hasCompleted: Boolean = false,
    val hasMissed: Boolean = false,
    val hasUpcoming: Boolean = false,
)

sealed class CalendarEntry {
    abstract val at: Instant

    data class Completed(val log: InjectionLog) : CalendarEntry() {
        override val at: Instant get() = log.loggedAt
    }

    data class Missed(val log: InjectionLog) : CalendarEntry() {
        override val at: Instant get() = log.loggedAt
    }

    data class Upcoming(override val at: Instant) : CalendarEntry()
}

class CalendarViewModel(
    repository: TrackerRepository,
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    private val selected = MutableStateFlow(LocalDate.now())

    val state: StateFlow<CalendarUiState> = combine(
        repository.snapshot,
        month,
        selected,
    ) { snap, currentMonth, selectedDate ->
        val zone = ZoneId.systemDefault()
        val entries = mutableListOf<CalendarEntry>()
        snap.logs.forEach { log ->
            entries += when (log.type) {
                LogType.DONE -> CalendarEntry.Completed(log)
                LogType.MISSED -> CalendarEntry.Missed(log)
            }
        }
        snap.regimen?.nextInjectionAt?.let { entries += CalendarEntry.Upcoming(it) }

        val marks = mutableMapOf<LocalDate, DayMarks>()
        entries.forEach { entry ->
            val date = entry.at.atZone(zone).toLocalDate()
            val current = marks[date] ?: DayMarks()
            marks[date] = when (entry) {
                is CalendarEntry.Completed -> current.copy(hasCompleted = true)
                is CalendarEntry.Missed -> current.copy(hasMissed = true)
                is CalendarEntry.Upcoming -> current.copy(hasUpcoming = true)
            }
        }
        CalendarUiState(
            month = currentMonth,
            selectedDate = selectedDate,
            marksByDate = marks,
            selectedEntries = entries
                .filter { it.at.atZone(zone).toLocalDate() == selectedDate }
                .sortedBy { it.at },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun previousMonth() = month.update { it.minusMonths(1) }
    fun nextMonth() = month.update { it.plusMonths(1) }
    fun selectDate(date: LocalDate) = selected.update { date }
}
