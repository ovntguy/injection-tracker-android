package com.deeeelay.injectiontracker.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class LauncherIconResourcesTest {
    private fun res(path: String): File = listOf(
        File("app/src/main/res/$path"),
        File("../app/src/main/res/$path"),
        File("src/main/res/$path"),
    ).first { it.exists() }

    @Test
    fun bothAdaptiveIconsWireMonochromeLayer() {
        listOf("ic_launcher.xml", "ic_launcher_round.xml").forEach { name ->
            val xml = res("mipmap-anydpi-v26/$name").readText()
            assertThat(xml).contains("<background android:drawable=\"@drawable/ic_launcher_background\"/>")
            assertThat(xml).contains("<foreground android:drawable=\"@drawable/ic_launcher_foreground\"/>")
            assertThat(xml).contains("<monochrome android:drawable=\"@drawable/ic_launcher_monochrome\"/>")
        }
        assertThat(res("drawable/ic_launcher_monochrome.xml").exists()).isTrue()
    }
}
