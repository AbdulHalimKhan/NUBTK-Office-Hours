package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.ui.components.DatePickerField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LeaveEntity
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.StatusCasualLeave
import com.example.ui.theme.StatusMedicalLeave
import com.example.utils.DateTimeUtils
import com.example.viewmodel.OfficeHoursViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveManagementScreen(
    viewModel: OfficeHoursViewModel,
    onNavigateBack: () -> Unit
) {
    val activePeriod by viewModel.activePeriod.collectAsStateWithLifecycle()
    val leaves by viewModel.activePeriodLeaves.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var leaveToEdit by remember { mutableStateOf<LeaveEntity?>(null) }
    var leaveToDelete by remember { mutableStateOf<LeaveEntity?>(null) }
    var warningMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Leave Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        activePeriod?.let {
                            Text(
                                text = it.name,
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DarkGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_leave_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Leave")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {


            if (leaves.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No leave records logged for this period.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(leaves, key = { it.id }) { leave ->
                    val isMedical = leave.leaveType.equals("MEDICAL", ignoreCase = true)
                    val badgeColor = if (isMedical) StatusMedicalLeave else StatusCasualLeave

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("leave_card_${leave.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.LocalHospital,
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.size(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isMedical) "Medical Leave" else "Casual Leave",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = badgeColor
                                        )
                                    }
                                    Text(
                                        text = DateTimeUtils.formatToDisplayDate(leave.date),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!leave.note.isNullOrBlank()) {
                                        Text(
                                            text = "Note: ${leave.note}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Row {
                                IconButton(onClick = { leaveToEdit = leave }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { leaveToDelete = leave }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var dateText by remember { mutableStateOf(DateTimeUtils.todayIso()) }
        var leaveType by remember { mutableStateOf("MEDICAL") } // MEDICAL or CASUAL
        var noteText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Leave Record") },
            text = {
                Column {
                    DatePickerField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = "Date",
                        placeholder = "YYYY-MM-DD",
                        testTag = "leave_add_date",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Leave Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = leaveType == "MEDICAL",
                            onClick = { leaveType = "MEDICAL" },
                            label = { Text("Medical Leave") }
                        )
                        FilterChip(
                            selected = leaveType == "CASUAL",
                            onClick = { leaveType = "CASUAL" },
                            label = { Text("Casual Leave") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Optional Note") },
                        placeholder = { Text("e.g. Sick leave, doctor visit") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addLeave(dateText, leaveType, noteText.ifBlank { null }) { warning ->
                            warningMessage = warning
                        }
                        showAddDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (leaveToEdit != null) {
        var dateText by remember { mutableStateOf(leaveToEdit!!.date) }
        var leaveType by remember { mutableStateOf(leaveToEdit!!.leaveType) }
        var noteText by remember { mutableStateOf(leaveToEdit!!.note ?: "") }

        AlertDialog(
            onDismissRequest = { leaveToEdit = null },
            title = { Text("Edit Leave Record") },
            text = {
                Column {
                    DatePickerField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = "Date",
                        placeholder = "YYYY-MM-DD",
                        testTag = "leave_edit_date",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Leave Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = leaveType.equals("MEDICAL", ignoreCase = true),
                            onClick = { leaveType = "MEDICAL" },
                            label = { Text("Medical Leave") }
                        )
                        FilterChip(
                            selected = leaveType.equals("CASUAL", ignoreCase = true),
                            onClick = { leaveType = "CASUAL" },
                            label = { Text("Casual Leave") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Optional Note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.editLeave(leaveToEdit!!.id, dateText, leaveType, noteText.ifBlank { null })
                        leaveToEdit = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { leaveToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (leaveToDelete != null) {
        ConfirmationDialog(
            title = "Delete Leave Record?",
            message = "Remove ${leaveToDelete!!.leaveType} Leave on ${leaveToDelete!!.date}?",
            confirmButtonText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteLeave(leaveToDelete!!.id)
                leaveToDelete = null
            },
            onDismiss = { leaveToDelete = null }
        )
    }

    if (warningMessage != null) {
        AlertDialog(
            onDismissRequest = { warningMessage = null },
            title = { Text("Date Notice") },
            text = { Text(warningMessage!!) },
            confirmButton = {
                Button(onClick = { warningMessage = null }) {
                    Text("Understood")
                }
            }
        )
    }
}
