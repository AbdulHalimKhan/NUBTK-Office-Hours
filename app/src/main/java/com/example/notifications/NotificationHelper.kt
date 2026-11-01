package com.example.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "nubtk_timer_channel"
    private const val CHANNEL_NAME = "NUBTK Timer Alerts"
    private const val NOTIFICATION_ID = 1001
    private const val ALARM_REQUEST_CODE = 2001
    const val ACTION_WORK_HOURS_COMPLETED = "com.example.ACTION_WORK_HOURS_COMPLETED"

    private const val PREFS_NAME = "nubtk_notification_prefs"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_LAST_NOTIFIED_SESSION = "last_notified_session"

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when daily working hours goal is met"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun isNotificationEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        if (!enabled) {
            cancelWorkHoursAlarm(context)
        }
    }

    fun hasNotifiedForSession(context: Context, sessionKey: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_NOTIFIED_SESSION, null) == sessionKey
    }

    fun markNotifiedForSession(context: Context, sessionKey: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_NOTIFIED_SESSION, sessionKey).apply()
    }

    /**
     * Resets the notification state so a new target schedule or new punch session can notify.
     */
    fun resetNotificationTriggeredState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_LAST_NOTIFIED_SESSION).apply()
    }

    /**
     * Attempts to trigger the Office Hours countdown completion notification.
     * Enforces strict single-trigger semantics:
     * 1. Checks user ON/OFF preference.
     * 2. Checks system notification permission.
     * 3. Checks persistent session key (date + punchInTimestamp + dailyMinutes).
     * Returns true if notification was posted, false if suppressed or disabled.
     */
    fun maybeNotifyWorkHoursCompleted(
        context: Context,
        dateIso: String,
        punchInTimestamp: Long,
        targetHours: Int,
        targetMinutes: Int
    ): Boolean {
        if (!isNotificationEnabled(context)) {
            return false
        }

        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) {
            return false
        }

        val totalMins = (targetHours * 60) + targetMinutes
        val sessionKey = "${dateIso}_${punchInTimestamp}_${totalMins}"

        if (hasNotifiedForSession(context, sessionKey)) {
            return false
        }

        showWorkHoursCompletedNotification(context, targetHours, targetMinutes)
        markNotifiedForSession(context, sessionKey)
        return true
    }

    fun showWorkHoursCompletedNotification(context: Context, targetHours: Int, targetMinutes: Int) {
        initNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeString = if (targetMinutes > 0) "${targetHours}h ${targetMinutes}m" else "${targetHours} hours"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Office Hours Completed! ⏱️")
            .setContentText("You have completed your daily working hours ($timeString). Don't forget to Punch Out!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Great job! You have reached your scheduled daily office hours ($timeString) for today. Please remember to Punch Out before leaving.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permission might not be granted yet on Android 13+
        }
    }

    fun scheduleWorkHoursAlarm(context: Context, punchInTimestamp: Long, dailyWorkingMinutes: Int) {
        if (!isNotificationEnabled(context)) return

        val triggerAtMillis = punchInTimestamp + (dailyWorkingMinutes * 60 * 1000L)
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, WorkHoursAlarmReceiver::class.java).apply {
            action = ACTION_WORK_HOURS_COMPLETED
            putExtra("dailyWorkingMinutes", dailyWorkingMinutes)
            putExtra("punchInTimestamp", punchInTimestamp)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelWorkHoursAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, WorkHoursAlarmReceiver::class.java).apply {
            action = ACTION_WORK_HOURS_COMPLETED
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
