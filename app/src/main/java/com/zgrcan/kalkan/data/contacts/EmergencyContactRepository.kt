package com.zgrcan.kalkan.data.contacts

import com.zgrcan.kalkan.model.EmergencyContact
import kotlinx.coroutines.flow.Flow

interface EmergencyContactRepository {
    fun observeContacts(uid: String): Flow<List<EmergencyContact>>

    suspend fun addContact(uid: String, contact: EmergencyContact): Result<EmergencyContact>

    suspend fun deleteContact(uid: String, contactId: String): Result<Unit>
}
