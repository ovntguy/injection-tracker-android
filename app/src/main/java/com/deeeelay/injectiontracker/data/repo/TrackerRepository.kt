package com.deeeelay.injectiontracker.data.repo

import com.deeeelay.injectiontracker.data.local.InjectionLogDao
import com.deeeelay.injectiontracker.data.local.toDomain
import com.deeeelay.injectiontracker.data.local.toEntity
import com.deeeelay.injectiontracker.data.prefs.TrackerPreferences
import com.deeeelay.injectiontracker.domain.InjectionLog
import com.deeeelay.injectiontracker.domain.InjectionSite
import com.deeeelay.injectiontracker.domain.LogType
import com.deeeelay.injectiontracker.domain.Regimen
import com.deeeelay.injectiontracker.domain.Scheduling
import com.deeeelay.injectiontracker.domain.TrackerSnapshot
import com.deeeelay.injectiontracker.notifications.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.util.UUID

class TrackerRepository(
    private val logDao: InjectionLogDao,
    private val prefs: TrackerPreferences,
    private val scheduler: ReminderScheduler,
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
    private val now: () -> Instant = { Instant.now() },
) {
    val snapshot: Flow<TrackerSnapshot> = combine(prefs.snapshot, logDao.observeAll()) { pref, logs ->
        TrackerSnapshot(
            setupComplete = pref.setupComplete,
            regimen = pref.regimen,
            logs = logs.map { it.toDomain() },
            dayBeforeEnabled = pref.dayBeforeEnabled,
            atTimeEnabled = pref.atTimeEnabled,
        )
    }

    suspend fun saveSetup(
        medicineName: String,
        dosage: String,
        frequency: com.deeeelay.injectiontracker.domain.Frequency,
        startDateTime: Instant,
    ) {
        val regimen = Regimen(
            medicineName = medicineName.trim(),
            dosage = dosage.trim(),
            frequency = frequency,
            startDateTime = startDateTime,
            nextInjectionAt = startDateTime,
        )
        prefs.saveSetup(regimen)
        scheduler.reschedule(regimen.nextInjectionAt, dayBefore = true, atTime = true)
    }

    suspend fun logDone(loggedAt: Instant, site: InjectionSite) {
        val current = prefs.current().regimen ?: return
        val log = InjectionLog(
            id = idFactory(),
            type = LogType.DONE,
            loggedAt = loggedAt,
            site = site,
        )
        logDao.insert(log.toEntity())
        val next = Scheduling.nextAfterTerminalLog(loggedAt, current.frequency)
        prefs.updateNextInjectionAt(next)
        val flags = prefs.current()
        scheduler.reschedule(next, flags.dayBeforeEnabled, flags.atTimeEnabled)
    }

    suspend fun logMissed(loggedAt: Instant = now()) {
        val current = prefs.current().regimen ?: return
        val log = InjectionLog(
            id = idFactory(),
            type = LogType.MISSED,
            loggedAt = loggedAt,
            site = null,
        )
        logDao.insert(log.toEntity())
        val next = Scheduling.nextAfterTerminalLog(loggedAt, current.frequency)
        prefs.updateNextInjectionAt(next)
        val flags = prefs.current()
        scheduler.reschedule(next, flags.dayBeforeEnabled, flags.atTimeEnabled)
    }

    suspend fun updateRegimen(
        medicineName: String,
        dosage: String,
        frequency: com.deeeelay.injectiontracker.domain.Frequency,
        startDateTime: Instant,
    ) {
        val current = prefs.current()
        val old = current.regimen ?: return
        val lastLogAt = logDao.getAll().maxByOrNull { it.loggedAtEpochMillis }
            ?.let { Instant.ofEpochMilli(it.loggedAtEpochMillis) }
        val frequencyOrAnchorChanged =
            frequency != old.frequency || startDateTime != old.startDateTime
        val next = if (frequencyOrAnchorChanged) {
            Scheduling.nextAfterRegimenEdit(startDateTime, lastLogAt, frequency)
        } else {
            old.nextInjectionAt
        }
        prefs.updateRegimen(
            old.copy(
                medicineName = medicineName.trim(),
                dosage = dosage.trim(),
                frequency = frequency,
                startDateTime = startDateTime,
                nextInjectionAt = next,
            ),
        )
        val flags = prefs.current()
        scheduler.reschedule(next, flags.dayBeforeEnabled, flags.atTimeEnabled)
    }

    suspend fun setDayBeforeEnabled(enabled: Boolean) {
        prefs.setDayBeforeEnabled(enabled)
        val current = prefs.current()
        scheduler.reschedule(current.regimen?.nextInjectionAt, enabled, current.atTimeEnabled)
    }

    suspend fun setAtTimeEnabled(enabled: Boolean) {
        prefs.setAtTimeEnabled(enabled)
        val current = prefs.current()
        scheduler.reschedule(current.regimen?.nextInjectionAt, current.dayBeforeEnabled, enabled)
    }

    suspend fun rescheduleFromStorage() {
        val current = prefs.current()
        scheduler.reschedule(
            current.regimen?.nextInjectionAt,
            current.dayBeforeEnabled,
            current.atTimeEnabled,
        )
    }
}
