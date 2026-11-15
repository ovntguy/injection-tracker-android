package com.deeeelay.injectiontracker.ui.screens.regimen

import com.deeeelay.injectiontracker.domain.Frequency
import java.time.Instant

/** How the user left a regimen form. Only a changed Edit regimen save shows the snackbar. */
enum class RegimenSaveTrigger {
    EditRegimenChanged,
    EditRegimenNoChange,
    EditRegimenCancelled,
    SetupCompleted,
    EditRegimenFailed,
}

object RegimenUpdatedSnackbar {
    const val NAV_RESULT_KEY = "regimen_updated"

    fun shouldShow(trigger: RegimenSaveTrigger): Boolean =
        trigger == RegimenSaveTrigger.EditRegimenChanged

    /** True when the Edit regimen form differs from what was loaded, after trim. */
    fun formChanged(
        originalName: String,
        originalDosage: String,
        originalFrequency: Frequency,
        originalStart: Instant,
        name: String,
        dosage: String,
        frequency: Frequency,
        start: Instant,
    ): Boolean =
        name.trim() != originalName.trim() ||
            dosage.trim() != originalDosage.trim() ||
            frequency != originalFrequency ||
            start != originalStart
}
