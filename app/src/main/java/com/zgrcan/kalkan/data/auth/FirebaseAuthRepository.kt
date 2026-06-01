package com.zgrcan.kalkan.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
) : AuthRepository {
    override val currentFirebaseUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override suspend fun signInAsGuest(): Result<FirebaseUser> = runCatching {
        auth.currentUser?.takeIf { it.isAnonymous }?.let { return@runCatching it }
        val result = auth.signInAnonymously().await()
        requireNotNull(result.user)
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<FirebaseUser> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val currentUser = auth.currentUser
        val result = if (currentUser != null && currentUser.isAnonymous) {
            try {
                currentUser.linkWithCredential(credential).await()
            } catch (error: Exception) {
                val code = (error as? FirebaseAuthException)?.errorCode
                if (code == "ERROR_CREDENTIAL_ALREADY_IN_USE" || code == "ERROR_PROVIDER_ALREADY_LINKED") {
                    auth.signInWithCredential(credential).await()
                } else {
                    throw error
                }
            }
        } else {
            auth.signInWithCredential(credential).await()
        }
        requireNotNull(result.user)
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
