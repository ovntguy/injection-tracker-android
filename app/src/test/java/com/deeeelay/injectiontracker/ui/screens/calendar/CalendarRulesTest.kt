package com.deeeelay.injectiontracker.ui.screens.calendar

import com.deeeelay.injectiontracker.ui.screens.calendar.CalendarFixtures.entries
import com.deeeelay.injectiontracker.ui.screens.calendar.CalendarFixtures.zone
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File
import java.time.LocalDate
import java.time.YearMonth

/** P4 items 9 and 10. */
class CalendarRulesTest {
    @Test
    fun severalLogsOnOneDayAreOneRowEachOldestFirst() {
        val rows = CalendarRules.entriesOn(LocalDate.of(2026, 11, 7), entries, zone)
        assertThat(rows.map { it::class }).containsExactly(
            CalendarEntry.Missed::class,
            CalendarEntry.Completed::class,
        ).inOrder()
    }

    @Test
    fun onlyTheSingleNextShotIsUpcoming() {
        assertThat(entries.filterIsInstance<CalendarEntry.Upcoming>()).hasSize(1)
        val rows = CalendarRules.entriesOn(LocalDate.of(2026, 11, 21), entries, zone)
        assertThat(rows).containsExactly(CalendarEntry.Upcoming(CalendarFixtures.next))
    }

    @Test
    fun emptyDayAndNoSelectionHaveNoRows() {
        assertThat(CalendarRules.entriesOn(LocalDate.of(2026, 11, 14), entries, zone)).isEmpty()
        assertThat(CalendarRules.entriesOn(null, entries, zone)).isEmpty()
    }

    @Test
    fun marksMatchEntryKinds() {
        val marks = CalendarRules.marks(entries, zone)
        assertThat(marks[LocalDate.of(2026, 11, 7)]).isEqualTo(DayMarks(hasCompleted = true, hasMissed = true))
        assertThat(marks[LocalDate.of(2026, 11, 21)]).isEqualTo(DayMarks(hasUpcoming = true))
    }

    @Test
    fun monthChangeClearsSelectionAndCurrentMonthReselectsToday() {
        val today = CalendarFixtures.today
        assertThat(CalendarRules.selectionAfterMonthChange(YearMonth.of(2026, 12), today)).isNull()
        assertThat(CalendarRules.selectionAfterMonthChange(YearMonth.of(2026, 10), today)).isNull()
        assertThat(CalendarRules.selectionAfterMonthChange(YearMonth.of(2026, 11), today)).isEqualTo(today)
    }

    @Test
    fun dayHeaderFormat() {
        val today = CalendarFixtures.today
        assertThat(CalendarFormat.dayHeader(LocalDate.of(2026, 11, 7), today)).isEqualTo("Saturday, Nov 7")
        assertThat(CalendarFormat.dayHeader(LocalDate.of(2025, 12, 30), today)).isEqualTo("Tuesday, Dec 30, 2025")
    }

    @Test
    fun designStringsAreExact() {
        val xml = listOf("src/main/res/values/strings.xml", "app/src/main/res/values/strings.xml")
            .map(::File).first { it.exists() }.readText()
        fun s(name: String) = Regex("""<string name="$name">([^<]*)</string>""").find(xml)!!.groupValues[1]
        assertThat(s("calendar_done")).isEqualTo("Done")
        assertThat(s("calendar_missed")).isEqualTo("Missed")
        assertThat(s("calendar_upcoming")).isEqualTo("Upcoming")
        assertThat(s("calendar_nothing_logged")).isEqualTo("Nothing logged this day")
        assertThat(s("calendar_row_done")).isEqualTo("Done · %1\$s")
        assertThat(s("calendar_row_missed")).isEqualTo("Marked at %1\$s")
        assertThat(s("calendar_row_next_shot")).isEqualTo("Next shot")
        assertThat(s("calendar_row_upcoming")).isEqualTo("%1\$s · %2\$s")
        assertThat(xml).doesNotContain("Completed circle")
    }
}
