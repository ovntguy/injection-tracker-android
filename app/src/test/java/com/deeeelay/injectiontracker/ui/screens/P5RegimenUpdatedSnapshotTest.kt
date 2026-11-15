package com.deeeelay.injectiontracker.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
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
import com.deeeelay.injectiontracker.ui.screens.home.HomeContent
import com.deeeelay.injectiontracker.ui.screens.home.HomeUiState
import com.deeeelay.injectiontracker.ui.screens.settings.RegimenSummary
import com.deeeelay.injectiontracker.ui.screens.settings.SettingsContent
import com.deeeelay.injectiontracker.ui.screens.settings.SettingsUiState
import com.deeeelay.injectiontracker.ui.theme.InjectionTrackerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.time.ZonedDateTime
import java.time.ZoneId

private val zone: ZoneId = ZoneId.of("America/Chicago")
private val now = ZonedDateTime.of(2026, 11, 14, 10, 0, 0, 0, zone).toInstant()
private val lastAt = ZonedDateTime.of(2026, 11, 7, 17, 50, 0, 0, zone).toInstant()
private val due = ZonedDateTime.of(2026, 11, 21, 17, 50, 0, 0, zone).toInstant()

private val settingsState = SettingsUiState(
    dayBeforeEnabled = true,
    atTimeEnabled = true,
    regimen = RegimenSummary("Hyrimoz", "40mg/0.4mL", Frequency.EVERY_OTHER_WEEK),
)

private val homeState = HomeUiState(
    loading = false,
    medicineName = "Hyrimoz",
    dosage = "40mg/0.4mL",
    frequency = Frequency.EVERY_OTHER_WEEK,
    nextInjectionAt = due,
    lastSite = InjectionSite.RIGHT_UPPER_THIGH_MID,
    lastSiteAt = lastAt,
)

@Composable
private fun ScreenWithSnackbar(tab: MainTab, content: @Composable () -> Unit) = InjectionTrackerTheme {
    Scaffold(
        snackbarHost = {
            Snackbar(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(R.string.snackbar_regimen_updated))
            }
        },
        bottomBar = { MainNavigationBar(selected = tab, onSelect = {}) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) { content() }
    }
}

@Composable
private fun SettingsShot() = ScreenWithSnackbar(MainTab.Settings) {
    SettingsContent(settingsState, notificationsAllowed = true, {}, {}, {}, {})
}

@Composable
private fun HomeShot() = ScreenWithSnackbar(MainTab.Home) {
    HomeContent(
        state = homeState,
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

@RunWith(Parameterized::class)
class P5RegimenUpdatedSnapshotTest(private val night: Boolean) {
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
    fun settingsSnackbar() = paparazzi.snapshot("settings_regimen_updated_$mode") { SettingsShot() }

    @Test
    fun homeSnackbar() = paparazzi.snapshot("home_regimen_updated_$mode") { HomeShot() }
}

/** 360×640 dp with the nav bar, the placement the snackbar is written for. */
@RunWith(Parameterized::class)
class P5RegimenUpdatedSmallScreenSnapshotTest(private val night: Boolean) {
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
    fun settingsSnackbar() = paparazzi.snapshot("settings_small_regimen_updated_$mode") { SettingsShot() }

    @Test
    fun homeSnackbar() = paparazzi.snapshot("home_small_regimen_updated_$mode") { HomeShot() }
}
