package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.DelayColorState
import com.example.ui.theme.StatusOnTime
import com.example.ui.theme.StatusShortfall
import com.example.ui.theme.StatusWorkedMore

@Composable
fun DelayIndicator(
    delayFormatted: String,
    delayState: DelayColorState,
    modifier: Modifier = Modifier
) {
    val cleanDelay = delayFormatted.replace("+", "").replace("-", "")
    val (color, icon, label) = when (delayState) {
        DelayColorState.RED -> Triple(StatusShortfall, Icons.Default.Warning, "Delay")
        DelayColorState.GREEN -> Triple(StatusWorkedMore, Icons.Default.CheckCircle, "Extra")
        DelayColorState.AMBER -> Triple(StatusOnTime, Icons.Default.Schedule, "On Time")
    }

    Box(
        modifier = modifier
            .testTag("delay_indicator")
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$label $cleanDelay",
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
