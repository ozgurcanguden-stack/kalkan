package com.zgrcan.kalkan.model

enum class FamilyRole(val value: String) {
    OWNER("owner"),
    MEMBER("member");

    companion object {
        fun from(value: String?): FamilyRole =
            entries.firstOrNull { it.value == value } ?: MEMBER
    }
}
