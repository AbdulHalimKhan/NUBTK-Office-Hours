package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.DayAttendanceInfo
import com.example.utils.DateTimeUtils

@Composable
fun ManualPunchDialog(
    day: DayAttendanceInfo,
    onDismiss: () -> Unit,
    onSave: (inTimestamp: Long?, outTimestamp: Long?, note: String?) -> Unit
) {
    val date = DateTimeUtils.parseIsoDate(day.date)

    // Prepopulate hours/mins
    var inHour by remember { mutableStateOf("09") }
    var inMinute by remember { mutableStateOf("00") }
    var hasIn by remember { mutableStateOf(day.punchInTimestamp != null) }

    var outHour by remember { mutableStateOf("17") }
    var outMinute by remember { mutableStateOf("00") }
    var hasOut by remember { mutableStateOf(day.punchOutTimestamp != null) }

    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log / Edit Punch (${DateTimeUtils.formatToDisplayDate(day.date)})",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Punch In Time (24h)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = inHour,
                        onValueChange = { if (it.length <= 2) inHour = it },
                        label = { Text("Hour") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = inMinute,
                        onValueChange = { if (it.length <= 2) inMinute = it },
                        label = { Text("Minute") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Punch Out Time (24h)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = outHour,
                        onValueChange = { if (it.length <= 2) outHour = it },
                        label = { Text("Hour") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = outMinute,
                        onValueChange = { if (it.length <= 2) outMinute = it },
                        label = { Text("Minute") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

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
                    val ih = inHour.toIntOrNull()?.coerceIn(0, 23) ?: 9
                    val im = inMinute.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    val inTs = DateTimeUtils.buildTimestamp(date, ih, im)

                    val oh = outHour.toIntOrNull()?.coerceIn(0, 23) ?: 17
                    val om = outMinute.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    val outTs = DateTimeUtils.buildTimestamp(date, oh, om)

                    onSave(inTs, outTs, noteText.ifBlank { null })
                }
            ) {
                Text("Save Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
