package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AttendanceStatus
import com.example.ui.theme.StatusCasualLeave
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusHoliday
import com.example.ui.theme.StatusInProgress
import com.example.ui.theme.StatusMedicalLeave
import com.example.ui.theme.StatusWeekend

@Composable
fun AttendanceStatusBadge(
    status: AttendanceStatus,
    modifier: Modifier = Modifier,
    customText: String? = null
) {
    val (backgroundColor, textColor) = when (status) {
        AttendanceStatus.COMPLETED,
        AttendanceStatus.WORKED_ON_WEEKEND,
        AttendanceStatus.WORKED_ON_HOLIDAY,
        AttendanceStatus.WORKED_DURING_LEAVE -> StatusCompleted.copy(alpha = 0.15f) to StatusCompleted
        AttendanceStatus.IN_PROGRESS -> StatusInProgress.copy(alpha = 0.18f) to Color(0xFF33691E)
        AttendanceStatus.WEEKEND -> StatusWeekend.copy(alpha = 0.15f) to StatusWeekend
        AttendanceStatus.HOLIDAY -> StatusHoliday.copy(alpha = 0.15f) to StatusHoliday
        AttendanceStatus.MEDICAL_LEAVE -> StatusMedicalLeave.copy(alpha = 0.15f) to StatusMedicalLeave
        AttendanceStatus.CASUAL_LEAVE -> StatusCasualLeave.copy(alpha = 0.15f) to StatusCasualLeave
        AttendanceStatus.WORKING_DAY -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    val displayText = customText ?: status.displayName

    Box(
        modifier = modifier
            .testTag("status_badge_${status.name.lowercase()}")
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = displayText,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}
