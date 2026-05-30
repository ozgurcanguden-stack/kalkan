package com.kalkan.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kalkan.app.domain.model.EmergencyStatus
import com.kalkan.app.domain.model.EmergencyType
import com.kalkan.app.domain.repository.EmergencyRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseEmergencyRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : EmergencyRepository {
    override suspend fun markSafe(): Result<EmergencyStatus> = createStatus(EmergencyType.SAFE)

    override suspend fun requestHelp(message: String?): Result<EmergencyStatus> =
        createStatus(EmergencyType.NEED_HELP, message)

    override suspend fun sendSos(): Result<EmergencyStatus> = createStatus(EmergencyType.SOS)

    override suspend fun shareLocation(): Result<EmergencyStatus> =
        createStatus(EmergencyType.LOCATION_SHARED)

    private suspend fun createStatus(
        type: EmergencyType,
        message: String? = null,
    ): Result<EmergencyStatus> = runCatching {
        val userId = requireNotNull(auth.currentUser?.uid) { "User must be signed in." }
        val document = firestore.collection("emergency_statuses").document()
        val status = EmergencyStatus(
            id = document.id,
            userId = userId,
            type = type,
            message = message,
            createdAt = System.currentTimeMillis(),
        )
        document.set(status).await()
        status
    }
}
