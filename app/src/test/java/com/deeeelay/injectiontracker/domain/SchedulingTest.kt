package com.deeeelay.injectiontracker.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class SchedulingTest {
    private val t0: Instant =
        LocalDateTime.of(2026, 9, 4, 8, 0).toInstant(ZoneOffset.UTC)

    @Test
    fun weeklyIntervalIsSevenDays() {
        assertThat(Scheduling.interval(Frequency.EVERY_WEEK)).isEqualTo(Duration.ofDays(7))
    }

    @Test
    fun everyOtherWeekIntervalIsFourteenDays() {
        assertThat(Scheduling.interval(Frequency.EVERY_OTHER_WEEK)).isEqualTo(Duration.ofDays(14))
    }

    @Test
    fun doneLogSchedulesNextFromLogTimeNotOriginalDue() {
        val originalDue = t0
        val loggedEarly = t0.minus(Duration.ofDays(1))
        val next = Scheduling.nextAfterTerminalLog(loggedEarly, Frequency.EVERY_WEEK)
        assertThat(next).isEqualTo(loggedEarly.plus(Duration.ofDays(7)))
        assertThat(next).isNotEqualTo(originalDue.plus(Duration.ofDays(7)))
    }

    @Test
    fun lateDoneLogSchedulesFromLateTimestamp() {
        val loggedLate = t0.plus(Duration.ofHours(36))
        val next = Scheduling.nextAfterTerminalLog(loggedLate, Frequency.EVERY_WEEK)
        assertThat(next).isEqualTo(loggedLate.plus(Duration.ofDays(7)))
    }

    @Test
    fun missedLogUsesMissTimeNotOriginalDue() {
        val originalDue = t0
        val missLoggedAt = t0.plus(Duration.ofDays(2))
        val next = Scheduling.nextAfterTerminalLog(missLoggedAt, Frequency.EVERY_OTHER_WEEK)
        assertThat(next).isEqualTo(missLoggedAt.plus(Duration.ofDays(14)))
        assertThat(next).isNotEqualTo(originalDue.plus(Duration.ofDays(14)))
    }

    @Test
    fun regimenEditWithNoLogsUsesStartDateTime() {
        val start = t0.plus(Duration.ofDays(3))
        val next = Scheduling.nextAfterRegimenEdit(
            startDateTime = start,
            lastTerminalLogAt = null,
            frequency = Frequency.EVERY_WEEK,
        )
        assertThat(next).isEqualTo(start)
    }

    @Test
    fun regimenFrequencyEditWithLogsUsesLastLogPlusNewInterval() {
        val lastLog = t0
        val nextWeekly = Scheduling.nextAfterRegimenEdit(t0.minus(Duration.ofDays(30)), lastLog, Frequency.EVERY_WEEK)
        val nextBiweekly = Scheduling.nextAfterRegimenEdit(t0.minus(Duration.ofDays(30)), lastLog, Frequency.EVERY_OTHER_WEEK)
        assertThat(nextWeekly).isEqualTo(lastLog.plus(Duration.ofDays(7)))
        assertThat(nextBiweekly).isEqualTo(lastLog.plus(Duration.ofDays(14)))
    }

    @Test
    fun dayBeforeReminderIsSameClockMinus24Hours() {
        val next = LocalDateTime.of(2026, 9, 11, 8, 30).toInstant(ZoneOffset.UTC)
        val reminder = Scheduling.dayBeforeReminderAt(next)
        assertThat(reminder).isEqualTo(next.minus(Duration.ofHours(24)))
        val reminderLocal = reminder.atZone(ZoneOffset.UTC).toLocalTime()
        val nextLocal = next.atZone(ZoneOffset.UTC).toLocalTime()
        assertThat(reminderLocal).isEqualTo(nextLocal)
    }

    @Test
    fun overdueWhenNextIsBeforeNow() {
        val now = t0
        assertThat(Scheduling.isOverdue(t0.minusSeconds(1), now)).isTrue()
        assertThat(Scheduling.isOverdue(t0, now)).isFalse()
        assertThat(Scheduling.isOverdue(t0.plusSeconds(1), now)).isFalse()
    }
}
