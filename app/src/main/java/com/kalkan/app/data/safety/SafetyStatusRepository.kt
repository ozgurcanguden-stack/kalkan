package com.kalkan.app.data.safety

import com.kalkan.app.model.SafetyStatus
import com.kalkan.app.model.SafetyStatusType

interface SafetyStatusRepository {
    suspend fun createSafetyStatus(
        uid: String,
        displayName: String,
        email: String?,
        statusType: SafetyStatusType,
    ): Result<SafetyStatus>
}
