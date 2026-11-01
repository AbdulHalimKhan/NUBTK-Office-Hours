package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AttendanceDatabase
import com.example.data.repository.AttendanceRepository
import com.example.utils.DateTimeUtils
import com.example.widget.NubtkTimerWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkHoursAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AttendanceDatabase.getDatabase(context)
                val repository = AttendanceRepository(db.attendanceDao())
                val activePeriod = repository.getActivePeriod()
                if (activePeriod != null) {
                    val todayIso = DateTimeUtils.todayIso()
                    val record = repository.getRecordForDate(activePeriod.id, todayIso)
                    // Only notify if currently punched in and not yet punched out
                    if (record?.punchInTimestamp != null && record.punchOutTimestamp == null) {
                        val expectedMins = activePeriod.dailyWorkingMinutes
                        val hours = expectedMins / 60
                        val mins = expectedMins % 60
                        NotificationHelper.showWorkHoursCompletedNotification(context, hours, mins)
                    }
                }
                NubtkTimerWidgetProvider.updateAllWidgets(context)
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}
