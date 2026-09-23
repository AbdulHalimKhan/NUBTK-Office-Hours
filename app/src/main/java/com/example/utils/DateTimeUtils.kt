package com.example.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

object DateTimeUtils {
    val ISO_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    val DISPLAY_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)
    val SHORT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale.US)
    val TIME_12H_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
    val TIME_24H_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    val FULL_DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.US)

    fun today(): LocalDate = LocalDate.now()

    fun todayIso(): String = today().format(ISO_DATE_FORMATTER)

    fun parseIsoDate(iso: String): LocalDate {
        return try {
            LocalDate.parse(iso, ISO_DATE_FORMATTER)
        } catch (_: Exception) {
            LocalDate.now()
        }
    }

    fun formatToDisplayDate(iso: String): String {
        return try {
            val date = LocalDate.parse(iso, ISO_DATE_FORMATTER)
            date.format(DISPLAY_DATE_FORMATTER)
        } catch (_: Exception) {
            iso
        }
    }

    fun formatToShortDate(iso: String): String {
        return try {
            val date = LocalDate.parse(iso, ISO_DATE_FORMATTER)
            date.format(SHORT_DATE_FORMATTER)
        } catch (_: Exception) {
            iso
        }
    }

    fun formatTimestampTo12H(timestamp: Long?): String {
        if (timestamp == null || timestamp <= 0) return "—"
        return try {
            val localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
            localTime.format(TIME_12H_FORMATTER)
        } catch (_: Exception) {
            "—"
        }
    }

    fun formatTimestampTo24H(timestamp: Long?): String {
        if (timestamp == null || timestamp <= 0) return "—"
        return try {
            val localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
            localTime.format(TIME_24H_FORMATTER)
        } catch (_: Exception) {
            "—"
        }
    }

    fun formatMinutesToHoursMinutes(minutes: Int): String {
        val totalMinutes = abs(minutes)
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return String.format(Locale.US, "%02d:%02d", hours, mins)
    }

    fun formatSignedDelay(delayMinutes: Int): String {
        return formatMinutesToHoursMinutes(delayMinutes)
    }

    fun calculateDurationMinutes(punchIn: Long?, punchOut: Long?): Int {
        if (punchIn == null || punchOut == null || punchOut <= punchIn) return 0
        val durationMillis = punchOut - punchIn
        return (durationMillis / (1000 * 60)).toInt()
    }

    fun buildTimestamp(date: LocalDate, hour: Int, minute: Int): Long {
        val dateTime = LocalDateTime.of(date, LocalTime.of(hour, minute))
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
