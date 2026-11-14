package com.deeeelay.injectiontracker.ui.screens.calendar

import com.deeeelay.injectiontracker.domain.InjectionLog
import com.deeeelay.injectiontracker.domain.InjectionSite
import com.deeeelay.injectiontracker.domain.LogType
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime

internal object CalendarFixtures {
    val zone: ZoneId = ZoneId.of("America/Chicago")
    val today: LocalDate = LocalDate.of(2026, 11, 14)
    val now: Instant = ZonedDateTime.of(2026, 11, 14, 10, 0, 0, 0, zone).toInstant()
    private fun at(m: Int, d: Int, h: Int, min: Int) = ZonedDateTime.of(2026, m, d, h, min, 0, 0, zone).toInstant()

    val oct24Done = InjectionLog("d0", LogType.DONE, at(10, 24, 17, 50), InjectionSite.RIGHT_ABDOMEN_MID)
    val nov7Missed = InjectionLog("m1", LogType.MISSED, at(11, 7, 9, 10), null)
    val nov7Done = InjectionLog("d1", LogType.DONE, at(11, 7, 17, 50), InjectionSite.RIGHT_UPPER_THIGH_MID)
    val next: Instant = at(11, 21, 17, 50)

    // Listed out of order on purpose: rows must still come out oldest first.
    val entries = CalendarRules.entries(listOf(nov7Done, oct24Done, nov7Missed), next)

    fun state(month: YearMonth, selected: LocalDate?) = CalendarUiState(
        month = month,
        today = today,
        selectedDate = selected,
        marksByDate = CalendarRules.marks(entries, zone),
        selectedEntries = CalendarRules.entriesOn(selected, entries, zone),
    )
}
