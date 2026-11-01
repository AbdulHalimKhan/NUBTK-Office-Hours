package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.DayAttendanceInfo
import com.example.ui.theme.DarkGreen
import com.example.utils.DateTimeUtils
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

enum class PunchEditType {
    PUNCH_IN,
    PUNCH_OUT
}

@Composable
fun EditSinglePunchDialog(
    day: DayAttendanceInfo,
    editType: PunchEditType,
    onDismiss: () -> Unit,
    onSave: (timestamp: Long?, note: String?) -> Unit
) {
    val date = DateTimeUtils.parseIsoDate(day.date)
    val existingTimestamp = if (editType == PunchEditType.PUNCH_IN) {
        day.punchInTimestamp
    } else {
        day.punchOutTimestamp
    }

    val initialTime = remember(existingTimestamp) {
        if (existingTimestamp != null) {
            Instant.ofEpochMilli(existingTimestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        } else if (editType == PunchEditType.PUNCH_IN) {
            LocalTime.of(9, 0)
        } else {
            LocalTime.of(17, 0)
        }
    }

    var hourStr by remember { mutableStateOf(String.format("%02d", initialTime.hour)) }
    var minuteStr by remember { mutableStateOf(String.format("%02d", initialTime.minute)) }
    var noteText by remember { mutableStateOf("") }

    val isPunchIn = editType == PunchEditType.PUNCH_IN
    val titleText = if (isPunchIn) "Edit Punch In" else "Edit Punch Out"
    val icon = if (isPunchIn) Icons.AutoMirrored.Filled.Login else Icons.AutoMirrored.Filled.Logout
    val currentFormatted = if (isPunchIn) day.punchInFormatted else day.punchOutFormatted

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = DarkGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = DateTimeUtils.formatToDisplayDate(day.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (existingTimestamp != null) {
                    Text(
                        text = "Current record: $currentFormatted",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = if (isPunchIn) "Punch In Time (24-Hour Format)" else "Punch Out Time (24-Hour Format)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hourStr,
                        onValueChange = {
                            if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                                hourStr = it
                            }
                        },
                        label = { Text("Hour (0-23)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("punch_edit_hour_input"),
                        singleLine = true
                    )

                    Text(
                        text = ":",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = minuteStr,
                        onValueChange = {
                            if (it.length <= 2 && it.all { char -> char.isDigit() }) {
                                minuteStr = it
                            }
                        },
                        label = { Text("Minute (0-59)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("punch_edit_minute_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val now = LocalTime.now()
                            hourStr = String.format("%02d", now.hour)
                            minuteStr = String.format("%02d", now.minute)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Current Time", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            if (isPunchIn) {
                                hourStr = "09"
                                minuteStr = "00"
                            } else {
                                hourStr = "17"
                                minuteStr = "00"
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isPunchIn) "09:00 AM" else "05:00 PM", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Optional Note") },
                    placeholder = { Text("Reason for adjustment...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("punch_edit_note_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (existingTimestamp != null) {
                    TextButton(
                        onClick = {
                            onSave(null, noteText.ifBlank { null })
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }

                Button(
                    onClick = {
                        val h = hourStr.toIntOrNull()?.coerceIn(0, 23) ?: (if (isPunchIn) 9 else 17)
                        val m = minuteStr.toIntOrNull()?.coerceIn(0, 59) ?: 0
                        val newTimestamp = DateTimeUtils.buildTimestamp(date, h, m)
                        onSave(newTimestamp, noteText.ifBlank { null })
                    },
                    modifier = Modifier.testTag("save_punch_edit_button")
                ) {
                    Text("Save $titleText")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
