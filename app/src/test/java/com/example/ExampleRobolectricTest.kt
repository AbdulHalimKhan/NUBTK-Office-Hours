package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.AttendanceCalculator
import com.example.domain.DayClassification
import com.example.domain.DelayColorState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NUBTK Timer", appName)
    }

    @Test
    fun `test attendance calculation example from specification`() {
        // Attendance period: 31 calendar days, 8 weekends, 2 holidays, 1 medical leave, 0 casual leave -> 20 working days
        // Expected: 20 * 8h = 160h (9600 mins)
        // Actual: 156h 30m (9390 mins) -> Delay: +03:30 (RED)
        val weekendDays = setOf(5, 6) // Friday, Saturday
        val holidays = mapOf(
            "2026-09-05" to com.example.data.local.HolidayEntity(
                id = 1,
                periodId = 1,
                date = "2026-09-05",
                name = "Foundation Day",
                isOfficeDay = false
            ),
            "2026-09-12" to com.example.data.local.HolidayEntity(
                id = 2,
                periodId = 1,
                date = "2026-09-12",
                name = "National Holiday",
                isOfficeDay = false
            )
        )
        val leaves = mapOf(
            "2026-09-10" to com.example.data.local.LeaveEntity(
                id = 1,
                periodId = 1,
                date = "2026-09-10",
                leaveType = "MEDICAL",
                note = "Sick leave"
            )
        )

        val days = AttendanceCalculator.generateDaysList(
            startDate = LocalDate.of(2026, 8, 21),
            endDate = LocalDate.of(2026, 9, 20),
            weekendDays = weekendDays,
            holidaysMap = holidays,
            leavesMap = leaves,
            recordsMap = emptyMap(),
            dailyWorkingMinutes = 480
        )

        assertEquals(31, days.size)
        val summary = AttendanceCalculator.calculateSummary(days)
        assertEquals(31, summary.calendarDays)
        assertEquals(8, summary.weekendDays)
        assertEquals(2, summary.holidayDays)
        assertEquals(1, summary.medicalLeaveDays)
        assertEquals(0, summary.casualLeaveDays)
        assertEquals(20, summary.workingDays)
        assertEquals(9600, summary.expectedMinutes) // 160:00
        assertEquals(DelayColorState.RED, summary.delayColorState)
    }
}
