package com.zgrcan.kalkan.data.auth

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentFirebaseUser: Flow<FirebaseUser?>
    fun getCurrentUser(): FirebaseUser?
    suspend fun signInAsGuest(): Result<FirebaseUser>
    suspend fun signInWithGoogleIdToken(idToken: String): Result<FirebaseUser>
    suspend fun signOut()
}
