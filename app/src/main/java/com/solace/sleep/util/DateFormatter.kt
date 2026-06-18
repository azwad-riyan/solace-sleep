package com.solace.sleep.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateFormatter {

    fun monthYearLabel(yearMonth: YearMonth): String {
        return yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    }

    fun dayOfWeekLabel(dayOfWeek: DayOfWeek): String {
        return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    fun monthName(month: Month): String {
        return month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    fun relativeDateLabel(date: LocalDate): String {
        val today = LocalDate.now()
        return when {
            date == today -> "Today"
            date == today.minusDays(1) -> "Yesterday"
            date == today.plusDays(1) -> "Tomorrow"
            else -> date.formatDateWeekday()
        }
    }

    fun calendarWeekDayHeaders(): List<String> {
        val firstDayOfWeek = DayOfWeek.SUNDAY
        return (0..6).map { offset ->
            val day = DayOfWeek.of((firstDayOfWeek.value - 1 + offset) % 7 + 1)
            day.getDisplayName(TextStyle.NARROW, Locale.getDefault())
        }
    }

    fun LocalDate.formatDateWeekday(): String {
        return DateTimeFormatter.ofPattern("EEE, MMM d").format(this)
    }
}
