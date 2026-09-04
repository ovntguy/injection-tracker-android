package com.deeeelay.injectiontracker.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.deeeelay.injectiontracker.data.local.AppDatabase
import com.deeeelay.injectiontracker.data.prefs.TrackerPreferences
import com.deeeelay.injectiontracker.data.repo.TrackerRepository
import com.deeeelay.injectiontracker.notifications.ReminderScheduler
import com.deeeelay.injectiontracker.ui.screens.calendar.CalendarViewModel
import com.deeeelay.injectiontracker.ui.screens.home.HomeViewModel
import com.deeeelay.injectiontracker.ui.screens.logdone.LogDoneViewModel
import com.deeeelay.injectiontracker.ui.screens.regimen.EditRegimenViewModel
import com.deeeelay.injectiontracker.ui.screens.settings.SettingsViewModel
import com.deeeelay.injectiontracker.ui.screens.setup.SetupViewModel

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.create(appContext)
    val preferences = TrackerPreferences(appContext)
    val scheduler = ReminderScheduler(appContext)
    val repository = TrackerRepository(
        logDao = database.logDao(),
        prefs = preferences,
        scheduler = scheduler,
    )
    val viewModelFactory = InjectionViewModelFactory(repository)
}

class InjectionViewModelFactory(
    private val repository: TrackerRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val vm: ViewModel = when {
            modelClass.isAssignableFrom(SetupViewModel::class.java) -> SetupViewModel(repository)
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository)
            modelClass.isAssignableFrom(LogDoneViewModel::class.java) -> LogDoneViewModel(repository)
            modelClass.isAssignableFrom(CalendarViewModel::class.java) -> CalendarViewModel(repository)
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(repository)
            modelClass.isAssignableFrom(EditRegimenViewModel::class.java) -> EditRegimenViewModel(repository)
            else -> throw IllegalArgumentException("Unknown ViewModel ${modelClass.name}")
        }
        return vm as T
    }
}
