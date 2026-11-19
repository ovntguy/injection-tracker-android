package com.deeeelay.injectiontracker.ui.screens.regimen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deeeelay.injectiontracker.data.repo.TrackerRepository
import com.deeeelay.injectiontracker.domain.Frequency
import com.deeeelay.injectiontracker.ui.components.combineLocalDateTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class EditRegimenState(
    val loaded: Boolean = false,
    val medicineName: String = "",
    val dosage: String = "",
    val frequency: Frequency = Frequency.EVERY_WEEK,
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now().withSecond(0).withNano(0),
    val nameError: Boolean = false,
    val dosageError: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val savedWithChanges: Boolean = false,
    val saveFailed: Boolean = false,
)

class EditRegimenViewModel(
    private val repository: TrackerRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(EditRegimenState())
    val state: StateFlow<EditRegimenState> = _state.asStateFlow()

    private var originalName = ""
    private var originalDosage = ""
    private var originalFrequency = Frequency.EVERY_WEEK
    private var originalStart: Instant? = null

    init {
        viewModelScope.launch {
            val snap = repository.snapshot.first { it.regimen != null }
            val regimen = snap.regimen ?: return@launch
            val zoned = regimen.startDateTime.atZone(ZoneId.systemDefault())
            originalName = regimen.medicineName
            originalDosage = regimen.dosage
            originalFrequency = regimen.frequency
            originalStart = regimen.startDateTime
            _state.update {
                it.copy(
                    loaded = true,
                    medicineName = regimen.medicineName,
                    dosage = regimen.dosage,
                    frequency = regimen.frequency,
                    date = zoned.toLocalDate(),
                    time = zoned.toLocalTime().withSecond(0).withNano(0),
                )
            }
        }
    }

    fun updateName(value: String) = _state.update { it.copy(medicineName = value, nameError = false) }
    fun updateDosage(value: String) = _state.update { it.copy(dosage = value, dosageError = false) }
    fun updateFrequency(value: Frequency) = _state.update { it.copy(frequency = value) }
    fun updateDate(value: LocalDate) = _state.update { it.copy(date = value) }
    fun updateTime(value: LocalTime) = _state.update { it.copy(time = value) }

    fun save() {
        val current = _state.value
        if (!current.loaded || current.saving || current.saved) return
        val nameBlank = current.medicineName.isBlank()
        val dosageBlank = current.dosage.isBlank()
        if (nameBlank || dosageBlank) {
            _state.update { it.copy(nameError = nameBlank, dosageError = dosageBlank) }
            return
        }
        val start = combineLocalDateTime(current.date, current.time)
        val original = originalStart
        val changed = original != null &&
            RegimenUpdatedSnackbar.formChanged(
                originalName = originalName,
                originalDosage = originalDosage,
                originalFrequency = originalFrequency,
                originalStart = original,
                name = current.medicineName,
                dosage = current.dosage,
                frequency = current.frequency,
                start = start,
            )
        if (!changed) {
            _state.update { it.copy(saving = false, saved = true, savedWithChanges = false) }
            return
        }
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            try {
                repository.updateRegimen(
                    medicineName = current.medicineName,
                    dosage = current.dosage,
                    frequency = current.frequency,
                    startDateTime = start,
                )
                _state.update { it.copy(saving = false, saved = true, savedWithChanges = true) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.update { it.copy(saving = false, saveFailed = true) }
            }
        }
    }

    fun clearSaveFailed() = _state.update { it.copy(saveFailed = false) }
}
