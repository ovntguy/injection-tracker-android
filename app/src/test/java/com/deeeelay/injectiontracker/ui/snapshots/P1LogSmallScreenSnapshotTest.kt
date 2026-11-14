package com.deeeelay.injectiontracker.ui.snapshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import com.deeeelay.injectiontracker.domain.InjectionSite
import com.deeeelay.injectiontracker.ui.screens.logdone.LogDoneContent
import com.deeeelay.injectiontracker.ui.screens.logdone.LogDoneState
import com.deeeelay.injectiontracker.ui.theme.InjectionTrackerTheme
import androidx.compose.foundation.rememberScrollState
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
 * Design Critique screenshots for the short-screen case (360×640 dp): the body crop holds the
 * 1.7 dp/unit floor and the content scrolls, while the readout and Save log stay pinned in the
 * bottom bar. Scroll top and bottom, each before and after a zone tap.
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

    private val mode get() = if (night) "dark" else "light"

    private val beforeTap = LogDoneState(
        date = LocalDate.of(2026, 11, 14),
        time = LocalTime.of(9, 8),
        lastSite = InjectionSite.RIGHT_UPPER_ARM_PROXIMAL,
        lastSiteAt = ZonedDateTime.of(2026, 11, 7, 17, 50, 0, 0, zone).toInstant(),
    )
    private val afterTap = beforeTap.copy(site = InjectionSite.RIGHT_UPPER_THIGH_MID)

    /** [atBottom] starts the scroll past the end; ScrollState clamps it to maxValue on layout. */
    private fun shot(name: String, state: LogDoneState, atBottom: Boolean) =
        paparazzi.snapshot(name = "log_small_${name}_$mode") {
            InjectionTrackerTheme {
                Surface {
                    LogDoneContent(
                        state, {}, {}, {}, {}, {}, zone = zone,
                        scrollState = rememberScrollState(if (atBottom) Int.MAX_VALUE else 0),
                    )
                }
            }
        }

    @Test
    fun scrollTopBeforeTap() = shot("top_before_tap", beforeTap, atBottom = false)

    @Test
    fun scrollTopAfterTap() = shot("top_after_tap", afterTap, atBottom = false)

    @Test
    fun scrollBottomBeforeTap() = shot("bottom_before_tap", beforeTap, atBottom = true)

    @Test
    fun scrollBottomAfterTap() = shot("bottom_after_tap", afterTap, atBottom = true)
}
