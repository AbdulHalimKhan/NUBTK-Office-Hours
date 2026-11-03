package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.AttendanceCalculator
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.domain.DayAttendanceInfo
import com.example.pdf.AttendancePdfGenerator
import com.example.ui.components.AttendanceSummaryCard
import com.example.ui.components.AttendanceTable
import com.example.ui.components.EditSinglePunchDialog
import com.example.ui.components.ManualPunchDialog
import com.example.ui.components.PunchEditType
import com.example.ui.components.UserProfileDialog
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.PrimaryLimeGreen
import com.example.utils.DateTimeUtils
import com.example.utils.PdfShareUtils
import com.example.viewmodel.OfficeHoursViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentRecordScreen(
    viewModel: OfficeHoursViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activePeriod by viewModel.activePeriod.collectAsStateWithLifecycle()
    val allDays by viewModel.activePeriodDays.collectAsStateWithLifecycle()
    val fullSummary by viewModel.activePeriodSummary.collectAsStateWithLifecycle()
    val generatedPdf by viewModel.generatedPdfFile.collectAsStateWithLifecycle()
    val exportedPdfResult by viewModel.exportedPdfResult.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    val (targetFolderName, targetFolderPath) = remember(context) {
        AttendancePdfGenerator.getTargetFolderInfo(context)
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Full Period, 1: Selected Days
    var selectedSectionIndex by remember { mutableIntStateOf(0) } // 0: Days 1-10, 1: Days 11-20, 2: Days 21-End, 3: Custom

    var customStartDate by remember { mutableStateOf("") }
    var customEndDate by remember { mutableStateOf("") }

    var actionChooserDay by remember { mutableStateOf<DayAttendanceInfo?>(null) }
    var singlePunchEditDay by remember { mutableStateOf<Pair<DayAttendanceInfo, PunchEditType>?>(null) }
    var editingDay by remember { mutableStateOf<DayAttendanceInfo?>(null) }

    val tenDayRanges = remember(activePeriod) {
        if (activePeriod != null) {
            AttendanceCalculator.generateTenDayRanges(activePeriod!!.startDate, activePeriod!!.endDate)
        } else emptyList()
    }

    val (displayedDays, displayedSummary, rangeTitle, exportStart, exportEnd) = remember(
        selectedTabIndex,
        selectedSectionIndex,
        customStartDate,
        customEndDate,
        allDays,
        fullSummary,
        tenDayRanges,
        activePeriod
    ) {
        if (selectedTabIndex == 0) {
            PeriodViewData(
                days = allDays,
                summary = fullSummary,
                title = "Full Period",
                start = activePeriod?.startDate ?: "",
                end = activePeriod?.endDate ?: ""
            )
        } else {
            // Selected Days logic
            if (selectedSectionIndex < tenDayRanges.size) {
                val range = tenDayRanges[selectedSectionIndex]
                val filtered = allDays.filter { it.date >= range.startDate && it.date <= range.endDate }
                val summary = AttendanceCalculator.calculateSummary(filtered)
                PeriodViewData(
                    days = filtered,
                    summary = summary,
                    title = range.title,
                    start = range.startDate,
                    end = range.endDate
                )
            } else {
                // Custom Range
                val s = customStartDate.ifBlank { activePeriod?.startDate ?: "" }
                val e = customEndDate.ifBlank { activePeriod?.endDate ?: "" }
                val filtered = allDays.filter { it.date >= s && it.date <= e }
                val summary = AttendanceCalculator.calculateSummary(filtered)
                PeriodViewData(
                    days = filtered,
                    summary = summary,
                    title = "Custom Range",
                    start = s,
                    end = e
                )
            }
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            viewModel.savePdfToUri(
                context = context,
                uri = uri,
                startDateIso = exportStart,
                endDateIso = exportEnd,
                rangeTitle = rangeTitle
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Current Attendance Record",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        activePeriod?.let {
                            Text(
                                text = "${DateTimeUtils.formatToDisplayDate(it.startDate)} – ${DateTimeUtils.formatToDisplayDate(it.endDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            createDocumentLauncher.launch("Attendance_Report.pdf")
                        },
                        modifier = Modifier.testTag("download_pdf_topbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Attendance PDF",
                            tint = DarkGreen
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { showOverflowMenu = true },
                            modifier = Modifier.testTag("pdf_overflow_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "PDF Options"
                            )
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            if (generatedPdf != null) {
                                DropdownMenuItem(
                                    text = { Text("Open PDF") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Visibility,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        PdfShareUtils.openPdf(context, generatedPdf!!)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share PDF") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        PdfShareUtils.sharePdf(context, generatedPdf!!)
                                    }
                                )
                                HorizontalDivider()
                            }
                            DropdownMenuItem(
                                text = { Text("Copy Saved Folder Path") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    val pathToCopy = exportedPdfResult?.folderPath ?: targetFolderPath
                                    PdfShareUtils.copyPathToClipboard(context, pathToCopy)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Attached Profile") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    showEditProfileDialog = true
                                }
                            )
                        }
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Tabs: Full Period vs Selected Days
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("current_record_tabs")
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Full Period", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = {
                        selectedTabIndex = 1
                        if (customStartDate.isBlank() && activePeriod != null) {
                            customStartDate = activePeriod!!.startDate
                            customEndDate = activePeriod!!.endDate
                        }
                    },
                    text = { Text("Selected Days", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // If Selected Days tab is active, show the 10-day section chips + custom option
            if (selectedTabIndex == 1) {
                Text(
                    text = "SELECT SECTION / RANGE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tenDayRanges.forEachIndexed { idx, r ->
                        FilterChip(
                            selected = selectedSectionIndex == idx,
                            onClick = { selectedSectionIndex = idx },
                            label = { Text(r.title, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryLimeGreen.copy(alpha = 0.2f),
                                selectedLabelColor = DarkGreen
                            )
                        )
                    }
                    FilterChip(
                        selected = selectedSectionIndex == tenDayRanges.size,
                        onClick = { selectedSectionIndex = tenDayRanges.size },
                        label = { Text("Custom Range", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryLimeGreen.copy(alpha = 0.2f),
                            selectedLabelColor = DarkGreen
                        )
                    )
                }

                // If custom range selected, show start / end inputs
                if (selectedSectionIndex == tenDayRanges.size) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DatePickerField(
                            value = customStartDate,
                            onValueChange = { customStartDate = it },
                            label = "Start Date",
                            placeholder = "YYYY-MM-DD",
                            testTag = "custom_range_start",
                            modifier = Modifier.weight(1f)
                        )
                        DatePickerField(
                            value = customEndDate,
                            onValueChange = { customEndDate = it },
                            label = "End Date",
                            placeholder = "YYYY-MM-DD",
                            testTag = "custom_range_end",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "💡 Tap 📅 on either field to pick from calendar, or type manually",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Summary Card
            AttendanceSummaryCard(
                summary = displayedSummary,
                title = "$rangeTitle Summary"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Attendance Table
            AttendanceTable(
                days = displayedDays,
                onDayClick = { day -> actionChooserDay = day }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (actionChooserDay != null) {
        val targetDay = actionChooserDay!!
        AlertDialog(
            onDismissRequest = { actionChooserDay = null },
            title = {
                Text(
                    text = "Edit Attendance (${DateTimeUtils.formatToDisplayDate(targetDay.date)})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Choose punch action to perform for this date:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = {
                            val day = targetDay
                            actionChooserDay = null
                            singlePunchEditDay = Pair(day, PunchEditType.PUNCH_IN)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Punch In (${targetDay.punchInFormatted})")
                    }

                    OutlinedButton(
                        onClick = {
                            val day = targetDay
                            actionChooserDay = null
                            singlePunchEditDay = Pair(day, PunchEditType.PUNCH_OUT)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Punch Out (${targetDay.punchOutFormatted})")
                    }

                    OutlinedButton(
                        onClick = {
                            val day = targetDay
                            actionChooserDay = null
                            editingDay = day
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Both / Manual Entry")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { actionChooserDay = null }) {
                    Text("Close")
                }
            }
        )
    }

    if (singlePunchEditDay != null) {
        val (targetDay, editType) = singlePunchEditDay!!
        EditSinglePunchDialog(
            day = targetDay,
            editType = editType,
            onDismiss = { singlePunchEditDay = null },
            onSave = { ts, note ->
                if (editType == PunchEditType.PUNCH_IN) {
                    viewModel.editPunchIn(targetDay.date, ts, note)
                } else {
                    viewModel.editPunchOut(targetDay.date, ts, note)
                }
                singlePunchEditDay = null
            }
        )
    }

    if (editingDay != null) {
        ManualPunchDialog(
            day = editingDay!!,
            onDismiss = { editingDay = null },
            onSave = { inTs, outTs, note ->
                viewModel.manualPunch(editingDay!!.date, inTs, outTs, note)
                editingDay = null
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
}

private data class PeriodViewData(
    val days: List<DayAttendanceInfo>,
    val summary: com.example.domain.PeriodSummary,
    val title: String,
    val start: String,
    val end: String
)
