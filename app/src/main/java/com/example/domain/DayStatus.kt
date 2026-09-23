package com.example.domain

enum class DayClassification {
    WORKING_DAY,
    WEEKEND,
    HOLIDAY,
    MEDICAL_LEAVE,
    CASUAL_LEAVE
}

enum class AttendanceStatus(val displayName: String) {
    WORKING_DAY("WORKING DAY"),
    WEEKEND("WEEKEND"),
    HOLIDAY("HOLIDAY"),
    MEDICAL_LEAVE("MEDICAL LEAVE"),
    CASUAL_LEAVE("CASUAL LEAVE"),
    IN_PROGRESS("IN PROGRESS"),
    COMPLETED("COMPLETED"),
    WORKED_ON_WEEKEND("WORKED ON WEEKEND"),
    WORKED_ON_HOLIDAY("WORKED ON HOLIDAY"),
    WORKED_DURING_LEAVE("WORKED DURING LEAVE")
}

data class DayAttendanceInfo(
    val date: String, // YYYY-MM-DD
    val dayOfWeekName: String, // "Mon", "Tue", etc.
    val baseClassification: DayClassification,
    val displayStatus: AttendanceStatus,
    val statusDetail: String? = null, // e.g., Holiday name or Leave note
    val punchInTimestamp: Long? = null,
    val punchOutTimestamp: Long? = null,
    val punchInFormatted: String = "—",
    val punchOutFormatted: String = "—",
    val durationMinutes: Int = 0,
    val durationFormatted: String = "—",
    val expectedMinutes: Int = 0,
    val isWorkingDay: Boolean = false,
    val isHolidayOfficeDay: Boolean = false,
    val isCompleted: Boolean = false,
    val isInProgress: Boolean = false
)
