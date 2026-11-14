package com.deeeelay.injectiontracker.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ZoneReadoutTest {
    @Test
    fun everyLockedIdMapsToDisplayLabel() {
        val expected = mapOf(
            "right_upper_arm_proximal" to "Right arm, upper",
            "right_upper_arm_mid" to "Right arm, mid",
            "right_upper_arm_distal" to "Right arm, lower",
            "left_upper_arm_proximal" to "Left arm, upper",
            "left_upper_arm_mid" to "Left arm, mid",
            "left_upper_arm_distal" to "Left arm, lower",
            "right_abdomen_proximal" to "Right abdomen, upper",
            "right_abdomen_mid" to "Right abdomen, mid",
            "right_abdomen_distal" to "Right abdomen, lower",
            "left_abdomen_proximal" to "Left abdomen, upper",
            "left_abdomen_mid" to "Left abdomen, mid",
            "left_abdomen_distal" to "Left abdomen, lower",
            "right_upper_thigh_proximal" to "Right thigh, upper",
            "right_upper_thigh_mid" to "Right thigh, mid",
            "right_upper_thigh_distal" to "Right thigh, lower",
            "left_upper_thigh_proximal" to "Left thigh, upper",
            "left_upper_thigh_mid" to "Left thigh, mid",
            "left_upper_thigh_distal" to "Left thigh, lower",
        )
        assertThat(InjectionSite.entries.map { it.zoneId }).containsExactlyElementsIn(expected.keys)
        InjectionSite.entries.forEach { site ->
            assertThat(ZoneReadout.label(site)).isEqualTo(expected.getValue(site.zoneId))
        }
    }

    @Test
    fun mappingDoesNotRenameIds() {
        assertThat(InjectionSite.RIGHT_UPPER_THIGH_DISTAL.zoneId).isEqualTo("right_upper_thigh_distal")
        assertThat(InjectionSite.fromPersistedName("left_abdomen_proximal")).isEqualTo(InjectionSite.LEFT_ABDOMEN_PROXIMAL)
    }
}
