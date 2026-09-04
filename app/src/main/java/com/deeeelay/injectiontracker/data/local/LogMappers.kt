package com.deeeelay.injectiontracker.data.local

import com.deeeelay.injectiontracker.domain.InjectionLog
import com.deeeelay.injectiontracker.domain.InjectionSite
import com.deeeelay.injectiontracker.domain.LogType
import java.time.Instant

fun InjectionLogEntity.toDomain(): InjectionLog =
    InjectionLog(
        id = id,
        type = LogType.valueOf(type),
        loggedAt = Instant.ofEpochMilli(loggedAtEpochMillis),
        site = site?.let { InjectionSite.valueOf(it) },
    )

fun InjectionLog.toEntity(): InjectionLogEntity =
    InjectionLogEntity(
        id = id,
        type = type.name,
        loggedAtEpochMillis = loggedAt.toEpochMilli(),
        site = site?.name,
    )
