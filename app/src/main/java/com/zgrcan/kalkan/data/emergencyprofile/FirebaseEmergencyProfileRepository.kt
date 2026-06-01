package com.zgrcan.kalkan.data.emergencyprofile

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.zgrcan.kalkan.data.firestore.getNumberAsLong
import com.zgrcan.kalkan.model.EmergencyBloodTypes
import com.zgrcan.kalkan.model.EmergencyProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseEmergencyProfileRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : EmergencyProfileRepository {

    private val cache = ConcurrentHashMap<String, EmergencyProfile?>()

    override fun observeProfile(uid: String): Flow<EmergencyProfile?> = callbackFlow {
        if (uid.isBlank()) {
            trySend(cache[uid])
            close()
            return@callbackFlow
        }

        val registration = profileDocument(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "observeProfile failed", error)
                trySend(cache[uid])
                return@addSnapshotListener
            }
            val profile = runCatching { snapshot?.toEmergencyProfile() }.getOrElse { parseError ->
                Log.e(TAG, "observeProfile parse failed", parseError)
                cache[uid]
            }
            cache[uid] = profile
            trySend(profile)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun saveProfile(uid: String, profile: EmergencyProfile): Result<EmergencyProfile> = runCatching {
        val authUid = requireNotNull(auth.currentUser?.uid) { "Kullanıcı oturumu bulunamadı." }
        require(uid == authUid) { "Geçersiz kullanıcı kimliği." }

        val payload = profile.copy(updatedAt = System.currentTimeMillis())
        profileDocument(uid).set(payload.toFirestoreMap()).await()
        cache[uid] = payload
        payload
    }.recoverWith("Acil Durum Kartı kaydedilemedi. Lütfen tekrar deneyin.")

    override suspend fun deleteProfile(uid: String): Result<Unit> = runCatching {
        val authUid = requireNotNull(auth.currentUser?.uid) { "Kullanıcı oturumu bulunamadı." }
        require(uid == authUid) { "Geçersiz kullanıcı kimliği." }
        profileDocument(uid).delete().await()
        cache.remove(uid)
        Unit
    }.recoverWith("Acil Durum Kartı silinemedi. Lütfen tekrar deneyin.")

    override fun getCachedProfile(uid: String): EmergencyProfile? = cache[uid]

    override suspend fun fetchProfileOnce(uid: String): EmergencyProfile? = runCatching {
        if (uid.isBlank()) return@runCatching null
        val profile = profileDocument(uid).get().await().toEmergencyProfile()
        cache[uid] = profile
        profile
    }.getOrElse { error ->
        Log.e(TAG, "fetchProfileOnce failed", error)
        cache[uid]
    }

    private fun profileDocument(uid: String) =
        firestore.collection("users")
            .document(uid)
            .collection(COLLECTION)
            .document(EmergencyBloodTypes.DOCUMENT_ID)

    private fun EmergencyProfile.toFirestoreMap(): Map<String, Any?> = mapOf(
        "fullName" to fullName.trim(),
        "bloodType" to bloodType,
        "allergies" to allergies.trim(),
        "chronicDiseases" to chronicDiseases.trim(),
        "medications" to medications.trim(),
        "emergencyNote" to emergencyNote.trim(),
        "primaryContactName" to primaryContactName.trim(),
        "primaryContactPhone" to primaryContactPhone.filter { it.isDigit() },
        "updatedAt" to updatedAt,
    )

    private fun DocumentSnapshot.toEmergencyProfile(): EmergencyProfile? {
        if (!exists()) return null
        val bloodType = readStringField("bloodType")
        return EmergencyProfile(
            fullName = readStringField("fullName"),
            bloodType = bloodType.ifBlank { EmergencyBloodTypes.UNKNOWN },
            allergies = readStringField("allergies"),
            chronicDiseases = readStringField("chronicDiseases"),
            medications = readStringField("medications"),
            emergencyNote = readStringField("emergencyNote"),
            primaryContactName = readStringField("primaryContactName"),
            primaryContactPhone = readPhoneField("primaryContactPhone"),
            updatedAt = getNumberAsLong("updatedAt") ?: 0L,
        )
    }

    private fun DocumentSnapshot.readStringField(field: String): String =
        when (val value = get(field)) {
            null -> ""
            is String -> value
            else -> value.toString()
        }

    private fun DocumentSnapshot.readPhoneField(field: String): String =
        readStringField(field).filter { it.isDigit() }

    private fun <T> Result<T>.recoverWith(userMessage: String): Result<T> {
        val ex = exceptionOrNull() ?: return this
        val msg = ex.message.orEmpty()
        val isFirebaseInternal = ex is FirebaseException ||
            msg.isBlank() ||
            msg.contains("FirebaseFirestore", ignoreCase = true) ||
            msg.contains("PERMISSION_DENIED") ||
            msg.contains("com.google")
        return if (isFirebaseInternal) {
            Log.e(TAG, "Firebase error (hidden from user): $msg")
            Result.failure(Exception(userMessage))
        } else {
            this
        }
    }

    companion object {
        private const val TAG = "EmergencyProfileRepo"
        private const val COLLECTION = "emergency_profile"
    }
}
