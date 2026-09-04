package com.deeeelay.injectiontracker.domain

data class TrackerSnapshot(
    val setupComplete: Boolean,
    val regimen: Regimen?,
    val logs: List<InjectionLog>,
    val dayBeforeEnabled: Boolean,
    val atTimeEnabled: Boolean,
) {
    val lastDoneSite: InjectionSite?
        get() = logs
            .filter { it.type == LogType.DONE && it.site != null }
            .maxByOrNull { it.loggedAt }
            ?.site

    val lastTerminalLogAt
        get() = logs.maxByOrNull { it.loggedAt }?.loggedAt
}
