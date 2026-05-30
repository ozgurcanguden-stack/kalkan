package com.kalkan.app.data.announcement

import com.kalkan.app.model.Announcement

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
}
