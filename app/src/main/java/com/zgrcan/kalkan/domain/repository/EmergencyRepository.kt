package com.zgrcan.kalkan.domain.repository

import com.zgrcan.kalkan.domain.model.EmergencyStatus

interface EmergencyRepository {
    suspend fun markSafe(): Result<EmergencyStatus>
    suspend fun requestHelp(message: String? = null): Result<EmergencyStatus>
    suspend fun sendSos(): Result<EmergencyStatus>
    suspend fun shareLocation(): Result<EmergencyStatus>
}
