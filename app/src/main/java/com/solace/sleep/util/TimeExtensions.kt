package com.solace.sleep.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun Instant.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    atZone(zone).toLocalDate()

fun Instant.toLocalTime(zone: ZoneId = ZoneId.systemDefault()): LocalTime =
    atZone(zone).toLocalTime()

fun Instant.toZonedDateTime(zone: ZoneId = ZoneId.systemDefault()): ZonedDateTime =
    atZone(zone)

fun LocalDate.startOfDay(zone: ZoneId = ZoneId.systemDefault()): Instant =
    atStartOfDay(zone).toInstant()

fun LocalDate.endOfDay(zone: ZoneId = ZoneId.systemDefault()): Instant =
    atTime(LocalTime.MAX).atZone(zone).toInstant()

fun Duration.toMinutes(): Long = toMinutes()

fun isWithinDetectionWindow(now: LocalTime, start: LocalTime, end: LocalTime): Boolean {
    return if (start > end) {
        // Spans midnight: e.g. 21:00 -> 10:00
        now >= start || now <= end
    } else {
        now >= start && now <= end
    }
}

fun durationBetween(start: Instant, end: Instant): Duration =
    Duration.between(start, end)

fun durationToHoursMinutes(minutes: Int): Pair<Int, Int> =
    Pair(minutes / 60, minutes % 60)

private val TIME_FORMATTER_12H = DateTimeFormatter.ofPattern("h:mm a")
private val TIME_FORMATTER_24H = DateTimeFormatter.ofPattern("HH:mm")
private val DATE_FORMATTER_SHORT = DateTimeFormatter.ofPattern("MMM d")
private val DATE_FORMATTER_FULL = DateTimeFormatter.ofPattern("MMMM d, yyyy")
private val DATE_FORMATTER_WEEKDAY = DateTimeFormatter.ofPattern("EEE, MMM d")

fun Instant.formatTime12h(zone: ZoneId = ZoneId.systemDefault()): String =
    TIME_FORMATTER_12H.format(atZone(zone))

fun Instant.formatTime24h(zone: ZoneId = ZoneId.systemDefault()): String =
    TIME_FORMATTER_24H.format(atZone(zone))

fun Instant.formatDateShort(zone: ZoneId = ZoneId.systemDefault()): String =
    DATE_FORMATTER_SHORT.format(atZone(zone))

fun Instant.formatDateFull(zone: ZoneId = ZoneId.systemDefault()): String =
    DATE_FORMATTER_FULL.format(atZone(zone))

fun Instant.formatDateWeekday(zone: ZoneId = ZoneId.systemDefault()): String =
    DATE_FORMATTER_WEEKDAY.format(atZone(zone))

fun LocalDate.formatShort(): String = DATE_FORMATTER_SHORT.format(this)
fun LocalDate.formatFull(): String = DATE_FORMATTER_FULL.format(this)
fun LocalDate.formatDateWeekday(): String = DATE_FORMATTER_WEEKDAY.format(this)

fun minutesToHoursMinutesString(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0 && m > 0) "${h}h ${m}m"
    else if (h > 0) "${h}h"
    else "${m}m"
}
