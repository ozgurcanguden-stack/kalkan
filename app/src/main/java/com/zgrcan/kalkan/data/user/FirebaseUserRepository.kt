package com.zgrcan.kalkan.data.user

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.zgrcan.kalkan.data.firestore.getNumberAsDouble
import com.zgrcan.kalkan.data.firestore.getNumberAsLong
import com.zgrcan.kalkan.model.AppUser
import com.zgrcan.kalkan.model.BackupFrequency
import com.zgrcan.kalkan.model.UserRole
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UserRepository {
    override fun observeUser(uid: String): Flow<AppUser?> = callbackFlow {
        val registration = users.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "observeUser failed", error)
                trySend(null)
                return@addSnapshotListener
            }
            val user = runCatching { snapshot?.toAppUser() }.getOrElse { parseError ->
                Log.e(TAG, "observeUser parse failed", parseError)
                null
            }
            trySend(user)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun ensureUser(firebaseUser: FirebaseUser): Result<AppUser> = runCatching {
        val userRef = users.document(firebaseUser.uid)
        val now = System.currentTimeMillis()
        val snapshot = userRef.get().await()

        if (!snapshot.exists()) {
            val user = AppUser(
                uid = firebaseUser.uid,
                displayName = firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: "Misafir Kullanıcı",
                email = firebaseUser.email,
                photoUrl = firebaseUser.photoUrl?.toString(),
                role = UserRole.USER,
                isAdmin = false,
                createdAt = now,
                lastLoginAt = now,
                earthquakeNotificationsEnabled = true,
                earthquakeNotificationMinMagnitude = 4.0,
            )
            userRef.set(user.toFirestoreMap()).await()
            user
        } else {
            val updates = mutableMapOf<String, Any>("lastLoginAt" to now)
            if (snapshot.getString("backupFrequency").isNullOrBlank()) {
                updates["backupFrequency"] = BackupFrequency.DAILY.key
            }
            val authPhotoUrl = firebaseUser.photoUrl?.toString()?.takeIf { it.isNotBlank() }
            if (authPhotoUrl != null && snapshot.getString("photoUrl") != authPhotoUrl) {
                updates["photoUrl"] = authPhotoUrl
            }
            userRef.update(updates).await()
            userRef.get().await().toAppUser()
                ?: firebaseUser.toAppUserFromAuth(now)
        }
    }

    private fun FirebaseUser.toAppUserFromAuth(lastLoginAt: Long): AppUser =
        AppUser(
            uid = uid,
            displayName = displayName?.takeIf { it.isNotBlank() } ?: "Kullanıcı",
            email = email,
            photoUrl = photoUrl?.toString(),
            role = UserRole.USER,
            isAdmin = false,
            createdAt = lastLoginAt,
            lastLoginAt = lastLoginAt,
            earthquakeNotificationsEnabled = true,
            earthquakeNotificationMinMagnitude = 4.0,
        )

    private val users
        get() = firestore.collection("users")

    private fun AppUser.toFirestoreMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "displayName" to displayName,
        "email" to email,
        "photoUrl" to photoUrl,
        "role" to role.value,
        "isAdmin" to isAdmin,
        "createdAt" to createdAt,
        "lastLoginAt" to lastLoginAt,
        "fcmToken" to fcmToken,
        "notificationPermissionGranted" to notificationPermissionGranted,
        "lastFcmTokenUpdatedAt" to lastFcmTokenUpdatedAt,
        "familyGroupId" to familyGroupId,
        "familyInviteCode" to familyInviteCode,
        "backupFrequency" to BackupFrequency.DAILY.key,
        "earthquakeNotificationsEnabled" to earthquakeNotificationsEnabled,
        "earthquakeNotificationMinMagnitude" to earthquakeNotificationMinMagnitude,
    )

    private fun DocumentSnapshot.toAppUser(): AppUser? {
        if (!exists()) return null
        val role = UserRole.from(getString("role"))
        val eqEnabled = getBoolean("earthquakeNotificationsEnabled")
        return AppUser(
            uid = getString("uid").orEmpty().ifBlank { id },
            displayName = getString("displayName").orEmpty(),
            email = getString("email"),
            photoUrl = getString("photoUrl"),
            role = role,
            isAdmin = getBoolean("isAdmin") == true && role == UserRole.SUPER_ADMIN,
            createdAt = getNumberAsLong("createdAt") ?: 0L,
            lastLoginAt = getNumberAsLong("lastLoginAt") ?: 0L,
            fcmToken = getString("fcmToken"),
            notificationPermissionGranted = getBoolean("notificationPermissionGranted") == true,
            lastFcmTokenUpdatedAt = getNumberAsLong("lastFcmTokenUpdatedAt") ?: 0L,
            familyGroupId = getString("familyGroupId"),
            familyInviteCode = getString("familyInviteCode"),
            earthquakeNotificationsEnabled = eqEnabled ?: true,
            earthquakeNotificationMinMagnitude = getNumberAsDouble("earthquakeNotificationMinMagnitude")
                ?: if (eqEnabled == false) null else 4.0,
        )
    }

    companion object {
        private const val TAG = "FirebaseUserRepo"
    }
}
