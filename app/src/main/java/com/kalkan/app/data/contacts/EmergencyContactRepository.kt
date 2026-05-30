package com.kalkan.app.data.contacts

import com.kalkan.app.model.EmergencyContact
import kotlinx.coroutines.flow.Flow

interface EmergencyContactRepository {
    fun observeContacts(uid: String): Flow<List<EmergencyContact>>

    suspend fun addContact(uid: String, contact: EmergencyContact): Result<EmergencyContact>

    suspend fun deleteContact(uid: String, contactId: String): Result<Unit>
}
