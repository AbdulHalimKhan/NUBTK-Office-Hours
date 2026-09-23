package com.example.domain

import com.example.data.local.AttendancePeriodEntity

enum class DelayColorState {
    RED,   // Actual < Expected (delay, shortfall, e.g. +03:30)
    GREEN, // Actual > Expected (worked extra, e.g. -04:30)
    AMBER  // Actual == Expected (00:00)
}

data class PeriodSummary(
    val calendarDays: Int = 0,
    val workingDays: Int = 0,
    val weekendDays: Int = 0,
    val holidayDays: Int = 0,
    val medicalLeaveDays: Int = 0,
    val casualLeaveDays: Int = 0,
    val workingDaysCompleted: Int = 0,
    val expectedMinutes: Int = 0,
    val actualMinutes: Int = 0,
    val delayMinutes: Int = 0, // Expected - Actual
    val expectedFormatted: String = "00:00",
    val actualFormatted: String = "00:00",
    val delaySignedFormatted: String = "00:00",
    val delayColorState: DelayColorState = DelayColorState.AMBER,
    val progressFraction: Float = 0f
) {
    val isExtra: Boolean
        get() = actualMinutes > expectedMinutes

    val delayOrExtraLabel: String
        get() = if (isExtra) "Extra" else "Delay"
}

data class TenDayRange(
    val title: String,
    val startDate: String,
    val endDate: String,
    val dayCount: Int
)

data class UserProfile(
    val name: String = "",
    val designation: String = "",
    val department: String = "",
    val employeeId: String = ""
)

data class PeriodAttendanceReport(
    val period: AttendancePeriodEntity,
    val rangeTitle: String,
    val startDate: String,
    val endDate: String,
    val days: List<DayAttendanceInfo>,
    val summary: PeriodSummary,
    val userProfile: UserProfile? = null
)
