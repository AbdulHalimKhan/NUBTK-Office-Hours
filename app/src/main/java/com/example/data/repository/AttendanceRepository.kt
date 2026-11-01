package com.example.data.repository

import com.example.data.local.AttendanceDao
import com.example.data.local.AttendancePeriodEntity
import com.example.data.local.AttendanceRecordEntity
import com.example.data.local.HolidayEntity
import com.example.data.local.LeaveEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.WeekendSettingsEntity
import com.example.utils.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

class AttendanceRepository(private val dao: AttendanceDao) {

    val allPeriods: Flow<List<AttendancePeriodEntity>> = dao.getAllPeriods()
    val activePeriodFlow: Flow<AttendancePeriodEntity?> = dao.getActivePeriodFlow()

    suspend fun getActivePeriod(): AttendancePeriodEntity? = dao.getActivePeriod()

    suspend fun getPeriodById(id: Long): AttendancePeriodEntity? = dao.getPeriodById(id)

    fun getPeriodByIdFlow(id: Long): Flow<AttendancePeriodEntity?> = dao.getPeriodByIdFlow(id)

    suspend fun insertPeriod(period: AttendancePeriodEntity): Long = dao.insertPeriod(period)

    suspend fun updatePeriod(period: AttendancePeriodEntity) = dao.updatePeriod(period)

    suspend fun deletePeriod(period: AttendancePeriodEntity) = dao.deletePeriod(period)

    suspend fun setActivePeriod(periodId: Long) = dao.setActivePeriod(periodId)

    // Records
    fun getRecordsForPeriod(periodId: Long): Flow<List<AttendanceRecordEntity>> =
        dao.getRecordsForPeriod(periodId)

    suspend fun getRecordForDate(periodId: Long, date: String): AttendanceRecordEntity? =
        dao.getRecordForDate(periodId, date)

    fun getRecordForDateFlow(periodId: Long, date: String): Flow<AttendanceRecordEntity?> =
        dao.getRecordForDateFlow(periodId, date)

    suspend fun saveRecord(record: AttendanceRecordEntity): Long =
        dao.insertOrUpdateRecord(record)

    suspend fun deleteRecord(record: AttendanceRecordEntity) =
        dao.deleteRecord(record)

    // Holidays
    fun getHolidaysForPeriod(periodId: Long): Flow<List<HolidayEntity>> =
        dao.getHolidaysForPeriod(periodId)

    suspend fun getHolidayForDate(periodId: Long, date: String): HolidayEntity? =
        dao.getHolidayForDate(periodId, date)

    suspend fun insertHoliday(holiday: HolidayEntity): Long =
        dao.insertHoliday(holiday)

    suspend fun updateHoliday(holiday: HolidayEntity) =
        dao.updateHoliday(holiday)

    suspend fun deleteHoliday(holiday: HolidayEntity) =
        dao.deleteHoliday(holiday)

    suspend fun deleteHolidayById(holidayId: Long) =
        dao.deleteHolidayById(holidayId)

    suspend fun setHolidayOfficeDay(holidayId: Long, isOfficeDay: Boolean) =
        dao.setHolidayOfficeDay(holidayId, isOfficeDay)

    suspend fun importBangladeshHolidaysForPeriod(periodId: Long, startDateIso: String, endDateIso: String): Int {
        val bdHolidays = com.example.data.BangladeshHolidayCalendar.getHolidaysForPeriod(startDateIso, endDateIso)
        var count = 0
        for (bh in bdHolidays) {
            val existing = dao.getHolidayForDate(periodId, bh.date)
            if (existing == null) {
                dao.insertHoliday(
                    HolidayEntity(
                        periodId = periodId,
                        date = bh.date,
                        name = bh.name,
                        isOfficeDay = false
                    )
                )
                count++
            }
        }
        return count
    }

    // Leaves
    fun getLeavesForPeriod(periodId: Long): Flow<List<LeaveEntity>> =
        dao.getLeavesForPeriod(periodId)

    suspend fun getLeaveForDate(periodId: Long, date: String): LeaveEntity? =
        dao.getLeaveForDate(periodId, date)

    suspend fun insertLeave(leave: LeaveEntity): Long =
        dao.insertLeave(leave)

    suspend fun updateLeave(leave: LeaveEntity) =
        dao.updateLeave(leave)

    suspend fun deleteLeave(leave: LeaveEntity) =
        dao.deleteLeave(leave)

    suspend fun deleteLeaveById(leaveId: Long) =
        dao.deleteLeaveById(leaveId)

    // Weekend Settings
    fun getWeekendSettingsForPeriod(periodId: Long): Flow<List<WeekendSettingsEntity>> =
        dao.getWeekendSettingsForPeriod(periodId)

    suspend fun setWeekendDaysForPeriod(periodId: Long, days: List<Int>) =
        dao.setWeekendDaysForPeriod(periodId, days)

    // User Profile
    val userProfileFlow: Flow<UserProfileEntity?> = dao.getUserProfileFlow()

    suspend fun getUserProfile(): UserProfileEntity? = dao.getUserProfile()

    suspend fun saveUserProfile(name: String, designation: String, department: String, employeeId: String) {
        dao.saveUserProfile(
            UserProfileEntity(
                id = 1,
                name = name.trim(),
                designation = designation.trim(),
                department = department.trim(),
                employeeId = employeeId.trim()
            )
        )
    }

    /**
     * Punch In logic:
     * - Checks if already punched in for the given date.
     * - Returns Result.success or Result.failure with human error message.
     */
    suspend fun punchIn(periodId: Long, date: String, timestamp: Long = System.currentTimeMillis()): Result<AttendanceRecordEntity> {
        val existing = dao.getRecordForDate(periodId, date)
        if (existing?.punchInTimestamp != null) {
            return Result.failure(IllegalStateException("You have already punched in today."))
        }

        val record = AttendanceRecordEntity(
            id = existing?.id ?: 0,
            periodId = periodId,
            date = date,
            status = "IN_PROGRESS",
            punchInTimestamp = timestamp,
            punchOutTimestamp = null,
            totalWorkingMinutes = 0
        )
        dao.insertOrUpdateRecord(record)
        return Result.success(record)
    }

    /**
     * Punch Out logic:
     * - Checks that Punch In exists.
     * - Checks that Punch Out doesn't already exist.
     * - Calculates duration in minutes.
     * - Marks record COMPLETED.
     */
    suspend fun punchOut(periodId: Long, date: String, timestamp: Long = System.currentTimeMillis()): Result<AttendanceRecordEntity> {
        val existing = dao.getRecordForDate(periodId, date)
        if (existing == null || existing.punchInTimestamp == null) {
            return Result.failure(IllegalStateException("Please Punch In first."))
        }
        if (existing.punchOutTimestamp != null) {
            return Result.failure(IllegalStateException("You have already punched out today."))
        }

        val duration = DateTimeUtils.calculateDurationMinutes(existing.punchInTimestamp, timestamp)
        val updated = existing.copy(
            status = "COMPLETED",
            punchOutTimestamp = timestamp,
            totalWorkingMinutes = duration
        )
        dao.insertOrUpdateRecord(updated)
        return Result.success(updated)
    }

    /**
     * Initialize sample initial period if database is empty.
     */
    suspend fun ensureInitialData() {
        val active = dao.getActivePeriod()
        if (active == null) {
            val defaultPeriod = AttendancePeriodEntity(
                name = "21 Aug 2026 – 20 Sep 2026",
                startDate = "2026-08-21",
                endDate = "2026-09-20",
                dailyWorkingMinutes = 480, // 8 hours
                isActive = true
            )
            val periodId = dao.insertPeriod(defaultPeriod)
            dao.setActivePeriod(periodId)

            // Default weekend: Friday (5) and Saturday (6)
            dao.setWeekendDaysForPeriod(periodId, listOf(DayOfWeek.FRIDAY.value, DayOfWeek.SATURDAY.value))

            // Import Bangladesh Google Calendar holidays for this period
            importBangladeshHolidaysForPeriod(periodId, defaultPeriod.startDate, defaultPeriod.endDate)

            // University foundation day
            dao.insertHoliday(
                HolidayEntity(
                    periodId = periodId,
                    date = "2026-09-05",
                    name = "University Foundation Day",
                    isOfficeDay = false
                )
            )

            // Sample sick leave from specification
            dao.insertLeave(
                LeaveEntity(
                    periodId = periodId,
                    date = "2026-09-10",
                    leaveType = "MEDICAL",
                    note = "Sick leave"
                )
            )
        }

        // User profile starts completely blank upon installation; user inputs their own details
        val profile = dao.getUserProfile()
        if (profile != null && profile.name == "Md. Abdul Halim Khan" && profile.designation == "Lecturer") {
            dao.saveUserProfile(
                UserProfileEntity(
                    id = 1,
                    name = "",
                    designation = "",
                    department = "",
                    employeeId = ""
                )
            )
        }
    }
}
