package com.kalkan.app.domain.model

data class EmergencyStatus(
    val id: String = "",
    val userId: String = "",
    val type: EmergencyType = EmergencyType.SAFE,
    val message: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long = 0L,
)

enum class EmergencyType {
    SAFE,
    NEED_HELP,
    SOS,
    LOCATION_SHARED,
}
