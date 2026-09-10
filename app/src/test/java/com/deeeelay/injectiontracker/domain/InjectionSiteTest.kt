package com.deeeelay.injectiontracker.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InjectionSiteTest {
    @Test
    fun lockedZoneCountIs18() {
        assertThat(InjectionSite.entries).hasSize(InjectionSite.ZONE_COUNT)
        assertThat(InjectionSite.ZONE_COUNT).isEqualTo(18)
    }

    @Test
    fun lockedCentersMatchSpec() {
        assertThat(InjectionSite.LEFT_UPPER_ARM_PROXIMAL.viewportX).isEqualTo(47.0f)
        assertThat(InjectionSite.LEFT_UPPER_ARM_PROXIMAL.viewportY).isEqualTo(60.0f)
        assertThat(InjectionSite.LEFT_UPPER_ARM_MID.viewportX).isEqualTo(45.5f)
        assertThat(InjectionSite.LEFT_UPPER_ARM_MID.viewportY).isEqualTo(73.0f)
        assertThat(InjectionSite.LEFT_UPPER_ARM_DISTAL.viewportX).isEqualTo(44.5f)
        assertThat(InjectionSite.LEFT_UPPER_ARM_DISTAL.viewportY).isEqualTo(86.0f)
        assertThat(InjectionSite.RIGHT_UPPER_ARM_PROXIMAL.viewportX).isEqualTo(93.0f)
        assertThat(InjectionSite.RIGHT_UPPER_ARM_PROXIMAL.viewportY).isEqualTo(60.0f)
        assertThat(InjectionSite.RIGHT_UPPER_ARM_MID.viewportX).isEqualTo(94.5f)
        assertThat(InjectionSite.RIGHT_UPPER_ARM_MID.viewportY).isEqualTo(73.0f)
        assertThat(InjectionSite.RIGHT_UPPER_ARM_DISTAL.viewportX).isEqualTo(95.5f)
        assertThat(InjectionSite.RIGHT_UPPER_ARM_DISTAL.viewportY).isEqualTo(86.0f)
        assertThat(InjectionSite.LEFT_ABDOMEN_PROXIMAL.viewportX).isEqualTo(60.0f)
        assertThat(InjectionSite.LEFT_ABDOMEN_PROXIMAL.viewportY).isEqualTo(92.0f)
        assertThat(InjectionSite.LEFT_ABDOMEN_MID.viewportX).isEqualTo(59.5f)
        assertThat(InjectionSite.LEFT_ABDOMEN_MID.viewportY).isEqualTo(105.0f)
        assertThat(InjectionSite.LEFT_ABDOMEN_DISTAL.viewportX).isEqualTo(59.5f)
        assertThat(InjectionSite.LEFT_ABDOMEN_DISTAL.viewportY).isEqualTo(118.0f)
        assertThat(InjectionSite.RIGHT_ABDOMEN_PROXIMAL.viewportX).isEqualTo(80.0f)
        assertThat(InjectionSite.RIGHT_ABDOMEN_PROXIMAL.viewportY).isEqualTo(92.0f)
        assertThat(InjectionSite.RIGHT_ABDOMEN_MID.viewportX).isEqualTo(80.5f)
        assertThat(InjectionSite.RIGHT_ABDOMEN_MID.viewportY).isEqualTo(105.0f)
        assertThat(InjectionSite.RIGHT_ABDOMEN_DISTAL.viewportX).isEqualTo(80.5f)
        assertThat(InjectionSite.RIGHT_ABDOMEN_DISTAL.viewportY).isEqualTo(118.0f)
        assertThat(InjectionSite.LEFT_UPPER_THIGH_PROXIMAL.viewportX).isEqualTo(61.0f)
        assertThat(InjectionSite.LEFT_UPPER_THIGH_PROXIMAL.viewportY).isEqualTo(134.0f)
        assertThat(InjectionSite.LEFT_UPPER_THIGH_MID.viewportX).isEqualTo(61.0f)
        assertThat(InjectionSite.LEFT_UPPER_THIGH_MID.viewportY).isEqualTo(146.0f)
        assertThat(InjectionSite.LEFT_UPPER_THIGH_DISTAL.viewportX).isEqualTo(60.5f)
        assertThat(InjectionSite.LEFT_UPPER_THIGH_DISTAL.viewportY).isEqualTo(158.0f)
        assertThat(InjectionSite.RIGHT_UPPER_THIGH_PROXIMAL.viewportX).isEqualTo(79.0f)
        assertThat(InjectionSite.RIGHT_UPPER_THIGH_PROXIMAL.viewportY).isEqualTo(134.0f)
        assertThat(InjectionSite.RIGHT_UPPER_THIGH_MID.viewportX).isEqualTo(79.0f)
        assertThat(InjectionSite.RIGHT_UPPER_THIGH_MID.viewportY).isEqualTo(146.0f)
        assertThat(InjectionSite.RIGHT_UPPER_THIGH_DISTAL.viewportX).isEqualTo(79.5f)
        assertThat(InjectionSite.RIGHT_UPPER_THIGH_DISTAL.viewportY).isEqualTo(158.0f)
    }

    @Test
    fun zoneIdsMatchJsonStyleSnakeCase() {
        assertThat(InjectionSite.LEFT_ABDOMEN_MID.zoneId).isEqualTo("left_abdomen_mid")
        assertThat(InjectionSite.RIGHT_UPPER_THIGH_PROXIMAL.zoneId)
            .isEqualTo("right_upper_thigh_proximal")
    }

    @Test
    fun nearestPicksLeftAbdomenMidAroundItsCenter() {
        val site = InjectionSite.nearest(59.5f, 105.0f, maxDistance = 24f)
        assertThat(site).isEqualTo(InjectionSite.LEFT_ABDOMEN_MID)
    }

    @Test
    fun nearestReturnsNullWhenFarFromAllZones() {
        val site = InjectionSite.nearest(70f, 20f, maxDistance = 10f)
        assertThat(site).isNull()
    }

    @Test
    fun overlappingHitsResolveToNearestCenter() {
        val midpointX =
            (InjectionSite.LEFT_ABDOMEN_MID.viewportX + InjectionSite.RIGHT_ABDOMEN_MID.viewportX) / 2f
        val left = InjectionSite.nearest(midpointX - 0.2f, 105.0f, maxDistance = 24f)
        val right = InjectionSite.nearest(midpointX + 0.2f, 105.0f, maxDistance = 24f)
        assertThat(left).isEqualTo(InjectionSite.LEFT_ABDOMEN_MID)
        assertThat(right).isEqualTo(InjectionSite.RIGHT_ABDOMEN_MID)
    }

    @Test
    fun hitTargetIsLockedAt48dp() {
        assertThat(InjectionSite.HIT_TARGET_DP).isEqualTo(48f)
        val site = InjectionSite.LEFT_ABDOMEN_MID
        assertThat(InjectionSite.nearest(site.viewportX, site.viewportY, 48f)).isEqualTo(site)
        assertThat(InjectionSite.nearest(70f, 8f, 48f)).isNull()
    }

    @Test
    fun fromPersistedNameReads18ZoneIdsAndLegacy6ZoneNames() {
        assertThat(InjectionSite.fromPersistedName("LEFT_ABDOMEN_MID"))
            .isEqualTo(InjectionSite.LEFT_ABDOMEN_MID)
        assertThat(InjectionSite.fromPersistedName("LEFT_STOMACH"))
            .isEqualTo(InjectionSite.LEFT_ABDOMEN_MID)
        assertThat(InjectionSite.fromPersistedName("RIGHT_UPPER_ARM"))
            .isEqualTo(InjectionSite.RIGHT_UPPER_ARM_MID)
        assertThat(InjectionSite.fromPersistedName("unknown")).isNull()
        assertThat(InjectionSite.fromPersistedName(null)).isNull()
    }
}
