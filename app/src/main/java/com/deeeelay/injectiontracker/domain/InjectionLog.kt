package com.deeeelay.injectiontracker.domain

import java.time.Instant

data class InjectionLog(
    val id: String,
    val type: LogType,
    val loggedAt: Instant,
    val site: InjectionSite?,
)
