package com.zgrcan.kalkan.data.admin

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface AdminStatsRepository {
    suspend fun getTotalUsersCount(): Result<Long>
    suspend fun getEarthquakeEnabledUsersCount(): Result<Long>
    suspend fun getTotalFamilyGroupsCount(): Result<Long>
    suspend fun getActiveSosCount(): Result<Long>
    suspend fun getRecentNotifiedEarthquakes(limit: Long = 10): Result<List<Map<String, Any>>>
}

@Singleton
class FirebaseAdminStatsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : AdminStatsRepository {

    override suspend fun getTotalUsersCount(): Result<Long> = runCatching {
        val query = firestore.collection("users")
        val countTask = query.count().get(AggregateSource.SERVER).await()
        countTask.count
    }

    override suspend fun getEarthquakeEnabledUsersCount(): Result<Long> = runCatching {
        // We will try counting users where earthquakeNotificationsEnabled == true
        // If it fails due to missing index, we return an error so the UI can gracefully show "N/A"
        val query = firestore.collection("users")
            .whereEqualTo("earthquakeNotificationsEnabled", true)
        val countTask = query.count().get(AggregateSource.SERVER).await()
        countTask.count
    }

    override suspend fun getTotalFamilyGroupsCount(): Result<Long> = runCatching {
        val query = firestore.collection("families")
        val countTask = query.count().get(AggregateSource.SERVER).await()
        countTask.count
    }

    override suspend fun getActiveSosCount(): Result<Long> = runCatching {
        // Querying users with safetyStatus.statusType == "emergency"
        // Again, if index is missing, catching will prevent crash.
        val query = firestore.collection("users")
            .whereEqualTo("safetyStatus.statusType", "emergency")
        val countTask = query.count().get(AggregateSource.SERVER).await()
        countTask.count
    }

    override suspend fun getRecentNotifiedEarthquakes(limit: Long): Result<List<Map<String, Any>>> = runCatching {
        val snapshot = firestore.collection("earthquake_events")
            .whereEqualTo("notificationSent", true)
            .orderBy("notificationSentAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
        
        snapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }
    }
}
