package com.kalkan.app.data.settings

import com.kalkan.app.model.AppUser
import com.kalkan.app.model.BackupFrequency

interface SettingsRepository {
    suspend fun manualBackup(user: AppUser): Result<Unit>
    suspend fun clearUserData(user: AppUser): Result<Unit>
    suspend fun deleteAccount(user: AppUser): Result<Unit>
    suspend fun getBackupFrequency(uid: String): Result<BackupFrequency>
    suspend fun setBackupFrequency(uid: String, frequency: BackupFrequency): Result<Unit>
    suspend fun getBackupTimestamps(uid: String): Result<Pair<Long?, Long?>>
}
