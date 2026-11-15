package com.deeeelay.injectiontracker.ui.screens.calendar

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * Day cell states (BUILD FROM HERE, rev 2026-11-15): 40 dp circles, selected is
 * primaryContainer (never primary), today is a 1 dp primary outline, markers 2 dp below.
 */
class CalendarDayCellTest {
    private val source by lazy {
        listOf(
            "app/src/main/java/com/deeeelay/injectiontracker/ui/screens/calendar/CalendarScreen.kt",
            "src/main/java/com/deeeelay/injectiontracker/ui/screens/calendar/CalendarScreen.kt",
        ).map(::File).first { it.exists() }.readText()
    }

    @Test
    fun dateCircleIs40dpAndMarkersSit2dpBelowWith2dpGap() {
        assertThat(source).contains("private val DateCircle = 40.dp")
        assertThat(source).contains("private val TodayOutline = 1.dp")
        assertThat(source).contains("private val MarkerGap = 2.dp")
        assertThat(source).contains("Arrangement.spacedBy(MarkerGap)")
        assertThat(source).contains("Modifier.padding(top = MarkerGap)")
    }

    @Test
    fun selectedCircleUsesPrimaryContainerNeverPrimaryFill() {
        assertThat(source).contains("Modifier.background(scheme.primaryContainer, CircleShape)")
        assertThat(source).contains("selected -> scheme.onPrimaryContainer")
        assertThat(source).doesNotContain("background(scheme.primary, CircleShape)")
        assertThat(source).contains("today -> Modifier.border(TodayOutline, scheme.primary, CircleShape)")
        assertThat(source).contains("today -> scheme.primary")
    }
}
