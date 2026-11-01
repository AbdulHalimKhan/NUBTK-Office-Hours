package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.PrimaryLimeGreen
import com.example.viewmodel.OfficeHoursViewModel
import com.example.utils.DateTimeUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()

    var profileName by remember(userProfile) { mutableStateOf(userProfile?.name ?: "") }
    var profileDesignation by remember(userProfile) { mutableStateOf(userProfile?.designation ?: "") }
    var profileDepartment by remember(userProfile) { mutableStateOf(userProfile?.department ?: "") }
    var profileEmployeeId by remember(userProfile) { mutableStateOf(userProfile?.employeeId ?: "") }

    var periodName by remember(activePeriod) { mutableStateOf(activePeriod?.name ?: "") }
    var startDate by remember(activePeriod) { mutableStateOf(activePeriod?.startDate ?: "") }
    var endDate by remember(activePeriod) { mutableStateOf(activePeriod?.endDate ?: "") }
    var dailyHours by remember(activePeriod) {
        val mins = activePeriod?.dailyWorkingMinutes ?: 480
        mutableStateOf((mins / 60).toString())
    }
    var dailyMinutes by remember(activePeriod) {
        val mins = activePeriod?.dailyWorkingMinutes ?: 480
        mutableStateOf((mins % 60).toString())
    }

    var showEditConfirmDialog by remember { mutableStateOf(false) }
    var showCreatePeriodDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Action button: Create New Period
            Button(
                onClick = { showCreatePeriodDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("create_new_period_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CREATE NEW ATTENDANCE PERIOD", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Profile Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_profile_section_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = DarkGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "EMPLOYEE / USER PROFILE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkGreen
                        )
                    }

                    Text(
                        text = "Set your name, designation, department, and employee ID. These are automatically attached to the header of all downloadable PDF attendance reports.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = profileName,
                        onValueChange = { profileName = it },
                        label = { Text("Full Name *") },
                        placeholder = { Text("e.g. Md. Abdul Halim Khan") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = DarkGreen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_profile_name"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = profileDesignation,
                        onValueChange = { profileDesignation = it },
                        label = { Text("Designation") },
                        placeholder = { Text("e.g. Lecturer / Assistant Professor") },
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = DarkGreen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_profile_designation"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = profileDepartment,
                        onValueChange = { profileDepartment = it },
                        label = { Text("Department") },
                        placeholder = { Text("e.g. Computer Science & Engineering") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = DarkGreen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_profile_department"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = profileEmployeeId,
                        onValueChange = { profileEmployeeId = it },
                        label = { Text("Employee ID") },
                        placeholder = { Text("e.g. NUBTK-2023-045") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = DarkGreen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_profile_employee_id"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.saveUserProfile(
                                name = profileName,
                                designation = profileDesignation,
                                department = profileDepartment,
                                employeeId = profileEmployeeId,
                                onComplete = onNavigateBack
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("save_profile_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE & RETURN TO HOME", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notification Settings Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notification_settings_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = DarkGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "OFFICE HOURS NOTIFICATION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGreen,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = if (notificationsEnabled) "Notifications are ON" else "Notifications are OFF",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                            modifier = Modifier.testTag("notification_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "When turned ON, the app sends an alert once when your daily working countdown reaches the target office hours. When turned OFF, notifications are completely prevented without affecting timer countdown or widget features.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Active Period Settings Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_period_settings_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ACTIVE ATTENDANCE PERIOD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = periodName,
                        onValueChange = { periodName = it },
                        label = { Text("Period Name / Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DatePickerField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = "Start Date",
                            placeholder = "YYYY-MM-DD",
                            testTag = "active_period_start_date",
                            modifier = Modifier.weight(1f)
                        )
                        DatePickerField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = "End Date",
                            placeholder = "YYYY-MM-DD",
                            testTag = "active_period_end_date",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "💡 Tap 📅 on either field to pick from calendar, or type manually",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Daily Expected Working Hours",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dailyHours,
                            onValueChange = { dailyHours = it },
                            label = { Text("Hours") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dailyMinutes,
                            onValueChange = { dailyMinutes = it },
                            label = { Text("Minutes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showEditConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryLimeGreen)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Update Period Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Weekend Configuration Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekend_settings_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Weekend, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WEEKEND DAYS CONFIGURATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select all days that should count as weekends. Weekend days have 0 expected hours and do not contribute to delay.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val allDaysOfWeek = listOf(
                        DayOfWeek.SATURDAY,
                        DayOfWeek.SUNDAY,
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allDaysOfWeek.forEach { dayOfWeek ->
                            val isSelected = activeWeekends.contains(dayOfWeek.value)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val updated = if (isSelected) {
                                        activeWeekends - dayOfWeek.value
                                    } else {
                                        activeWeekends + dayOfWeek.value
                                    }
                                    viewModel.updateWeekendDays(updated)
                                },
                                label = {
                                    Text(
                                        text = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US),
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryLimeGreen.copy(alpha = 0.2f),
                                    selectedLabelColor = DarkGreen
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Quick Management Shortcuts Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MANAGEMENT & SYSTEM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onNavigateHolidays,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage Holidays", modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onNavigateLeaves,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.LocalHospital, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage Leaves (Medical / Casual)", modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onNavigateAbout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("About & Developer Info", modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showEditConfirmDialog) {
        ConfirmationDialog(
            title = "Update Active Period?",
            message = "Changing active period dates or daily working hours will re-calculate expected hours and delays for the period. Do you want to proceed?",
            confirmButtonText = "Update & Return",
            onConfirm = {
                val h = dailyHours.toIntOrNull() ?: 8
                val m = dailyMinutes.toIntOrNull() ?: 0
                viewModel.updateActivePeriod(periodName, startDate, endDate, h, m, onComplete = onNavigateBack)
                showEditConfirmDialog = false
            },
            onDismiss = { showEditConfirmDialog = false }
        )
    }

    if (showCreatePeriodDialog) {
        CreatePeriodDialog(
            onDismiss = { showCreatePeriodDialog = false },
            onConfirm = { name, s, e, h, m, weekends, includeBds ->
                viewModel.createNewPeriod(name, s, e, h, m, weekends, includeBds, onComplete = onNavigateBack)
                showCreatePeriodDialog = false
            }
        )
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
        title = { Text("Create New Attendance Period") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Creating a new period will not delete any previous records.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Period Name (e.g. Sep–Oct 2026)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatePickerField(
                        value = start,
                        onValueChange = { start = it },
                        label = "Start Date",
                        placeholder = "YYYY-MM-DD",
                        testTag = "create_period_start_date",
                        modifier = Modifier.weight(1f)
                    )
                    DatePickerField(
                        value = end,
                        onValueChange = { end = it },
                        label = "End Date",
                        placeholder = "YYYY-MM-DD",
                        testTag = "create_period_end_date",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💡 Tap 📅 on either field to pick from calendar, or type manually",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { hours = it },
                        label = { Text("Hours/Day") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { minutes = it },
                        label = { Text("Mins/Day") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Weekend Days:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        DayOfWeek.FRIDAY,
                        DayOfWeek.SATURDAY,
                        DayOfWeek.SUNDAY,
                        DayOfWeek.MONDAY,
                        DayOfWeek.THURSDAY
                    ).forEach { day ->
                        val sel = weekends.contains(day.value)
                        FilterChip(
                            selected = sel,
                            onClick = {
                                weekends = if (sel) weekends - day.value else weekends + day.value
                            },
                            label = { Text(day.getDisplayName(TextStyle.SHORT, Locale.US), fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Include Bangladesh Holidays", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Auto-adds Google Calendar public holidays (editable as office days)", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = includeBangladeshHolidays,
                        onCheckedChange = { includeBangladeshHolidays = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val h = hours.toIntOrNull() ?: 8
                    val m = minutes.toIntOrNull() ?: 0
                    onConfirm(name, start, end, h, m, weekends, includeBangladeshHolidays)
                }
            ) {
                Text("Create Period")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
