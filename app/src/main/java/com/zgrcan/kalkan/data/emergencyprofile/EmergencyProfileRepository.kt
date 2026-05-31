package com.zgrcan.kalkan.data.emergencyprofile

import com.zgrcan.kalkan.model.EmergencyProfile
import kotlinx.coroutines.flow.Flow

interface EmergencyProfileRepository {
    fun observeProfile(uid: String): Flow<EmergencyProfile?>
    suspend fun saveProfile(uid: String, profile: EmergencyProfile): Result<EmergencyProfile>
    suspend fun deleteProfile(uid: String): Result<Unit>
    fun getCachedProfile(uid: String): EmergencyProfile?
}
