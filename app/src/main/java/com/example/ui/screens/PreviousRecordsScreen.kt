package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AttendancePeriodEntity
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.PrimaryLimeGreen
import com.example.utils.DateTimeUtils
import com.example.viewmodel.OfficeHoursViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviousRecordsScreen(
    viewModel: OfficeHoursViewModel,
    onNavigateBack: () -> Unit,
    onPeriodSelected: () -> Unit
) {
    val periods by viewModel.allPeriods.collectAsStateWithLifecycle()
    val activePeriod by viewModel.activePeriod.collectAsStateWithLifecycle()

    var periodToActivate by remember { mutableStateOf<AttendancePeriodEntity?>(null) }
    var periodToDelete by remember { mutableStateOf<AttendancePeriodEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Previous Attendance Records",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "ALL ATTENDANCE PERIODS (${periods.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Tap on any period to make it active and inspect its records, generate PDFs, and review progress.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (periods.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No attendance periods found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(periods, key = { it.id }) { period ->
                    val isActive = period.id == activePeriod?.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("period_item_${period.id}")
                            .clickable {
                                if (!isActive) {
                                    periodToActivate = period
                                } else {
                                    onPeriodSelected()
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 3.dp else 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = if (isActive) "Active" else "Inactive",
                                        tint = if (isActive) DarkGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = period.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) DarkGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (isActive) {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkGreen
                                    )
                                } else {
                                    IconButton(
                                        onClick = { periodToDelete = period },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Period",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${DateTimeUtils.formatToDisplayDate(period.startDate)} — ${DateTimeUtils.formatToDisplayDate(period.endDate)}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${DateTimeUtils.formatMinutesToHoursMinutes(period.dailyWorkingMinutes)}/day",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (!isActive) {
                                            periodToActivate = period
                                        } else {
                                            onPeriodSelected()
                                        }
                                    }
                                ) {
                                    Text(if (isActive) "View Records" else "Switch & View")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (periodToActivate != null) {
        ConfirmationDialog(
            title = "Switch Active Period?",
            message = "Do you want to switch the active attendance period to '${periodToActivate!!.name}'? Your historical records will be preserved.",
            confirmButtonText = "Switch",
            onConfirm = {
                viewModel.switchActivePeriod(periodToActivate!!.id)
                periodToActivate = null
                onPeriodSelected()
            },
            onDismiss = { periodToActivate = null }
        )
    }

    if (periodToDelete != null) {
        ConfirmationDialog(
            title = "Delete Attendance Period?",
            message = "Are you sure you want to delete '${periodToDelete!!.name}' and all associated records? This cannot be undone.",
            confirmButtonText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deletePeriod(periodToDelete!!)
                periodToDelete = null
            },
            onDismiss = { periodToDelete = null }
        )
    }
}
