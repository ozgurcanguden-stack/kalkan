package com.zgrcan.kalkan.model

enum class AnnouncementTargetAudience(val value: String, val label: String) {
    ALL("all", "Tum Kullanicilar"),
    GUESTS("guests", "Sadece Misafirler"),
    REGISTERED("registered", "Sadece Kayitli Kullanicilar"),
    ;

    companion object {
        fun from(value: String?): AnnouncementTargetAudience =
            entries.firstOrNull { it.value == value } ?: ALL
    }
}
