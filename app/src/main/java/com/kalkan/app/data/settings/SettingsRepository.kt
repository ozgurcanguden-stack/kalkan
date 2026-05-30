package com.kalkan.app.data.settings

import com.kalkan.app.model.AppUser

interface SettingsRepository {
    suspend fun manualBackup(user: AppUser, deviceName: String, appVersion: String): Result<Unit>
    suspend fun clearUserData(user: AppUser): Result<Unit>
    suspend fun deleteAccount(user: AppUser): Result<Unit>
}
