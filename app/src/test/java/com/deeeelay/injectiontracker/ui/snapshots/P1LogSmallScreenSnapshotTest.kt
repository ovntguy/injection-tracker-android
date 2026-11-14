package com.deeeelay.injectiontracker.ui.snapshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import com.deeeelay.injectiontracker.domain.InjectionSite
import com.deeeelay.injectiontracker.ui.screens.logdone.LogDoneContent
import com.deeeelay.injectiontracker.ui.screens.logdone.LogDoneState
import com.deeeelay.injectiontracker.ui.theme.InjectionTrackerTheme
import androidx.compose.material3.Surface
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Design Critique screenshot for the short-screen case (360×640 dp): the body crop holds the
 * 1.7 dp/unit floor, the content scrolls, and Save log stays pinned in the bottom bar.
 */
@RunWith(Parameterized::class)
class P1LogSmallScreenSnapshotTest(private val night: Boolean) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun modes() = listOf(false, true)

        private val zone: ZoneId = ZoneId.of("America/Chicago")
    }

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.NEXUS_5.copy(
            nightMode = if (night) NightMode.NIGHT else NightMode.NOTNIGHT,
        ),
        maxPercentDifference = 0.0,
    )

    @Test
    fun logSmallScreenScrollsWithPinnedSave() = paparazzi.snapshot(
        name = "log_small_screen_${if (night) "dark" else "light"}",
    ) {
        InjectionTrackerTheme {
            Surface {
                LogDoneContent(
                    LogDoneState(
                        date = LocalDate.of(2026, 11, 14),
                        time = LocalTime.of(9, 8),
                        site = InjectionSite.RIGHT_UPPER_THIGH_MID,
                        lastSite = InjectionSite.RIGHT_UPPER_ARM_PROXIMAL,
                        lastSiteAt = ZonedDateTime.of(2026, 11, 7, 17, 50, 0, 0, zone).toInstant(),
                    ),
                    {}, {}, {}, {}, {}, zone = zone,
                )
            }
        }
    }
}
