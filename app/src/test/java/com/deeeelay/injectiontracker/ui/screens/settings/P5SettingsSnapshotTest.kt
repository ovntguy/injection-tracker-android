package com.deeeelay.injectiontracker.ui.screens.settings

import androidx.compose.material3.Surface
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import com.deeeelay.injectiontracker.domain.Frequency
import com.deeeelay.injectiontracker.ui.theme.InjectionTrackerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

private val everyOtherWeek = SettingsUiState(
    dayBeforeEnabled = true,
    atTimeEnabled = true,
    regimen = RegimenSummary("Hyrimoz", "40mg/0.4mL", Frequency.EVERY_OTHER_WEEK),
)

private fun shot(p: Paparazzi, name: String, state: SettingsUiState, allowed: Boolean) = p.snapshot(name) {
    InjectionTrackerTheme {
        Surface { SettingsContent(state, allowed, {}, {}, {}, {}) }
    }
}

/** Design Critique screenshots for P5 (Settings items 12, 13, 17), Pixel 5. */
@RunWith(Parameterized::class)
class P5SettingsSnapshotTest(private val night: Boolean) {
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
    fun defaultBothOn() = shot(paparazzi, "settings_default_on_$mode", everyOtherWeek, allowed = true)

    @Test
    fun oneReminderOffEveryWeek() = shot(
        paparazzi,
        "settings_when_due_off_every_week_$mode",
        everyOtherWeek.copy(atTimeEnabled = false, regimen = everyOtherWeek.regimen!!.copy(frequency = Frequency.EVERY_WEEK)),
        allowed = true,
    )

    @Test
    fun notificationsDenied() = shot(paparazzi, "settings_notifications_denied_$mode", everyOtherWeek, allowed = false)
}

/** 360×640 dp. */
@RunWith(Parameterized::class)
class P5SettingsSmallScreenSnapshotTest(private val night: Boolean) {
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
    fun defaultBothOn() = shot(paparazzi, "settings_small_default_on_$mode", everyOtherWeek, allowed = true)

    @Test
    fun notificationsDenied() = shot(paparazzi, "settings_small_notifications_denied_$mode", everyOtherWeek, allowed = false)
}
