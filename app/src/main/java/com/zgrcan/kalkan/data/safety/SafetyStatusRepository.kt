package com.zgrcan.kalkan.data.safety

import com.zgrcan.kalkan.model.SafetyStatus
import com.zgrcan.kalkan.model.SafetyStatusType
import com.zgrcan.kalkan.model.UserLocation

interface SafetyStatusRepository {
    suspend fun createSafetyStatus(
        uid: String,
        displayName: String,
        email: String?,
        statusType: SafetyStatusType,
        location: UserLocation? = null,
    ): Result<SafetyStatus>

    suspend fun getLatestSafetyStatus(uid: String): Result<SafetyStatus?>

    suspend fun getStatusCooldownSnapshot(uid: String): Result<StatusCooldownSnapshot>
}

data class StatusCooldownSnapshot(
    val lastSosAt: Long? = null,
    val lastHelpRequestAt: Long? = null,
    val lastLocationShareAt: Long? = null,
)
