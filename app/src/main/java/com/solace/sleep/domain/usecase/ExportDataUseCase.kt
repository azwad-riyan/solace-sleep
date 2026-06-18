package com.solace.sleep.domain.usecase

import android.content.Context
import com.solace.sleep.data.repository.SleepSessionRepository
import com.solace.sleep.domain.model.SleepSession
import com.solace.sleep.util.formatDateFull
import com.solace.sleep.util.formatTime12h
import com.solace.sleep.util.minutesToHoursMinutesString
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class ExportDataUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionRepository: SleepSessionRepository
) {
    enum class ExportFormat { CSV, PDF }

    suspend fun export(
        profileId: String,
        startDate: LocalDate,
        endDate: LocalDate,
        format: ExportFormat
    ): File {
        val zone = ZoneId.systemDefault()
        val sessions = sessionRepository.getSessionsForDateRange(profileId, startDate, endDate, zone)

        return when (format) {
            ExportFormat.CSV -> exportCsv(sessions)
            ExportFormat.PDF -> exportPdf(sessions, startDate, endDate)
        }
    }

    private fun exportCsv(sessions: List<SleepSession>): File {
        val file = File(context.cacheDir, "solace_export_${System.currentTimeMillis()}.csv")
        val sb = StringBuilder()
        sb.appendLine("Date,Bedtime,Wake Time,Duration,Type,Quality,Tags,Notes,Source,Confidence")

        sessions.forEach { s ->
            sb.appendLine(
                "${s.sleepOnset.formatDateFull()}," +
                        "${s.sleepOnset.formatTime12h()}," +
                        "${s.wakeTime.formatTime12h()}," +
                        "${minutesToHoursMinutesString(s.durationMinutes)}," +
                        "${s.sessionType.name}," +
                        "${s.qualityScore ?: ""}," +
                        "\"${s.tags.joinToString(";")}\"," +
                        "\"${s.notes?.replace("\"", "'") ?: ""}\"," +
                        "${s.source.name}," +
                        "${s.confidenceScore ?: ""}"
            )
        }
        file.writeText(sb.toString())
        return file
    }

    private fun exportPdf(
        sessions: List<SleepSession>,
        startDate: LocalDate,
        endDate: LocalDate
    ): File {
        PDFBoxResourceLoader.init(context)
        val file = File(context.cacheDir, "solace_report_${System.currentTimeMillis()}.pdf")

        PDDocument().use { doc ->
            val page = PDPage(PDRectangle.A4)
            doc.addPage(page)

            PDPageContentStream(doc, page).use { stream ->
                stream.beginText()
                stream.setFont(PDType1Font.HELVETICA_BOLD, 20f)
                stream.newLineAtOffset(50f, 780f)
                stream.showText("Solace Sleep Report")

                stream.setFont(PDType1Font.HELVETICA, 12f)
                stream.newLineAtOffset(0f, -30f)
                stream.showText("Period: ${startDate} to ${endDate}")
                stream.newLineAtOffset(0f, -20f)
                stream.showText("Total Sessions: ${sessions.size}")

                if (sessions.isNotEmpty()) {
                    val avgMinutes = sessions.map { it.durationMinutes }.average().toInt()
                    stream.newLineAtOffset(0f, -20f)
                    stream.showText("Average Duration: ${minutesToHoursMinutesString(avgMinutes)}")
                }

                stream.newLineAtOffset(0f, -40f)
                stream.setFont(PDType1Font.HELVETICA_BOLD, 11f)
                stream.showText("Date             Bedtime    Wake       Duration   Quality")

                stream.setFont(PDType1Font.HELVETICA, 10f)
                var yOffset = -20f
                sessions.take(40).forEach { s ->
                    stream.newLineAtOffset(0f, yOffset)
                    val line = "${s.sleepOnset.formatDateFull().take(16).padEnd(17)}" +
                            "${s.sleepOnset.formatTime12h().padEnd(11)}" +
                            "${s.wakeTime.formatTime12h().padEnd(11)}" +
                            "${minutesToHoursMinutesString(s.durationMinutes).padEnd(11)}" +
                            "${s.qualityScore?.toString() ?: "-"}"
                    stream.showText(line)
                    yOffset = -15f
                }
                stream.endText()
            }
            doc.save(file)
        }
        return file
    }
}
