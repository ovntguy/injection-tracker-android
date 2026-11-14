package com.deeeelay.injectiontracker.domain

import com.deeeelay.injectiontracker.domain.NextShotRelative.Companion.of
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

/** Item 16 table: calendar days in local time, whole hours/minutes rounded down. */
class NextShotRelativeTest {
    private val zone = ZoneId.of("America/Chicago")
    private val now = ZonedDateTime.of(2026, 11, 14, 10, 0, 0, 0, zone).toInstant()
    private fun at(d: Int, h: Int, m: Int = 0) = ZonedDateTime.of(2026, 11, d, h, m, 0, 0, zone).toInstant()

    @Test
    fun twoOrMoreCalendarDaysOut() {
        assertThat(of(at(21, 17, 50), now, zone)).isEqualTo(NextShotRelative.InDays(7))
        // 23:30 the day after tomorrow is still 2 calendar days, not "tomorrow".
        assertThat(of(at(16, 0, 30), now, zone)).isEqualTo(NextShotRelative.InDays(2))
    }

    @Test
    fun tomorrowByCalendarDayEvenIfUnder24h() {
        assertThat(of(at(15, 0, 15), now, zone)).isEqualTo(NextShotRelative.Tomorrow)
        assertThat(of(at(15, 23, 59), now, zone)).isEqualTo(NextShotRelative.Tomorrow)
    }

    @Test
    fun laterToday() {
        assertThat(of(at(14, 17, 50), now, zone)).isEqualTo(NextShotRelative.TodayInHours(7))
        assertThat(of(at(14, 11, 0), now, zone)).isEqualTo(NextShotRelative.TodayInHours(1))
        assertThat(of(at(14, 10, 59), now, zone)).isEqualTo(NextShotRelative.InMinutes(59))
        assertThat(of(now.plus(Duration.ofSeconds(61)), now, zone)).isEqualTo(NextShotRelative.InMinutes(1))
    }

    @Test
    fun dueNowWithinOneMinuteEitherWay() {
        assertThat(of(now, now, zone)).isEqualTo(NextShotRelative.DueNow)
        assertThat(of(now.plusSeconds(60), now, zone)).isEqualTo(NextShotRelative.DueNow)
        assertThat(of(now.minusSeconds(60), now, zone)).isEqualTo(NextShotRelative.DueNow)
        assertThat(of(now, now, zone).overdue).isFalse()
    }

    @Test
    fun overdueSameDay() {
        val m = of(at(14, 9, 30), now, zone)
        assertThat(m).isEqualTo(NextShotRelative.OverdueByMinutes(30))
        assertThat(m.overdue).isTrue()
        assertThat(of(at(14, 7, 10), now, zone)).isEqualTo(NextShotRelative.OverdueByHours(2))
    }

    @Test
    fun overdueByCalendarDays() {
        assertThat(of(at(13, 23, 0), now, zone)).isEqualTo(NextShotRelative.DaysOverdue(1))
        val two = of(at(12, 10, 0), now, zone)
        assertThat(two).isEqualTo(NextShotRelative.DaysOverdue(2))
        assertThat(two.overdue).isTrue()
    }

    @Test
    fun usesLocalCalendarDaysNotUtc() {
        // 23:30 CST on Nov 14 is Nov 15 in UTC, but it's still "today" locally.
        assertThat(of(at(14, 23, 30), now, zone)).isEqualTo(NextShotRelative.TodayInHours(13))
    }
}
