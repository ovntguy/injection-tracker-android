package com.deeeelay.injectiontracker.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.deeeelay.injectiontracker.InjectionTrackerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "BootReceiver"

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in RESCHEDULE_ACTIONS) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as InjectionTrackerApp
                app.container.repository.rescheduleFromStorage()
            } catch (e: Exception) {
                Log.e(TAG, "Reschedule failed", e)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        /**
         * Boot clears alarms; an exact-alarm grant lets the pending ones be re-set as exact.
         * The API 31 action is a compile-time string; older devices simply never send it.
         */
        @SuppressLint("InlinedApi")
        val RESCHEDULE_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
        )
    }
}
