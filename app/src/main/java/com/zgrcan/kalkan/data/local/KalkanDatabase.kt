package com.zgrcan.kalkan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zgrcan.kalkan.data.local.dao.EmergencyContactDao
import com.zgrcan.kalkan.data.local.dao.OfflineInfoDao
import com.zgrcan.kalkan.data.local.entity.EmergencyContactEntity
import com.zgrcan.kalkan.data.local.entity.OfflineInfoEntity

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
