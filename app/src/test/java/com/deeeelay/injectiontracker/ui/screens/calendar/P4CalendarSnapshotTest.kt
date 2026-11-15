package com.deeeelay.injectiontracker.ui.screens.calendar

import androidx.compose.material3.Surface
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import com.deeeelay.injectiontracker.ui.theme.InjectionTrackerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.time.LocalDate
import java.time.YearMonth

private fun snap(paparazzi: Paparazzi, name: String, state: CalendarUiState) = paparazzi.snapshot(name) {
    InjectionTrackerTheme {
        Surface {
            CalendarContent(state, {}, {}, {}, now = CalendarFixtures.now, zone = CalendarFixtures.zone)
        }
    }
}

private val nov = YearMonth.of(2026, 11)

/** Design Critique screenshots for P4 (Calendar items 9 and 10), Pixel 5. Today is Sat Nov 14, 2026. */
@RunWith(Parameterized::class)
class P4CalendarSnapshotTest(private val night: Boolean) {
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

    @Test
    fun openTodayNothingLogged() = snap(paparazzi, "calendar_open_today_empty_$mode", CalendarFixtures.state(nov, CalendarFixtures.today))

    @Test
    fun dayWithMissedAndDone() = snap(paparazzi, "calendar_day_missed_and_done_$mode", CalendarFixtures.state(nov, LocalDate.of(2026, 11, 7)))

    @Test
    fun dayWithUpcoming() = snap(paparazzi, "calendar_day_upcoming_$mode", CalendarFixtures.state(nov, LocalDate.of(2026, 11, 21)))

    @Test
    fun otherMonthNoSelection() = snap(paparazzi, "calendar_other_month_no_selection_$mode", CalendarFixtures.state(YearMonth.of(2026, 10), null))

    @Test
    fun otherMonthDayTapped() = snap(paparazzi, "calendar_other_month_day_done_$mode", CalendarFixtures.state(YearMonth.of(2026, 10), LocalDate.of(2026, 10, 24)))
}

/** 360×640 dp. */
@RunWith(Parameterized::class)
class P4CalendarSmallScreenSnapshotTest(private val night: Boolean) {
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
    fun openTodayNothingLogged() = snap(paparazzi, "calendar_small_open_today_empty_$mode", CalendarFixtures.state(nov, CalendarFixtures.today))

    @Test
    fun dayWithMissedAndDone() = snap(paparazzi, "calendar_small_day_missed_and_done_$mode", CalendarFixtures.state(nov, LocalDate.of(2026, 11, 7)))

    @Test
    fun dayWithUpcoming() = snap(paparazzi, "calendar_small_day_upcoming_$mode", CalendarFixtures.state(nov, LocalDate.of(2026, 11, 21)))
}
