package com.kalkan.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kalkan.app.data.local.entity.EmergencyContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyContactDao {
    @Query("SELECT * FROM emergency_contacts WHERE userId = :userId ORDER BY isPrimary DESC, name ASC")
    fun observeContacts(userId: String): Flow<List<EmergencyContactEntity>>

    @Upsert
    suspend fun upsert(contact: EmergencyContactEntity)

    @Query("DELETE FROM emergency_contacts WHERE id = :id")
    suspend fun deleteById(id: String)
}
