package com.kalkan.app.feature.earthquakes

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val TR_LOCALE = Locale("tr", "TR")
private val ISTANBUL_TIME_ZONE: TimeZone = TimeZone.getTimeZone("Europe/Istanbul")

fun Long.formatEarthquakeDate(): String {
    if (this == 0L) return "Tarih yok"
    return SimpleDateFormat("dd MMM HH:mm", TR_LOCALE).apply {
        timeZone = ISTANBUL_TIME_ZONE
    }.format(Date(this))
}

fun Long.formatLastUpdatedAt(): String =
    SimpleDateFormat("d MMMM HH:mm", TR_LOCALE).apply {
        timeZone = ISTANBUL_TIME_ZONE
    }.format(Date(this))
