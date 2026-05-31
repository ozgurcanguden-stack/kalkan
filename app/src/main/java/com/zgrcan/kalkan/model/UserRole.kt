package com.zgrcan.kalkan.model

enum class UserRole(val value: String) {
    USER("user"),
    SUPER_ADMIN("super_admin");

    companion object {
        fun from(value: String?): UserRole =
            entries.firstOrNull { it.value == value } ?: USER
    }
}
