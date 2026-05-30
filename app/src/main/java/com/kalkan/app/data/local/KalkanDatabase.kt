package com.kalkan.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kalkan.app.data.local.dao.EmergencyContactDao
import com.kalkan.app.data.local.dao.OfflineInfoDao
import com.kalkan.app.data.local.entity.EmergencyContactEntity
import com.kalkan.app.data.local.entity.OfflineInfoEntity

@Database(
    entities = [
        EmergencyContactEntity::class,
        OfflineInfoEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class KalkanDatabase : RoomDatabase() {
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun offlineInfoDao(): OfflineInfoDao
}
