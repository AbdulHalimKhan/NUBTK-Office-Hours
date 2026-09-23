package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.DelayColorState
import com.example.domain.PeriodSummary
import com.example.ui.theme.StatusOnTime
import com.example.ui.theme.StatusShortfall
import com.example.ui.theme.StatusWorkedMore

@Composable
fun AttendanceSummaryCard(
    summary: PeriodSummary,
    modifier: Modifier = Modifier,
    title: String = "Attendance Summary"
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("attendance_summary_card"),
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
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                DelayIndicator(
                    delayFormatted = summary.delaySignedFormatted,
                    delayState = summary.delayColorState
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary metrics: Expected vs Actual vs Extra/Delay vs Working Days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(
                    label = "Expected",
                    value = summary.expectedFormatted,
                    modifier = Modifier.weight(1f)
                )
                MetricColumn(
                    label = "Actual",
                    value = summary.actualFormatted,
                    modifier = Modifier.weight(1f)
                )

                val diffLabel = summary.delayOrExtraLabel
                val diffColor = when (summary.delayColorState) {
                    DelayColorState.RED -> StatusShortfall
                    DelayColorState.GREEN -> StatusWorkedMore
                    DelayColorState.AMBER -> StatusOnTime
                }
                MetricColumn(
                    label = diffLabel,
                    value = summary.delaySignedFormatted.replace("+", "").replace("-", ""),
                    labelColor = diffColor,
                    valueColor = diffColor,
                    modifier = Modifier.weight(1f)
                )
                MetricColumn(
                    label = "Working Days",
                    value = "${summary.workingDaysCompleted} / ${summary.workingDays}",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            // Breakdown of calendar days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MiniMetric(label = "Calendar", value = "${summary.calendarDays} d")
                MiniMetric(label = "Weekends", value = "${summary.weekendDays} d")
                MiniMetric(label = "Holidays", value = "${summary.holidayDays} d")
                MiniMetric(label = "Medical", value = "${summary.medicalLeaveDays} d")
                MiniMetric(label = "Casual", value = "${summary.casualLeaveDays} d")
            }
        }
    }
}

@Composable
private fun MetricColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (labelColor != MaterialTheme.colorScheme.onSurfaceVariant) FontWeight.SemiBold else FontWeight.Normal,
            color = labelColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun MiniMetric(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
