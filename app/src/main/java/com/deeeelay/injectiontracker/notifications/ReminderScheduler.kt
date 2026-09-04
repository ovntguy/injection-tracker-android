package com.deeeelay.injectiontracker.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.Instant

class ReminderScheduler(context: Context) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun reschedule(nextInjectionAt: Instant?, dayBefore: Boolean, atTime: Boolean) {
        cancel(REQUEST_DAY_BEFORE, TYPE_DAY_BEFORE)
        cancel(REQUEST_AT_TIME, TYPE_AT_TIME)
        if (nextInjectionAt == null) return
        val now = Instant.now()
        if (dayBefore) {
            val whenAt = com.deeeelay.injectiontracker.domain.Scheduling.dayBeforeReminderAt(nextInjectionAt)
            if (whenAt.isAfter(now)) {
                setAlarm(REQUEST_DAY_BEFORE, whenAt, TYPE_DAY_BEFORE)
            }
        }
        if (atTime && nextInjectionAt.isAfter(now)) {
            setAlarm(REQUEST_AT_TIME, nextInjectionAt, TYPE_AT_TIME)
        }
    }

    private fun setAlarm(requestCode: Int, at: Instant, type: String) {
        val pending = pendingIntent(requestCode, type)
        val triggerAt = at.toEpochMilli()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    private fun cancel(requestCode: Int, type: String) {
        alarmManager.cancel(pendingIntent(requestCode, type))
    }

    private fun pendingIntent(requestCode: Int, type: String): PendingIntent {
        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, type)
        }
        return PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val EXTRA_TYPE = "reminder_type"
        const val TYPE_DAY_BEFORE = "day_before"
        const val TYPE_AT_TIME = "at_time"
        const val REQUEST_DAY_BEFORE = 1001
        const val REQUEST_AT_TIME = 1002
        const val NOTIFICATION_ID_DAY_BEFORE = 2001
        const val NOTIFICATION_ID_AT_TIME = 2002
        const val CHANNEL_ID = "injection_reminders"
    }
}
