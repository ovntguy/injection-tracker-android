package com.deeeelay.injectiontracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deeeelay.injectiontracker.data.repo.TrackerRepository
import com.deeeelay.injectiontracker.domain.Frequency
import com.deeeelay.injectiontracker.domain.InjectionSite
import com.deeeelay.injectiontracker.domain.Scheduling
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

data class HomeUiState(
    val loading: Boolean = true,
    val medicineName: String = "",
    val dosage: String = "",
    val frequency: Frequency = Frequency.EVERY_WEEK,
    val nextInjectionAt: Instant? = null,
    val overdue: Boolean = false,
    val lastSite: InjectionSite? = null,
)

class HomeViewModel(
    private val repository: TrackerRepository,
) : ViewModel() {
    val state: StateFlow<HomeUiState> = repository.snapshot
        .map { snap ->
            val regimen = snap.regimen
            HomeUiState(
                loading = false,
                medicineName = regimen?.medicineName.orEmpty(),
                dosage = regimen?.dosage.orEmpty(),
                frequency = regimen?.frequency ?: Frequency.EVERY_WEEK,
                nextInjectionAt = regimen?.nextInjectionAt,
                overdue = regimen?.let { Scheduling.isOverdue(it.nextInjectionAt, Instant.now()) } == true,
                lastSite = snap.lastDoneSite,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun logMissed() {
        viewModelScope.launch { repository.logMissed() }
    }
}
