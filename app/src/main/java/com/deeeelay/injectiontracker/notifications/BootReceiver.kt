package com.deeeelay.injectiontracker.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.deeeelay.injectiontracker.InjectionTrackerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as InjectionTrackerApp
                app.container.repository.rescheduleFromStorage()
            } finally {
                pending.finish()
            }
        }
    }
}
