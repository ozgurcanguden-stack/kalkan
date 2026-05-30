package com.kalkan.app.model

data class AppUser(
    val uid: String = "",
    val displayName: String = "",
    val email: String? = null,
    val photoUrl: String? = null,
    val role: UserRole = UserRole.USER,
    val isAdmin: Boolean = false,
    val createdAt: Long = 0L,
    val lastLoginAt: Long = 0L,
    val fcmToken: String? = null,
    val notificationPermissionGranted: Boolean = false,
    val lastFcmTokenUpdatedAt: Long = 0L,
    val familyGroupId: String? = null,
    val familyInviteCode: String? = null,
) {
    val isGuest: Boolean
        get() = email.isNullOrBlank()
}
