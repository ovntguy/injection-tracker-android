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
        assertThat(InjectionSite.LEFT_UPPER_ARM_PROXIMAL.viewportX).isEqualTo(46.9f)
        assertThat(InjectionSite.LEFT_UPPER_ARM_PROXIMAL.viewportY).isEqualTo(68.0f)
        assertThat(InjectionSite.LEFT_UPPER_ARM_MID.viewportX).isEqualTo(45.1f)
        assertThat(InjectionSite.LEFT_UPPER_ARM_MID.viewportY).isEqualTo(84.8f)
        assertThat(InjectionSite.LEFT_UPPER_ARM_DISTAL.viewportX).isEqualTo(43.5f)
        assertThat(InjectionSite.LEFT_UPPER_ARM_DISTAL.viewportY).isEqualTo(101.6f)
        assertThat(InjectionSite.RIGHT_UPPER_ARM_PROXIMAL.viewportX).isEqualTo(93.2f)
        assertThat(InjectionSite.RIGHT_UPPER_ARM_PROXIMAL.viewportY).isEqualTo(68.0f)
        assertThat(InjectionSite.RIGHT_UPPER_ARM_MID.viewportX).isEqualTo(94.9f)
        assertThat(InjectionSite.RIGHT_UPPER_ARM_MID.viewportY).isEqualTo(84.8f)
        assertThat(InjectionSite.RIGHT_UPPER_ARM_DISTAL.viewportX).isEqualTo(96.8f)
        assertThat(InjectionSite.RIGHT_UPPER_ARM_DISTAL.viewportY).isEqualTo(101.6f)
        assertThat(InjectionSite.LEFT_ABDOMEN_PROXIMAL.viewportX).isEqualTo(60.1f)
        assertThat(InjectionSite.LEFT_ABDOMEN_PROXIMAL.viewportY).isEqualTo(93.2f)
        assertThat(InjectionSite.LEFT_ABDOMEN_MID.viewportX).isEqualTo(59.6f)
        assertThat(InjectionSite.LEFT_ABDOMEN_MID.viewportY).isEqualTo(110.0f)
        assertThat(InjectionSite.LEFT_ABDOMEN_DISTAL.viewportX).isEqualTo(59.7f)
        assertThat(InjectionSite.LEFT_ABDOMEN_DISTAL.viewportY).isEqualTo(126.8f)
        assertThat(InjectionSite.RIGHT_ABDOMEN_PROXIMAL.viewportX).isEqualTo(79.9f)
        assertThat(InjectionSite.RIGHT_ABDOMEN_PROXIMAL.viewportY).isEqualTo(93.2f)
        assertThat(InjectionSite.RIGHT_ABDOMEN_MID.viewportX).isEqualTo(80.4f)
        assertThat(InjectionSite.RIGHT_ABDOMEN_MID.viewportY).isEqualTo(110.0f)
        assertThat(InjectionSite.RIGHT_ABDOMEN_DISTAL.viewportX).isEqualTo(80.3f)
        assertThat(InjectionSite.RIGHT_ABDOMEN_DISTAL.viewportY).isEqualTo(126.8f)
        assertThat(InjectionSite.LEFT_UPPER_THIGH_PROXIMAL.viewportX).isEqualTo(60.7f)
        assertThat(InjectionSite.LEFT_UPPER_THIGH_PROXIMAL.viewportY).isEqualTo(139.4f)
        assertThat(InjectionSite.LEFT_UPPER_THIGH_MID.viewportX).isEqualTo(61.4f)
        assertThat(InjectionSite.LEFT_UPPER_THIGH_MID.viewportY).isEqualTo(156.2f)
        assertThat(InjectionSite.LEFT_UPPER_THIGH_DISTAL.viewportX).isEqualTo(61.3f)
        assertThat(InjectionSite.LEFT_UPPER_THIGH_DISTAL.viewportY).isEqualTo(173.0f)
        assertThat(InjectionSite.RIGHT_UPPER_THIGH_PROXIMAL.viewportX).isEqualTo(79.4f)
        assertThat(InjectionSite.RIGHT_UPPER_THIGH_PROXIMAL.viewportY).isEqualTo(139.4f)
        assertThat(InjectionSite.RIGHT_UPPER_THIGH_MID.viewportX).isEqualTo(78.7f)
        assertThat(InjectionSite.RIGHT_UPPER_THIGH_MID.viewportY).isEqualTo(156.2f)
        assertThat(InjectionSite.RIGHT_UPPER_THIGH_DISTAL.viewportX).isEqualTo(78.9f)
        assertThat(InjectionSite.RIGHT_UPPER_THIGH_DISTAL.viewportY).isEqualTo(173.0f)
    }

    @Test
    fun zoneIdsMatchJsonStyleSnakeCase() {
        assertThat(InjectionSite.LEFT_ABDOMEN_MID.zoneId).isEqualTo("left_abdomen_mid")
        assertThat(InjectionSite.RIGHT_UPPER_THIGH_PROXIMAL.zoneId)
            .isEqualTo("right_upper_thigh_proximal")
    }

    @Test
    fun nearestPicksLeftAbdomenMidAroundItsCenter() {
        val site = InjectionSite.nearest(59.6f, 110.0f, maxDistance = 24f)
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
        val left = InjectionSite.nearest(midpointX - 0.2f, 110.0f, maxDistance = 24f)
        val right = InjectionSite.nearest(midpointX + 0.2f, 110.0f, maxDistance = 24f)
        assertThat(left).isEqualTo(InjectionSite.LEFT_ABDOMEN_MID)
        assertThat(right).isEqualTo(InjectionSite.RIGHT_ABDOMEN_MID)
    }

    @Test
    fun hitTargetIsLockedAt48dp() {
        assertThat(InjectionSite.HIT_TARGET_DP).isEqualTo(48f)
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
