package com.deeeelay.injectiontracker.data.local

import com.deeeelay.injectiontracker.domain.InjectionLog
import com.deeeelay.injectiontracker.domain.InjectionSite
import com.deeeelay.injectiontracker.domain.LogType
import java.time.Instant

/** Null for a row whose type this version doesn't know. */
fun InjectionLogEntity.toDomain(): InjectionLog? {
    val logType = LogType.entries.find { it.name == type } ?: return null
    return InjectionLog(
        id = id,
        type = logType,
        loggedAt = Instant.ofEpochMilli(loggedAtEpochMillis),
        site = InjectionSite.fromPersistedName(site),
    )
}

fun InjectionLog.toEntity(): InjectionLogEntity =
    InjectionLogEntity(
        id = id,
        type = type.name,
        loggedAtEpochMillis = loggedAt.toEpochMilli(),
        site = site?.name,
    )
