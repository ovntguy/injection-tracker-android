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
    /** The picked date and time are later than now; a shot can't be logged ahead of time. */
    val inFuture: Boolean = false,
) {
    /** Save is enabled as soon as a zone is picked; date and time are already defaulted. */
    val canSave: Boolean get() = site != null && !saving && !saved && !inFuture
}

class LogDoneViewModel(
    private val repository: TrackerRepository,
    private val now: () -> Instant = Instant::now,
    private val zone: ZoneId = ZoneId.systemDefault(),
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

    fun updateDate(value: LocalDate) = _state.update { checkFuture(it.copy(date = value)) }
    fun updateTime(value: LocalTime) = _state.update { checkFuture(it.copy(time = value)) }
    fun selectSite(site: InjectionSite) = _state.update { it.copy(site = site) }

    /** Latest date the picker offers. */
    fun today(): LocalDate = now().atZone(zone).toLocalDate()

    private fun checkFuture(state: LogDoneState): LogDoneState =
        state.copy(inFuture = combineLocalDateTime(state.date, state.time, zone).isAfter(now()))

    fun save() {
        val current = checkFuture(_state.value)
        _state.value = current
        if (!current.canSave) return
        val site = current.site ?: return
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            repository.logDone(
                loggedAt = combineLocalDateTime(current.date, current.time, zone),
                site = site,
            )
            _state.update { it.copy(saving = false, saved = true) }
        }
    }
}
