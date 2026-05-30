package com.kalkan.app.data.remote

import com.google.gson.annotations.SerializedName
import com.kalkan.app.domain.model.Earthquake
import java.time.LocalDateTime
import java.time.ZoneId

data class AfadEarthquakeDto(
    @SerializedName("eventID")
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

private fun String?.toEpochMillis(): Long {
    if (this.isNullOrBlank()) return 0L
    return runCatching {
        LocalDateTime
            .parse(this)
            .atZone(ZoneId.of("Europe/Istanbul"))
            .toInstant()
            .toEpochMilli()
    }.getOrDefault(0L)
}
