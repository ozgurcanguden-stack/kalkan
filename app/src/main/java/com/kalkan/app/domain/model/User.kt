package com.kalkan.app.domain.model

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String? = null,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
    val role: UserRole = UserRole.USER,
    val isGuest: Boolean = false,
    val city: String? = null,
    val district: String? = null,
    val createdAt: Long = 0L,
    val lastActiveAt: Long = 0L,
)

enum class UserRole {
    USER,
    SUPER_ADMIN,
}
