package com.deeeelay.injectiontracker.ui.screens

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/** Both the missed-dose and Regimen updated messages go through the tab Scaffold host. */
class SharedSnackbarHostTest {
    private fun read(vararg relative: String): String =
        relative.map(::File).first { it.exists() }.readText()

    private val nav by lazy {
        read(
            "app/src/main/java/com/deeeelay/injectiontracker/ui/navigation/InjectionTrackerNav.kt",
            "src/main/java/com/deeeelay/injectiontracker/ui/navigation/InjectionTrackerNav.kt",
        )
    }
    private val home by lazy {
        read(
            "app/src/main/java/com/deeeelay/injectiontracker/ui/screens/home/HomeScreen.kt",
            "src/main/java/com/deeeelay/injectiontracker/ui/screens/home/HomeScreen.kt",
        )
    }

    @Test
    fun oneHostShowsMissedDoseAndRegimenUpdated() {
        assertThat(Regex("""SnackbarHost\(""").findAll(nav).count()).isEqualTo(1)
        assertThat(nav).contains("R.string.home_marked_missed")
        assertThat(nav).contains("R.string.snackbar_regimen_updated")
        assertThat(nav).contains("SnackbarDuration.Short")
        assertThat(nav).contains("onMissedLogged = { showTabSnackbar(missedMessage) }")
        assertThat(home).doesNotContain("SnackbarHost")
        assertThat(home).contains("onMissedLogged()")
    }
}
