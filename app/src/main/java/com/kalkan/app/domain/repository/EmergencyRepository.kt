package com.kalkan.app.domain.repository

import com.kalkan.app.domain.model.EmergencyStatus

interface EmergencyRepository {
    suspend fun markSafe(): Result<EmergencyStatus>
    suspend fun requestHelp(message: String? = null): Result<EmergencyStatus>
    suspend fun sendSos(): Result<EmergencyStatus>
    suspend fun shareLocation(): Result<EmergencyStatus>
}
