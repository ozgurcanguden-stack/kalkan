package com.kalkan.app.model

data class SafetyStatus(
    val id: String = "",
    val uid: String = "",
    val displayName: String = "",
    val email: String? = null,
    val statusType: SafetyStatusType = SafetyStatusType.SAFE,
    val message: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long = 0L,
)
