package com.deeeelay.injectiontracker.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import com.deeeelay.injectiontracker.R
import com.deeeelay.injectiontracker.domain.Frequency
import com.deeeelay.injectiontracker.domain.InjectionSite
import com.deeeelay.injectiontracker.ui.navigation.MainNavigationBar
import com.deeeelay.injectiontracker.ui.navigation.MainTab
import com.deeeelay.injectiontracker.ui.theme.InjectionTrackerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

internal object P3Fixtures {
    val zone: ZoneId = ZoneId.of("America/Chicago")
    val now: Instant = ZonedDateTime.of(2026, 11, 14, 10, 0, 0, 0, zone).toInstant()
    val lastAt: Instant = ZonedDateTime.of(2026, 11, 7, 17, 50, 0, 0, zone).toInstant()
    val due: Instant = ZonedDateTime.of(2026, 11, 21, 17, 50, 0, 0, zone).toInstant()

    val hasDoneLog = HomeUiState(
        loading = false,
        medicineName = "Hyrimoz",
        dosage = "40mg/0.4mL",
        frequency = Frequency.EVERY_OTHER_WEEK,
        nextInjectionAt = due,
        lastSite = InjectionSite.RIGHT_UPPER_THIGH_MID,
        lastSiteAt = lastAt,
    )

    /** New user, or only missed logs: same state (missed never sets a last site). */
    val noDoneLogs = hasDoneLog.copy(lastSite = null, lastSiteAt = null)

    @Composable
    fun Home(state: HomeUiState) = InjectionTrackerTheme {
        Surface {
            HomeContent(
                state = state,
                remindersWanted = false,
                notificationsAllowed = true,
                onLogInjection = {},
                onMarkMissed = {},
                onEditRegimen = {},
                onEnableNotifications = {},
                now = now,
                zone = zone,
            )
        }
    }

    /** 360×640 Home with the bottom nav, the short-screen case the height cap is written for. */
    @Composable
    fun HomeWithNav(state: HomeUiState) = InjectionTrackerTheme {
        Scaffold(
            bottomBar = { MainNavigationBar(selected = MainTab.Home, onSelect = {}) },
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                HomeContent(
                    state = state,
                    remindersWanted = false,
                    notificationsAllowed = true,
                    onLogInjection = {},
                    onMarkMissed = {},
                    onEditRegimen = {},
                    onEnableNotifications = {},
                    now = now,
                    zone = zone,
                )
            }
        }
    }
}

/** Design Critique screenshots for P3 (Home + nav, items 6, 11, 16, 18), Pixel 5. */
@RunWith(Parameterized::class)
class P3HomeNavSnapshotTest(private val night: Boolean) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun modes() = listOf(false, true)
    }

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(nightMode = if (night) NightMode.NIGHT else NightMode.NOTNIGHT),
        maxPercentDifference = 0.0,
    )

    private val mode get() = if (night) "dark" else "light"
    private val now = P3Fixtures.now
    private val zone = P3Fixtures.zone

    @Test
    fun homeHasDoneLog() = paparazzi.snapshot("home_has_done_log_$mode") { P3Fixtures.Home(P3Fixtures.hasDoneLog) }

    @Test
    fun homeNoShotsLoggedYet() = paparazzi.snapshot("home_no_shots_yet_$mode") { P3Fixtures.Home(P3Fixtures.noDoneLogs) }

    @Test
    fun nextShotUpcomingStates() = paparazzi.snapshot("next_shot_upcoming_$mode") {
        Cards(
            P3Fixtures.due, // in 7 days
            ZonedDateTime.of(2026, 11, 15, 9, 0, 0, 0, zone).toInstant(), // tomorrow
            now.plus(Duration.ofHours(7).plusMinutes(50)), // today, in 7 h
            now.plus(Duration.ofMinutes(25)), // in 25 min
        )
    }

    @Test
    fun nextShotDueAndOverdueStates() = paparazzi.snapshot("next_shot_due_overdue_$mode") {
        Cards(
            now.plusSeconds(30), // due now
            now.minus(Duration.ofMinutes(40)), // overdue by 40 min
            now.minus(Duration.ofHours(3)), // overdue by 3 h
            now.minus(Duration.ofDays(1)), // 1 day overdue
            now.minus(Duration.ofDays(2)), // 2 days overdue
        )
    }

    @Test
    fun navIndicatorPerTab() = paparazzi.snapshot("nav_bar_$mode") {
        InjectionTrackerTheme {
            Surface {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    MainTab.entries.forEach { tab -> MainNavigationBar(selected = tab, onSelect = {}) }
                }
            }
        }
    }

    @Test
    fun markMissedSheetAndSnackbar() = paparazzi.snapshot("mark_missed_sheet_snackbar_$mode") {
        InjectionTrackerTheme {
            Surface {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.padding(vertical = 24.dp)) {
                    MissedSheetContent(onConfirm = {}, onCancel = {})
                    Snackbar(modifier = Modifier.padding(horizontal = 12.dp)) {
                        Text(stringResource(R.string.home_marked_missed))
                    }
                }
            }
        }
    }

    @Composable
    private fun Cards(vararg dues: Instant) = InjectionTrackerTheme {
        Surface {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                dues.forEach { NextShotCard(due = it, now = now, zone = zone) }
            }
        }
    }
}

/** Same Home states on a 360×640 dp screen. */
@RunWith(Parameterized::class)
class P3HomeSmallScreenSnapshotTest(private val night: Boolean) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun modes() = listOf(false, true)
    }

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.NEXUS_5.copy(nightMode = if (night) NightMode.NIGHT else NightMode.NOTNIGHT),
        maxPercentDifference = 0.0,
    )

    private val mode get() = if (night) "dark" else "light"

    @Test
    fun homeHasDoneLog() = paparazzi.snapshot("home_small_has_done_log_$mode") { P3Fixtures.HomeWithNav(P3Fixtures.hasDoneLog) }

    @Test
    fun homeNoShotsLoggedYet() = paparazzi.snapshot("home_small_no_shots_yet_$mode") { P3Fixtures.HomeWithNav(P3Fixtures.noDoneLogs) }
}
