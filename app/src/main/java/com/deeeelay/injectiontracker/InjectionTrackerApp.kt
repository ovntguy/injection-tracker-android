package com.deeeelay.injectiontracker

import android.app.Application
import com.deeeelay.injectiontracker.di.AppContainer
import com.deeeelay.injectiontracker.notifications.ReminderReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class InjectionTrackerApp : Application() {
    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderReceiver.ensureChannel(this)
        applicationScope.launch {
            container.repository.rescheduleFromStorage()
        }
    }
}
