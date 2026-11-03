package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.ui.components.DatePickerField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AttendancePeriodEntity
import com.example.data.local.LeaveEntity
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.PrimaryLimeGreen
import com.example.ui.theme.SecondaryGreen
import com.example.ui.theme.StatusCasualLeave
import com.example.ui.theme.StatusMedicalLeave
import com.example.viewmodel.OfficeHoursViewModel
import com.example.utils.DateTimeUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val SEVEN_DAYS_OF_WEEK = listOf(
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY,
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: OfficeHoursViewModel,
    onNavigateBack: () -> Unit,
    onNavigateHolidays: () -> Unit,
    onNavigateLeaves: () -> Unit,
    onNavigateAbout: () -> Unit
) {
    val activePeriod by viewModel.activePeriod.collectAsStateWithLifecycle()
    val activeWeekends by viewModel.activePeriodWeekends.collectAsStateWithLifecycle()
    val activeLeaves by viewModel.activePeriodLeaves.collectAsStateWithLifecycle()
    val activeHolidays by viewModel.activePeriodHolidays.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()

    var showSetPeriodDialog by remember { mutableStateOf(false) }
    var showCreatePeriodDialog by remember { mutableStateOf(false) }
    var showSetWeekendDialog by remember { mutableStateOf(false) }
    var showManageLeaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Setting Preference",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Notification Section: Title and ON/OFF switch only
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notification_settings_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = DarkGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Notification",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                        modifier = Modifier.testTag("notification_toggle_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DarkGreen
                        )
                    )
                }
            }

            // 2. Attendance Period Section: Short title, summary, and "Set Attendance Period" button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_period_settings_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Attendance Period",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (activePeriod != null) {
                        Text(
                            text = activePeriod!!.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${DateTimeUtils.formatToDisplayDate(activePeriod!!.startDate)} – ${DateTimeUtils.formatToDisplayDate(activePeriod!!.endDate)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Daily Target: ${DateTimeUtils.formatMinutesToHoursMinutes(activePeriod!!.dailyWorkingMinutes)}",
                            fontSize = 12.sp,
                            color = DarkGreen,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "No active attendance period set",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showSetPeriodDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("set_attendance_period_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                        ) {
                            Text("Set Attendance Period", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showCreatePeriodDialog = true },
                            modifier = Modifier.testTag("create_period_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 3. Weekend Days Configuration Section: Displays selected days, with single "Set Weekend" button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekend_settings_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Weekend, contentDescription = null, tint = DarkGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Weekend Configuration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val weekendNames = SEVEN_DAYS_OF_WEEK
                        .filter { activeWeekends.contains(it.value) }
                        .joinToString(", ") { it.getDisplayName(TextStyle.FULL, Locale.US) }
                        .ifBlank { "No weekend days set" }

                    Text(
                        text = weekendNames,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkGreen,
                        modifier = Modifier.testTag("selected_weekend_display")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showSetWeekendDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("set_weekend_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) {
                        Text("Set Weekend", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // 4. Leave Management Section: Short title, summary, with single "Manage Leave" button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("leave_management_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = SecondaryGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Leave Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${activeLeaves.size} leave record(s) in active period",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showManageLeaveDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manage_leave_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) {
                        Text("Manage Leave", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Holiday Management Section: Short title, summary, with single "Manage Holiday" button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("holiday_management_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Celebration, contentDescription = null, tint = SecondaryGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Manage Holiday",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${activeHolidays.size} holiday record(s) in active period",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onNavigateHolidays,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manage_holiday_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) {
                        Text("Manage Holiday", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialog 1: Set Attendance Period Popup
    if (showSetPeriodDialog && activePeriod != null) {
        val curr = activePeriod!!
        var editName by remember { mutableStateOf(curr.name) }
        var editStart by remember { mutableStateOf(curr.startDate) }
        var editEnd by remember { mutableStateOf(curr.endDate) }
        var editHours by remember { mutableIntStateOf(curr.dailyWorkingMinutes / 60) }
        var editMinutes by remember { mutableIntStateOf(curr.dailyWorkingMinutes % 60) }

        AlertDialog(
            onDismissRequest = { showSetPeriodDialog = false },
            title = {
                Text("Set Attendance Period", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Period Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DatePickerField(
                        label = "Start Date",
                        value = editStart,
                        onValueChange = { editStart = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DatePickerField(
                        label = "End Date",
                        value = editEnd,
                        onValueChange = { editEnd = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editHours.toString(),
                            onValueChange = { editHours = it.toIntOrNull() ?: 0 },
                            label = { Text("Hours") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editMinutes.toString(),
                            onValueChange = { editMinutes = it.toIntOrNull() ?: 0 },
                            label = { Text("Minutes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateActivePeriod(
                            name = editName,
                            startDate = editStart,
                            endDate = editEnd,
                            dailyHours = editHours,
                            dailyMinutes = editMinutes
                        )
                        showSetPeriodDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetPeriodDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog 2: Create New Period Popup
    if (showCreatePeriodDialog) {
        CreatePeriodDialog(
            onDismiss = { showCreatePeriodDialog = false },
            onConfirm = { name, s, e, h, m, weekends, includeBds ->
                viewModel.createNewPeriod(name, s, e, h, m, weekends, includeBds)
                showCreatePeriodDialog = false
            }
        )
    }

    // Dialog 3: Set Weekend Popup
    // Lists all 7 days with checkboxes, allows selecting single or multiple days, Confirm and Cancel
    if (showSetWeekendDialog) {
        var tempWeekends by remember { mutableStateOf(activeWeekends) }

        AlertDialog(
            onDismissRequest = { showSetWeekendDialog = false },
            title = {
                Text("Set Weekend", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SEVEN_DAYS_OF_WEEK.forEach { dayOfWeek ->
                        val isSelected = tempWeekends.contains(dayOfWeek.value)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    tempWeekends = if (isSelected) {
                                        tempWeekends - dayOfWeek.value
                                    } else {
                                        tempWeekends + dayOfWeek.value
                                    }
                                }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    tempWeekends = if (checked) {
                                        tempWeekends + dayOfWeek.value
                                    } else {
                                        tempWeekends - dayOfWeek.value
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = DarkGreen
                                )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) DarkGreen else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateWeekendDays(tempWeekends)
                        showSetWeekendDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                    modifier = Modifier.testTag("confirm_weekend_button")
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSetWeekendDialog = false },
                    modifier = Modifier.testTag("cancel_weekend_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog 4: Manage Leave Popup
    // Enter leave type (Medical / Casual), start date, end date, reason, Confirm and Cancel, plus list of leaves
    if (showManageLeaveDialog) {
        var leaveType by remember { mutableStateOf("CASUAL") }
        var leaveStartDate by remember { mutableStateOf(DateTimeUtils.todayIso()) }
        var leaveEndDate by remember { mutableStateOf(DateTimeUtils.todayIso()) }
        var leaveReason by remember { mutableStateOf("") }
        var leaveToDelete by remember { mutableStateOf<LeaveEntity?>(null) }

        AlertDialog(
            onDismissRequest = { showManageLeaveDialog = false },
            title = {
                Text("Manage Leave", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Leave Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = leaveType == "CASUAL",
                            onClick = { leaveType = "CASUAL" },
                            label = { Text("Casual Leave") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusCasualLeave.copy(alpha = 0.2f),
                                selectedLabelColor = DarkGreen
                            )
                        )
                        FilterChip(
                            selected = leaveType == "MEDICAL",
                            onClick = { leaveType = "MEDICAL" },
                            label = { Text("Medical Leave") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusMedicalLeave.copy(alpha = 0.2f),
                                selectedLabelColor = DarkGreen
                            )
                        )
                    }

                    DatePickerField(
                        label = "Start Date",
                        value = leaveStartDate,
                        onValueChange = { leaveStartDate = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DatePickerField(
                        label = "End Date",
                        value = leaveEndDate,
                        onValueChange = { leaveEndDate = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = leaveReason,
                        onValueChange = { leaveReason = it },
                        label = { Text("Reason (Optional)") },
                        placeholder = { Text("e.g. Doctor appointment, personal matter") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (activeLeaves.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Text(
                            text = "Existing Leaves (${activeLeaves.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        activeLeaves.take(5).forEach { leave ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${DateTimeUtils.formatToDisplayDate(leave.date)} • ${leave.leaveType}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    val note = leave.note?.takeIf { it.isNotBlank() }
                                    if (note != null) {
                                        Text(note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                IconButton(
                                    onClick = { leaveToDelete = leave },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val start = DateTimeUtils.parseIsoDate(leaveStartDate)
                        val end = DateTimeUtils.parseIsoDate(leaveEndDate)
                        var curr = if (start.isAfter(end)) end else start
                        val finalEnd = if (start.isAfter(end)) start else end

                        while (!curr.isAfter(finalEnd)) {
                            val dStr = curr.format(DateTimeUtils.ISO_DATE_FORMATTER)
                            viewModel.addLeave(dStr, leaveType, leaveReason.ifBlank { null })
                            curr = curr.plusDays(1)
                        }
                        showManageLeaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                    modifier = Modifier.testTag("confirm_add_leave_button")
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showManageLeaveDialog = false },
                    modifier = Modifier.testTag("cancel_manage_leave_button")
                ) {
                    Text("Cancel")
                }
            }
        )

        leaveToDelete?.let { target ->
            AlertDialog(
                onDismissRequest = { leaveToDelete = null },
                title = { Text("Delete Leave Record?") },
                text = { Text("Remove ${target.leaveType} Leave on ${DateTimeUtils.formatToDisplayDate(target.date)}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteLeave(target.id)
                            leaveToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { leaveToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreatePeriodDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, startDate: String, endDate: String, dailyHours: Int, dailyMinutes: Int, weekendDays: Set<Int>, includeBangladeshHolidays: Boolean) -> Unit
) {
    val today = remember { LocalDate.now() }
    var name by remember { mutableStateOf("") }
    var start by remember { mutableStateOf(today.format(DateTimeUtils.ISO_DATE_FORMATTER)) }
    var end by remember { mutableStateOf(today.plusDays(29).format(DateTimeUtils.ISO_DATE_FORMATTER)) }
    var hours by remember { mutableStateOf("8") }
    var minutes by remember { mutableStateOf("0") }
    var weekends by remember { mutableStateOf(setOf(DayOfWeek.FRIDAY.value, DayOfWeek.SATURDAY.value)) }
    var includeBangladeshHolidays by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Attendance Period", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Period Name (optional)") },
                    placeholder = { Text("e.g. Nov 2026 – Dec 2026") },
                    modifier = Modifier.fillMaxWidth()
                )

                DatePickerField(
                    label = "Start Date",
                    value = start,
                    onValueChange = { start = it },
                    modifier = Modifier.fillMaxWidth()
                )

                DatePickerField(
                    label = "End Date",
                    value = end,
                    onValueChange = { end = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { hours = it },
                        label = { Text("Hours") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { minutes = it },
                        label = { Text("Minutes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Weekend Days", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SEVEN_DAYS_OF_WEEK.forEach { dow ->
                        val isSelected = weekends.contains(dow.value)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                weekends = if (isSelected) {
                                    weekends - dow.value
                                } else {
                                    weekends + dow.value
                                }
                            },
                            label = { Text(dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val h = hours.toIntOrNull() ?: 8
                    val m = minutes.toIntOrNull() ?: 0
                    onConfirm(name, start, end, h, m, weekends, includeBangladeshHolidays)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
