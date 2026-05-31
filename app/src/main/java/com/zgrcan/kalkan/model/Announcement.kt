package com.zgrcan.kalkan.model

data class Announcement(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val targetAudience: AnnouncementTargetAudience = AnnouncementTargetAudience.ALL,
    val priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
    val createdByUid: String = "",
    val createdByName: String = "",
    val createdAt: Long = 0L,
    val status: AnnouncementStatus = AnnouncementStatus.PUBLISHED,
)

enum class AnnouncementStatus(val value: String) {
    DRAFT("draft"),
    PUBLISHED("published"),
    ;

    companion object {
        fun from(value: String?): AnnouncementStatus =
            entries.firstOrNull { it.value == value } ?: PUBLISHED
    }
}
