package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import com.example.MainActivity
import com.example.R
import com.example.data.local.AttendanceDatabase
import com.example.data.repository.AttendanceRepository
import com.example.notifications.NotificationHelper
import com.example.utils.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

class NubtkTimerWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_PUNCH_IN = "com.example.widget.ACTION_WIDGET_PUNCH_IN"
        const val ACTION_WIDGET_PUNCH_OUT = "com.example.widget.ACTION_WIDGET_PUNCH_OUT"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, NubtkTimerWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, NubtkTimerWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                updateAllWidgets(context)
            }
            ACTION_WIDGET_PUNCH_IN -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AttendanceDatabase.getDatabase(context)
                        val repository = AttendanceRepository(db.attendanceDao())
                        val activePeriod = repository.getActivePeriod()
                        if (activePeriod == null) {
                            showToast(context, "No active attendance period found.")
                        } else {
                            val todayDate = DateTimeUtils.today()
                            val todayIso = DateTimeUtils.todayIso()
                            val weekends = repository.getWeekendSettingsForPeriod(activePeriod.id).first().map { it.dayOfWeek }.toSet()
                            val holidays = repository.getHolidaysForPeriod(activePeriod.id).first().associateBy { it.date }
                            val leaves = repository.getLeavesForPeriod(activePeriod.id).first().associateBy { it.date }

                            val holiday = holidays[todayIso]
                            val isOfficialHoliday = holiday != null && !holiday.isOfficeDay
                            val leave = leaves[todayIso]
                            val isApprovedLeave = leave != null
                            val isWeekend = weekends.contains(todayDate.dayOfWeek.value)

                            // Priority 1: Official Holiday, Priority 2: Approved Leave, Priority 3: Weekend
                            when {
                                isOfficialHoliday -> {
                                    val name = holiday?.name ?: "Official Holiday"
                                    showToast(context, "Today is an official holiday ($name)! Punch In disabled.")
                                }
                                isApprovedLeave -> {
                                    val type = if (leave?.leaveType.equals("MEDICAL", true)) "Medical" else "Casual"
                                    showToast(context, "Today is your approved $type leave! Punch In disabled.")
                                }
                                isWeekend -> {
                                    showToast(context, "Today is your off day (Weekend)! Punch In disabled.")
                                }
                                else -> {
                                    val result = repository.punchIn(activePeriod.id, todayIso)
                                    result.onSuccess { record ->
                                        val formattedTime = DateTimeUtils.formatTimestampTo12H(record.punchInTimestamp)
                                        showToast(context, "Punched In at $formattedTime")
                                        NotificationHelper.scheduleWorkHoursAlarm(
                                            context,
                                            record.punchInTimestamp ?: System.currentTimeMillis(),
                                            activePeriod.dailyWorkingMinutes
                                        )
                                    }.onFailure { error ->
                                        showToast(context, error.message ?: "Punch In failed")
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        showToast(context, "Error: ${e.message}")
                    } finally {
                        updateAllWidgets(context)
                        pendingResult.finish()
                    }
                }
            }
            ACTION_WIDGET_PUNCH_OUT -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AttendanceDatabase.getDatabase(context)
                        val repository = AttendanceRepository(db.attendanceDao())
                        val activePeriod = repository.getActivePeriod()
                        if (activePeriod == null) {
                            showToast(context, "No active attendance period found.")
                        } else {
                            val todayIso = DateTimeUtils.todayIso()
                            val result = repository.punchOut(activePeriod.id, todayIso)
                            result.onSuccess { record ->
                                val formattedTime = DateTimeUtils.formatTimestampTo12H(record.punchOutTimestamp ?: System.currentTimeMillis())
                                val dur = DateTimeUtils.formatMinutesToHoursMinutes(record.totalWorkingMinutes)
                                showToast(context, "Punched Out at $formattedTime (Worked: $dur)")
                                NotificationHelper.cancelWorkHoursAlarm(context)
                            }.onFailure { error ->
                                showToast(context, error.message ?: "Punch Out failed")
                            }
                        }
                    } catch (e: Exception) {
                        showToast(context, "Error: ${e.message}")
                    } finally {
                        updateAllWidgets(context)
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    private suspend fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_nubtk_timer)

        // Launch app on background tap
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, appPendingIntent)

        // Punch In PendingIntent
        val punchInIntent = Intent(context, NubtkTimerWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_PUNCH_IN
        }
        val punchInPendingIntent = PendingIntent.getBroadcast(
            context,
            101,
            punchInIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_punch_in, punchInPendingIntent)

        // Punch Out PendingIntent
        val punchOutIntent = Intent(context, NubtkTimerWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_PUNCH_OUT
        }
        val punchOutPendingIntent = PendingIntent.getBroadcast(
            context,
            102,
            punchOutIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_widget_punch_out, punchOutPendingIntent)

        // Format today's date using Bangladesh local time
        val today = DateTimeUtils.today()
        val dateFormatted = today.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))
        views.setTextViewText(R.id.widget_date_text, dateFormatted)

        try {
            val db = AttendanceDatabase.getDatabase(context)
            val repository = AttendanceRepository(db.attendanceDao())
            val activePeriod = repository.getActivePeriod()

            if (activePeriod == null) {
                // Fallback: No active period setup
                views.setTextViewText(R.id.widget_status_badge, "NO ACTIVE PERIOD")
                views.setViewVisibility(R.id.widget_timer_container, View.VISIBLE)
                views.setViewVisibility(R.id.widget_special_day_container, View.GONE)
                views.setTextViewText(R.id.widget_countdown_text, "--:--:--")
                views.setTextViewText(R.id.widget_sub_text, "Open NUBTK Timer to set up period")
                views.setViewVisibility(R.id.widget_offday_text, View.GONE)
                views.setViewVisibility(R.id.widget_actions_row, View.VISIBLE)
            } else {
                val todayIso = DateTimeUtils.todayIso()
                val weekends = repository.getWeekendSettingsForPeriod(activePeriod.id).first().map { it.dayOfWeek }.toSet()
                val holidays = repository.getHolidaysForPeriod(activePeriod.id).first().associateBy { it.date }
                val leaves = repository.getLeavesForPeriod(activePeriod.id).first().associateBy { it.date }
                val record = repository.getRecordForDate(activePeriod.id, todayIso)

                val holiday = holidays[todayIso]
                val isOfficialHoliday = holiday != null && !holiday.isOfficeDay
                val leave = leaves[todayIso]
                val isApprovedLeave = leave != null
                val isWeekend = weekends.contains(today.dayOfWeek.value)

                // DISPLAY PRIORITY:
                // 1. Official Holiday > 2. Approved Leave > 3. Weekend > 4. Normal Working Day
                when {
                    isOfficialHoliday -> {
                        // CASE B: OFFICIAL HOLIDAY
                        // Display badge "HOLIDAY", show holiday name/description, completely hide time count
                        views.setTextViewText(R.id.widget_status_badge, "HOLIDAY")
                        views.setViewVisibility(R.id.widget_timer_container, View.GONE)
                        views.setViewVisibility(R.id.widget_special_day_container, View.VISIBLE)
                        views.setTextViewText(
                            R.id.widget_special_day_title,
                            holiday?.name?.ifBlank { "Official Holiday" } ?: "Official Holiday"
                        )
                        views.setTextViewText(R.id.widget_special_day_desc, "Public Holiday • Office Closed")

                        views.setViewVisibility(R.id.widget_actions_row, View.GONE)
                        views.setViewVisibility(R.id.widget_offday_text, View.VISIBLE)
                        views.setTextViewText(R.id.widget_offday_text, "Official Holiday")
                    }

                    isApprovedLeave -> {
                        // CASE C: APPROVED LEAVE
                        // Display badge "ON LEAVE", show leave reason/info, completely hide time count
                        views.setTextViewText(R.id.widget_status_badge, "ON LEAVE")
                        views.setViewVisibility(R.id.widget_timer_container, View.GONE)
                        views.setViewVisibility(R.id.widget_special_day_container, View.VISIBLE)

                        val leaveTypeStr = if (leave?.leaveType.equals("MEDICAL", ignoreCase = true)) {
                            "Medical Leave"
                        } else {
                            "Casual Leave"
                        }
                        views.setTextViewText(R.id.widget_special_day_title, leaveTypeStr)
                        views.setTextViewText(
                            R.id.widget_special_day_desc,
                            leave?.note?.ifBlank { null } ?: "Approved Leave Day • Off Duty"
                        )

                        views.setViewVisibility(R.id.widget_actions_row, View.GONE)
                        views.setViewVisibility(R.id.widget_offday_text, View.VISIBLE)
                        views.setTextViewText(R.id.widget_offday_text, "Approved Leave Day")
                    }

                    isWeekend -> {
                        // WEEKEND OFF DAY
                        views.setTextViewText(R.id.widget_status_badge, "WEEKEND")
                        views.setViewVisibility(R.id.widget_timer_container, View.GONE)
                        views.setViewVisibility(R.id.widget_special_day_container, View.VISIBLE)
                        views.setTextViewText(R.id.widget_special_day_title, "Weekend")
                        views.setTextViewText(R.id.widget_special_day_desc, "Weekly Off Day • Office Closed")

                        views.setViewVisibility(R.id.widget_actions_row, View.GONE)
                        views.setViewVisibility(R.id.widget_offday_text, View.VISIBLE)
                        views.setTextViewText(R.id.widget_offday_text, "Today is your off day")
                    }

                    else -> {
                        // CASE A: NORMAL WORKING DAY (or holiday designated as declared office day)
                        // Preserve normal time count and working hour calculation
                        views.setViewVisibility(R.id.widget_timer_container, View.VISIBLE)
                        views.setViewVisibility(R.id.widget_special_day_container, View.GONE)
                        views.setViewVisibility(R.id.widget_actions_row, View.VISIBLE)
                        views.setViewVisibility(R.id.widget_offday_text, View.GONE)

                        val targetMins = activePeriod.dailyWorkingMinutes
                        val targetHours = targetMins / 60
                        val targetRemainderMins = targetMins % 60
                        val targetFormatted = String.format(Locale.getDefault(), "%dh %02dm", targetHours, targetRemainderMins)

                        when {
                            record == null || record.punchInTimestamp == null -> {
                                views.setTextViewText(R.id.widget_status_badge, "READY TO PUNCH IN")
                                views.setTextViewText(
                                    R.id.widget_countdown_text,
                                    String.format(Locale.getDefault(), "%02d:%02d:00", targetHours, targetRemainderMins)
                                )
                                views.setTextViewText(R.id.widget_sub_text, "Daily Target: $targetFormatted")
                            }
                            record.punchInTimestamp != null && record.punchOutTimestamp == null -> {
                                val elapsedMillis = System.currentTimeMillis() - record.punchInTimestamp
                                val targetMillis = targetMins * 60 * 1000L
                                val remainingMillis = targetMillis - elapsedMillis

                                if (remainingMillis > 0) {
                                    val remainingSecs = (remainingMillis / 1000) % 60
                                    val remainingMins = (remainingMillis / (1000 * 60)) % 60
                                    val remainingHrs = remainingMillis / (1000 * 60 * 60)
                                    views.setTextViewText(R.id.widget_status_badge, "WORKING • COUNTDOWN")
                                    views.setTextViewText(
                                        R.id.widget_countdown_text,
                                        String.format(Locale.getDefault(), "%02d:%02d:%02d", remainingHrs, remainingMins, remainingSecs)
                                    )
                                    views.setTextViewText(R.id.widget_sub_text, "Remaining to reach $targetFormatted goal")
                                } else {
                                    val overtimeMillis = -remainingMillis
                                    val overSecs = (overtimeMillis / 1000) % 60
                                    val overMins = (overtimeMillis / (1000 * 60)) % 60
                                    val overHrs = overtimeMillis / (1000 * 60 * 60)
                                    views.setTextViewText(R.id.widget_status_badge, "GOAL COMPLETED! 🎉")
                                    views.setTextViewText(
                                        R.id.widget_countdown_text,
                                        String.format(Locale.getDefault(), "+%02d:%02d:%02d", overHrs, overMins, overSecs)
                                    )
                                    views.setTextViewText(R.id.widget_sub_text, "Goal met! Don't forget to Punch Out")
                                }
                            }
                            record.punchOutTimestamp != null -> {
                                views.setTextViewText(R.id.widget_status_badge, "SHIFT COMPLETED")
                                val workedHrs = record.totalWorkingMinutes / 60
                                val workedMins = record.totalWorkingMinutes % 60
                                views.setTextViewText(
                                    R.id.widget_countdown_text,
                                    String.format(Locale.getDefault(), "%dh %02dm", workedHrs, workedMins)
                                )
                                views.setTextViewText(R.id.widget_sub_text, "Punched out • Daily total recorded")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback to normal working day time-count behaviour if holiday/leave info unavailable
            views.setViewVisibility(R.id.widget_timer_container, View.VISIBLE)
            views.setViewVisibility(R.id.widget_special_day_container, View.GONE)
            views.setViewVisibility(R.id.widget_actions_row, View.VISIBLE)
            views.setViewVisibility(R.id.widget_offday_text, View.GONE)
            views.setTextViewText(R.id.widget_status_badge, "NUBTK TIMER")
            views.setTextViewText(R.id.widget_countdown_text, "--:--:--")
            views.setTextViewText(R.id.widget_sub_text, "Tap to open app")
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun showToast(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
