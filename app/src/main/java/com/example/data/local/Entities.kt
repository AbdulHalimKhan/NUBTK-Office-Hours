package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_periods",
    indices = [Index(value = ["isActive"])]
)
data class AttendancePeriodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startDate: String, // YYYY-MM-DD
    val endDate: String,   // YYYY-MM-DD
    val dailyWorkingMinutes: Int = 480, // 8 hours = 480 minutes
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = AttendancePeriodEntity::class,
            parentColumns = ["id"],
            childColumns = ["periodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["periodId", "date"], unique = true),
        Index(value = ["periodId"])
    ]
)
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val periodId: Long,
    val date: String, // YYYY-MM-DD
    val status: String,
    val punchInTimestamp: Long? = null,
    val punchOutTimestamp: Long? = null,
    val totalWorkingMinutes: Int = 0,
    val note: String? = null
)

@Entity(
    tableName = "holidays",
    foreignKeys = [
        ForeignKey(
            entity = AttendancePeriodEntity::class,
            parentColumns = ["id"],
            childColumns = ["periodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["periodId", "date"], unique = true),
        Index(value = ["periodId"])
    ]
)
data class HolidayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val periodId: Long,
    val date: String, // YYYY-MM-DD
    val name: String,
    val isOfficeDay: Boolean = false
)

@Entity(
    tableName = "leaves",
    foreignKeys = [
        ForeignKey(
            entity = AttendancePeriodEntity::class,
            parentColumns = ["id"],
            childColumns = ["periodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["periodId", "date"], unique = true),
        Index(value = ["periodId"])
    ]
)
data class LeaveEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val periodId: Long,
    val date: String, // YYYY-MM-DD
    val leaveType: String, // "MEDICAL" or "CASUAL"
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "weekend_settings",
    foreignKeys = [
        ForeignKey(
            entity = AttendancePeriodEntity::class,
            parentColumns = ["id"],
            childColumns = ["periodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["periodId", "dayOfWeek"], unique = true),
        Index(value = ["periodId"])
    ]
)
data class WeekendSettingsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val periodId: Long,
    val dayOfWeek: Int // java.time.DayOfWeek value: 1 = Monday ... 5 = Friday, 6 = Saturday, 7 = Sunday
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "",
    val designation: String = "",
    val department: String = "",
    val employeeId: String = ""
)
