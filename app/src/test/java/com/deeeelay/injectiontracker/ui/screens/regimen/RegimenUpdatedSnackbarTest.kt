package com.deeeelay.injectiontracker.ui.screens.regimen

import com.deeeelay.injectiontracker.domain.Frequency
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File
import java.time.Instant

class RegimenUpdatedSnackbarTest {
    private val start = Instant.parse("2026-11-07T23:50:00Z")

    @Test
    fun showsOnAChangedSave() {
        assertThat(RegimenUpdatedSnackbar.shouldShow(RegimenSaveTrigger.EditRegimenChanged)).isTrue()
        assertThat(
            RegimenUpdatedSnackbar.formChanged(
                originalName = "Hyrimoz",
                originalDosage = "40mg/0.4mL",
                originalFrequency = Frequency.EVERY_OTHER_WEEK,
                originalStart = start,
                name = "Hyrimoz",
                dosage = "40mg/0.4mL",
                frequency = Frequency.EVERY_WEEK,
                start = start,
            ),
        ).isTrue()
    }

    @Test
    fun notOnANoOpSave() {
        assertThat(RegimenUpdatedSnackbar.shouldShow(RegimenSaveTrigger.EditRegimenNoChange)).isFalse()
        assertThat(
            RegimenUpdatedSnackbar.formChanged(
                originalName = "Hyrimoz",
                originalDosage = "40mg/0.4mL",
                originalFrequency = Frequency.EVERY_OTHER_WEEK,
                originalStart = start,
                name = "  Hyrimoz  ",
                dosage = "40mg/0.4mL",
                frequency = Frequency.EVERY_OTHER_WEEK,
                start = start,
            ),
        ).isFalse()
    }

    @Test
    fun notOnSetup() {
        assertThat(RegimenUpdatedSnackbar.shouldShow(RegimenSaveTrigger.SetupCompleted)).isFalse()
    }

    @Test
    fun notOnACancel() {
        assertThat(RegimenUpdatedSnackbar.shouldShow(RegimenSaveTrigger.EditRegimenCancelled)).isFalse()
    }

    @Test
    fun notOnAFailedSave() {
        assertThat(RegimenUpdatedSnackbar.shouldShow(RegimenSaveTrigger.EditRegimenFailed)).isFalse()
    }

    @Test
    fun copyIsExactAndHasNoAction() {
        val xml = listOf("src/main/res/values/strings.xml", "app/src/main/res/values/strings.xml")
            .map(::File).first { it.exists() }.readText()
        val value = Regex("""<string name="snackbar_regimen_updated">([^<]*)</string>""")
            .find(xml)!!.groupValues[1]
        assertThat(value).isEqualTo("Regimen updated")
        assertThat(value).doesNotContain(".")
    }
}
