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
     * Upcoming after a frequency or anchor (start date/time) edit.
     * Past logs are not rewritten. If any terminal log exists, upcoming is last log + interval;
     * otherwise upcoming is the (possibly new) start date/time.
     */
    fun nextAfterRegimenEdit(
        startDateTime: Instant,
        lastTerminalLogAt: Instant?,
        frequency: Frequency,
    ): Instant =
        if (lastTerminalLogAt != null) {
            lastTerminalLogAt.plus(interval(frequency))
        } else {
            startDateTime
        }

    fun dayBeforeReminderAt(nextInjectionAt: Instant): Instant =
        nextInjectionAt.minus(Duration.ofHours(24))

    fun isOverdue(nextInjectionAt: Instant, now: Instant): Boolean =
        nextInjectionAt.isBefore(now)
}
