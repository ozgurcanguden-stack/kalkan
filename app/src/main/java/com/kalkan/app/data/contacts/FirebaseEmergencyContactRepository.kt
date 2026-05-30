package com.kalkan.app.data.contacts

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.kalkan.app.model.EmergencyContact
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseEmergencyContactRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : EmergencyContactRepository {
    override fun observeContacts(uid: String): Flow<List<EmergencyContact>> = callbackFlow {
        require(uid.isNotBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = contactsCollection(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val contacts = snapshot?.documents
                ?.mapNotNull { it.toEmergencyContact() }
                ?.sortedWith(
                    compareByDescending<EmergencyContact> { it.isPrimary }
                        .thenBy { it.name.lowercase() },
                )
                ?: emptyList()
            trySend(contacts)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun addContact(uid: String, contact: EmergencyContact): Result<EmergencyContact> = runCatching {
        val authUid = requireNotNull(auth.currentUser?.uid) { "Kullanıcı oturumu bulunamadı." }
        require(uid == authUid) { "Geçersiz kullanıcı kimliği." }

        val collection = contactsCollection(uid)
        val now = System.currentTimeMillis()
        val document = collection.document()

        if (contact.isPrimary) {
            clearPrimaryFlags(uid)
        }

        val payload = contact.copy(
            id = document.id,
            createdAt = now,
            updatedAt = now,
        )
        document.set(payload.toFirestoreMap()).await()
        payload
    }

    override suspend fun deleteContact(uid: String, contactId: String): Result<Unit> = runCatching {
        val authUid = requireNotNull(auth.currentUser?.uid) { "Kullanıcı oturumu bulunamadı." }
        require(uid == authUid) { "Geçersiz kullanıcı kimliği." }
        require(contactId.isNotBlank()) { "Kişi kimliği boş olamaz." }
        contactsCollection(uid).document(contactId).delete().await()
    }

    private suspend fun clearPrimaryFlags(uid: String) {
        val snapshot = contactsCollection(uid)
            .whereEqualTo("isPrimary", true)
            .get()
            .await()

        snapshot.documents.forEach { document ->
            document.reference.update(
                mapOf(
                    "isPrimary" to false,
                    "updatedAt" to System.currentTimeMillis(),
                ),
            ).await()
        }
    }

    private fun contactsCollection(uid: String) =
        firestore.collection("users").document(uid).collection(COLLECTION)

    private fun EmergencyContact.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "phone" to phone,
        "relation" to relation,
        "isPrimary" to isPrimary,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
    )

    private fun DocumentSnapshot.toEmergencyContact(): EmergencyContact? {
        if (!exists()) return null
        return EmergencyContact(
            id = getString("id").orEmpty().ifBlank { id },
            name = getString("name").orEmpty(),
            phone = getString("phone").orEmpty(),
            relation = getString("relation").orEmpty(),
            isPrimary = getBoolean("isPrimary") == true,
            createdAt = getLong("createdAt") ?: 0L,
            updatedAt = getLong("updatedAt") ?: 0L,
        )
    }

    companion object {
        private const val COLLECTION = "emergency_contacts"
    }
}
