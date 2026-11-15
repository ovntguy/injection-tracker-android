package com.deeeelay.injectiontracker.ui.snapshots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import com.deeeelay.injectiontracker.domain.Frequency
import com.deeeelay.injectiontracker.domain.InjectionSite
import com.deeeelay.injectiontracker.ui.screens.home.HomeContent
import com.deeeelay.injectiontracker.ui.screens.home.HomeUiState
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

/** Design Critique screenshots for P1 (Log screen items 1–4 + code-drawn Home marker). */
@RunWith(Parameterized::class)
class P1LogAndHomeSnapshotTest(private val night: Boolean) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun modes() = listOf(false, true)

        private val zone: ZoneId = ZoneId.of("America/Chicago")
        private val lastAt = ZonedDateTime.of(2026, 11, 7, 17, 50, 0, 0, zone).toInstant()
    }

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(
            nightMode = if (night) NightMode.NIGHT else NightMode.NOTNIGHT,
        ),
        maxPercentDifference = 0.0,
    )

    private val mode get() = if (night) "dark" else "light"

    private val base = LogDoneState(date = LocalDate.of(2026, 11, 14), time = LocalTime.of(9, 8))

    private fun log(name: String, state: LogDoneState) = paparazzi.snapshot(name = "log_${name}_$mode") {
        InjectionTrackerTheme {
            Surface {
                LogDoneContent(state, {}, {}, {}, {}, {}, zone = zone)
            }
        }
    }

    @Test
    fun logRestNoSelection() = log("rest", base)

    @Test
    fun logRestWithLastSite() = log(
        "rest_last_site",
        base.copy(lastSite = InjectionSite.RIGHT_UPPER_ARM_PROXIMAL, lastSiteAt = lastAt),
    )

    @Test
    fun logSelected() = log(
        "selected",
        base.copy(
            site = InjectionSite.RIGHT_UPPER_THIGH_MID,
            lastSite = InjectionSite.RIGHT_UPPER_ARM_PROXIMAL,
            lastSiteAt = lastAt,
        ),
    )

    @Test
    fun logSelectedIsLastSite() = log(
        "selected_is_last_site",
        base.copy(
            site = InjectionSite.LEFT_ABDOMEN_MID,
            lastSite = InjectionSite.LEFT_ABDOMEN_MID,
            lastSiteAt = lastAt,
        ),
    )

    @Test
    fun homeMarker() = paparazzi.snapshot(name = "home_marker_$mode") {
        InjectionTrackerTheme {
            Surface {
                HomeContent(
                    state = HomeUiState(
                        loading = false,
                        medicineName = "Hyrimoz",
                        dosage = "40mg/0.4mL",
                        frequency = Frequency.EVERY_OTHER_WEEK,
                        nextInjectionAt = ZonedDateTime.of(2026, 11, 21, 17, 50, 0, 0, zone).toInstant(),
                        lastSite = InjectionSite.RIGHT_UPPER_THIGH_MID,
                        lastSiteAt = lastAt,
                    ),
                    remindersWanted = false,
                    notificationsAllowed = true,
                    onLogInjection = {},
                    onMarkMissed = {},
                    onEditRegimen = {},
                    onEnableNotifications = {},
                    now = ZonedDateTime.of(2026, 11, 14, 10, 0, 0, 0, zone).toInstant(),
                    zone = zone,
                )
            }
        }
    }
}
