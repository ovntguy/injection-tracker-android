package com.deeeelay.injectiontracker.domain

data class TrackerSnapshot(
    val setupComplete: Boolean,
    val regimen: Regimen?,
    val logs: List<InjectionLog>,
    val dayBeforeEnabled: Boolean,
    val atTimeEnabled: Boolean,
) {
    /** Latest done log with a site; missed logs never count (Home last-site label and marker). */
    val lastDoneLog: InjectionLog?
        get() = logs
            .filter { it.type == LogType.DONE && it.site != null }
            .maxByOrNull { it.loggedAt }

    val lastDoneSite: InjectionSite?
        get() = lastDoneLog?.site

    val lastTerminalLogAt
        get() = logs.maxByOrNull { it.loggedAt }?.loggedAt
}
