package com.fitpulse.ai.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.fitpulse.ai.MainActivity
import com.fitpulse.ai.R
import com.fitpulse.ai.data.model.ReminderSettings
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("reminder_type") ?: "workout"
        val title = if (type == "water") "Hydration Check 💧" else "Time to Train! 💪"
        val body = if (type == "water") {
            "Drink 250 ml of water to stay on track with your recovery target."
        } else {
            "Your FitPanda workout is ready. Let's build momentum today!"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "fitpulse_reminders"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "FitPanda Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            type.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        manager.notify(type.hashCode(), notification)
    }
}

object ReminderScheduler {
    private const val REQ_WORKOUT = 1001
    private const val REQ_WATER = 1002

    fun applySettings(context: Context, settings: ReminderSettings) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val workoutIntent = PendingIntent.getBroadcast(
            context,
            REQ_WORKOUT,
            Intent(context, ReminderReceiver::class.java).putExtra("reminder_type", "workout"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (settings.workoutReminderEnabled) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, settings.workoutReminderHour)
                set(Calendar.MINUTE, settings.workoutReminderMinute)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                workoutIntent
            )
        } else {
            alarmManager.cancel(workoutIntent)
        }

        val waterIntent = PendingIntent.getBroadcast(
            context,
            REQ_WATER,
            Intent(context, ReminderReceiver::class.java).putExtra("reminder_type", "water"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (settings.waterReminderEnabled) {
            val startAt = System.currentTimeMillis() + (2 * 60 * 60 * 1000L)
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                startAt,
                2 * 60 * 60 * 1000L,
                waterIntent
            )
        } else {
            alarmManager.cancel(waterIntent)
        }
    }
}
