package com.kalkan.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kalkan.app.domain.model.User
import com.kalkan.app.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository {
    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            trySend(
                firebaseUser?.let {
                    User(
                        id = it.uid,
                        fullName = it.displayName.orEmpty(),
                        email = it.email,
                        phoneNumber = it.phoneNumber,
                        photoUrl = it.photoUrl?.toString(),
                        isGuest = it.isAnonymous,
                    )
                },
            )
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInAsGuest(): Result<User> = runCatching {
        val result = auth.signInAnonymously().await()
        val firebaseUser = requireNotNull(result.user)
        val user = User(id = firebaseUser.uid, isGuest = true)
        firestore.collection("users").document(user.id).set(user).await()
        user
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
