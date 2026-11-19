package com.deeeelay.injectiontracker.data.local

import com.deeeelay.injectiontracker.domain.InjectionSite
import com.deeeelay.injectiontracker.domain.LogType
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LogMappersTest {
    @Test
    fun knownRowMapsToDomain() {
        val log = InjectionLogEntity("a", "DONE", 1_000L, "LEFT_ABDOMEN_MID").toDomain()
        assertThat(log?.type).isEqualTo(LogType.DONE)
        assertThat(log?.site).isEqualTo(InjectionSite.LEFT_ABDOMEN_MID)
    }

    @Test
    fun unknownTypeIsSkippedInsteadOfCrashing() {
        assertThat(InjectionLogEntity("b", "SKIPPED", 1_000L, null).toDomain()).isNull()
    }
}
