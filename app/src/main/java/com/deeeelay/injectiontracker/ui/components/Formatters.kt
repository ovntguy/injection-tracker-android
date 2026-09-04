package com.deeeelay.injectiontracker.ui.components

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

object Formatters {
    private val dateMedium: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    private val timeShort: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    private val dateTime: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)

    fun dateTime(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        dateTime.withZone(zone).format(instant)

    fun date(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        dateMedium.withZone(zone).format(instant)

    fun time(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        timeShort.withZone(zone).format(instant)

    fun heroDateTime(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String {
        val zdt = instant.atZone(zone)
        return "${dateMedium.format(zdt)} · ${timeShort.format(zdt)}"
    }

    fun monthTitle(yearMonth: java.time.YearMonth, locale: Locale = Locale.getDefault()): String {
        val month = yearMonth.month.getDisplayName(TextStyle.FULL, locale)
        return "$month ${yearMonth.year}"
    }
}
