package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    // --- Period Operations ---

    @Query("SELECT * FROM attendance_periods ORDER BY startDate DESC")
    fun getAllPeriods(): Flow<List<AttendancePeriodEntity>>

    @Query("SELECT * FROM attendance_periods WHERE isActive = 1 LIMIT 1")
    fun getActivePeriodFlow(): Flow<AttendancePeriodEntity?>

    @Query("SELECT * FROM attendance_periods WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePeriod(): AttendancePeriodEntity?

    @Query("SELECT * FROM attendance_periods WHERE id = :id LIMIT 1")
    suspend fun getPeriodById(id: Long): AttendancePeriodEntity?

    @Query("SELECT * FROM attendance_periods WHERE id = :id LIMIT 1")
    fun getPeriodByIdFlow(id: Long): Flow<AttendancePeriodEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeriod(period: AttendancePeriodEntity): Long

    @Update
    suspend fun updatePeriod(period: AttendancePeriodEntity)

    @Delete
    suspend fun deletePeriod(period: AttendancePeriodEntity)

    @Query("UPDATE attendance_periods SET isActive = 0")
    suspend fun clearActivePeriods()

    @Query("UPDATE attendance_periods SET isActive = 1 WHERE id = :periodId")
    suspend fun markPeriodActive(periodId: Long)

    @Transaction
    suspend fun setActivePeriod(periodId: Long) {
        clearActivePeriods()
        markPeriodActive(periodId)
    }

    // --- Record Operations ---

    @Query("SELECT * FROM attendance_records WHERE periodId = :periodId ORDER BY date ASC")
    fun getRecordsForPeriod(periodId: Long): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE periodId = :periodId AND date = :date LIMIT 1")
    suspend fun getRecordForDate(periodId: Long, date: String): AttendanceRecordEntity?

    @Query("SELECT * FROM attendance_records WHERE periodId = :periodId AND date = :date LIMIT 1")
    fun getRecordForDateFlow(periodId: Long, date: String): Flow<AttendanceRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: AttendanceRecordEntity): Long

    @Delete
    suspend fun deleteRecord(record: AttendanceRecordEntity)

    // --- Holiday Operations ---

    @Query("SELECT * FROM holidays WHERE periodId = :periodId ORDER BY date ASC")
    fun getHolidaysForPeriod(periodId: Long): Flow<List<HolidayEntity>>

    @Query("SELECT * FROM holidays WHERE periodId = :periodId AND date = :date LIMIT 1")
    suspend fun getHolidayForDate(periodId: Long, date: String): HolidayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(holiday: HolidayEntity): Long

    @Update
    suspend fun updateHoliday(holiday: HolidayEntity)

    @Delete
    suspend fun deleteHoliday(holiday: HolidayEntity)

    @Query("DELETE FROM holidays WHERE id = :holidayId")
    suspend fun deleteHolidayById(holidayId: Long)

    @Query("UPDATE holidays SET isOfficeDay = :isOfficeDay WHERE id = :holidayId")
    suspend fun setHolidayOfficeDay(holidayId: Long, isOfficeDay: Boolean)

    // --- Leave Operations ---

    @Query("SELECT * FROM leaves WHERE periodId = :periodId ORDER BY date ASC")
    fun getLeavesForPeriod(periodId: Long): Flow<List<LeaveEntity>>

    @Query("SELECT * FROM leaves WHERE periodId = :periodId AND date = :date LIMIT 1")
    suspend fun getLeaveForDate(periodId: Long, date: String): LeaveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeave(leave: LeaveEntity): Long

    @Update
    suspend fun updateLeave(leave: LeaveEntity)

    @Delete
    suspend fun deleteLeave(leave: LeaveEntity)

    @Query("DELETE FROM leaves WHERE id = :leaveId")
    suspend fun deleteLeaveById(leaveId: Long)

    // --- Weekend Settings Operations ---

    @Query("SELECT * FROM weekend_settings WHERE periodId = :periodId")
    fun getWeekendSettingsForPeriod(periodId: Long): Flow<List<WeekendSettingsEntity>>

    @Query("SELECT * FROM weekend_settings WHERE periodId = :periodId")
    suspend fun getWeekendSettingsSync(periodId: Long): List<WeekendSettingsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeekendSettings(settings: List<WeekendSettingsEntity>)

    @Query("DELETE FROM weekend_settings WHERE periodId = :periodId")
    suspend fun deleteWeekendSettingsForPeriod(periodId: Long)

    @Transaction
    suspend fun setWeekendDaysForPeriod(periodId: Long, days: List<Int>) {
        deleteWeekendSettingsForPeriod(periodId)
        val entities = days.map { WeekendSettingsEntity(periodId = periodId, dayOfWeek = it) }
        insertWeekendSettings(entities)
    }

    // --- User Profile Operations ---

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)
}
