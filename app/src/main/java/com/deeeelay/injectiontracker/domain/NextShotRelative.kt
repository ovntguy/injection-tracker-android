package com.deeeelay.injectiontracker.domain

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Relative due time for the Home "Next shot" card line 2 (Design polish spec, item 16).
 * Calendar days are counted in local time; hours and minutes are whole units, rounded down.
 */
sealed interface NextShotRelative {
    val overdue: Boolean get() = false

    data class InDays(val days: Long) : NextShotRelative
    data object Tomorrow : NextShotRelative
    data class TodayInHours(val hours: Long) : NextShotRelative
    data class InMinutes(val minutes: Long) : NextShotRelative
    data object DueNow : NextShotRelative
    data class OverdueByHours(val hours: Long) : NextShotRelative { override val overdue get() = true }
    data class OverdueByMinutes(val minutes: Long) : NextShotRelative { override val overdue get() = true }
    data class DaysOverdue(val days: Long) : NextShotRelative { override val overdue get() = true }

    companion object {
        private val DUE_NOW_WINDOW: Duration = Duration.ofMinutes(1)

        fun of(due: Instant, now: Instant, zone: ZoneId): NextShotRelative {
            val delta = Duration.between(now, due)
            if (delta.abs() <= DUE_NOW_WINDOW) return DueNow
            val calendarDays = ChronoUnit.DAYS.between(
                now.atZone(zone).toLocalDate(),
                due.atZone(zone).toLocalDate(),
            )
            return if (!delta.isNegative) {
                when {
                    calendarDays >= 2 -> InDays(calendarDays)
                    calendarDays == 1L -> Tomorrow
                    delta.toHours() >= 1 -> TodayInHours(delta.toHours())
                    else -> InMinutes(delta.toMinutes())
                }
            } else {
                val late = delta.negated()
                when {
                    calendarDays <= -1 -> DaysOverdue(-calendarDays)
                    late.toHours() >= 1 -> OverdueByHours(late.toHours())
                    else -> OverdueByMinutes(late.toMinutes())
                }
            }
        }
    }
}
