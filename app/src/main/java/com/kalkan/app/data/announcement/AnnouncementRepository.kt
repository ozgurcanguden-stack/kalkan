package com.kalkan.app.data.announcement

import com.kalkan.app.model.Announcement

interface AnnouncementRepository {
    suspend fun createAnnouncement(announcement: Announcement): Result<Announcement>

    suspend fun getRecentAnnouncements(limit: Int = 5): Result<List<Announcement>>
}
