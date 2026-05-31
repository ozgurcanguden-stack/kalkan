package com.zgrcan.kalkan.model

data class EmergencyContact(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val relation: String = "",
    val isPrimary: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
