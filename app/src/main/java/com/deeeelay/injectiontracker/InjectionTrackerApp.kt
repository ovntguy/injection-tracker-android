package com.deeeelay.injectiontracker

import android.app.Application
import android.util.Log
import com.deeeelay.injectiontracker.di.AppContainer
import com.deeeelay.injectiontracker.notifications.ReminderReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "InjectionTrackerApp"

class InjectionTrackerApp : Application() {
    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderReceiver.ensureChannel(this)
        applicationScope.launch {
            try {
                container.openDatabase()
                container.preferences.migrateLeftRightSiteIdsIfNeeded()
                container.repository.rescheduleFromStorage()
            } catch (e: Exception) {
                Log.e(TAG, "Startup migration or reschedule failed", e)
            }
        }
    }
}
