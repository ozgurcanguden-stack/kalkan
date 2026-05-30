package com.kalkan.app.data.settings

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.kalkan.app.data.family.FamilyRepository
import com.kalkan.app.model.AppUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseSettingsRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val familyRepository: FamilyRepository,
) : SettingsRepository {

    override suspend fun manualBackup(user: AppUser, deviceName: String, appVersion: String): Result<Unit> = runCatching {
        val uid = user.uid
        require(uid.isNotBlank()) { "Geçersiz kullanıcı kimliği." }
        val now = System.currentTimeMillis()

        Log.d("FirebaseSettings", "START: manualBackup | uid='$uid' | device='$deviceName'")

        // 1. Update users/{uid}/backup_metadata/current
        val metadataRef = firestore.collection("users").document(uid)
            .collection("backup_metadata").document("current")
        
        metadataRef.set(mapOf(
            "lastManualBackupAt" to now,
            "deviceName" to deviceName,
            "appVersion" to appVersion
        )).await()

        // 2. Update users/{uid} fields
        val userRef = firestore.collection("users").document(uid)
        userRef.update(mapOf(
            "lastManualBackupAt" to now,
            "lastSyncAt" to now
        )).await()

        Log.d("FirebaseSettings", "SUCCESS: manualBackup completed | uid='$uid'")
    }

    override suspend fun clearUserData(user: AppUser): Result<Unit> = runCatching {
        val uid = user.uid
        require(uid.isNotBlank()) { "Geçersiz kullanıcı kimliği." }
        Log.d("FirebaseSettings", "START: clearUserData | uid='$uid'")

        // 1. Delete all emergency contacts
        val contactsSnapshot = firestore.collection("users").document(uid)
            .collection("emergency_contacts").get().await()
        
        if (!contactsSnapshot.isEmpty) {
            firestore.runBatch { batch ->
                for (doc in contactsSnapshot.documents) {
                    batch.delete(doc.reference)
                }
            }.await()
            Log.d("FirebaseSettings", "SUCCESS: Deleted ${contactsSnapshot.size()} emergency contacts")
        }

        // 2. Leave family group if joined
        val familyGroupId = user.familyGroupId
        if (!familyGroupId.isNullOrBlank()) {
            Log.d("FirebaseSettings", "Leaving family group: $familyGroupId")
            familyRepository.leaveFamilyGroup(user, familyGroupId).getOrThrow()
        }

        Log.d("FirebaseSettings", "SUCCESS: clearUserData completed | uid='$uid'")
    }

    override suspend fun deleteAccount(user: AppUser): Result<Unit> = runCatching {
        val uid = user.uid
        require(uid.isNotBlank()) { "Geçersiz kullanıcı kimliği." }
        val currentUser = auth.currentUser ?: throw Exception("Aktif oturum bulunamadı.")
        Log.d("FirebaseSettings", "START: deleteAccount | uid='$uid'")

        // 1. Clear Firestore User Data first (Contacts and Family leaving)
        clearUserData(user).getOrThrow()

        // 2. Delete users/{uid} document
        val userRef = firestore.collection("users").document(uid)
        userRef.delete().await()
        Log.d("FirebaseSettings", "SUCCESS: Deleted users/{uid} document")

        // 3. Delete Firebase Auth Account
        try {
            currentUser.delete().await()
            Log.d("FirebaseSettings", "SUCCESS: Deleted Firebase Auth Account")
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            Log.w("FirebaseSettings", "Reauthentication required for user deletion", e)
            throw Exception("Hesabınızı silmek için yakın zamanda giriş yapmış olmanız gerekir. Lütfen güvenliğiniz için çıkış yapıp tekrar giriş yaptıktan sonra hesabı silmeyi deneyin.")
        } catch (e: Exception) {
            val errMsg = e.message ?: "Hesap silinirken bilinmeyen hata oluştu."
            Log.e("FirebaseSettings", "Auth delete error: $errMsg", e)
            throw Exception(errMsg)
        }
    }
}
