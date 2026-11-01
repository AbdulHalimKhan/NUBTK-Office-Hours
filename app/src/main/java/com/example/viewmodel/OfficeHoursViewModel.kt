package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AttendanceDatabase
import com.example.data.local.AttendancePeriodEntity
import com.example.data.local.AttendanceRecordEntity
import com.example.data.local.HolidayEntity
import com.example.data.local.LeaveEntity
import com.example.data.local.UserProfileEntity
import com.example.data.repository.AttendanceRepository
import com.example.domain.AttendanceCalculator
import com.example.domain.DayAttendanceInfo
import com.example.domain.PeriodAttendanceReport
import com.example.domain.PeriodSummary
import com.example.domain.UserProfile
import com.example.pdf.AttendancePdfGenerator
import com.example.utils.DateTimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class OfficeHoursViewModel(
    application: Application,
    private val repository: AttendanceRepository
) : AndroidViewModel(application) {

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    private val _generatedPdfFile = MutableStateFlow<File?>(null)
    val generatedPdfFile: StateFlow<File?> = _generatedPdfFile.asStateFlow()

    private val _exportedPdfResult = MutableStateFlow<com.example.pdf.PdfExportResult?>(null)
    val exportedPdfResult: StateFlow<com.example.pdf.PdfExportResult?> = _exportedPdfResult.asStateFlow()

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun saveUserProfile(name: String, designation: String, department: String, employeeId: String) {
        viewModelScope.launch {
            repository.saveUserProfile(name, designation, department, employeeId)
            _messageEvent.emit("Employee profile saved successfully.")
        }
    }

    val allPeriods: StateFlow<List<AttendancePeriodEntity>> = repository.allPeriods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePeriod: StateFlow<AttendancePeriodEntity?> = repository.activePeriodFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Reactively combine all records, holidays, leaves, and weekends for the active period
    val activePeriodHolidays: StateFlow<List<HolidayEntity>> = activePeriod.flatMapLatest { period ->
        if (period != null) repository.getHolidaysForPeriod(period.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePeriodLeaves: StateFlow<List<LeaveEntity>> = activePeriod.flatMapLatest { period ->
        if (period != null) repository.getLeavesForPeriod(period.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePeriodWeekends: StateFlow<Set<Int>> = activePeriod.flatMapLatest { period ->
        if (period != null) repository.getWeekendSettingsForPeriod(period.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        .combine(flowOf(Unit)) { settings, _ ->
            settings.map { it.dayOfWeek }.toSet()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val activePeriodRecords: StateFlow<List<AttendanceRecordEntity>> = activePeriod.flatMapLatest { period ->
        if (period != null) repository.getRecordsForPeriod(period.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All days classified for active period
    val activePeriodDays: StateFlow<List<DayAttendanceInfo>> = combine(
        activePeriod,
        activePeriodWeekends,
        activePeriodHolidays,
        activePeriodLeaves,
        activePeriodRecords
    ) { period, weekends, holidays, leaves, records ->
        if (period == null) return@combine emptyList<DayAttendanceInfo>()

        val holidaysMap = holidays.associateBy { it.date }
        val leavesMap = leaves.associateBy { it.date }
        val recordsMap = records.associateBy { it.date }

        val start = DateTimeUtils.parseIsoDate(period.startDate)
        val end = DateTimeUtils.parseIsoDate(period.endDate)

        AttendanceCalculator.generateDaysList(
            startDate = start,
            endDate = end,
            weekendDays = weekends,
            holidaysMap = holidaysMap,
            leavesMap = leavesMap,
            recordsMap = recordsMap,
            dailyWorkingMinutes = period.dailyWorkingMinutes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active period summary
    val activePeriodSummary: StateFlow<PeriodSummary> = activePeriodDays.combine(flowOf(Unit)) { days, _ ->
        AttendanceCalculator.calculateSummary(days)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PeriodSummary())

    // Today's attendance info
    val todayInfo: StateFlow<DayAttendanceInfo?> = activePeriodDays.combine(flowOf(Unit)) { days, _ ->
        val todayIso = DateTimeUtils.todayIso()
        days.find { it.date == todayIso } ?: run {
            // If today is outside active period, classify today using current active period settings
            val period = activePeriod.value
            if (period != null) {
                AttendanceCalculator.classifyDay(
                    date = LocalDate.now(),
                    weekendDays = activePeriodWeekends.value,
                    holidaysMap = activePeriodHolidays.value.associateBy { it.date },
                    leavesMap = activePeriodLeaves.value.associateBy { it.date },
                    record = activePeriodRecords.value.find { it.date == todayIso },
                    dailyWorkingMinutes = period.dailyWorkingMinutes
                )
            } else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            repository.ensureInitialData()
        }
    }

    fun punchInToday() {
        viewModelScope.launch {
            val period = activePeriod.value
            if (period == null) {
                _messageEvent.emit("Please create an active attendance period first.")
                return@launch
            }
            val todayIso = DateTimeUtils.todayIso()
            val result = repository.punchIn(period.id, todayIso)
            result.onSuccess {
                _messageEvent.emit("Punched In successfully at ${DateTimeUtils.formatTimestampTo12H(it.punchInTimestamp)}")
            }.onFailure {
                _messageEvent.emit(it.message ?: "Punch In failed")
            }
        }
    }

    fun punchOutToday() {
        viewModelScope.launch {
            val period = activePeriod.value
            if (period == null) {
                _messageEvent.emit("Please create an active attendance period first.")
                return@launch
            }
            val todayIso = DateTimeUtils.todayIso()
            val result = repository.punchOut(period.id, todayIso)
            result.onSuccess {
                val durFormatted = DateTimeUtils.formatMinutesToHoursMinutes(it.totalWorkingMinutes)
                _messageEvent.emit("Punched Out successfully! Duration: $durFormatted")
            }.onFailure {
                _messageEvent.emit(it.message ?: "Punch Out failed")
            }
        }
    }

    fun manualPunch(
        dateIso: String,
        inTimestamp: Long?,
        outTimestamp: Long?,
        note: String? = null
    ) {
        viewModelScope.launch {
            val period = activePeriod.value ?: return@launch
            val duration = DateTimeUtils.calculateDurationMinutes(inTimestamp, outTimestamp)
            val status = when {
                inTimestamp != null && outTimestamp != null -> "COMPLETED"
                inTimestamp != null -> "IN_PROGRESS"
                else -> "WORKING_DAY"
            }
            val existing = repository.getRecordForDate(period.id, dateIso)
            val record = AttendanceRecordEntity(
                id = existing?.id ?: 0,
                periodId = period.id,
                date = dateIso,
                status = status,
                punchInTimestamp = inTimestamp,
                punchOutTimestamp = outTimestamp,
                totalWorkingMinutes = duration,
                note = note
            )
            repository.saveRecord(record)
            _messageEvent.emit("Attendance record updated for ${DateTimeUtils.formatToDisplayDate(dateIso)}")
        }
    }

    fun editPunchIn(
        dateIso: String,
        inTimestamp: Long?,
        note: String? = null
    ) {
        viewModelScope.launch {
            val period = activePeriod.value ?: return@launch
            val existing = repository.getRecordForDate(period.id, dateIso)
            val currentOut = existing?.punchOutTimestamp
            val duration = DateTimeUtils.calculateDurationMinutes(inTimestamp, currentOut)
            val status = when {
                inTimestamp != null && currentOut != null -> "COMPLETED"
                inTimestamp != null -> "IN_PROGRESS"
                else -> "WORKING_DAY"
            }
            val record = AttendanceRecordEntity(
                id = existing?.id ?: 0,
                periodId = period.id,
                date = dateIso,
                status = status,
                punchInTimestamp = inTimestamp,
                punchOutTimestamp = currentOut,
                totalWorkingMinutes = duration,
                note = note ?: existing?.note
            )
            repository.saveRecord(record)
            val msg = if (inTimestamp != null) "Punch In saved for ${DateTimeUtils.formatToDisplayDate(dateIso)}" else "Punch In cleared for ${DateTimeUtils.formatToDisplayDate(dateIso)}"
            _messageEvent.emit(msg)
        }
    }

    fun editPunchOut(
        dateIso: String,
        outTimestamp: Long?,
        note: String? = null
    ) {
        viewModelScope.launch {
            val period = activePeriod.value ?: return@launch
            val existing = repository.getRecordForDate(period.id, dateIso)
            val currentIn = existing?.punchInTimestamp
            val duration = DateTimeUtils.calculateDurationMinutes(currentIn, outTimestamp)
            val status = when {
                currentIn != null && outTimestamp != null -> "COMPLETED"
                currentIn != null -> "IN_PROGRESS"
                else -> "WORKING_DAY"
            }
            val record = AttendanceRecordEntity(
                id = existing?.id ?: 0,
                periodId = period.id,
                date = dateIso,
                status = status,
                punchInTimestamp = currentIn,
                punchOutTimestamp = outTimestamp,
                totalWorkingMinutes = duration,
                note = note ?: existing?.note
            )
            repository.saveRecord(record)
            val msg = if (outTimestamp != null) "Punch Out saved for ${DateTimeUtils.formatToDisplayDate(dateIso)}" else "Punch Out cleared for ${DateTimeUtils.formatToDisplayDate(dateIso)}"
            _messageEvent.emit(msg)
        }
    }

    fun createNewPeriod(
        name: String,
        startDate: String,
        endDate: String,
        dailyHours: Int,
        dailyMinutes: Int,
        weekendDays: Set<Int>,
        includeBangladeshHolidays: Boolean = true
    ) {
        viewModelScope.launch {
            val totalMinutes = (dailyHours * 60) + dailyMinutes
            if (totalMinutes <= 0) {
                _messageEvent.emit("Daily working hours must be greater than 0.")
                return@launch
            }
            val start = DateTimeUtils.parseIsoDate(startDate)
            val end = DateTimeUtils.parseIsoDate(endDate)
            if (start.isAfter(end)) {
                _messageEvent.emit("Start date cannot be after end date.")
                return@launch
            }

            val periodName = name.ifBlank {
                "${DateTimeUtils.formatToDisplayDate(startDate)} – ${DateTimeUtils.formatToDisplayDate(endDate)}"
            }

            val newPeriod = AttendancePeriodEntity(
                name = periodName,
                startDate = startDate,
                endDate = endDate,
                dailyWorkingMinutes = totalMinutes,
                isActive = true
            )
            val newId = repository.insertPeriod(newPeriod)
            repository.setActivePeriod(newId)
            repository.setWeekendDaysForPeriod(newId, weekendDays.toList())

            if (includeBangladeshHolidays) {
                repository.importBangladeshHolidaysForPeriod(newId, startDate, endDate)
            }

            _messageEvent.emit("New attendance period created successfully.")
        }
    }

    fun updateActivePeriod(
        name: String,
        startDate: String,
        endDate: String,
        dailyHours: Int,
        dailyMinutes: Int
    ) {
        viewModelScope.launch {
            val current = activePeriod.value ?: return@launch
            val totalMinutes = (dailyHours * 60) + dailyMinutes
            if (totalMinutes <= 0) {
                _messageEvent.emit("Daily working hours must be greater than 0.")
                return@launch
            }
            val start = DateTimeUtils.parseIsoDate(startDate)
            val end = DateTimeUtils.parseIsoDate(endDate)
            if (start.isAfter(end)) {
                _messageEvent.emit("Start date cannot be after end date.")
                return@launch
            }

            val updated = current.copy(
                name = name.ifBlank { "${DateTimeUtils.formatToDisplayDate(startDate)} – ${DateTimeUtils.formatToDisplayDate(endDate)}" },
                startDate = startDate,
                endDate = endDate,
                dailyWorkingMinutes = totalMinutes
            )
            repository.updatePeriod(updated)
            _messageEvent.emit("Attendance period settings updated.")
        }
    }

    fun switchActivePeriod(periodId: Long) {
        viewModelScope.launch {
            repository.setActivePeriod(periodId)
            _messageEvent.emit("Switched active attendance period.")
        }
    }

    fun deletePeriod(period: AttendancePeriodEntity) {
        viewModelScope.launch {
            repository.deletePeriod(period)
            _messageEvent.emit("Period '${period.name}' deleted.")
        }
    }

    fun updateWeekendDays(days: Set<Int>) {
        viewModelScope.launch {
            val period = activePeriod.value ?: return@launch
            repository.setWeekendDaysForPeriod(period.id, days.toList())
            _messageEvent.emit("Weekend settings updated.")
        }
    }

    // Holiday Management
    fun importBangladeshHolidays() {
        viewModelScope.launch {
            val period = activePeriod.value ?: return@launch
            val addedCount = repository.importBangladeshHolidaysForPeriod(period.id, period.startDate, period.endDate)
            if (addedCount > 0) {
                _messageEvent.emit("Added $addedCount Bangladesh holidays (Google Calendar) to this period.")
            } else {
                _messageEvent.emit("All Bangladesh holidays for this period are already in your list.")
            }
        }
    }

    fun setHolidayOfficeDay(holidayId: Long, isOfficeDay: Boolean) {
        viewModelScope.launch {
            repository.setHolidayOfficeDay(holidayId, isOfficeDay)
            if (isOfficeDay) {
                _messageEvent.emit("Set as regular Office Day (requires punch in/out & working hours).")
            } else {
                _messageEvent.emit("Reverted to Public Holiday (Off Day).")
            }
        }
    }

    fun addHoliday(date: String, name: String, isOfficeDay: Boolean = false, onWarningNeeded: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            val period = activePeriod.value ?: return@launch
            // Check if already leave on date
            val leave = repository.getLeaveForDate(period.id, date)
            if (leave != null) {
                onWarningNeeded?.invoke("This date already has a ${leave.leaveType} leave. Holiday will take precedence in records.")
            }
            val holiday = HolidayEntity(
                periodId = period.id,
                date = date,
                name = name.ifBlank { "Holiday" },
                isOfficeDay = isOfficeDay
            )
            repository.insertHoliday(holiday)
            _messageEvent.emit("Holiday added: ${holiday.name}")
        }
    }

    fun editHoliday(id: Long, date: String, name: String, isOfficeDay: Boolean = false) {
        viewModelScope.launch {
            val period = activePeriod.value ?: return@launch
            val holiday = HolidayEntity(id = id, periodId = period.id, date = date, name = name, isOfficeDay = isOfficeDay)
            repository.updateHoliday(holiday)
            _messageEvent.emit("Holiday updated.")
        }
    }

    fun deleteHoliday(holidayId: Long) {
        viewModelScope.launch {
            repository.deleteHolidayById(holidayId)
            _messageEvent.emit("Holiday removed.")
        }
    }

    // Leave Management
    fun addLeave(date: String, leaveType: String, note: String?, onWarningNeeded: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            val period = activePeriod.value ?: return@launch
            val holiday = repository.getHolidayForDate(period.id, date)
            if (holiday != null) {
                onWarningNeeded?.invoke("This date is already marked as a Holiday ('${holiday.name}'). Leave will take priority.")
            }
            val leave = LeaveEntity(
                periodId = period.id,
                date = date,
                leaveType = leaveType,
                note = note
            )
            repository.insertLeave(leave)
            _messageEvent.emit("$leaveType Leave added for ${DateTimeUtils.formatToDisplayDate(date)}.")
        }
    }

    fun editLeave(id: Long, date: String, leaveType: String, note: String?) {
        viewModelScope.launch {
            val period = activePeriod.value ?: return@launch
            val leave = LeaveEntity(id = id, periodId = period.id, date = date, leaveType = leaveType, note = note)
            repository.updateLeave(leave)
            _messageEvent.emit("Leave record updated.")
        }
    }

    fun deleteLeave(leaveId: Long) {
        viewModelScope.launch {
            repository.deleteLeaveById(leaveId)
            _messageEvent.emit("Leave record removed.")
        }
    }

    // PDF generation
    fun generatePdf(
        context: Context,
        startDateIso: String? = null,
        endDateIso: String? = null,
        rangeTitle: String = "Full Period"
    ) {
        viewModelScope.launch {
            val period = activePeriod.value
            if (period == null) {
                _messageEvent.emit("No active period to export.")
                return@launch
            }

            val allDays = activePeriodDays.value
            val effectiveStart = startDateIso ?: period.startDate
            val effectiveEnd = endDateIso ?: period.endDate

            val filteredDays = allDays.filter { day ->
                day.date >= effectiveStart && day.date <= effectiveEnd
            }
            val summary = AttendanceCalculator.calculateSummary(filteredDays)

            val profileEntity = repository.getUserProfile()
            val userProfile = profileEntity?.let {
                UserProfile(
                    name = it.name,
                    designation = it.designation,
                    department = it.department,
                    employeeId = it.employeeId
                )
            }

            val report = PeriodAttendanceReport(
                period = period,
                rangeTitle = rangeTitle,
                startDate = effectiveStart,
                endDate = effectiveEnd,
                days = filteredDays,
                summary = summary,
                userProfile = userProfile
            )

            val result = AttendancePdfGenerator.generatePdf(context, report)
            result.onSuccess { exportResult ->
                _generatedPdfFile.value = exportResult.file
                _exportedPdfResult.value = exportResult
                _messageEvent.emit("PDF saved to ${exportResult.displayFolderPath}: ${exportResult.fileName}")
            }.onFailure { error ->
                _messageEvent.emit("PDF generation failed: ${error.message}")
            }
        }
    }

    fun clearGeneratedPdf() {
        _generatedPdfFile.value = null
        _exportedPdfResult.value = null
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AttendanceDatabase.getDatabase(application)
            val repo = AttendanceRepository(db.attendanceDao())
            return OfficeHoursViewModel(application, repo) as T
        }
    }
}
