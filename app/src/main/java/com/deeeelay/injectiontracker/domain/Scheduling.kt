package com.deeeelay.injectiontracker.domain

import java.time.Duration
import java.time.Instant

object Scheduling {
    fun interval(frequency: Frequency): Duration =
        Duration.ofDays(frequency.intervalDays)

    /** After a done log, next due is the log timestamp plus the interval. */
    fun nextAfterTerminalLog(loggedAt: Instant, frequency: Frequency): Instant =
        loggedAt.plus(interval(frequency))

    /**
     * After marking the dose due at [due] as missed, the schedule keeps its cadence: next due is
     * [due] plus the interval, advanced by whole intervals until it is after [now]. Marking
     * early or late therefore never shifts later doses.
     */
    fun nextAfterMissed(due: Instant, now: Instant, frequency: Frequency): Instant {
        val step = interval(frequency)
        var next = due.plus(step)
        while (!next.isAfter(now)) next = next.plus(step)
        return next
    }

    /**
     * Upcoming after a frequency or anchor (start date/time) edit. Past logs are not rewritten.
     *
     * - No terminal log yet: upcoming is the (possibly new) start date/time.
     * - Anchor changed: upcoming is the first slot on the new anchor's cadence
     *   (start + k × interval, k ≥ 0) after the last log, so "my shots are on Thursdays" works.
     * - Only the frequency changed: upcoming is last log + the new interval.
     */
    fun nextAfterRegimenEdit(
        startDateTime: Instant,
        lastTerminalLogAt: Instant?,
        frequency: Frequency,
        anchorChanged: Boolean = false,
    ): Instant {
        if (lastTerminalLogAt == null) return startDateTime
        if (!anchorChanged) return lastTerminalLogAt.plus(interval(frequency))
        if (startDateTime.isAfter(lastTerminalLogAt)) return startDateTime
        val step = interval(frequency)
        val periods = Duration.between(startDateTime, lastTerminalLogAt).toMillis() / step.toMillis() + 1
        return startDateTime.plus(step.multipliedBy(periods))
    }

    fun dayBeforeReminderAt(nextInjectionAt: Instant): Instant =
        nextInjectionAt.minus(Duration.ofHours(24))

    fun isOverdue(nextInjectionAt: Instant, now: Instant): Boolean =
        nextInjectionAt.isBefore(now)
}
