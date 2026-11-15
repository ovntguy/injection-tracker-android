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
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val today: LocalDate = LocalDate.now(),
    /** Null after changing to a month other than the current one, until a day is tapped. */
    val selectedDate: LocalDate? = LocalDate.now(),
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

    /** The single next scheduled shot. */
    data class Upcoming(override val at: Instant) : CalendarEntry()
}

/** Pure calendar rules (Design polish spec, P4 item 10), kept out of the ViewModel for tests. */
object CalendarRules {
    /** Every done and missed log, plus the single next shot. */
    fun entries(logs: List<InjectionLog>, nextInjectionAt: Instant?): List<CalendarEntry> =
        logs.map { log ->
            when (log.type) {
                LogType.DONE -> CalendarEntry.Completed(log)
                LogType.MISSED -> CalendarEntry.Missed(log)
            }
        } + listOfNotNull(nextInjectionAt?.let { CalendarEntry.Upcoming(it) })

    fun marks(entries: List<CalendarEntry>, zone: ZoneId): Map<LocalDate, DayMarks> {
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
        return marks
    }

    /** One row per entry on [date], oldest first. */
    fun entriesOn(date: LocalDate?, entries: List<CalendarEntry>, zone: ZoneId): List<CalendarEntry> =
        if (date == null) emptyList()
        else entries.filter { it.at.atZone(zone).toLocalDate() == date }.sortedBy { it.at }

    /** Changing month clears the selection; returning to the current month reselects today. */
    fun selectionAfterMonthChange(month: YearMonth, today: LocalDate): LocalDate? =
        if (month == YearMonth.from(today)) today else null
}

class CalendarViewModel(
    repository: TrackerRepository,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now(zone))
    private val selected = MutableStateFlow<LocalDate?>(LocalDate.now(zone))

    val state: StateFlow<CalendarUiState> = combine(
        repository.snapshot,
        month,
        selected,
    ) { snap, currentMonth, selectedDate ->
        val entries = CalendarRules.entries(snap.logs, snap.regimen?.nextInjectionAt)
        CalendarUiState(
            month = currentMonth,
            today = LocalDate.now(zone),
            selectedDate = selectedDate,
            marksByDate = CalendarRules.marks(entries, zone),
            selectedEntries = CalendarRules.entriesOn(selectedDate, entries, zone),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun previousMonth() = changeMonth(month.value.minusMonths(1))
    fun nextMonth() = changeMonth(month.value.plusMonths(1))
    fun selectDate(date: LocalDate) {
        selected.value = date
    }

    private fun changeMonth(to: YearMonth) {
        month.value = to
        selected.value = CalendarRules.selectionAfterMonthChange(to, LocalDate.now(zone))
    }
}
