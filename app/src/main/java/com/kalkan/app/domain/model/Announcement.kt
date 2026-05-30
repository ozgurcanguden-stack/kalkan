package com.kalkan.app.domain.model

data class Announcement(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: AnnouncementType = AnnouncementType.INFO,
    val targetCity: String? = null,
    val createdBy: String = "",
    val createdAt: Long = 0L,
)

enum class AnnouncementType {
    INFO,
    WARNING,
    EMERGENCY,
}
