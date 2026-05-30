package com.kalkan.app.data.user

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.kalkan.app.model.AppUser
import com.kalkan.app.model.UserRole
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
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toAppUser())
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
            )
            userRef.set(user.toFirestoreMap()).await()
            user
        } else {
            userRef.update("lastLoginAt", now).await()
            requireNotNull(userRef.get().await().toAppUser())
        }
    }

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
    )

    private fun DocumentSnapshot.toAppUser(): AppUser? {
        if (!exists()) return null
        val role = UserRole.from(getString("role"))
        return AppUser(
            uid = getString("uid").orEmpty().ifBlank { id },
            displayName = getString("displayName").orEmpty(),
            email = getString("email"),
            photoUrl = getString("photoUrl"),
            role = role,
            isAdmin = getBoolean("isAdmin") == true && role == UserRole.SUPER_ADMIN,
            createdAt = getLong("createdAt") ?: 0L,
            lastLoginAt = getLong("lastLoginAt") ?: 0L,
            fcmToken = getString("fcmToken"),
            notificationPermissionGranted = getBoolean("notificationPermissionGranted") == true,
            lastFcmTokenUpdatedAt = getLong("lastFcmTokenUpdatedAt") ?: 0L,
        )
    }
}
