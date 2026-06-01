package com.zgrcan.kalkan.data.user

import com.google.firebase.auth.FirebaseUser
import com.zgrcan.kalkan.model.AppUser
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUser(uid: String): Flow<AppUser?>
    suspend fun ensureUser(firebaseUser: FirebaseUser): Result<AppUser>
    suspend fun markUserInactive(uid: String): Result<Unit>
}
