package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.AttendanceStatus
import com.example.domain.DayClassification
import com.example.notifications.NotificationHelper
import com.example.ui.components.AttendanceStatusBadge
import com.example.ui.components.DelayIndicator
import com.example.ui.components.EditSinglePunchDialog
import com.example.ui.components.ManualPunchDialog
import com.example.ui.components.PunchButton
import com.example.ui.components.PunchEditType
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.PrimaryLimeGreen
import com.example.utils.DateTimeUtils
import com.example.viewmodel.OfficeHoursViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: OfficeHoursViewModel,
    onNavigateEmployeeInfo: () -> Unit,
    onNavigateAttendancePeriod: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateAttendanceRecords: () -> Unit,
    onNavigateManagement: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val activePeriod by viewModel.activePeriod.collectAsStateWithLifecycle()
    val summary by viewModel.activePeriodSummary.collectAsStateWithLifecycle()
    val todayInfo by viewModel.todayInfo.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var showManualPunchDialog by remember { mutableStateOf(false) }
    var singlePunchEditType by remember { mutableStateOf<PunchEditType?>(null) }
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // System back button handler: close drawer first if it is open
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    // Request runtime notification permission on Android 13+ (API 33+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { /* permission callback handled */ }

        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Ticker running every second for live countdown
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTime = System.currentTimeMillis()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(300.dp)
                    .testTag("navigation_drawer_sheet"),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Drawer Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(DarkGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "NUBTK Timer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val userName = userProfile?.name?.takeIf { it.isNotBlank() }
                        Text(
                            text = userName ?: "Office Hours Tracker",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))

                // Item 1: Employee Information
                NavigationDrawerItem(
                    label = { Text("Employee Information", fontWeight = FontWeight.Medium) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Employee Information"
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateEmployeeInfo()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("menu_item_employee_info")
                )

                // Item 2: Attendance Period
                NavigationDrawerItem(
                    label = { Text("Attendance Period", fontWeight = FontWeight.Medium) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Attendance Period"
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateAttendancePeriod()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("menu_item_attendance_period")
                )

                // Item 3: Settings
                NavigationDrawerItem(
                    label = { Text("Settings", fontWeight = FontWeight.Medium) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateSettings()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("menu_item_settings")
                )

                // Item 4: Attendance Records
                NavigationDrawerItem(
                    label = { Text("Attendance Records", fontWeight = FontWeight.Medium) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Attendance Records"
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateAttendanceRecords()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("menu_item_attendance_records")
                )

                // Item 5: Management
                NavigationDrawerItem(
                    label = { Text("Management", fontWeight = FontWeight.Medium) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Management"
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateManagement()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("menu_item_management")
                )

                Spacer(modifier = Modifier.weight(1f))

                // Drawer Footer
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Version ${com.example.BuildConfig.VERSION_NAME} • NUBTK Office Hours",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(DarkGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Clock",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "NUBTK Timer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Track Your Time. Know Your Progress.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        // Three-line Hamburger Menu icon placed in the top app bar at the top-right corner
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("hamburger_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Navigation Menu",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val isOfficialHoliday = todayInfo?.baseClassification == DayClassification.HOLIDAY
                val isApprovedLeave = todayInfo?.baseClassification == DayClassification.CASUAL_LEAVE ||
                        todayInfo?.baseClassification == DayClassification.MEDICAL_LEAVE
                val isWeekendOrOff = todayInfo != null && (!todayInfo!!.isWorkingDay || todayInfo!!.baseClassification == DayClassification.WEEKEND) &&
                        !isOfficialHoliday && !isApprovedLeave

                // 1. Prominent Status / Countdown Display
                when {
                    isOfficialHoliday -> {
                        // Official Holiday Card: display HOLIDAY and available holiday description instead of countdown
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("official_holiday_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ),
                            border = BorderStroke(1.5.dp, DarkGreen.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 28.dp, horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(DarkGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Celebration,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "HOLIDAY",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp,
                                    color = DarkGreen
                                )
                                val holidayDetail = todayInfo?.statusDetail?.takeIf { it.isNotBlank() } ?: "Official Public Holiday"
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = holidayDetail,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    isApprovedLeave -> {
                        // Approved Leave Card: display ON LEAVE and leave information instead of countdown
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("approved_leave_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                            ),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 28.dp, horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(MaterialTheme.colorScheme.secondary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "ON LEAVE",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                val leaveDetail = todayInfo?.statusDetail?.takeIf { it.isNotBlank() } ?: "Approved Leave Day"
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = leaveDetail,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    isWeekendOrOff -> {
                        // Weekend / Off-day Card: show off-day message and no punch button
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("off_day_message_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(DarkGreen.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Weekend,
                                        contentDescription = null,
                                        tint = DarkGreen,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Today is your off day",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val reason = todayInfo?.statusDetail?.takeIf { it.isNotBlank() } ?: "Weekend • No office hours scheduled"
                                    Text(
                                        text = reason,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        // Normal Working Day: Display PROMINENT COUNTDOWN
                        // Strictly NO label or heading above or beside the countdown
                        val dailyWorkingMins = activePeriod?.dailyWorkingMinutes ?: 480
                        val targetDurationMillis = dailyWorkingMins * 60 * 1000L
                        val inTimestamp = todayInfo?.punchInTimestamp ?: 0L
                        val hasPunchIn = todayInfo?.punchInTimestamp != null
                        val hasPunchOut = todayInfo?.punchOutTimestamp != null
                        val isPunchedInActive = hasPunchIn && !hasPunchOut

                        val elapsedMillis = if (isPunchedInActive) (currentTime - inTimestamp).coerceAtLeast(0L) else 0L
                        val remainingMillis = targetDurationMillis - elapsedMillis
                        val isTargetMet = isPunchedInActive && remainingMillis <= 0L

                        // Trigger notification only once when work hours meet the target
                        LaunchedEffect(isTargetMet, inTimestamp) {
                            if (isTargetMet && isPunchedInActive) {
                                val todayIso = DateTimeUtils.todayIso()
                                val hrs = dailyWorkingMins / 60
                                val mins = dailyWorkingMins % 60
                                NotificationHelper.maybeNotifyWorkHoursCompleted(
                                    context = context,
                                    dateIso = todayIso,
                                    punchInTimestamp = inTimestamp,
                                    targetHours = hrs,
                                    targetMinutes = mins
                                )
                            }
                        }

                        val countdownDisplayText = when {
                            isPunchedInActive -> {
                                if (isTargetMet) {
                                    val overtimeSecs = (-remainingMillis / 1000).coerceAtLeast(0L)
                                    val ovHrs = overtimeSecs / 3600
                                    val ovMins = (overtimeSecs % 3600) / 60
                                    val ovSecs = overtimeSecs % 60
                                    String.format(Locale.getDefault(), "+%02d:%02d:%02d", ovHrs, ovMins, ovSecs)
                                } else {
                                    val remainingSecs = (remainingMillis / 1000).coerceAtLeast(0L)
                                    val remHrs = remainingSecs / 3600
                                    val remMins = (remainingSecs % 3600) / 60
                                    val remSecs = remainingSecs % 60
                                    String.format(Locale.getDefault(), "%02d:%02d:%02d", remHrs, remMins, remSecs)
                                }
                            }
                            hasPunchOut -> {
                                "00:00:00"
                            }
                            else -> {
                                val hrs = dailyWorkingMins / 60
                                val mins = dailyWorkingMins % 60
                                String.format(Locale.getDefault(), "%02d:%02d:00", hrs, mins)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("today_countdown_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isTargetMet) PrimaryLimeGreen.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.5.dp,
                                if (isTargetMet) DarkGreen.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Strictly only the countdown timer itself without any label above or beside
                                Text(
                                    text = countdownDisplayText,
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp,
                                    color = if (isTargetMet) DarkGreen else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.testTag("countdown_timer_display")
                                )

                                if (isPunchedInActive) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    val progress = (elapsedMillis.toFloat() / targetDurationMillis.toFloat()).coerceIn(0f, 1f)
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = if (isTargetMet) DarkGreen else PrimaryLimeGreen,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Punch In / Punch Out Action Buttons (shown on working days)
                val isWorkingDay = !isOfficialHoliday && !isApprovedLeave && !isWeekendOrOff
                if (isWorkingDay) {
                    val hasPunchIn = todayInfo?.punchInTimestamp != null
                    val hasPunchOut = todayInfo?.punchOutTimestamp != null

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PunchButton(
                            isPunchIn = true,
                            onClick = { viewModel.punchInToday() },
                            modifier = Modifier.weight(1f),
                            enabled = !hasPunchIn
                        )

                        PunchButton(
                            isPunchIn = false,
                            onClick = { viewModel.punchOutToday() },
                            modifier = Modifier.weight(1f),
                            enabled = hasPunchIn && !hasPunchOut
                        )
                    }

                    // 3. Today's Punch Information & Edit Controls
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("today_attendance_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "TODAY",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = DateTimeUtils.formatToDisplayDate(DateTimeUtils.todayIso()),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                val status = todayInfo?.displayStatus ?: AttendanceStatus.WORKING_DAY
                                val statusDetail = todayInfo?.statusDetail
                                AttendanceStatusBadge(
                                    status = status,
                                    customText = if (!statusDetail.isNullOrBlank() && status != AttendanceStatus.COMPLETED) {
                                        "${status.displayName} ($statusDetail)"
                                    } else null
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Punch In", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = todayInfo?.punchInFormatted ?: "—",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column {
                                    Text("Punch Out", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = todayInfo?.punchOutFormatted ?: "—",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column {
                                    Text("Duration", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = todayInfo?.durationFormatted ?: "—",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { singlePunchEditType = PunchEditType.PUNCH_IN },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("edit_punch_in_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Login,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Edit Punch In", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { singlePunchEditType = PunchEditType.PUNCH_OUT },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("edit_punch_out_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Edit Punch Out", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // 4. Working Days Progress Summary Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("progress_summary_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "WORKING DAYS PROGRESS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                DelayIndicator(
                                    delayFormatted = summary.delaySignedFormatted,
                                    delayState = summary.delayColorState
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "Working Days: ${summary.workingDaysCompleted} / ${summary.workingDays}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val percentage = (summary.progressFraction * 100).toInt()
                                Text(
                                    text = "$percentage%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { summary.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = PrimaryLimeGreen,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Expected", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(summary.expectedFormatted, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Actual", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(summary.actualFormatted, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                                val diffLabel = summary.delayOrExtraLabel
                                val diffColor = when (summary.delayColorState) {
                                    com.example.domain.DelayColorState.RED -> com.example.ui.theme.StatusShortfall
                                    com.example.domain.DelayColorState.GREEN -> com.example.ui.theme.StatusWorkedMore
                                    com.example.domain.DelayColorState.AMBER -> com.example.ui.theme.StatusOnTime
                                }
                                Column {
                                    Text(
                                        text = diffLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = diffColor
                                    )
                                    Text(
                                        text = summary.delaySignedFormatted.replace("+", "").replace("-", ""),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = diffColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showManualPunchDialog && todayInfo != null) {
        ManualPunchDialog(
            day = todayInfo!!,
            onDismiss = { showManualPunchDialog = false },
            onSave = { inTs, outTs, note ->
                viewModel.manualPunch(todayInfo!!.date, inTs, outTs, note)
                showManualPunchDialog = false
            }
        )
    }

    if (singlePunchEditType != null && todayInfo != null) {
        EditSinglePunchDialog(
            day = todayInfo!!,
            editType = singlePunchEditType!!,
            onDismiss = { singlePunchEditType = null },
            onSave = { ts, note ->
                if (singlePunchEditType == PunchEditType.PUNCH_IN) {
                    viewModel.editPunchIn(todayInfo!!.date, ts, note)
                } else {
                    viewModel.editPunchOut(todayInfo!!.date, ts, note)
                }
                singlePunchEditType = null
            }
        )
    }
}
