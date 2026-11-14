package com.deeeelay.injectiontracker.ui

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.deeeelay.injectiontracker.domain.InjectionLog
import com.deeeelay.injectiontracker.domain.InjectionSite
import com.deeeelay.injectiontracker.domain.LogType
import com.deeeelay.injectiontracker.domain.TrackerSnapshot
import com.deeeelay.injectiontracker.ui.components.Formatters
import com.deeeelay.injectiontracker.ui.theme.LightColors
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime

/** P3 (items 6, 11, 16, 18): exact Design strings, date format, last-site source, nav colors. */
class HomeStringsAndThemeTest {
    private val strings by lazy {
        val f = listOf("src/main/res/values/strings.xml", "app/src/main/res/values/strings.xml")
            .map(::File).first { it.exists() }
        f.readText()
    }

    private fun string(name: String): String =
        Regex("""<string name="$name">([^<]*)</string>""").find(strings)!!.groupValues[1]

    private fun plural(name: String, quantity: String): String =
        Regex("""<plurals name="$name">[\s\S]*?<item quantity="$quantity">([^<]*)</item>""")
            .find(strings)!!.groupValues[1]

    @Test
    fun designStringsAreExact() {
        assertThat(string("home_last_site")).isEqualTo("Last site")
        assertThat(string("home_no_shots_yet")).isEqualTo("No shots logged yet")
        assertThat(string("home_last_site_description")).isEqualTo("Last site: %1\$s, %2\$s")
        assertThat(string("home_next_shot")).isEqualTo("Next shot")
        assertThat(string("action_mark_as_missed")).isEqualTo("Mark as missed")
        assertThat(string("missed_title")).isEqualTo("Mark as missed?")
        assertThat(string("action_mark_missed")).isEqualTo("Mark missed")
        assertThat(string("home_marked_missed")).isEqualTo("Marked missed · schedule updated")
        assertThat(plural("next_in_days", "other")).isEqualTo("in %d days")
        assertThat(string("next_tomorrow")).isEqualTo("tomorrow")
        assertThat(string("next_today_in_hours")).isEqualTo("today, in %d h")
        assertThat(string("next_in_minutes")).isEqualTo("in %d min")
        assertThat(string("next_due_now")).isEqualTo("due now")
        assertThat(string("next_overdue_by_hours")).isEqualTo("overdue by %d h")
        assertThat(string("next_overdue_by_minutes")).isEqualTo("overdue by %d min")
        assertThat(plural("next_days_overdue", "one")).isEqualTo("%d day overdue")
        assertThat(plural("next_days_overdue", "other")).isEqualTo("%d days overdue")
        assertThat(string("nav_home")).isEqualTo("Home")
        assertThat(string("nav_calendar")).isEqualTo("Calendar")
        assertThat(string("nav_settings")).isEqualTo("Settings")
        assertThat(strings).doesNotContain("I missed it")
    }

    @Test
    fun shortDateAddsYearOnlyOutsideTheCurrentYear() {
        val zone = ZoneId.of("America/Chicago")
        val now = ZonedDateTime.of(2026, 11, 14, 10, 0, 0, 0, zone).toInstant()
        val nov7 = ZonedDateTime.of(2026, 11, 7, 17, 50, 0, 0, zone).toInstant()
        val dec30 = ZonedDateTime.of(2025, 12, 30, 9, 0, 0, 0, zone).toInstant()
        assertThat(Formatters.shortDate(nov7, now, zone)).isEqualTo("Nov 7")
        assertThat(Formatters.shortDate(dec30, now, zone)).isEqualTo("Dec 30, 2025")
    }

    @Test
    fun lastSiteDateComesFromLastDoneLogNotLastMiss() {
        val t0 = ZonedDateTime.of(2026, 11, 7, 17, 50, 0, 0, ZoneId.of("America/Chicago")).toInstant()
        val snap = TrackerSnapshot(
            setupComplete = true,
            regimen = null,
            logs = listOf(
                InjectionLog("d1", LogType.DONE, t0, site = InjectionSite.RIGHT_UPPER_THIGH_MID),
                InjectionLog("m1", LogType.MISSED, t0.plusSeconds(86_400 * 7), site = null),
            ),
            dayBeforeEnabled = false,
            atTimeEnabled = false,
        )
        assertThat(snap.lastDoneLog?.loggedAt).isEqualTo(t0)
        assertThat(snap.lastDoneSite).isEqualTo(InjectionSite.RIGHT_UPPER_THIGH_MID)
        val onlyMissed = snap.copy(logs = snap.logs.filter { it.type == LogType.MISSED })
        assertThat(onlyMissed.lastDoneLog).isNull()
    }

    @Test
    fun navColorsComeFromTheSeedBaselineNotTeal() {
        val baseline = lightColorScheme()
        assertThat(LightColors.primary).isEqualTo(Color(0xFF6750A4))
        assertThat(LightColors.secondaryContainer).isEqualTo(baseline.secondaryContainer)
        assertThat(LightColors.onSecondaryContainer).isEqualTo(baseline.onSecondaryContainer)
        assertThat(LightColors.secondary).isEqualTo(baseline.secondary)
        assertThat(LightColors.secondaryContainer).isNotEqualTo(Color(0xFFB2DFDB))
    }
}
