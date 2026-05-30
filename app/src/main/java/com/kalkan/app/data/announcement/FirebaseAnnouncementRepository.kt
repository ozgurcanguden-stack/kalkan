package com.kalkan.app.data.announcement

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kalkan.app.model.Announcement
import com.kalkan.app.model.AnnouncementPriority
import com.kalkan.app.model.AnnouncementStatus
import com.kalkan.app.model.AnnouncementTargetAudience
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAnnouncementRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : AnnouncementRepository {
    override suspend fun createAnnouncement(announcement: Announcement): Result<Announcement> = runCatching {
        val document = announcements.document()
        val payload = announcement.copy(
            id = document.id,
            createdAt = if (announcement.createdAt > 0L) announcement.createdAt else System.currentTimeMillis(),
            status = announcement.status,
        )
        document.set(payload.toFirestoreMap()).await()
        payload
    }

    override suspend fun getRecentAnnouncements(limit: Int): Result<List<Announcement>> = runCatching {
        announcements
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .documents
            .mapNotNull { it.toAnnouncement() }
    }

    override suspend fun getPublishedAnnouncementsForUser(
        isGuest: Boolean,
        isRegistered: Boolean,
    ): Result<List<Announcement>> = runCatching {
        announcements
            .whereEqualTo("status", AnnouncementStatus.PUBLISHED.value)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(FETCH_LIMIT)
            .get()
            .await()
            .documents
            .mapNotNull { it.toAnnouncement() }
            .filter { matchesAudience(it, isGuest, isRegistered) }
            .sortedWith(announcementComparator)
    }

    override suspend fun getAnnouncementById(
        id: String,
        isGuest: Boolean,
        isRegistered: Boolean,
    ): Result<Announcement> = runCatching {
        val snapshot = announcements.document(id).get().await()
        val announcement = snapshot.toAnnouncement()
            ?: error("Duyuru bulunamadi.")
        require(announcement.status == AnnouncementStatus.PUBLISHED) {
            "Bu duyuru yayinda degil."
        }
        require(matchesAudience(announcement, isGuest, isRegistered)) {
            "Bu duyuruyu goruntuleme yetkiniz yok."
        }
        announcement
    }

    private val announcements
        get() = firestore.collection(COLLECTION)

    private fun Announcement.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "message" to message,
        "targetAudience" to targetAudience.value,
        "priority" to priority.value,
        "createdByUid" to createdByUid,
        "createdByName" to createdByName,
        "createdAt" to createdAt,
        "status" to status.value,
    )

    private fun DocumentSnapshot.toAnnouncement(): Announcement? {
        if (!exists()) return null
        return Announcement(
            id = getString("id").orEmpty().ifBlank { id },
            title = getString("title").orEmpty(),
            message = getString("message").orEmpty(),
            targetAudience = AnnouncementTargetAudience.from(getString("targetAudience")),
            priority = AnnouncementPriority.from(getString("priority")),
            createdByUid = getString("createdByUid").orEmpty(),
            createdByName = getString("createdByName").orEmpty(),
            createdAt = getLong("createdAt") ?: 0L,
            status = AnnouncementStatus.from(getString("status")),
        )
    }

    companion object {
        private const val COLLECTION = "announcements"
        private const val FETCH_LIMIT = 50L

        private val announcementComparator = compareByDescending<Announcement> { it.priority.sortWeight }
            .thenByDescending { it.createdAt }

        private fun matchesAudience(
            announcement: Announcement,
            isGuest: Boolean,
            isRegistered: Boolean,
        ): Boolean = when (announcement.targetAudience) {
            AnnouncementTargetAudience.ALL -> true
            AnnouncementTargetAudience.GUESTS -> isGuest
            AnnouncementTargetAudience.REGISTERED -> isRegistered
        }

        private val AnnouncementPriority.sortWeight: Int
            get() = when (this) {
                AnnouncementPriority.URGENT -> 3
                AnnouncementPriority.IMPORTANT -> 2
                AnnouncementPriority.NORMAL -> 1
            }
    }
}
