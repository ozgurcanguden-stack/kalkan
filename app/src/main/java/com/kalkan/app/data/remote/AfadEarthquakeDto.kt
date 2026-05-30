package com.kalkan.app.data.remote

import com.google.gson.annotations.SerializedName
import com.kalkan.app.domain.model.Earthquake
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class AfadEarthquakeDto(
    @SerializedName(value = "eventID", alternate = ["event_id", "eventId"])
    val eventId: String?,
    val location: String?,
    val latitude: String?,
    val longitude: String?,
    val depth: String?,
    val type: String?,
    val magnitude: String?,
    val date: String?,
) {
    fun toDomain(): Earthquake =
        Earthquake(
            id = eventId.orEmpty(),
            title = type.orEmpty(),
            location = location.orEmpty(),
            magnitude = magnitude.toDoubleValue(),
            depth = depth.toDoubleValue(),
            latitude = latitude.toDoubleValue(),
            longitude = longitude.toDoubleValue(),
            dateTime = date.toEpochMillis(),
            source = "AFAD",
        )
}

private fun String?.toDoubleValue(): Double =
    this?.replace(',', '.')?.toDoubleOrNull() ?: 0.0

private val AFAD_DATE_FORMATTERS = listOf(
    DateTimeFormatter.ISO_LOCAL_DATE_TIME,
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
)

private val ISTANBUL_ZONE: ZoneId = ZoneId.of("Europe/Istanbul")

private fun String?.toEpochMillis(): Long {
    if (this.isNullOrBlank()) return 0L

    runCatching {
        return OffsetDateTime.parse(this).toInstant().toEpochMilli()
    }

    val normalized = this.trim().replace(' ', 'T').substringBefore('+').substringBefore('Z')
    for (formatter in AFAD_DATE_FORMATTERS) {
        try {
            return LocalDateTime
                .parse(normalized, formatter)
                .atZone(ISTANBUL_ZONE)
                .toInstant()
                .toEpochMilli()
        } catch (_: DateTimeParseException) {
            // try next format
        }
    }
    return 0L
}
