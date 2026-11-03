package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.DatePickerField
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.PrimaryLimeGreen
import com.example.utils.DateTimeUtils
import com.example.viewmodel.OfficeHoursViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AttendancePeriodScreen(
    viewModel: OfficeHoursViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRecords: () -> Unit
) {
    val periods by viewModel.allPeriods.collectAsStateWithLifecycle()
    val activePeriod by viewModel.activePeriod.collectAsStateWithLifecycle()
    val activeWeekends by viewModel.activePeriodWeekends.collectAsStateWithLifecycle()

    var showEditPeriodDialog by remember { mutableStateOf(false) }
    var showCreatePeriodDialog by remember { mutableStateOf(false) }
    var periodToSwitch by remember { mutableStateOf<AttendancePeriodEntity?>(null) }
    var periodToDelete by remember { mutableStateOf<AttendancePeriodEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Attendance Period",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("attendance_period_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showCreatePeriodDialog = true },
                        modifier = Modifier.testTag("create_period_topbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Period",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Current Active Attendance Period Card
            Text(
                text = "CURRENT ACTIVE PERIOD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            if (activePeriod != null) {
                val period = activePeriod!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("current_active_period_details_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = period.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${DateTimeUtils.formatToDisplayDate(period.startDate)} – ${DateTimeUtils.formatToDisplayDate(period.endDate)}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(DarkGreen, RoundedCornerShape(100.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Daily Target", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    DateTimeUtils.formatMinutesToHoursMinutes(period.dailyWorkingMinutes),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text("Weekends", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val weekendNames = activeWeekends.map { dayVal ->
                                    DayOfWeek.of(dayVal).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                }.joinToString(", ").ifBlank { "None" }
                                Text(
                                    weekendNames,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showEditPeriodDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("edit_active_period_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Configure", fontSize = 12.sp)
                            }

                            Button(
                                onClick = onNavigateToRecords,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("view_period_records_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View Records", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No active attendance period set.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showCreatePeriodDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Attendance Period")
                        }
                    }
                }
            }

            // Section 2: Switch Attendance Period
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SWITCH ATTENDANCE PERIOD (${periods.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                TextButton(
                    onClick = { showCreatePeriodDialog = true },
                    modifier = Modifier.testTag("add_new_period_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Period", fontSize = 12.sp)
                }
            }

            if (periods.isEmpty()) {
                Text(
                    text = "No previous attendance periods found.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    periods.forEach { period ->
                        val isActive = period.id == activePeriod?.id

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("period_switch_card_${period.id}")
                                .clickable {
                                    if (!isActive) {
                                        periodToSwitch = period
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 3.dp else 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = if (isActive) "Active" else "Inactive",
                                    tint = if (isActive) DarkGreen else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = period.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${DateTimeUtils.formatToDisplayDate(period.startDate)} – ${DateTimeUtils.formatToDisplayDate(period.endDate)}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Daily: ${DateTimeUtils.formatMinutesToHoursMinutes(period.dailyWorkingMinutes)}",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (!isActive) {
                                    IconButton(
                                        onClick = { periodToDelete = period },
                                        modifier = Modifier.testTag("delete_period_${period.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Period",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for switching active period
    periodToSwitch?.let { target ->
        ConfirmationDialog(
            title = "Switch Attendance Period?",
            message = "Do you want to switch active attendance period to '${target.name}' (${DateTimeUtils.formatToDisplayDate(target.startDate)} – ${DateTimeUtils.formatToDisplayDate(target.endDate)})?",
            confirmButtonText = "Switch Period",
            onConfirm = {
                viewModel.switchActivePeriod(target.id)
                periodToSwitch = null
            },
            onDismiss = { periodToSwitch = null }
        )
    }

    // Confirmation dialog for deleting a period
    periodToDelete?.let { target ->
        ConfirmationDialog(
            title = "Delete Attendance Period?",
            message = "Are you sure you want to delete period '${target.name}'? All records, holidays, and leaves associated with this period will be permanently removed.",
            confirmButtonText = "Delete Permanently",
            onConfirm = {
                viewModel.deletePeriod(target)
                periodToDelete = null
            },
            onDismiss = { periodToDelete = null }
        )
    }

    // Dialog for configuring / editing active period
    if (showEditPeriodDialog && activePeriod != null) {
        val curr = activePeriod!!
        var editName by remember { mutableStateOf(curr.name) }
        var editStart by remember { mutableStateOf(curr.startDate) }
        var editEnd by remember { mutableStateOf(curr.endDate) }
        var editHours by remember { mutableIntStateOf(curr.dailyWorkingMinutes / 60) }
        var editMinutes by remember { mutableIntStateOf(curr.dailyWorkingMinutes % 60) }
        var editWeekends by remember { mutableStateOf(activeWeekends) }

        AlertDialog(
            onDismissRequest = { showEditPeriodDialog = false },
            title = {
                Text("Configure Attendance Period", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                            label = { Text("Daily Hours") },
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

                    Text("Weekly Off-Days (Weekends)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DayOfWeek.entries.forEach { dow ->
                            val isSelected = editWeekends.contains(dow.value)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    editWeekends = if (isSelected) {
                                        editWeekends - dow.value
                                    } else {
                                        editWeekends + dow.value
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
                        viewModel.updateActivePeriod(
                            name = editName,
                            startDate = editStart,
                            endDate = editEnd,
                            dailyHours = editHours,
                            dailyMinutes = editMinutes
                        )
                        viewModel.updateWeekendDays(editWeekends)
                        showEditPeriodDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPeriodDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog for creating new attendance period
    if (showCreatePeriodDialog) {
        var newName by remember { mutableStateOf("") }
        var newStart by remember { mutableStateOf(DateTimeUtils.todayIso()) }
        var newEnd by remember { mutableStateOf(DateTimeUtils.today().plusMonths(1).format(DateTimeUtils.ISO_DATE_FORMATTER)) }
        var newHours by remember { mutableIntStateOf(8) }
        var newMinutes by remember { mutableIntStateOf(0) }
        var newWeekends by remember { mutableStateOf(setOf(DayOfWeek.FRIDAY.value, DayOfWeek.SATURDAY.value)) }

        AlertDialog(
            onDismissRequest = { showCreatePeriodDialog = false },
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
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Period Name (optional)") },
                        placeholder = { Text("e.g. Nov 2026 – Dec 2026") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DatePickerField(
                        label = "Start Date",
                        value = newStart,
                        onValueChange = { newStart = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DatePickerField(
                        label = "End Date",
                        value = newEnd,
                        onValueChange = { newEnd = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newHours.toString(),
                            onValueChange = { newHours = it.toIntOrNull() ?: 0 },
                            label = { Text("Daily Hours") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newMinutes.toString(),
                            onValueChange = { newMinutes = it.toIntOrNull() ?: 0 },
                            label = { Text("Minutes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Weekly Off-Days (Weekends)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DayOfWeek.entries.forEach { dow ->
                            val isSelected = newWeekends.contains(dow.value)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    newWeekends = if (isSelected) {
                                        newWeekends - dow.value
                                    } else {
                                        newWeekends + dow.value
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
                        viewModel.createNewPeriod(
                            name = newName,
                            startDate = newStart,
                            endDate = newEnd,
                            dailyHours = newHours,
                            dailyMinutes = newMinutes,
                            weekendDays = newWeekends
                        )
                        showCreatePeriodDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text("Create Period")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePeriodDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
