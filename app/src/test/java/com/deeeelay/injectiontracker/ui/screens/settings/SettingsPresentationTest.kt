package com.deeeelay.injectiontracker.ui.screens.settings

import com.deeeelay.injectiontracker.domain.Frequency
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/** P5 items 12, 13, 17: presentation and copy only. */
class SettingsPresentationTest {
    private val state = SettingsUiState(
        dayBeforeEnabled = true,
        atTimeEnabled = false,
        regimen = RegimenSummary("Hyrimoz", "40mg/0.4mL", Frequency.EVERY_OTHER_WEEK),
    )

    @Test
    fun switchesMirrorStoredPrefsWhenNotificationsAllowed() {
        assertThat(ReminderSwitches.of(state, notificationsAllowed = true))
            .isEqualTo(ReminderSwitches(dayBefore = true, atTime = false, enabled = true))
    }

    @Test
    fun deniedPermissionShowsBothOffAndDisabledWithoutTouchingPrefs() {
        assertThat(ReminderSwitches.of(state, notificationsAllowed = false))
            .isEqualTo(ReminderSwitches(dayBefore = false, atTime = false, enabled = false))
        // The stored values are untouched.
        assertThat(state.dayBeforeEnabled).isTrue()
    }

    @Test
    fun remindersDefaultOn() {
        val fresh = SettingsUiState()
        assertThat(fresh.dayBeforeEnabled).isTrue()
        assertThat(fresh.atTimeEnabled).isTrue()
    }

    @Test
    fun designStringsAreExact() {
        val xml = listOf("src/main/res/values/strings.xml", "app/src/main/res/values/strings.xml")
            .map(::File).first { it.exists() }.readText()
        fun s(name: String) = Regex("""<string name="$name">([^<]*)</string>""").find(xml)!!.groupValues[1]
        assertThat(s("settings_title")).isEqualTo("Settings")
        assertThat(s("settings_reminders")).isEqualTo("Reminders")
        assertThat(s("settings_regimen")).isEqualTo("Regimen")
        assertThat(s("settings_day_before")).isEqualTo("Day before")
        assertThat(s("settings_day_before_support")).isEqualTo("24 hours before your shot")
        assertThat(s("settings_when_due")).isEqualTo("When it\\'s due")
        assertThat(s("settings_notifications_off")).isEqualTo("Notifications are off for this app")
        assertThat(s("settings_turn_on")).isEqualTo("Turn on")
        assertThat(s("settings_regimen_headline")).isEqualTo("%1\$s %2\$s")
        assertThat(s("frequency_every_week")).isEqualTo("Every week")
        assertThat(s("frequency_every_other_week")).isEqualTo("Every other week")
        assertThat(xml).doesNotContain("Same clock as the injection")
        assertThat(xml).doesNotContain(">At time<")
    }
}
