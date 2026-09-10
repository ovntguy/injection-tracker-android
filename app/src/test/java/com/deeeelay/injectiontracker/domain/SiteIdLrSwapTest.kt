package com.deeeelay.injectiontracker.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SiteIdLrSwapTest {
    @Test
    fun swapsEnumNamesAndSnakeCaseAndIsInvolution() {
        val pairs = listOf(
            "LEFT_ABDOMEN_MID" to "RIGHT_ABDOMEN_MID",
            "RIGHT_UPPER_ARM_PROXIMAL" to "LEFT_UPPER_ARM_PROXIMAL",
            "left_abdomen_mid" to "right_abdomen_mid",
            "right_upper_thigh_distal" to "left_upper_thigh_distal",
            "LEFT_STOMACH" to "RIGHT_STOMACH",
            "RIGHT_UPPER_ARM" to "LEFT_UPPER_ARM",
        )
        pairs.forEach { (from, to) ->
            assertThat(SiteIdLrSwap.swapPersisted(from)).isEqualTo(to)
            assertThat(SiteIdLrSwap.swapPersisted(to)).isEqualTo(from)
            assertThat(SiteIdLrSwap.swapPersisted(SiteIdLrSwap.swapPersisted(from))).isEqualTo(from)
        }
        assertThat(SiteIdLrSwap.swapPersisted(null)).isNull()
        assertThat(SiteIdLrSwap.swapPersisted("")).isEmpty()
        assertThat(SiteIdLrSwap.swapPersisted("DONE")).isEqualTo("DONE")
    }

    @Test
    fun everyZoneIdSwapsToOppositeSideKeepingBandAndRegion() {
        InjectionSite.entries.forEach { site ->
            val swappedName = SiteIdLrSwap.swapPersisted(site.name)!!
            val swappedZone = SiteIdLrSwap.swapPersisted(site.zoneId)!!
            val opposite = InjectionSite.fromPersistedName(swappedName)
            assertThat(opposite).isNotNull()
            assertThat(opposite!!.viewportY).isEqualTo(site.viewportY)
            assertThat(opposite.viewportX == site.viewportX).isFalse()
            assertThat(swappedZone).isEqualTo(opposite.zoneId)
            val expectedPrefix = if (site.name.startsWith("LEFT_")) "RIGHT_" else "LEFT_"
            assertThat(swappedName).startsWith(expectedPrefix)
        }
    }

    @Test
    fun migratedLegacySixZoneNamesResolveToCorrectedAnatomy() {
        // Pre-v1.1 LEFT_* names were the screen-left (now anatomical right) spots.
        assertThat(InjectionSite.fromPersistedName(SiteIdLrSwap.swapPersisted("LEFT_STOMACH")))
            .isEqualTo(InjectionSite.RIGHT_ABDOMEN_MID)
        assertThat(InjectionSite.fromPersistedName(SiteIdLrSwap.swapPersisted("LEFT_UPPER_ARM")))
            .isEqualTo(InjectionSite.RIGHT_UPPER_ARM_MID)
        assertThat(InjectionSite.fromPersistedName(SiteIdLrSwap.swapPersisted("LEFT_UPPER_THIGH")))
            .isEqualTo(InjectionSite.RIGHT_UPPER_THIGH_MID)
        assertThat(InjectionSite.fromPersistedName(SiteIdLrSwap.swapPersisted("RIGHT_STOMACH")))
            .isEqualTo(InjectionSite.LEFT_ABDOMEN_MID)
        assertThat(InjectionSite.RIGHT_ABDOMEN_MID.viewportX).isEqualTo(59.5f)
        assertThat(InjectionSite.LEFT_ABDOMEN_MID.viewportX).isEqualTo(80.5f)
    }
}
