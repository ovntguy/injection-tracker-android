package com.deeeelay.injectiontracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deeeelay.injectiontracker.data.repo.TrackerRepository
import com.deeeelay.injectiontracker.domain.Frequency
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val dayBeforeEnabled: Boolean = true,
    val atTimeEnabled: Boolean = true,
    /** Current regimen for the Regimen row (item 13); null before setup. */
    val regimen: RegimenSummary? = null,
)

data class RegimenSummary(
    val medicineName: String,
    val dosage: String,
    val frequency: Frequency,
)

/** What the reminder switches show (item 17); the stored preferences are never changed here. */
data class ReminderSwitches(val dayBefore: Boolean, val atTime: Boolean, val enabled: Boolean) {
    companion object {
        fun of(state: SettingsUiState, notificationsAllowed: Boolean) =
            if (notificationsAllowed) {
                ReminderSwitches(state.dayBeforeEnabled, state.atTimeEnabled, enabled = true)
            } else {
                ReminderSwitches(dayBefore = false, atTime = false, enabled = false)
            }
    }
}

class SettingsViewModel(
    private val repository: TrackerRepository,
) : ViewModel() {
    val state: StateFlow<SettingsUiState> = repository.snapshot
        .map { snap ->
            SettingsUiState(
                dayBeforeEnabled = snap.dayBeforeEnabled,
                atTimeEnabled = snap.atTimeEnabled,
                regimen = snap.regimen?.let { RegimenSummary(it.medicineName, it.dosage, it.frequency) },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setDayBefore(enabled: Boolean) {
        viewModelScope.launch { repository.setDayBeforeEnabled(enabled) }
    }

    fun setAtTime(enabled: Boolean) {
        viewModelScope.launch { repository.setAtTimeEnabled(enabled) }
    }
}
