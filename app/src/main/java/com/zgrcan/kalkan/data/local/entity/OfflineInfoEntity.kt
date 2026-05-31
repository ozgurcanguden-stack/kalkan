package com.zgrcan.kalkan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zgrcan.kalkan.domain.model.OfflineInfoCategory

@Entity(tableName = "offline_info")
data class OfflineInfoEntity(
    @PrimaryKey val id: String,
    val category: OfflineInfoCategory,
    val title: String,
    val content: String,
    val sortOrder: Int,
)
