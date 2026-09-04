package com.deeeelay.injectiontracker.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class InjectionSiteTest {
    @Test
    fun lockedCentersMatchSpec() {
        assertThat(InjectionSite.LEFT_UPPER_ARM.viewportX).isEqualTo(46.1f)
        assertThat(InjectionSite.LEFT_UPPER_ARM.viewportY).isEqualTo(80.6f)
        assertThat(InjectionSite.RIGHT_UPPER_ARM.viewportX).isEqualTo(93.9f)
        assertThat(InjectionSite.RIGHT_UPPER_ARM.viewportY).isEqualTo(80.6f)
        assertThat(InjectionSite.LEFT_STOMACH.viewportX).isEqualTo(61.1f)
        assertThat(InjectionSite.LEFT_STOMACH.viewportY).isEqualTo(105.8f)
        assertThat(InjectionSite.RIGHT_STOMACH.viewportX).isEqualTo(78.9f)
        assertThat(InjectionSite.RIGHT_STOMACH.viewportY).isEqualTo(105.8f)
        assertThat(InjectionSite.LEFT_UPPER_THIGH.viewportX).isEqualTo(60.2f)
        assertThat(InjectionSite.LEFT_UPPER_THIGH.viewportY).isEqualTo(147.8f)
        assertThat(InjectionSite.RIGHT_UPPER_THIGH.viewportX).isEqualTo(79.8f)
        assertThat(InjectionSite.RIGHT_UPPER_THIGH.viewportY).isEqualTo(147.8f)
    }

    @Test
    fun zoneIdsMatchLockedDrawableBasenames() {
        assertThat(InjectionSite.LEFT_UPPER_ARM.zoneId).isEqualTo("left_upper_arm")
        assertThat(InjectionSite.RIGHT_UPPER_ARM.zoneId).isEqualTo("right_upper_arm")
        assertThat(InjectionSite.LEFT_STOMACH.zoneId).isEqualTo("left_stomach")
        assertThat(InjectionSite.RIGHT_STOMACH.zoneId).isEqualTo("right_stomach")
        assertThat(InjectionSite.LEFT_UPPER_THIGH.zoneId).isEqualTo("left_upper_thigh")
        assertThat(InjectionSite.RIGHT_UPPER_THIGH.zoneId).isEqualTo("right_upper_thigh")
        val ids = InjectionSite.entries.map { it.zoneId }
        assertThat(ids).containsNoDuplicates()
    }

    @Test
    fun nearestPicksLeftStomachAroundItsCenter() {
        val site = InjectionSite.nearest(61.1f, 105.8f, maxDistance = 24f)
        assertThat(site).isEqualTo(InjectionSite.LEFT_STOMACH)
    }

    @Test
    fun nearestReturnsNullWhenFarFromAllZones() {
        val site = InjectionSite.nearest(70f, 20f, maxDistance = 10f)
        assertThat(site).isNull()
    }

    @Test
    fun overlappingHitsResolveToNearestCenter() {
        val midpointX = (61.1f + 78.9f) / 2f
        val left = InjectionSite.nearest(midpointX - 0.2f, 105.8f, maxDistance = 24f)
        val right = InjectionSite.nearest(midpointX + 0.2f, 105.8f, maxDistance = 24f)
        assertThat(left).isEqualTo(InjectionSite.LEFT_STOMACH)
        assertThat(right).isEqualTo(InjectionSite.RIGHT_STOMACH)
    }
}
