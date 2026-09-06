package com.deeeelay.injectiontracker.ui.silhouette

import com.deeeelay.injectiontracker.domain.InjectionSite
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class SilhouetteDrawableTest {
    @Test
    fun displayEmptyAndLastSiteMapToLockedDrawableNames() {
        assertThat(silhouetteDrawableName(SilhouetteMode.Display, null))
            .isEqualTo("silhouette_display_empty")
        assertThat(silhouetteDrawableName(SilhouetteMode.Display, InjectionSite.LEFT_ABDOMEN_MID))
            .isEqualTo("silhouette_display_left_abdomen_mid")
        assertThat(silhouetteDrawableName(SilhouetteMode.Display, InjectionSite.RIGHT_UPPER_ARM_DISTAL))
            .isEqualTo("silhouette_display_right_upper_arm_distal")
    }

    @Test
    fun selectUnselectedAndZoneMapToLockedDrawableNames() {
        assertThat(silhouetteDrawableName(SilhouetteMode.Select, null))
            .isEqualTo("silhouette_select_unselected")
        assertThat(silhouetteDrawableName(SilhouetteMode.Select, InjectionSite.LEFT_UPPER_THIGH_PROXIMAL))
            .isEqualTo("silhouette_select_left_upper_thigh_proximal")
    }

    @Test
    fun everyZoneHasDisplayAndSelectDrawableNames() {
        assertThat(InjectionSite.entries).hasSize(18)
        InjectionSite.entries.forEach { site ->
            val zone = site.zoneId
            assertThat(silhouetteDrawableName(SilhouetteMode.Display, site))
                .isEqualTo("silhouette_display_$zone")
            assertThat(silhouetteDrawableName(SilhouetteMode.Select, site))
                .isEqualTo("silhouette_select_$zone")
        }
    }

    @Test
    fun lockedVectorDrawablesExistForEmptyAndEveryZone() {
        val dir = drawableDir()
        val names = buildList {
            add("silhouette_display_empty.xml")
            add("silhouette_select_unselected.xml")
            InjectionSite.entries.forEach { site ->
                add("silhouette_display_${site.zoneId}.xml")
                add("silhouette_select_${site.zoneId}.xml")
            }
        }
        assertThat(names).hasSize(38)
        names.forEach { name ->
            assertThat(File(dir, name).exists()).isTrue()
        }
        val emptyPath = Regex("""android:pathData="([^"]+)"""").find(
            File(dir, "silhouette_display_empty.xml").readText(),
        )!!.groupValues[1]
        names.forEach { name ->
            val text = File(dir, name).readText()
            assertThat(text).contains(emptyPath)
            if (name.contains("display") && name != "silhouette_display_empty.xml") {
                assertThat(text).contains("android:strokeWidth=\"1.25\"")
                assertThat(text).contains("#CAC4D0")
                assertThat(text).contains("1.75,1.75")
            }
            if (name.contains("select") && name != "silhouette_select_unselected.xml") {
                assertThat(text).contains("android:strokeWidth=\"1.5\"")
                assertThat(text).contains("#49454F")
                assertThat(text).contains("1.5,1.5")
            }
        }
    }

    private fun drawableDir(): File {
        val candidates = listOf(
            File("app/src/main/res/drawable"),
            File("../app/src/main/res/drawable"),
            File("src/main/res/drawable"),
        )
        return candidates.firstOrNull { it.isDirectory }
            ?: error("drawable dir not found from ${File(".").absolutePath}")
    }
}
