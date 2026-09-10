package com.deeeelay.injectiontracker.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Instant

class TrackerSnapshotTest {
    private val t0 = Instant.parse("2026-09-06T08:00:00Z")

    @Test
    fun lastDoneSiteIsNullWhenNoDoneLogs() {
        val snap = snapshot(
            InjectionLog("m1", LogType.MISSED, t0, site = null),
        )
        assertThat(snap.lastDoneSite).isNull()
    }

    @Test
    fun missedDoesNotMoveLastSite() {
        val snap = snapshot(
            InjectionLog("d1", LogType.DONE, t0, site = InjectionSite.LEFT_ABDOMEN_MID),
            InjectionLog("m1", LogType.MISSED, t0.plusSeconds(86_400), site = null),
        )
        assertThat(snap.lastDoneSite).isEqualTo(InjectionSite.LEFT_ABDOMEN_MID)
    }

    @Test
    fun laterDoneLogReplacesLastSite() {
        val snap = snapshot(
            InjectionLog("d1", LogType.DONE, t0, site = InjectionSite.LEFT_ABDOMEN_MID),
            InjectionLog("d2", LogType.DONE, t0.plusSeconds(86_400), site = InjectionSite.RIGHT_UPPER_THIGH_PROXIMAL),
        )
        assertThat(snap.lastDoneSite).isEqualTo(InjectionSite.RIGHT_UPPER_THIGH_PROXIMAL)
    }

    private fun snapshot(vararg logs: InjectionLog) = TrackerSnapshot(
        setupComplete = true,
        regimen = null,
        logs = logs.toList(),
        dayBeforeEnabled = false,
        atTimeEnabled = false,
    )
}
