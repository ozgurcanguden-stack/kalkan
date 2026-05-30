package com.kalkan.app.model

data class FamilyGroup(
    val id: String = "",
    val ownerUid: String = "",
    val name: String = "",
    val inviteCode: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
