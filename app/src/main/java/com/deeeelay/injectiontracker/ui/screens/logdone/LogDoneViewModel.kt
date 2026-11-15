package com.deeeelay.injectiontracker.ui.screens.logdone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deeeelay.injectiontracker.data.repo.TrackerRepository
import com.deeeelay.injectiontracker.domain.InjectionSite
import com.deeeelay.injectiontracker.domain.LogType
import com.deeeelay.injectiontracker.ui.components.combineLocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class LogDoneState(
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now().withSecond(0).withNano(0),
    val site: InjectionSite? = null,
    val lastSite: InjectionSite? = null,
    val lastSiteAt: Instant? = null,
    val saving: Boolean = false,
    val saved: Boolean = false,
) {
    /** Save is enabled as soon as a zone is picked; date and time are already defaulted. */
    val canSave: Boolean get() = site != null && !saving && !saved
}

class LogDoneViewModel(
    private val repository: TrackerRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(LogDoneState())
    val state: StateFlow<LogDoneState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.snapshot.collect { snap ->
                // Last site = most recent DONE log; Missed never moves it.
                val lastDone = snap.logs
                    .filter { it.type == LogType.DONE && it.site != null }
                    .maxByOrNull { it.loggedAt }
                _state.update { it.copy(lastSite = lastDone?.site, lastSiteAt = lastDone?.loggedAt) }
            }
        }
    }

    fun updateDate(value: LocalDate) = _state.update { it.copy(date = value) }
    fun updateTime(value: LocalTime) = _state.update { it.copy(time = value) }
    fun selectSite(site: InjectionSite) = _state.update { it.copy(site = site) }

    fun save() {
        val current = _state.value
        if (!current.canSave) return
        val site = current.site ?: return
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            repository.logDone(
                loggedAt = combineLocalDateTime(current.date, current.time, ZoneId.systemDefault()),
                site = site,
            )
            _state.update { it.copy(saving = false, saved = true) }
        }
    }
}
