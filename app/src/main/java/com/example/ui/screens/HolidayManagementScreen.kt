package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.data.local.HolidayEntity
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.LightGreen
import com.example.ui.theme.StatusHoliday
import com.example.utils.DateTimeUtils
import com.example.viewmodel.OfficeHoursViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayManagementScreen(
    viewModel: OfficeHoursViewModel,
    onNavigateBack: () -> Unit
) {
    val activePeriod by viewModel.activePeriod.collectAsStateWithLifecycle()
    val holidays by viewModel.activePeriodHolidays.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var holidayToEdit by remember { mutableStateOf<HolidayEntity?>(null) }
    var holidayToDelete by remember { mutableStateOf<HolidayEntity?>(null) }
    var warningMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Holiday Management",
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
                modifier = Modifier.testTag("add_holiday_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Holiday")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Import Bangladesh Holidays from Google Calendar Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_bd_holidays_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkGreen.copy(alpha = 0.08f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkGreen.copy(alpha = 0.35f)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkGreen.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.EventAvailable,
                                        contentDescription = null,
                                        tint = DarkGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Google Calendar Bangladesh Holidays",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGreen
                                )
                                Text(
                                    text = "Auto-import official public holidays for this period. Any holiday can be toggled to an office day as needed.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.importBangladeshHolidays() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("import_bd_holidays_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Bangladesh Holidays")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Public Holidays have 0 expected hours. If your office stays open, set it as an 'Office Day' to expect full working hours.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            if (holidays.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No holidays registered for this period.\nTap 'Import Bangladesh Holidays' or '+' to add.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(holidays, key = { it.id }) { holiday ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("holiday_card_${holiday.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (holiday.isOfficeDay) {
                                Color(0xFFF1F8E9) // subtle light green background for office day
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (holiday.isOfficeDay) Icons.Default.Business else Icons.Default.Celebration,
                                        contentDescription = null,
                                        tint = if (holiday.isOfficeDay) DarkGreen else StatusHoliday,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.size(12.dp))
                                    Column {
                                        Text(
                                            text = holiday.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = DateTimeUtils.formatToDisplayDate(holiday.date),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { holidayToEdit = holiday },
                                        modifier = Modifier.testTag("edit_holiday_${holiday.id}")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(
                                        onClick = { holidayToDelete = holiday },
                                        modifier = Modifier.testTag("delete_holiday_${holiday.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Status Chip and Quick Action Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (holiday.isOfficeDay) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = DarkGreen.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Business,
                                                contentDescription = null,
                                                tint = DarkGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Office Day (Working)",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkGreen
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.setHolidayOfficeDay(holiday.id, false) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("revert_holiday_${holiday.id}")
                                    ) {
                                        Text("Revert to Off Day", fontSize = 11.5.sp)
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = StatusHoliday.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Celebration,
                                                contentDescription = null,
                                                tint = StatusHoliday,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Public Holiday (Off)",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = StatusHoliday
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { viewModel.setHolidayOfficeDay(holiday.id, true) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("set_office_day_${holiday.id}")
                                    ) {
                                        Text("Set as Office Day", fontSize = 11.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var dateText by remember { mutableStateOf(activePeriod?.startDate ?: DateTimeUtils.todayIso()) }
        var nameText by remember { mutableStateOf("") }
        var isOfficeDay by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Holiday") },
            text = {
                Column {
                    DatePickerField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = "Date",
                        placeholder = "YYYY-MM-DD",
                        testTag = "holiday_add_date",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Holiday Name") },
                        placeholder = { Text("e.g. University Foundation Day") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Set as Office Day",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "If enabled, office hours will be expected on this day.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isOfficeDay,
                            onCheckedChange = { isOfficeDay = it },
                            modifier = Modifier.testTag("add_holiday_is_office_day_switch")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameText.isNotBlank()) {
                            viewModel.addHoliday(dateText, nameText, isOfficeDay) { warning ->
                                warningMessage = warning
                            }
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_holiday_btn")
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

    if (holidayToEdit != null) {
        var dateText by remember { mutableStateOf(holidayToEdit!!.date) }
        var nameText by remember { mutableStateOf(holidayToEdit!!.name) }
        var isOfficeDay by remember { mutableStateOf(holidayToEdit!!.isOfficeDay) }

        AlertDialog(
            onDismissRequest = { holidayToEdit = null },
            title = { Text("Edit Holiday") },
            text = {
                Column {
                    DatePickerField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = "Date",
                        placeholder = "YYYY-MM-DD",
                        testTag = "holiday_edit_date",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Holiday Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Set as Office Day",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isOfficeDay) "Currently expected as working office day." else "Currently an off-day (0 expected hours).",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isOfficeDay,
                            onCheckedChange = { isOfficeDay = it },
                            modifier = Modifier.testTag("edit_holiday_is_office_day_switch")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameText.isNotBlank()) {
                            viewModel.editHoliday(holidayToEdit!!.id, dateText, nameText, isOfficeDay)
                            holidayToEdit = null
                        }
                    },
                    modifier = Modifier.testTag("save_edit_holiday_btn")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { holidayToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (holidayToDelete != null) {
        ConfirmationDialog(
            title = "Delete Holiday?",
            message = "Remove holiday '${holidayToDelete!!.name}' on ${holidayToDelete!!.date}?",
            confirmButtonText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteHoliday(holidayToDelete!!.id)
                holidayToDelete = null
            },
            onDismiss = { holidayToDelete = null }
        )
    }

    if (warningMessage != null) {
        AlertDialog(
            onDismissRequest = { warningMessage = null },
            title = { Text("Date Conflict Notice") },
            text = { Text(warningMessage!!) },
            confirmButton = {
                Button(onClick = { warningMessage = null }) {
                    Text("Understood")
                }
            }
        )
    }
}
