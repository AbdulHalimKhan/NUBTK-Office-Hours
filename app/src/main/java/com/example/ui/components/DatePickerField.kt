package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.example.utils.DateTimeUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * An interactive date input field supporting both manual text input (typing YYYY-MM-DD)
 * and interactive calendar selection via Material 3 DatePicker dialog.
 */
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "YYYY-MM-DD",
    showHelperText: Boolean = true,
    testTag: String = ""
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = {
                IconButton(
                    onClick = { showDatePicker = true },
                    modifier = if (testTag.isNotEmpty()) Modifier.testTag("${testTag}_calendar_btn") else Modifier
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Pick $label from calendar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            supportingText = if (showHelperText) {
                {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Manual or tap 📅 Calendar",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else null,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
        )
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateIso = value,
            title = "Select $label",
            onDateSelected = { selectedIso ->
                onValueChange(selectedIso)
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * Material 3 Calendar DatePicker dialog modal.
 * Supports both Calendar grid view and manual date input via the mode toggle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialDateIso: String,
    title: String = "Select Date",
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMillis = remember(initialDateIso) {
        try {
            val localDate = LocalDate.parse(initialDateIso.trim(), DateTimeUtils.ISO_DATE_FORMATTER)
            localDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        } catch (_: Exception) {
            LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        initialDisplayMode = DisplayMode.Picker
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        onDateSelected(localDate.format(DateTimeUtils.ISO_DATE_FORMATTER))
                    }
                    onDismiss()
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = true,
            title = {
                Text(
                    text = title,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        )
    }
}
