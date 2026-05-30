package com.kalkan.app.domain.repository

import com.kalkan.app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signInAsGuest(): Result<User>
    suspend fun signOut()
}
