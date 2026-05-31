package com.zgrcan.kalkan.data.fcm

interface FcmRepository {
    suspend fun getCurrentToken(): Result<String>
    suspend fun syncTokenForCurrentUser(notificationPermissionGranted: Boolean): Result<Unit>
    suspend fun saveTokenForCurrentUser(token: String, notificationPermissionGranted: Boolean? = null): Result<Unit>
    suspend fun updateNotificationPermissionForCurrentUser(granted: Boolean): Result<Unit>
}
