package com.deeeelay.injectiontracker.ui.screens.logdone

import com.deeeelay.injectiontracker.domain.InjectionSite
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LogDoneStateTest {
    @Test
    fun saveDisabledUntilZonePicked() {
        assertThat(LogDoneState().canSave).isFalse()
        assertThat(LogDoneState(lastSite = InjectionSite.LEFT_ABDOMEN_MID).canSave).isFalse()
        assertThat(LogDoneState(site = InjectionSite.LEFT_ABDOMEN_MID).canSave).isTrue()
    }

    @Test
    fun saveDisabledWhileSavingOrSaved() {
        val picked = LogDoneState(site = InjectionSite.RIGHT_UPPER_THIGH_MID)
        assertThat(picked.copy(saving = true).canSave).isFalse()
        assertThat(picked.copy(saved = true).canSave).isFalse()
    }

    @Test
    fun saveDisabledWhenTimeIsInTheFuture() {
        val picked = LogDoneState(site = InjectionSite.RIGHT_UPPER_THIGH_MID)
        assertThat(picked.copy(inFuture = true).canSave).isFalse()
    }
}
