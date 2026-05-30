package com.kalkan.app.model

data class FamilyMember(
    val uid: String = "",
    val displayName: String = "",
    val email: String? = null,
    val phone: String? = null,
    val role: FamilyRole = FamilyRole.MEMBER,
    val joinedAt: Long = 0L,
    val lastStatusType: String? = null,
    val lastStatusMessage: String? = null,
    val lastStatusLatitude: Double? = null,
    val lastStatusLongitude: Double? = null,
    val lastStatusAt: Long? = null,
)
