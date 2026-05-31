package com.zgrcan.kalkan.domain.model

data class EmergencyContact(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val relation: String = "",
    val isPrimary: Boolean = false,
)
