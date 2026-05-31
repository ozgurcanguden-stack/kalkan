package com.zgrcan.kalkan.data.alert

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface EmergencyAlertRepository {
    suspend fun createAlert(
        title: String,
        body: String,
        region: String,
        priority: String,
        target: String,
        createdByUid: String,
        createdByName: String?,
    ): Result<Unit>

    suspend fun getRecentAlerts(limit: Long = 10): Result<List<Map<String, Any>>>
}

@Singleton
class FirebaseEmergencyAlertRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : EmergencyAlertRepository {

    override suspend fun createAlert(
        title: String,
        body: String,
        region: String,
        priority: String,
        target: String,
        createdByUid: String,
        createdByName: String?
    ): Result<Unit> = runCatching {
        val alertRef = firestore.collection("emergency_alerts").document()
        val data = mapOf(
            "title" to title,
            "body" to body,
            "region" to region,
            "priority" to priority,
            "target" to target,
            "createdByUid" to createdByUid,
            "createdByName" to createdByName,
            "createdAt" to System.currentTimeMillis(),
            "status" to "published",
            "source" to "admin_panel"
        )
        alertRef.set(data).await()
    }

    override suspend fun getRecentAlerts(limit: Long): Result<List<Map<String, Any>>> = runCatching {
        val snapshot = firestore.collection("emergency_alerts")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
        
        snapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }
    }
}
