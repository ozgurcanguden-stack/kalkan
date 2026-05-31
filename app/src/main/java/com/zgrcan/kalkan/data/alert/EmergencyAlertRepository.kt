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
}
