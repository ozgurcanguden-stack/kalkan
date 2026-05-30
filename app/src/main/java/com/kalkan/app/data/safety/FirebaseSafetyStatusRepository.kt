package com.kalkan.app.data.safety

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kalkan.app.model.SafetyStatus
import com.kalkan.app.model.SafetyStatusType
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseSafetyStatusRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : SafetyStatusRepository {
    override suspend fun createSafetyStatus(
        uid: String,
        displayName: String,
        email: String?,
        statusType: SafetyStatusType,
    ): Result<SafetyStatus> = runCatching {
        val authUid = auth.currentUser?.uid
        require(!authUid.isNullOrBlank()) { "Kullanıcı oturumu bulunamadı." }
        require(uid.isNotBlank()) { "Kullanıcı kimliği boş olamaz." }
        require(uid == authUid) { "Geçersiz kullanıcı kimliği." }

        val document = safetyStatusCollection.document()
        val createdAt = System.currentTimeMillis()
        val status = SafetyStatus(
            id = document.id,
            uid = uid,
            displayName = displayName.ifBlank { "Kullanıcı" },
            email = email,
            statusType = statusType,
            message = statusType.defaultMessage,
            latitude = null,
            longitude = null,
            createdAt = createdAt,
        )
        document.set(status.toFirestoreMap()).await()
        status
    }

    private val safetyStatusCollection
        get() = firestore.collection(COLLECTION)

    private fun SafetyStatus.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "uid" to uid,
        "displayName" to displayName,
        "email" to email,
        "statusType" to statusType.value,
        "message" to message,
        "latitude" to latitude,
        "longitude" to longitude,
        "createdAt" to createdAt,
    )

    companion object {
        private const val COLLECTION = "safety_status"
    }
}
