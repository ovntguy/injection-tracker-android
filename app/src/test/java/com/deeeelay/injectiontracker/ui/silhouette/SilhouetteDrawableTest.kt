package com.deeeelay.injectiontracker.ui.silhouette

import com.deeeelay.injectiontracker.domain.InjectionSite
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SilhouetteDrawableTest {
    @Test
    fun displayEmptyAndLastSiteMapToLockedDrawableNames() {
        assertThat(silhouetteDrawableName(SilhouetteMode.Display, null))
            .isEqualTo("silhouette_display_empty")
        assertThat(silhouetteDrawableName(SilhouetteMode.Display, InjectionSite.LEFT_STOMACH))
            .isEqualTo("silhouette_display_left_stomach")
        assertThat(silhouetteDrawableName(SilhouetteMode.Display, InjectionSite.RIGHT_UPPER_ARM))
            .isEqualTo("silhouette_display_right_upper_arm")
    }

    @Test
    fun selectUnselectedAndZoneMapToLockedDrawableNames() {
        assertThat(silhouetteDrawableName(SilhouetteMode.Select, null))
            .isEqualTo("silhouette_select_unselected")
        assertThat(silhouetteDrawableName(SilhouetteMode.Select, InjectionSite.LEFT_UPPER_THIGH))
            .isEqualTo("silhouette_select_left_upper_thigh")
    }

    @Test
    fun everyZoneHasDisplayAndSelectDrawableNames() {
        InjectionSite.entries.forEach { site ->
            val zone = site.name.lowercase()
            assertThat(silhouetteDrawableName(SilhouetteMode.Display, site))
                .isEqualTo("silhouette_display_$zone")
            assertThat(silhouetteDrawableName(SilhouetteMode.Select, site))
                .isEqualTo("silhouette_select_$zone")
        }
    }
}
