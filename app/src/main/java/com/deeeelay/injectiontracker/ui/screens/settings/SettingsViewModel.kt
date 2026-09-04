package com.deeeelay.injectiontracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deeeelay.injectiontracker.data.repo.TrackerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val dayBeforeEnabled: Boolean = true,
    val atTimeEnabled: Boolean = true,
)

class SettingsViewModel(
    private val repository: TrackerRepository,
) : ViewModel() {
    val state: StateFlow<SettingsUiState> = repository.snapshot
        .map { SettingsUiState(it.dayBeforeEnabled, it.atTimeEnabled) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setDayBefore(enabled: Boolean) {
        viewModelScope.launch { repository.setDayBeforeEnabled(enabled) }
    }

    fun setAtTime(enabled: Boolean) {
        viewModelScope.launch { repository.setAtTimeEnabled(enabled) }
    }
}
