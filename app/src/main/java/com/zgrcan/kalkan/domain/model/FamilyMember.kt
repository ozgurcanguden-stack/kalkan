package com.zgrcan.kalkan.domain.model

data class FamilyMember(
    val id: String = "",
    val ownerUserId: String = "",
    val memberUserId: String? = null,
    val name: String = "",
    val phoneNumber: String = "",
    val relation: String = "",
    val lastKnownLatitude: Double? = null,
    val lastKnownLongitude: Double? = null,
    val lastStatus: EmergencyType? = null,
    val updatedAt: Long? = null,
)
