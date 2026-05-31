package com.zgrcan.kalkan.data.fcm

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseFcmRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging,
) : FcmRepository {
    override suspend fun getCurrentToken(): Result<String> = runCatching {
        messaging.token.await()
    }

    override suspend fun syncTokenForCurrentUser(notificationPermissionGranted: Boolean): Result<Unit> =
        getCurrentToken().mapCatching { token ->
            saveTokenForCurrentUser(token, notificationPermissionGranted).getOrThrow()
        }

    override suspend fun saveTokenForCurrentUser(
        token: String,
        notificationPermissionGranted: Boolean?,
    ): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching
        if (uid == "local_guest") return@runCatching

        val updates = mutableMapOf<String, Any>(
            "fcmToken" to token,
            "lastFcmTokenUpdatedAt" to System.currentTimeMillis(),
        )
        notificationPermissionGranted?.let {
            updates["notificationPermissionGranted"] = it
        }
        firestore.collection("users").document(uid).update(updates).await()
    }

    override suspend fun updateNotificationPermissionForCurrentUser(granted: Boolean): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching
        firestore.collection("users")
            .document(uid)
            .update("notificationPermissionGranted", granted)
            .await()
    }
}
