package com.zgrcan.kalkan.model

data class FamilyMember(
    val uid: String = "",
    val displayName: String = "",
    val email: String? = null,
    val photoUrl: String? = null,
    val phone: String? = null,
    val role: FamilyRole = FamilyRole.MEMBER,
    val joinedAt: Long = 0L,
    val lastStatusType: String? = null,
    val lastStatusMessage: String? = null,
    val lastStatusLatitude: Double? = null,
    val lastStatusLongitude: Double? = null,
    val lastStatusAt: Long? = null,
) {
    val statusPriority: Int
        get() = when (SafetyStatusType.from(lastStatusType)) {
            SafetyStatusType.SOS -> 0
            SafetyStatusType.NEED_HELP -> 1
            SafetyStatusType.SHARE_LOCATION -> 2
            SafetyStatusType.SAFE -> 3
            null -> 4
        }
}
