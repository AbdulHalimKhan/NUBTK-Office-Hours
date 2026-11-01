package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.AttendanceStatus
import com.example.domain.DayAttendanceInfo
import com.example.domain.DayClassification
import com.example.notifications.NotificationHelper
import com.example.ui.components.AttendanceStatusBadge
import com.example.ui.components.DelayIndicator
import com.example.ui.components.EditSinglePunchDialog
import com.example.ui.components.ManualPunchDialog
import com.example.ui.components.PunchButton
import com.example.ui.components.PunchEditType
import com.example.ui.components.UserProfileDialog
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.PrimaryLimeGreen
import com.example.ui.theme.SecondaryGreen
import com.example.utils.DateTimeUtils
import com.example.viewmodel.OfficeHoursViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: OfficeHoursViewModel,
    onNavigateCurrentRecord: () -> Unit,
    onNavigatePreviousRecords: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateAbout: () -> Unit,
    onNavigateHolidays: () -> Unit,
    onNavigateLeaves: () -> Unit
) {
    val context = LocalContext.current
    val activePeriod by viewModel.activePeriod.collectAsStateWithLifecycle()
    val summary by viewModel.activePeriodSummary.collectAsStateWithLifecycle()
    val todayInfo by viewModel.todayInfo.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var showManualPunchDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var singlePunchEditType by remember { mutableStateOf<PunchEditType?>(null) }
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var notifiedTargetMetForDate by remember { mutableStateOf<String?>(null) }

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
                    IconButton(
                        onClick = onNavigateAbout,
                        modifier = Modifier.testTag("nav_about_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onNavigateSettings,
                        modifier = Modifier.testTag("nav_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Employee Profile Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_user_profile_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(DarkGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (userProfile?.name?.takeIf { it.isNotBlank() }?.firstOrNull() ?: 'U').uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile?.name?.takeIf { it.isNotBlank() } ?: "Set Employee Name",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val subtitle = buildString {
                            val desig = userProfile?.designation?.takeIf { it.isNotBlank() }
                            val dept = userProfile?.department?.takeIf { it.isNotBlank() }
                            if (desig != null && dept != null) append("$desig, $dept")
                            else if (desig != null) append(desig)
                            else if (dept != null) append(dept)
                            else append("Faculty / Staff • NUBTK")
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp
                        )

                        val empId = userProfile?.employeeId?.takeIf { it.isNotBlank() }
                        if (empId != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ID: $empId",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkGreen
                            )
                        }
                    }

                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.testTag("edit_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = DarkGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Current Period Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("current_period_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CURRENT ATTENDANCE PERIOD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activePeriod?.let {
                                "${DateTimeUtils.formatToDisplayDate(it.startDate)} – ${DateTimeUtils.formatToDisplayDate(it.endDate)}"
                            } ?: "No Active Period Configured",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (activePeriod != null) {
                            Text(
                                text = "Expected daily hours: ${DateTimeUtils.formatMinutesToHoursMinutes(activePeriod!!.dailyWorkingMinutes)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Today's Attendance Card
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

                    val isTodayOffDay = todayInfo != null && (!todayInfo!!.isWorkingDay || todayInfo!!.baseClassification != DayClassification.WORKING_DAY)

                    if (!isTodayOffDay) {
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
            }

            Spacer(modifier = Modifier.height(14.dp))

            val isOffDay = todayInfo != null && (!todayInfo!!.isWorkingDay || todayInfo!!.baseClassification != DayClassification.WORKING_DAY)
            val dailyWorkingMins = activePeriod?.dailyWorkingMinutes ?: 480
            val targetDurationMillis = dailyWorkingMins * 60 * 1000L
            val isPunchedInActive = todayInfo?.punchInTimestamp != null && todayInfo?.punchOutTimestamp == null

            // Today's Working Countdown Card (shown on working days when period is active)
            if (!isOffDay && activePeriod != null) {
                val inTimestamp = todayInfo?.punchInTimestamp ?: 0L
                val elapsedMillis = if (isPunchedInActive) (currentTime - inTimestamp).coerceAtLeast(0L) else 0L
                val remainingMillis = targetDurationMillis - elapsedMillis
                val isTargetMet = isPunchedInActive && remainingMillis <= 0L

                // Trigger notification when work hours meet the daily target (enforces single-trigger via persistent session key)
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

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("today_countdown_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTargetMet) {
                            PrimaryLimeGreen.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isTargetMet) PrimaryLimeGreen.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            if (isTargetMet) PrimaryLimeGreen else DarkGreen,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Countdown Timer",
                                        tint = if (isTargetMet) Color.Black else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "TODAY'S WORKING COUNTDOWN",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTargetMet) DarkGreen else MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Daily Target: ${DateTimeUtils.formatMinutesToHoursMinutes(dailyWorkingMins)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isPunchedInActive) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isTargetMet) DarkGreen else MaterialTheme.colorScheme.primaryContainer,
                                            RoundedCornerShape(100.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isTargetMet) "GOAL MET 🎉" else "IN PROGRESS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTargetMet) Color.White else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        when {
                            isPunchedInActive -> {
                                val remainingSecondsTotal = (remainingMillis / 1000).coerceAtLeast(0L)
                                val remHrs = remainingSecondsTotal / 3600
                                val remMins = (remainingSecondsTotal % 3600) / 60
                                val remSecs = remainingSecondsTotal % 60
                                val formattedCountdown = String.format(Locale.getDefault(), "%02d:%02d:%02d", remHrs, remMins, remSecs)

                                val progress = (elapsedMillis.toFloat() / targetDurationMillis.toFloat()).coerceIn(0f, 1f)

                                if (isTargetMet) {
                                    val overtimeTotalSecs = (-remainingMillis / 1000).coerceAtLeast(0L)
                                    val ovHrs = overtimeTotalSecs / 3600
                                    val ovMins = (overtimeTotalSecs % 3600) / 60
                                    val ovSecs = overtimeTotalSecs % 60
                                    val formattedOvertime = String.format(Locale.getDefault(), "+%02d:%02d:%02d", ovHrs, ovMins, ovSecs)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column {
                                            Text(
                                                text = "Required Hours Completed!",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkGreen
                                            )
                                            Text(
                                                text = "Overtime / Extra time worked:",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = formattedOvertime,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkGreen
                                        )
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column {
                                            Text(
                                                text = "Remaining Time:",
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            val targetFinishTimestamp = inTimestamp + targetDurationMillis
                                            Text(
                                                text = "Finish by: ${DateTimeUtils.formatTimestampTo12H(targetFinishTimestamp)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = formattedCountdown,
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = if (isTargetMet) DarkGreen else PrimaryLimeGreen,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val percent = (progress * 100).toInt()
                                    Text(
                                        text = "$percent% completed",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (isTargetMet) "Target achieved" else "${DateTimeUtils.formatMinutesToHoursMinutes((elapsedMillis / 60000).toInt())} worked",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            todayInfo?.punchOutTimestamp != null -> {
                                Text(
                                    text = "Shift completed for today! Total duration: ${todayInfo?.durationFormatted ?: "—"}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            else -> {
                                Text(
                                    text = "Ready to start work. Punch In to begin today\'s ${DateTimeUtils.formatMinutesToHoursMinutes(dailyWorkingMins)} countdown.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            if (isOffDay) {
                // On offday, do NOT show any button. Just show the message "Today is your off day"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("off_day_message_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
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
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            val reason = when (todayInfo?.baseClassification) {
                                DayClassification.WEEKEND -> "Weekend"
                                DayClassification.HOLIDAY -> todayInfo?.statusDetail?.takeIf { it.isNotBlank() } ?: "Public Holiday"
                                DayClassification.MEDICAL_LEAVE -> "Medical Leave"
                                DayClassification.CASUAL_LEAVE -> "Casual Leave"
                                else -> todayInfo?.statusDetail ?: "No office hours scheduled today"
                            }
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            } else {
                // 3. Main Action Buttons: PUNCH IN / PUNCH OUT (shown on working days)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val hasPunchIn = todayInfo?.punchInTimestamp != null
                    val hasPunchOut = todayInfo?.punchOutTimestamp != null

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
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Four Major Actions
            Text(
                text = "RECORDS & MANAGEMENT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = onNavigateCurrentRecord,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("current_record_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SecondaryGreen.copy(alpha = 0.15f),
                        contentColor = DarkGreen
                    )
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CURRENT RECORD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                FilledTonalButton(
                    onClick = onNavigatePreviousRecords,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("previous_record_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PREVIOUS RECORD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary row: Quick links to Holiday and Leave management
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateHolidays,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("home_holidays_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Manage Holidays", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onNavigateLeaves,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("home_leaves_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Manage Leaves", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
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

    if (showEditProfileDialog) {
        UserProfileDialog(
            initialProfile = userProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, desig, dept, empId ->
                viewModel.saveUserProfile(name, desig, dept, empId)
                showEditProfileDialog = false
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
