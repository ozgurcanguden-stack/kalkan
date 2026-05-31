package com.zgrcan.kalkan.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.zgrcan.kalkan.data.local.entity.OfflineInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineInfoDao {
    @Query("SELECT * FROM offline_info ORDER BY category ASC, sortOrder ASC")
    fun observeAll(): Flow<List<OfflineInfoEntity>>

    @Upsert
    suspend fun upsertAll(items: List<OfflineInfoEntity>): List<Long>
}
