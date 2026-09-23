package com.example.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.domain.DayAttendanceInfo
import com.example.domain.DelayColorState
import com.example.domain.PeriodAttendanceReport
import com.example.domain.PeriodSummary
import com.example.utils.DateTimeUtils
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

data class PdfExportResult(
    val file: File,
    val fileName: String,
    val folderPath: String,
    val displayFolderPath: String,
    val savedInDownloads: Boolean
)

object AttendancePdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 width at 72dpi
    private const val PAGE_HEIGHT = 842 // A4 height at 72dpi
    private const val MARGIN = 36f

    fun getTargetFolderInfo(context: Context): Pair<String, String> {
        val publicDownloads = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "NUBTK_Office_Hours")
        return Pair("Downloads / NUBTK_Office_Hours", publicDownloads.absolutePath)
    }

    fun generatePdf(
        context: Context,
        report: PeriodAttendanceReport
    ): Result<PdfExportResult> {
        return try {
            val document = PdfDocument()

            val titlePaint = Paint().apply {
                color = Color.rgb(0x2E, 0x7D, 0x32) // Dark Green
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(0x43, 0xA0, 0x47)
                textSize = 11.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val textPaint = Paint().apply {
                color = Color.rgb(0x1B, 0x1B, 0x1B)
                textSize = 9.5f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val boldTextPaint = Paint().apply {
                color = Color.rgb(0x1B, 0x1B, 0x1B)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val footerPaint = Paint().apply {
                color = Color.rgb(0x5F, 0x66, 0x5F)
                textSize = 8f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            val page1Capacity = 24
            val subsequentPageCapacity = 34

            val pages = mutableListOf<List<DayAttendanceInfo>>()
            if (report.days.isEmpty()) {
                pages.add(emptyList())
            } else {
                var currentIndex = 0
                var isFirst = true
                while (currentIndex < report.days.size) {
                    val capacity = if (isFirst) page1Capacity else subsequentPageCapacity
                    val endIndex = minOf(currentIndex + capacity, report.days.size)
                    pages.add(report.days.subList(currentIndex, endIndex))
                    currentIndex = endIndex
                    isFirst = false
                }
            }
            val totalPages = pages.size

            for ((pageIdx, pageRows) in pages.withIndex()) {
                val pageNumber = pageIdx + 1
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                var y = MARGIN

                if (pageNumber == 1) {
                    // Header with Employee Info
                    y = drawHeader(canvas, report, titlePaint, subtitlePaint, textPaint, y)
                    // Summary cards/table
                    y = drawSummary(canvas, report.summary, y)
                } else {
                    // Running header on continuation pages
                    canvas.drawText(
                        "NUBTK Office Hours — Attendance Report (Page $pageNumber of $totalPages)",
                        MARGIN,
                        y + 12f,
                        boldTextPaint
                    )
                    y += 24f
                }

                // Attendance Table
                y = drawTable(canvas, pageRows, y, boldTextPaint, textPaint)

                // Footer
                drawFooter(canvas, footerPaint, pageNumber, totalPages)

                document.finishPage(page)
            }

            val fileName = buildFileName(report)
            val pdfDir = File(context.cacheDir, "generated_pdfs").apply { mkdirs() }
            val outputFile = File(pdfDir, fileName)

            FileOutputStream(outputFile).use { out ->
                document.writeTo(out)
            }
            document.close()

            // Also copy/save into public Downloads folder
            var savedInDownloads = false
            var targetFolderDisplay = "Downloads / NUBTK_Office_Hours"
            var targetFolderPath = ""

            try {
                val publicDownloads = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "NUBTK_Office_Hours"
                )
                if (!publicDownloads.exists()) {
                    publicDownloads.mkdirs()
                }
                val destFile = File(publicDownloads, fileName)
                outputFile.copyTo(destFile, overwrite = true)
                savedInDownloads = true
                targetFolderPath = publicDownloads.absolutePath
            } catch (e: Exception) {
                // If public storage fails, fallback to app documents
                try {
                    val appDocs = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "NUBTK_Office_Hours").apply { mkdirs() }
                    val destFile = File(appDocs, fileName)
                    outputFile.copyTo(destFile, overwrite = true)
                    targetFolderDisplay = "Documents / NUBTK_Office_Hours"
                    targetFolderPath = appDocs.absolutePath
                } catch (ignored: Exception) {
                    targetFolderDisplay = "App Internal Cache"
                    targetFolderPath = pdfDir.absolutePath
                }
            }

            if (targetFolderPath.isEmpty()) {
                targetFolderPath = pdfDir.absolutePath
            }

            Result.success(
                PdfExportResult(
                    file = outputFile,
                    fileName = fileName,
                    folderPath = targetFolderPath,
                    displayFolderPath = targetFolderDisplay,
                    savedInDownloads = savedInDownloads
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun drawHeader(
        canvas: Canvas,
        report: PeriodAttendanceReport,
        titlePaint: Paint,
        subtitlePaint: Paint,
        textPaint: Paint,
        startY: Float
    ): Float {
        var y = startY + 14f
        canvas.drawText("NORTHERN UNIVERSITY OF BUSINESS & TECHNOLOGY KHULNA", MARGIN, y, titlePaint)
        y += 14f
        canvas.drawText("Faculty & Staff Attendance & Office Hours Report", MARGIN, y, subtitlePaint)
        y += 10f

        // Employee Information Box
        val profile = report.userProfile

        val cardBgPaint = Paint().apply {
            color = Color.rgb(0xFA, 0xFD, 0xF9)
            style = Paint.Style.FILL
        }
        val cardBorderPaint = Paint().apply {
            color = Color.rgb(0xCB, 0xD7, 0xC7)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val cardHeaderBgPaint = Paint().apply {
            color = Color.rgb(0xE8, 0xF5, 0xE9)
            style = Paint.Style.FILL
        }
        val sectionTitlePaint = Paint().apply {
            color = Color.rgb(0x2E, 0x7D, 0x32)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            color = Color.rgb(0x61, 0x61, 0x61)
            textSize = 8.5f
            isAntiAlias = true
        }
        val valBoldPaint = Paint().apply {
            color = Color.rgb(0x1B, 0x1B, 0x1B)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val valRegularPaint = Paint().apply {
            color = Color.rgb(0x21, 0x21, 0x21)
            textSize = 8.8f
            isAntiAlias = true
        }

        val cardHeight = 64f
        val cardRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + cardHeight)
        canvas.drawRoundRect(cardRect, 5f, 5f, cardBgPaint)
        canvas.drawRoundRect(cardRect, 5f, 5f, cardBorderPaint)

        // Title strip inside employee card
        val stripRect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 16f)
        canvas.drawRoundRect(stripRect, 5f, 5f, cardHeaderBgPaint)
        canvas.drawText("EMPLOYEE & PERIOD PROFILE", MARGIN + 10f, y + 11.5f, sectionTitlePaint)

        val col1X = MARGIN + 10f
        val col2X = PAGE_WIDTH / 2f + 10f

        val row1Y = y + 29f
        val row2Y = y + 43f
        val row3Y = y + 57f

        // Row 1: Name and Employee ID
        val nameText = profile?.name?.takeIf { it.isNotBlank() } ?: "Not Set"
        val empIdText = profile?.employeeId?.takeIf { it.isNotBlank() } ?: "Not Set"
        canvas.drawText("Name: ", col1X, row1Y, labelPaint)
        val nameLabelWidth = labelPaint.measureText("Name: ")
        canvas.drawText(nameText, col1X + nameLabelWidth, row1Y, valBoldPaint)

        canvas.drawText("Employee ID: ", col2X, row1Y, labelPaint)
        val empIdLabelWidth = labelPaint.measureText("Employee ID: ")
        canvas.drawText(empIdText, col2X + empIdLabelWidth, row1Y, valBoldPaint)

        // Row 2: Designation and Department
        val desigText = profile?.designation?.takeIf { it.isNotBlank() } ?: "Faculty / Staff"
        val deptText = profile?.department?.takeIf { it.isNotBlank() } ?: "NUBTK"
        canvas.drawText("Designation: ", col1X, row2Y, labelPaint)
        val desigLabelWidth = labelPaint.measureText("Designation: ")
        canvas.drawText(desigText, col1X + desigLabelWidth, row2Y, valRegularPaint)

        canvas.drawText("Department: ", col2X, row2Y, labelPaint)
        val deptLabelWidth = labelPaint.measureText("Department: ")
        canvas.drawText(deptText, col2X + deptLabelWidth, row2Y, valRegularPaint)

        // Row 3: Period & Report Scope
        val periodText = "${DateTimeUtils.formatToDisplayDate(report.period.startDate)} — ${DateTimeUtils.formatToDisplayDate(report.period.endDate)}"
        canvas.drawText("Period: ", col1X, row3Y, labelPaint)
        val periodLabelWidth = labelPaint.measureText("Period: ")
        canvas.drawText(periodText, col1X + periodLabelWidth, row3Y, valRegularPaint)

        val scopeText = "${report.rangeTitle} (Gen: ${LocalDateTime.now().format(DateTimeUtils.SHORT_DATE_FORMATTER)})"
        canvas.drawText("Scope: ", col2X, row3Y, labelPaint)
        val scopeLabelWidth = labelPaint.measureText("Scope: ")
        canvas.drawText(scopeText, col2X + scopeLabelWidth, row3Y, valRegularPaint)

        y += cardHeight + 8f

        val linePaint = Paint().apply {
            color = Color.rgb(0x7C, 0xB3, 0x42)
            strokeWidth = 1.5f
        }
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 8f
        return y
    }

    private fun drawSummary(canvas: Canvas, summary: PeriodSummary, startY: Float): Float {
        val boxPaint = Paint().apply {
            color = Color.rgb(0xF1, 0xF8, 0xE9) // Soft Lime surface
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(0xC4, 0xCD, 0xC2)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val labelPaint = Paint().apply {
            color = Color.rgb(0x5F, 0x66, 0x5F)
            textSize = 8.5f
            isAntiAlias = true
        }
        val valPaint = Paint().apply {
            color = Color.rgb(0x1B, 0x1B, 0x1B)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val boxRect = RectF(MARGIN, startY, PAGE_WIDTH - MARGIN, startY + 68f)
        canvas.drawRoundRect(boxRect, 6f, 6f, boxPaint)
        canvas.drawRoundRect(boxRect, 6f, 6f, borderPaint)

        // Column 1: Days breakdown
        val col1X = MARGIN + 12f
        var y1 = startY + 16f
        canvas.drawText("Calendar Days: ${summary.calendarDays}", col1X, y1, valPaint)
        y1 += 13f
        canvas.drawText("Working Days: ${summary.workingDays} (Completed: ${summary.workingDaysCompleted})", col1X, y1, valPaint)
        y1 += 13f
        canvas.drawText("Weekends: ${summary.weekendDays} | Holidays: ${summary.holidayDays}", col1X, y1, labelPaint)
        y1 += 13f
        canvas.drawText("Leaves: ${summary.medicalLeaveDays} Medical, ${summary.casualLeaveDays} Casual", col1X, y1, labelPaint)

        // Column 2: Hours & Delay
        val col2X = PAGE_WIDTH / 2f + 16f
        var y2 = startY + 16f
        canvas.drawText("Expected Hours: ${summary.expectedFormatted}", col2X, y2, valPaint)
        y2 += 13f
        canvas.drawText("Actual Worked Hours: ${summary.actualFormatted}", col2X, y2, valPaint)
        y2 += 15f

        val isExtra = summary.actualMinutes > summary.expectedMinutes
        val label = if (isExtra) "Extra" else if (summary.actualMinutes == summary.expectedMinutes) "On Time" else "Delay"
        val delayColor = when (summary.delayColorState) {
            DelayColorState.RED -> Color.rgb(0xD3, 0x2F, 0x2F) // RED for delay
            DelayColorState.GREEN -> Color.rgb(0x2E, 0x7D, 0x32) // GREEN for extra
            DelayColorState.AMBER -> Color.rgb(0xF5, 0x7F, 0x17) // AMBER for on time
        }
        val delayPaint = Paint().apply {
            color = delayColor
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val cleanValue = summary.delaySignedFormatted.replace("+", "").replace("-", "")
        canvas.drawText("$label: $cleanValue", col2X, y2, delayPaint)

        return startY + 76f
    }

    private fun drawTable(
        canvas: Canvas,
        rows: List<DayAttendanceInfo>,
        startY: Float,
        boldPaint: Paint,
        regularPaint: Paint
    ): Float {
        var y = startY

        // Table Header Background
        val thPaint = Paint().apply {
            color = Color.rgb(0x55, 0x8B, 0x2F)
            style = Paint.Style.FILL
        }
        val thTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val rowHeight = 18f
        val tableWidth = PAGE_WIDTH - (MARGIN * 2)

        // Column coordinates
        val xDate = MARGIN + 6f
        val xStatus = MARGIN + 110f
        val xPunchIn = MARGIN + 260f
        val xPunchOut = MARGIN + 340f
        val xDuration = MARGIN + 430f

        // Draw header row
        canvas.drawRect(MARGIN, y, MARGIN + tableWidth, y + rowHeight, thPaint)
        canvas.drawText("Date", xDate, y + 12f, thTextPaint)
        canvas.drawText("Status", xStatus, y + 12f, thTextPaint)
        canvas.drawText("Punch In", xPunchIn, y + 12f, thTextPaint)
        canvas.drawText("Punch Out", xPunchOut, y + 12f, thTextPaint)
        canvas.drawText("Duration", xDuration, y + 12f, thTextPaint)
        y += rowHeight

        val zebraPaint = Paint().apply {
            color = Color.rgb(0xF8, 0xFA, 0xF7)
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(0xDC, 0xE4, 0xDA)
            style = Paint.Style.STROKE
            strokeWidth = 0.5f
        }

        for ((index, row) in rows.withIndex()) {
            if (index % 2 == 1) {
                canvas.drawRect(MARGIN, y, MARGIN + tableWidth, y + rowHeight, zebraPaint)
            }
            canvas.drawRect(MARGIN, y, MARGIN + tableWidth, y + rowHeight, borderPaint)

            // Date + day of week
            val dateLabel = "${row.date} (${row.dayOfWeekName})"
            canvas.drawText(dateLabel, xDate, y + 12f, boldPaint)

            // Status label
            val statusColor = when (row.displayStatus) {
                com.example.domain.AttendanceStatus.COMPLETED,
                com.example.domain.AttendanceStatus.WORKED_ON_WEEKEND,
                com.example.domain.AttendanceStatus.WORKED_ON_HOLIDAY,
                com.example.domain.AttendanceStatus.WORKED_DURING_LEAVE -> Color.rgb(0x2E, 0x7D, 0x32)
                com.example.domain.AttendanceStatus.IN_PROGRESS -> Color.rgb(0x7C, 0xB3, 0x42)
                com.example.domain.AttendanceStatus.WEEKEND -> Color.rgb(0x60, 0x7D, 0x8B)
                com.example.domain.AttendanceStatus.HOLIDAY -> Color.rgb(0x55, 0x8B, 0x2F)
                com.example.domain.AttendanceStatus.MEDICAL_LEAVE -> Color.rgb(0xEF, 0x6C, 0x00)
                com.example.domain.AttendanceStatus.CASUAL_LEAVE -> Color.rgb(0x00, 0x89, 0x7B)
                else -> Color.rgb(0x1B, 0x1B, 0x1B)
            }
            val statusPaint = Paint(regularPaint).apply {
                color = statusColor
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 8.5f
            }

            val statusText = if (!row.statusDetail.isNullOrBlank() && row.displayStatus != com.example.domain.AttendanceStatus.COMPLETED) {
                "${row.displayStatus.displayName} (${row.statusDetail})"
            } else {
                row.displayStatus.displayName
            }
            // Truncate if too long
            val truncatedStatus = if (statusText.length > 25) statusText.take(24) + "…" else statusText
            canvas.drawText(truncatedStatus, xStatus, y + 12f, statusPaint)

            // Punch In / Punch Out
            canvas.drawText(row.punchInFormatted, xPunchIn, y + 12f, regularPaint)
            canvas.drawText(row.punchOutFormatted, xPunchOut, y + 12f, regularPaint)

            // Duration
            val durPaint = if (row.isCompleted) boldPaint else regularPaint
            canvas.drawText(row.durationFormatted, xDuration, y + 12f, durPaint)

            y += rowHeight
        }

        return y + 10f
    }

    private fun drawFooter(canvas: Canvas, footerPaint: Paint, pageNumber: Int, totalPages: Int) {
        val y = PAGE_HEIGHT - 32f
        val linePaint = Paint().apply {
            color = Color.rgb(0xDC, 0xE4, 0xDA)
            strokeWidth = 0.8f
        }
        canvas.drawLine(MARGIN, y - 8f, PAGE_WIDTH - MARGIN, y - 8f, linePaint)

        canvas.drawText(
            "Developed by Md. Abdul Halim Khan | Lecturer, Dept. of CSE, Northern University of Business & Technology, Khulna",
            MARGIN,
            y + 4f,
            footerPaint
        )
        canvas.drawText(
            "© 2026 Md. Abdul Halim Khan  •  Page $pageNumber of $totalPages",
            MARGIN,
            y + 16f,
            footerPaint
        )
    }

    private fun buildFileName(report: PeriodAttendanceReport): String {
        return if (report.startDate == report.period.startDate && report.endDate == report.period.endDate) {
            "NUBTK_Office_Hours_${report.startDate}_to_${report.endDate}.pdf"
        } else {
            "NUBTK_Office_Hours_Selected_${report.startDate}_to_${report.endDate}.pdf"
        }
    }
}
