package com.deeeelay.injectiontracker.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class SilhouetteZonesJsonTest {
    @Test
    fun assetsLockMatchesEnumCentersAndIds() {
        val json = readLockJson()
        assertThat(json).contains("\"zoneCount\": 18")
        assertThat(json).contains("\"hitTargetDp\": 48")
        assertThat(json).contains("\"displayDotRadius\": 1.75")
        assertThat(json).contains("\"selectCenterDotRadius\": 1.5")
        assertThat(json).contains("\"hitRegionsLocked\": true")
        assertThat(json).contains("zones.json v2")
        assertThat(json).contains("\"bodyFill\": \"#F3EDF7\"")
        assertThat(json).contains("\"displayStroke\": \"#CAC4D0\"")
        assertThat(json).contains("\"selectStroke\": \"#49454F\"")

        InjectionSite.entries.forEach { site ->
            assertThat(json).contains("\"id\": \"${site.zoneId}\"")
            assertThat(json).contains(formatCoord(site.viewportX))
            assertThat(json).contains(formatCoord(site.viewportY))
        }
        assertThat(Regex("\"id\": ").findAll(json).count()).isEqualTo(18)
    }

    private fun formatCoord(value: Float): String {
        val asInt = value.toInt()
        return if (value == asInt.toFloat()) {
            "%.1f".format(value)
        } else {
            value.toString().removeSuffix("f")
        }
    }

    private fun readLockJson(): String {
        val candidates = listOf(
            File("app/src/main/assets/silhouette_zones.json"),
            File("../app/src/main/assets/silhouette_zones.json"),
            File("src/main/assets/silhouette_zones.json"),
        )
        val file = candidates.firstOrNull { it.exists() }
            ?: error("silhouette_zones.json not found from ${File(".").absolutePath}")
        return file.readText()
    }
}
