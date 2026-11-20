package com.deeeelay.injectiontracker.domain

import java.time.Duration
import java.time.Instant

object Scheduling {
    fun interval(frequency: Frequency): Duration =
        Duration.ofDays(frequency.intervalDays)

    /** After a done or missed log, next due is the terminal log timestamp plus the interval. */
    fun nextAfterTerminalLog(loggedAt: Instant, frequency: Frequency): Instant =
        loggedAt.plus(interval(frequency))

    /**
     * Upcoming after a frequency or anchor (start date/time) edit. Past logs are not rewritten.
     *
     * - No terminal log yet: upcoming is the (possibly new) start date/time.
     * - Anchor changed: upcoming is the first slot on the new anchor's cadence
     *   (start + k × interval, k ≥ 0) that is at least one full interval after the last log.
     *   The prescribed interval counts from the actual injection, so a dose is never due sooner.
     * - Only the frequency changed: upcoming is last log + the new interval.
     */
    fun nextAfterRegimenEdit(
        startDateTime: Instant,
        lastTerminalLogAt: Instant?,
        frequency: Frequency,
        anchorChanged: Boolean = false,
    ): Instant {
        if (lastTerminalLogAt == null) return startDateTime
        val step = interval(frequency)
        val earliest = lastTerminalLogAt.plus(step)
        if (!anchorChanged) return earliest
        if (!startDateTime.isBefore(earliest)) return startDateTime
        val gapMillis = Duration.between(startDateTime, earliest).toMillis()
        val periods = (gapMillis + step.toMillis() - 1) / step.toMillis()
        return startDateTime.plus(step.multipliedBy(periods))
    }

    fun dayBeforeReminderAt(nextInjectionAt: Instant): Instant =
        nextInjectionAt.minus(Duration.ofHours(24))

    fun isOverdue(nextInjectionAt: Instant, now: Instant): Boolean =
        nextInjectionAt.isBefore(now)
}
