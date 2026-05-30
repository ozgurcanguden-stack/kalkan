package com.kalkan.app.data.settings

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.kalkan.app.data.family.FamilyRepository
import com.kalkan.app.model.AppUser
import com.kalkan.app.model.BackupFrequency
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseSettingsRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val familyRepository: FamilyRepository,
) : SettingsRepository {

    private fun userRef(uid: String) = firestore.collection("users").document(uid)

    override suspend fun manualBackup(user: AppUser): Result<Unit> = runCatching {
        val uid = user.uid
        require(uid.isNotBlank()) { "Geçersiz kullanıcı kimliği." }
        val now = System.currentTimeMillis()

        Log.d(TAG, "START: manualBackup | uid='$uid'")

        userRef(uid).update(mapOf(
            "lastManualBackupAt" to now,
            "lastSyncAt" to now
        )).await()

        Log.d(TAG, "SUCCESS: manualBackup completed | uid='$uid'")
        Unit
    }.recoverWith("Yedekleme sırasında bir hata oluştu. Lütfen tekrar deneyin.")

    override suspend fun getBackupFrequency(uid: String): Result<BackupFrequency> = runCatching {
        require(uid.isNotBlank()) { "Geçersiz kullanıcı kimliği." }
        val snapshot = userRef(uid).get().await()
        BackupFrequency.fromKey(snapshot.getString("backupFrequency"))
    }.recoverWith("Yedekleme ayarları yüklenemedi.")

    override suspend fun setBackupFrequency(uid: String, frequency: BackupFrequency): Result<Unit> = runCatching {
        require(uid.isNotBlank()) { "Geçersiz kullanıcı kimliği." }
        userRef(uid).update(mapOf(
            "backupFrequency" to frequency.key,
            "lastSyncAt" to System.currentTimeMillis(),
        )).await()
        Log.d(TAG, "Backup frequency set to '${frequency.key}' for uid='$uid'")
        Unit
    }.recoverWith("Yedekleme ayarı kaydedilemedi. Lütfen tekrar deneyin.")

    override suspend fun getBackupTimestamps(uid: String): Result<Pair<Long?, Long?>> = runCatching {
        require(uid.isNotBlank()) { "Geçersiz kullanıcı kimliği." }
        val snapshot = userRef(uid).get().await()
        Pair(snapshot.getLong("lastManualBackupAt"), snapshot.getLong("lastSyncAt"))
    }.recoverWith("Yedekleme bilgileri alınamadı.")

    override suspend fun clearUserData(user: AppUser): Result<Unit> = runCatching {
        val uid = user.uid
        require(uid.isNotBlank()) { "Geçersiz kullanıcı kimliği." }
        Log.d(TAG, "START: clearUserData | uid='$uid'")

        val contactsSnapshot = userRef(uid).collection("emergency_contacts").get().await()
        if (!contactsSnapshot.isEmpty) {
            firestore.runBatch { batch ->
                for (doc in contactsSnapshot.documents) batch.delete(doc.reference)
            }.await()
            Log.d(TAG, "Deleted ${contactsSnapshot.size()} emergency contacts")
        }

        val familyGroupId = user.familyGroupId
        if (!familyGroupId.isNullOrBlank()) {
            Log.d(TAG, "Leaving family group: $familyGroupId")
            familyRepository.leaveFamilyGroup(user, familyGroupId).getOrThrow()
        }

        Log.d(TAG, "SUCCESS: clearUserData completed | uid='$uid'")
        Unit
    }.recoverWith("Veriler temizlenirken bir hata oluştu.")

    override suspend fun deleteAccount(user: AppUser): Result<Unit> = runCatching {
        val uid = user.uid
        require(uid.isNotBlank()) { "Geçersiz kullanıcı kimliği." }
        val currentUser = auth.currentUser ?: throw Exception("Aktif oturum bulunamadı.")
        Log.d(TAG, "START: deleteAccount | uid='$uid'")

        clearUserData(user).getOrThrow()
        userRef(uid).delete().await()
        Log.d(TAG, "Deleted users/{uid} document")

        try {
            currentUser.delete().await()
            Log.d(TAG, "Deleted Firebase Auth Account")
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            throw Exception("Hesabınızı silmek için yakın zamanda giriş yapmaş olmanız gerekir. Lütfen çıkış yapıp tekrar giriş yaptıktan sonra deneyin.")
        } catch (e: Exception) {
            throw Exception("Hesap silinirken bir hata oluştu. Lütfen tekrar deneyin.")
        }
        Unit
    }.recoverWith("Hesap silinirken bir hata oluştu.")

    // Helper: converts Firebase-internal errors to user-friendly Turkish messages.
    // Our own explicitly thrown Exception messages are kept as-is.
    private fun <T> Result<T>.recoverWith(userMessage: String): Result<T> {
        val ex = exceptionOrNull() ?: return this // success — pass through
        val msg = ex.message.orEmpty()
        val isFirebaseInternal = ex is FirebaseException
            || msg.isBlank()
            || msg.contains("FirebaseFirestore", ignoreCase = true)
            || msg.contains("FAILED_PRECONDITION")
            || msg.contains("PERMISSION_DENIED")
            || msg.contains("NOT_FOUND")
            || msg.contains("com.google")
        return if (isFirebaseInternal) {
            Log.e(TAG, "Firebase error (hidden from user): $msg")
            Result.failure(Exception(userMessage))
        } else {
            Log.e(TAG, "Settings error: $msg")
            this // keep original Turkish message
        }
    }

    companion object {
        private const val TAG = "FirebaseSettings"
    }
}
