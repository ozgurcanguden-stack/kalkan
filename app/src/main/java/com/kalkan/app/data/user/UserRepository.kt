package com.kalkan.app.data.user

import com.google.firebase.auth.FirebaseUser
import com.kalkan.app.model.AppUser
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUser(uid: String): Flow<AppUser?>
    suspend fun ensureUser(firebaseUser: FirebaseUser): Result<AppUser>
}
