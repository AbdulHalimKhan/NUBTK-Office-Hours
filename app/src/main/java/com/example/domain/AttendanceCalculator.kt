package com.example.domain

import com.example.data.local.AttendancePeriodEntity
import com.example.data.local.AttendanceRecordEntity
import com.example.data.local.HolidayEntity
import com.example.data.local.LeaveEntity
import com.example.utils.DateTimeUtils
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

object AttendanceCalculator {

    /**
     * Centralized day status classification.
     * Priority:
     * 1. MEDICAL LEAVE
     * 2. CASUAL LEAVE
     * 3. HOLIDAY (or Declared Office Day if isOfficeDay is true)
     * 4. WEEKEND
     * 5. WORKING DAY
     */
    fun classifyDay(
        date: LocalDate,
        weekendDays: Set<Int>, // 1 = Monday ... 7 = Sunday
        holidaysMap: Map<String, HolidayEntity>, // isoDate -> HolidayEntity
        leavesMap: Map<String, LeaveEntity>, // isoDate -> leave
        record: AttendanceRecordEntity?,
        dailyWorkingMinutes: Int
    ): DayAttendanceInfo {
        val isoDate = date.format(DateTimeUtils.ISO_DATE_FORMATTER)
        val dayOfWeek = date.dayOfWeek
        val dayOfWeekValue = dayOfWeek.value
        val dayOfWeekName = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)

        val leave = leavesMap[isoDate]
        val holiday = holidaysMap[isoDate]
        val isWeekend = weekendDays.contains(dayOfWeekValue)

        val baseClassification: DayClassification
        val statusDetail: String?
        var isHolidayOfficeDay = false

        when {
            // Priority 1: Official Holiday
            holiday != null && !holiday.isOfficeDay -> {
                baseClassification = DayClassification.HOLIDAY
                statusDetail = holiday.name.ifBlank { "Public Holiday" }
            }
            // Priority 2: Approved Leave (Medical / Casual)
            leave != null && leave.leaveType.equals("MEDICAL", ignoreCase = true) -> {
                baseClassification = DayClassification.MEDICAL_LEAVE
                statusDetail = leave.note?.ifBlank { null } ?: "Medical Leave"
            }
            leave != null && leave.leaveType.equals("CASUAL", ignoreCase = true) -> {
                baseClassification = DayClassification.CASUAL_LEAVE
                statusDetail = leave.note?.ifBlank { null } ?: "Casual Leave"
            }
            // Holiday designated as declared office day
            holiday != null && holiday.isOfficeDay -> {
                baseClassification = DayClassification.WORKING_DAY
                statusDetail = "${holiday.name} (Declared Office Day)"
                isHolidayOfficeDay = true
            }
            // Weekend
            isWeekend -> {
                baseClassification = DayClassification.WEEKEND
                statusDetail = "Weekend (${dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)})"
            }
            // Priority 3: Normal Working Day
            else -> {
                baseClassification = DayClassification.WORKING_DAY
                statusDetail = null
            }
        }

        val hasPunchIn = record?.punchInTimestamp != null
        val hasPunchOut = record?.punchOutTimestamp != null
        val isCompleted = hasPunchIn && hasPunchOut
        val isInProgress = hasPunchIn && !hasPunchOut

        val durationMinutes = if (isCompleted) {
            DateTimeUtils.calculateDurationMinutes(record!!.punchInTimestamp, record.punchOutTimestamp)
        } else 0

        // Display status
        val displayStatus: AttendanceStatus = when {
            isInProgress -> AttendanceStatus.IN_PROGRESS
            isCompleted -> {
                when (baseClassification) {
                    DayClassification.WORKING_DAY -> AttendanceStatus.COMPLETED
                    DayClassification.WEEKEND -> AttendanceStatus.WORKED_ON_WEEKEND
                    DayClassification.HOLIDAY -> AttendanceStatus.WORKED_ON_HOLIDAY
                    DayClassification.MEDICAL_LEAVE, DayClassification.CASUAL_LEAVE -> AttendanceStatus.WORKED_DURING_LEAVE
                }
            }
            baseClassification == DayClassification.MEDICAL_LEAVE -> AttendanceStatus.MEDICAL_LEAVE
            baseClassification == DayClassification.CASUAL_LEAVE -> AttendanceStatus.CASUAL_LEAVE
            baseClassification == DayClassification.HOLIDAY -> AttendanceStatus.HOLIDAY
            baseClassification == DayClassification.WEEKEND -> AttendanceStatus.WEEKEND
            else -> AttendanceStatus.WORKING_DAY
        }

        val isWorkingDay = (baseClassification == DayClassification.WORKING_DAY)
        val expectedMinutes = if (isWorkingDay) dailyWorkingMinutes else 0

        return DayAttendanceInfo(
            date = isoDate,
            dayOfWeekName = dayOfWeekName,
            baseClassification = baseClassification,
            displayStatus = displayStatus,
            statusDetail = statusDetail,
            punchInTimestamp = record?.punchInTimestamp,
            punchOutTimestamp = record?.punchOutTimestamp,
            punchInFormatted = DateTimeUtils.formatTimestampTo12H(record?.punchInTimestamp),
            punchOutFormatted = DateTimeUtils.formatTimestampTo12H(record?.punchOutTimestamp),
            durationMinutes = durationMinutes,
            durationFormatted = if (isCompleted) DateTimeUtils.formatMinutesToHoursMinutes(durationMinutes) else "—",
            expectedMinutes = expectedMinutes,
            isWorkingDay = isWorkingDay,
            isHolidayOfficeDay = isHolidayOfficeDay,
            isCompleted = isCompleted,
            isInProgress = isInProgress
        )
    }

    /**
     * Generates all day info records for a period or date range.
     */
    fun generateDaysList(
        startDate: LocalDate,
        endDate: LocalDate,
        weekendDays: Set<Int>,
        holidaysMap: Map<String, HolidayEntity>,
        leavesMap: Map<String, LeaveEntity>,
        recordsMap: Map<String, AttendanceRecordEntity>,
        dailyWorkingMinutes: Int
    ): List<DayAttendanceInfo> {
        val days = mutableListOf<DayAttendanceInfo>()
        var curr = startDate
        while (!curr.isAfter(endDate)) {
            val iso = curr.format(DateTimeUtils.ISO_DATE_FORMATTER)
            val dayInfo = classifyDay(
                date = curr,
                weekendDays = weekendDays,
                holidaysMap = holidaysMap,
                leavesMap = leavesMap,
                record = recordsMap[iso],
                dailyWorkingMinutes = dailyWorkingMinutes
            )
            days.add(dayInfo)
            curr = curr.plusDays(1)
        }
        return days
    }

    /**
     * Computes the summary metrics for a list of classified days.
     */
    fun calculateSummary(days: List<DayAttendanceInfo>): PeriodSummary {
        val calendarDays = days.size
        val weekendDays = days.count { it.baseClassification == DayClassification.WEEKEND }
        val holidayDays = days.count { it.baseClassification == DayClassification.HOLIDAY }
        val medicalLeaveDays = days.count { it.baseClassification == DayClassification.MEDICAL_LEAVE }
        val casualLeaveDays = days.count { it.baseClassification == DayClassification.CASUAL_LEAVE }
        val workingDays = days.count { it.baseClassification == DayClassification.WORKING_DAY }
        val workingDaysCompleted = days.count { it.isWorkingDay && it.isCompleted }

        val expectedMinutes = days.sumOf { it.expectedMinutes }
        // Actual hours = sum of completed punch durations (excluding incomplete in-progress records)
        val actualMinutes = days.filter { it.isCompleted }.sumOf { it.durationMinutes }
        val delayMinutes = expectedMinutes - actualMinutes

        val delayColorState = when {
            actualMinutes < expectedMinutes -> DelayColorState.RED    // Shortfall (Delay)
            actualMinutes > expectedMinutes -> DelayColorState.GREEN  // Worked more (Extra)
            else -> DelayColorState.AMBER                             // On time (00:00)
        }

        val progressFraction = if (workingDays > 0) {
            (workingDaysCompleted.toFloat() / workingDays.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        return PeriodSummary(
            calendarDays = calendarDays,
            workingDays = workingDays,
            weekendDays = weekendDays,
            holidayDays = holidayDays,
            medicalLeaveDays = medicalLeaveDays,
            casualLeaveDays = casualLeaveDays,
            workingDaysCompleted = workingDaysCompleted,
            expectedMinutes = expectedMinutes,
            actualMinutes = actualMinutes,
            delayMinutes = delayMinutes,
            expectedFormatted = DateTimeUtils.formatMinutesToHoursMinutes(expectedMinutes),
            actualFormatted = DateTimeUtils.formatMinutesToHoursMinutes(actualMinutes),
            delaySignedFormatted = DateTimeUtils.formatSignedDelay(delayMinutes),
            delayColorState = delayColorState,
            progressFraction = progressFraction
        )
    }

    /**
     * Splits a period range into 10-day sections:
     * - Days 1–10
     * - Days 11–20
     * - Days 21–N (e.g. Days 21–30 or Days 21–31)
     */
    fun generateTenDayRanges(startDateIso: String, endDateIso: String): List<TenDayRange> {
        val start = DateTimeUtils.parseIsoDate(startDateIso)
        val end = DateTimeUtils.parseIsoDate(endDateIso)
        if (start.isAfter(end)) return emptyList()

        val totalDays = (ChronoUnit.DAYS.between(start, end) + 1).toInt()
        val ranges = mutableListOf<TenDayRange>()

        if (totalDays <= 10) {
            ranges.add(
                TenDayRange(
                    title = "Days 1–$totalDays",
                    startDate = startDateIso,
                    endDate = endDateIso,
                    dayCount = totalDays
                )
            )
            return ranges
        }

        // Section 1: Days 1–10
        val endDay10 = start.plusDays(9)
        ranges.add(
            TenDayRange(
                title = "Days 1–10",
                startDate = start.format(DateTimeUtils.ISO_DATE_FORMATTER),
                endDate = endDay10.format(DateTimeUtils.ISO_DATE_FORMATTER),
                dayCount = 10
            )
        )

        // Section 2: Days 11–20
        if (totalDays <= 20) {
            val startDay11 = start.plusDays(10)
            ranges.add(
                TenDayRange(
                    title = "Days 11–$totalDays",
                    startDate = startDay11.format(DateTimeUtils.ISO_DATE_FORMATTER),
                    endDate = end.format(DateTimeUtils.ISO_DATE_FORMATTER),
                    dayCount = totalDays - 10
                )
            )
        } else {
            val startDay11 = start.plusDays(10)
            val endDay20 = start.plusDays(19)
            ranges.add(
                TenDayRange(
                    title = "Days 11–20",
                    startDate = startDay11.format(DateTimeUtils.ISO_DATE_FORMATTER),
                    endDate = endDay20.format(DateTimeUtils.ISO_DATE_FORMATTER),
                    dayCount = 10
                )
            )

            // Section 3: Days 21–End
            val startDay21 = start.plusDays(20)
            ranges.add(
                TenDayRange(
                    title = "Days 21–$totalDays",
                    startDate = startDay21.format(DateTimeUtils.ISO_DATE_FORMATTER),
                    endDate = end.format(DateTimeUtils.ISO_DATE_FORMATTER),
                    dayCount = totalDays - 20
                )
            )
        }

        return ranges
    }
}
