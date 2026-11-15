package com.deeeelay.injectiontracker.ui.screens.home

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/** Item 16: the relative next-shot line refreshes on resume, not only on the minute tick. */
class HomeClockTest {
    private val source by lazy {
        listOf(
            "app/src/main/java/com/deeeelay/injectiontracker/ui/screens/home/HomeScreen.kt",
            "src/main/java/com/deeeelay/injectiontracker/ui/screens/home/HomeScreen.kt",
        ).map(::File).first { it.exists() }.readText()
    }

    @Test
    fun relativeLineRefreshesOnResumeAndEveryMinute() {
        assertThat(source).contains("fun rememberHomeNow")
        assertThat(source).contains("val now = rememberHomeNow()")
        assertThat(source).contains("Lifecycle.Event.ON_RESUME")
        assertThat(source).contains("now = Instant.now()")
        assertThat(source).contains("delay(60_000L - (System.currentTimeMillis() % 60_000L))")
    }
}
