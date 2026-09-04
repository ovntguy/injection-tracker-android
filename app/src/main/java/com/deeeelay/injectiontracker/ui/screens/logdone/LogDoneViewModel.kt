package com.deeeelay.injectiontracker.ui.screens.logdone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deeeelay.injectiontracker.data.repo.TrackerRepository
import com.deeeelay.injectiontracker.domain.InjectionSite
import com.deeeelay.injectiontracker.ui.components.combineLocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class LogDoneState(
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now().withSecond(0).withNano(0),
    val site: InjectionSite? = null,
    val siteError: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
)

class LogDoneViewModel(
    private val repository: TrackerRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(LogDoneState())
    val state: StateFlow<LogDoneState> = _state.asStateFlow()

    fun updateDate(value: LocalDate) = _state.update { it.copy(date = value) }
    fun updateTime(value: LocalTime) = _state.update { it.copy(time = value) }
    fun selectSite(site: InjectionSite) = _state.update { it.copy(site = site, siteError = false) }

    fun save() {
        val current = _state.value
        val site = current.site
        if (site == null) {
            _state.update { it.copy(siteError = true) }
            return
        }
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
