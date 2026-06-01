package com.zgrcan.kalkan.data.announcement

import com.zgrcan.kalkan.model.Announcement

interface AnnouncementRepository {
    suspend fun createAnnouncement(announcement: Announcement): Result<Announcement>

    suspend fun getRecentAnnouncements(limit: Int = 5): Result<List<Announcement>>

    suspend fun getPublishedAnnouncementsForUser(
        isGuest: Boolean,
        isRegistered: Boolean,
    ): Result<List<Announcement>>

    suspend fun getAnnouncementById(
        id: String,
        isGuest: Boolean,
        isRegistered: Boolean,
    ): Result<Announcement>

    suspend fun deleteAnnouncement(id: String): Result<Unit>
}
