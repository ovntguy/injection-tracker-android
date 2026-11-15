package com.deeeelay.injectiontracker.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * HARD LOCK: zone ids, cx/cy (in order) and hitRegionsLocked must match main @ 3a72ffc exactly.
 * Style fields (stroke, fill, dot radius) are Design-owned and may change.
 */
class ZonesJsonLockTest {
    private val json: String = listOf(
        File("app/src/main/assets/silhouette_zones.json"),
        File("../app/src/main/assets/silhouette_zones.json"),
        File("src/main/assets/silhouette_zones.json"),
    ).first { it.exists() }.readText()

    @Test
    fun idsAndCentersMatchMainExactly() {
        val zone = Regex("""\{\s*"id": "([a-z_]+)",\s*"label": "[^"]*",\s*"cx": ([0-9.]+),\s*"cy": ([0-9.]+)""")
        val parsed = zone.findAll(json).map { m ->
            Triple(m.groupValues[1], m.groupValues[2].toFloat(), m.groupValues[3].toFloat())
        }.toList()
        assertThat(parsed).containsExactlyElementsIn(MAIN_ZONES).inOrder()
        assertThat(Regex(""""id": """).findAll(json).count()).isEqualTo(18)
    }

    @Test
    fun hitRegionsStayLocked() {
        assertThat(Regex(""""hitRegionsLocked": (true|false)""").findAll(json).map { it.groupValues[1] }.toList())
            .containsExactly("true")
    }

    @Test
    fun enumMirrorsMainCenters() {
        assertThat(InjectionSite.entries.map { Triple(it.zoneId, it.viewportX, it.viewportY) })
            .containsExactlyElementsIn(MAIN_ZONES)
    }

    private companion object {
        /** Copied from app/src/main/assets/silhouette_zones.json at main 3a72ffc. */
        val MAIN_ZONES = listOf(
            Triple("right_upper_arm_proximal", 47.0f, 60.0f),
            Triple("right_upper_arm_mid", 45.5f, 73.0f),
            Triple("right_upper_arm_distal", 44.5f, 86.0f),
            Triple("left_upper_arm_proximal", 93.0f, 60.0f),
            Triple("left_upper_arm_mid", 94.5f, 73.0f),
            Triple("left_upper_arm_distal", 95.5f, 86.0f),
            Triple("right_abdomen_proximal", 60.0f, 92.0f),
            Triple("right_abdomen_mid", 59.5f, 105.0f),
            Triple("right_abdomen_distal", 59.5f, 118.0f),
            Triple("left_abdomen_proximal", 80.0f, 92.0f),
            Triple("left_abdomen_mid", 80.5f, 105.0f),
            Triple("left_abdomen_distal", 80.5f, 118.0f),
            Triple("right_upper_thigh_proximal", 61.0f, 134.0f),
            Triple("right_upper_thigh_mid", 61.0f, 146.0f),
            Triple("right_upper_thigh_distal", 60.5f, 158.0f),
            Triple("left_upper_thigh_proximal", 79.0f, 134.0f),
            Triple("left_upper_thigh_mid", 79.0f, 146.0f),
            Triple("left_upper_thigh_distal", 79.5f, 158.0f),
        )
    }
}
