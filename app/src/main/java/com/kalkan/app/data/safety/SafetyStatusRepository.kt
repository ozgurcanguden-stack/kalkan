package com.kalkan.app.data.safety

import com.kalkan.app.model.SafetyStatus
import com.kalkan.app.model.SafetyStatusType
import com.kalkan.app.model.UserLocation

interface SafetyStatusRepository {
    suspend fun createSafetyStatus(
        uid: String,
        displayName: String,
        email: String?,
        statusType: SafetyStatusType,
        location: UserLocation? = null,
    ): Result<SafetyStatus>

    suspend fun getLatestSafetyStatus(uid: String): Result<SafetyStatus?>
}
