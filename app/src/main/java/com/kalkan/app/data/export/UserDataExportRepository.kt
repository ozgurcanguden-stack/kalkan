package com.kalkan.app.data.export

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kalkan.app.model.AppUser
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class UserDataExportRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    suspend fun exportUserData(user: AppUser): Result<String> = runCatching {
        val uid = user.uid
        require(uid.isNotBlank()) { "Geçersiz kullanıcı kimliği." }

        val rootJson = JSONObject()

        // 1. User Profile Info
        val userJson = JSONObject().apply {
            put("uid", user.uid)
            put("displayName", user.displayName)
            put("email", user.email)
            put("role", user.role.value)
            put("createdAt", user.createdAt)
            put("lastLoginAt", user.lastLoginAt)
            put("familyGroupId", user.familyGroupId)
            put("familyInviteCode", user.familyInviteCode)
        }
        rootJson.put("user_profile", userJson)

        // 2. Emergency Contacts
        val contactsArray = JSONArray()
        val contactsSnapshot = firestore.collection("users").document(uid)
            .collection("emergency_contacts").get().await()
        for (doc in contactsSnapshot.documents) {
            val contactJson = JSONObject().apply {
                put("id", doc.getString("id"))
                put("name", doc.getString("name"))
                put("phone", doc.getString("phone"))
                put("relation", doc.getString("relation"))
                put("isPrimary", doc.getBoolean("isPrimary") == true)
            }
            contactsArray.put(contactJson)
        }
        rootJson.put("emergency_contacts", contactsArray)

        // 3. Recent Safety Statuses
        val safetyArray = JSONArray()
        val safetySnapshot = try {
            firestore.collection("safety_status")
                .whereEqualTo("uid", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()
        } catch (_: Exception) {
            null
        }

        if (safetySnapshot != null) {
            for (doc in safetySnapshot.documents) {
                val safetyJson = JSONObject().apply {
                    put("id", doc.getString("id"))
                    put("statusType", doc.getString("statusType"))
                    put("message", doc.getString("message"))
                    put("latitude", doc.getDouble("latitude"))
                    put("longitude", doc.getDouble("longitude"))
                    put("createdAt", doc.getLong("createdAt"))
                }
                safetyArray.put(safetyJson)
            }
        }
        rootJson.put("safety_history", safetyArray)

        // 4. Family Group Details
        val familyGroupId = user.familyGroupId
        if (!familyGroupId.isNullOrBlank()) {
            val familyJson = JSONObject()
            val groupSnapshot = firestore.collection("family_groups").document(familyGroupId).get().await()
            if (groupSnapshot.exists()) {
                familyJson.put("groupId", familyGroupId)
                familyJson.put("name", groupSnapshot.getString("name"))
                familyJson.put("inviteCode", groupSnapshot.getString("inviteCode"))
                familyJson.put("ownerUid", groupSnapshot.getString("ownerUid"))

                val membersArray = JSONArray()
                val membersSnapshot = firestore.collection("family_groups").document(familyGroupId)
                    .collection("members").get().await()
                for (doc in membersSnapshot.documents) {
                    val memberJson = JSONObject().apply {
                        put("uid", doc.getString("uid"))
                        put("displayName", doc.getString("displayName"))
                        put("email", doc.getString("email"))
                        put("role", doc.getString("role"))
                    }
                    membersArray.put(memberJson)
                }
                familyJson.put("members", membersArray)
            }
            rootJson.put("family_group", familyJson)
        }

        rootJson.toString(4) // Formatted with 4 spaces indent!
    }
}
