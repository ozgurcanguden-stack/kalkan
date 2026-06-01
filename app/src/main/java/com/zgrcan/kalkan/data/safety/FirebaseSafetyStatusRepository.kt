package com.zgrcan.kalkan.data.safety

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.zgrcan.kalkan.model.SafetyStatus
import com.zgrcan.kalkan.model.SafetyStatusType
import com.zgrcan.kalkan.model.UserLocation
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
        location: UserLocation?,
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
            latitude = location?.latitude,
            longitude = location?.longitude,
            locationAccuracy = location?.accuracy,
            locationProvider = location?.provider,
            createdAt = createdAt,
        )
        document.set(status.toFirestoreMap()).await()

        val userCooldownUpdate = when (statusType) {
            SafetyStatusType.SOS -> mapOf(
                "lastSosAt" to createdAt,
            )
            SafetyStatusType.NEED_HELP -> mapOf(
                "lastHelpRequestAt" to createdAt,
            )
            SafetyStatusType.SHARE_LOCATION -> mapOf(
                "lastLocationShareAt" to createdAt,
            )
            SafetyStatusType.SAFE -> mapOf(
                "lastSosAt" to FieldValue.delete(),
                "lastHelpRequestAt" to FieldValue.delete(),
                "lastLocationShareAt" to FieldValue.delete(),
            )
        }
        firestore.collection("users").document(uid).update(userCooldownUpdate).await()

        // 2. Eğer users/{uid}.familyGroupId varsa, family_groups/{groupId}/members/{uid} dökümanı güncellensin.
        try {
            val userSnapshot = firestore.collection("users").document(uid).get().await()
            val familyGroupId = userSnapshot.getString("familyGroupId")
            if (!familyGroupId.isNullOrBlank()) {
                val memberRef = firestore.collection("family_groups").document(familyGroupId)
                    .collection("members").document(uid)
                memberRef.update(
                    mapOf(
                        "lastStatusType" to statusType.value,
                        "lastStatusMessage" to statusType.defaultMessage,
                        "lastStatusLatitude" to location?.latitude,
                        "lastStatusLongitude" to location?.longitude,
                        "lastStatusAt" to createdAt
                    )
                ).await()
            }
        } catch (_: Exception) {
            // Aile grubu güncellenemese de ana durum kaydetme akışı bozulmasın.
        }

        status
    }

    override suspend fun getLatestSafetyStatus(uid: String): Result<SafetyStatus?> = runCatching {
        require(uid.isNotBlank()) { "Kullanıcı kimliği boş olamaz." }

        val snapshot = safetyStatusCollection
            .whereEqualTo("uid", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()

        snapshot.documents.firstOrNull()?.toSafetyStatus()
    }

    override suspend fun getStatusCooldownSnapshot(uid: String): Result<StatusCooldownSnapshot> = runCatching {
        require(uid.isNotBlank()) { "Kullanıcı kimliği boş olamaz." }
        val snapshot = firestore.collection("users").document(uid).get().await()
        StatusCooldownSnapshot(
            lastSosAt = snapshot.getLong("lastSosAt"),
            lastHelpRequestAt = snapshot.getLong("lastHelpRequestAt"),
            lastLocationShareAt = snapshot.getLong("lastLocationShareAt"),
        )
    }

    private val safetyStatusCollection
        get() = firestore.collection(COLLECTION)

    private fun DocumentSnapshot.toSafetyStatus(): SafetyStatus? {
        if (!exists()) return null
        return SafetyStatus(
            id = getString("id").orEmpty().ifBlank { id },
            uid = getString("uid").orEmpty(),
            displayName = getString("displayName").orEmpty(),
            email = getString("email"),
            statusType = SafetyStatusType.from(getString("statusType")) ?: SafetyStatusType.SAFE,
            message = getString("message").orEmpty(),
            latitude = getDouble("latitude"),
            longitude = getDouble("longitude"),
            locationAccuracy = getDouble("locationAccuracy")?.toFloat(),
            locationProvider = getString("locationProvider"),
            createdAt = getLong("createdAt") ?: 0L,
        )
    }

    private fun SafetyStatus.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "uid" to uid,
        "displayName" to displayName,
        "email" to email,
        "statusType" to statusType.value,
        "message" to message,
        "latitude" to latitude,
        "longitude" to longitude,
        "locationAccuracy" to locationAccuracy,
        "locationProvider" to locationProvider,
        "createdAt" to createdAt,
    )

    companion object {
        private const val COLLECTION = "safety_status"
    }
}
