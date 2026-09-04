package com.deeeelay.injectiontracker.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.deeeelay.injectiontracker.MainActivity
import com.deeeelay.injectiontracker.R
import com.deeeelay.injectiontracker.data.prefs.TrackerPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(ReminderScheduler.EXTRA_TYPE) ?: return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ensureChannel(context)
                val prefs = TrackerPreferences(context).current()
                val regimen = prefs.regimen ?: return@launch
                val timeText = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                    .withZone(ZoneId.systemDefault())
                    .format(regimen.nextInjectionAt)
                val (title, text, id) = if (type == ReminderScheduler.TYPE_DAY_BEFORE) {
                    Triple(
                        context.getString(R.string.notif_day_before_title),
                        context.getString(R.string.notif_day_before_text, regimen.medicineName, timeText),
                        ReminderScheduler.NOTIFICATION_ID_DAY_BEFORE,
                    )
                } else {
                    Triple(
                        context.getString(R.string.notif_at_time_title),
                        context.getString(
                            R.string.notif_at_time_text,
                            regimen.medicineName,
                            regimen.dosage,
                        ),
                        ReminderScheduler.NOTIFICATION_ID_AT_TIME,
                    )
                }
                val openApp = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_injection)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setContentIntent(openApp)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()
                if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                    NotificationManagerCompat.from(context).notify(id, notification)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        fun ensureChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(NotificationManager::class.java)
                val channel = NotificationChannel(
                    ReminderScheduler.CHANNEL_ID,
                    context.getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = context.getString(R.string.notif_channel_desc)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }
}
