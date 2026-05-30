package com.kalkan.app.data.family

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.kalkan.app.model.AppUser
import com.kalkan.app.model.FamilyGroup
import com.kalkan.app.model.FamilyMember
import com.kalkan.app.model.FamilyRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseFamilyRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : FamilyRepository {

    override fun observeFamilyGroup(groupId: String): Flow<FamilyGroup?> = callbackFlow {
        if (groupId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("family_groups").document(groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toFamilyGroup())
            }
        awaitClose { registration.remove() }
    }

    override fun observeFamilyMembers(groupId: String): Flow<List<FamilyMember>> = callbackFlow {
        if (groupId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("family_groups").document(groupId)
            .collection("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val members = snapshot?.documents?.mapNotNull { it.toFamilyMember() } ?: emptyList()
                trySend(members)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun createFamilyGroup(user: AppUser, groupName: String): Result<FamilyGroup> = runCatching {
        val trimmedName = groupName.trim()
        require(trimmedName.isNotBlank()) { "Grup adı boş olamaz." }
        require(user.uid.isNotBlank()) { "Geçersiz kullanıcı kimliği." }

        // 6 haneli benzersiz davet kodu üret
        var inviteCode = ""
        var isUnique = false
        var attempts = 0
        while (!isUnique && attempts < 10) {
            inviteCode = "KAL${(100..999).random()}"
            val existing = firestore.collection("family_groups")
                .whereEqualTo("inviteCode", inviteCode)
                .get()
                .await()
            if (existing.isEmpty) {
                isUnique = true
            }
            attempts++
        }

        val groupRef = firestore.collection("family_groups").document()
        val groupId = groupRef.id
        val now = System.currentTimeMillis()

        val group = FamilyGroup(
            id = groupId,
            ownerUid = user.uid,
            name = trimmedName,
            inviteCode = inviteCode,
            createdAt = now,
            updatedAt = now
        )

        val member = FamilyMember(
            uid = user.uid,
            displayName = user.displayName.ifBlank { "Misafir Kullanıcı" },
            email = user.email,
            role = FamilyRole.OWNER,
            joinedAt = now
        )

        val memberRef = groupRef.collection("members").document(user.uid)
        val userRef = firestore.collection("users").document(user.uid)

        val authUid = auth.currentUser?.uid ?: "AUTHENTICATION_NULL"
        val groupData = group.toFirestoreMap()
        val memberData = member.toFirestoreMap()
        val userData = mapOf(
            "familyGroupId" to groupId,
            "familyInviteCode" to inviteCode
        )

        // Adım 1: family_groups/{groupId} oluşturma
        try {
            Log.d("FirebaseFamilyRepository", "STEP: Adım 1: Aile Grubu Oluşturma | path='family_groups/$groupId' | auth.uid='$authUid' | ownerUid='${user.uid}' | memberUid='null' | groupId='$groupId' | yazılan data='$groupData'")
            groupRef.set(groupData).await()
            Log.d("FirebaseFamilyRepository", "STEP BAŞARILI: Adım 1 | path='family_groups/$groupId'")
        } catch (e: Exception) {
            val codeStr = (e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code?.name ?: "UNKNOWN"
            val errorMsg = "STEP HATA: Adım 1: Aile Grubu Oluşturma | path='family_groups/$groupId' | auth.uid='$authUid' | ownerUid='${user.uid}' | memberUid='null' | groupId='$groupId' | yazılan data='$groupData' | hata kodu='$codeStr' | hata mesajı='${e.message}'"
            Log.e("FirebaseFamilyRepository", errorMsg, e)
            throw Exception(errorMsg, e)
        }

        // Adım 2: family_groups/{groupId}/members/{auth.uid} oluşturma
        try {
            Log.d("FirebaseFamilyRepository", "STEP: Adım 2: Aile Üyeliği Oluşturma | path='family_groups/$groupId/members/${user.uid}' | auth.uid='$authUid' | ownerUid='${user.uid}' | memberUid='${user.uid}' | groupId='$groupId' | yazılan data='$memberData'")
            memberRef.set(memberData).await()
            Log.d("FirebaseFamilyRepository", "STEP BAŞARILI: Adım 2 | path='family_groups/$groupId/members/${user.uid}'")
        } catch (e: Exception) {
            val codeStr = (e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code?.name ?: "UNKNOWN"
            val errorMsg = "STEP HATA: Adım 2: Aile Üyeliği Oluşturma | path='family_groups/$groupId/members/${user.uid}' | auth.uid='$authUid' | ownerUid='${user.uid}' | memberUid='${user.uid}' | groupId='$groupId' | yazılan data='$memberData' | hata kodu='$codeStr' | hata mesajı='${e.message}'"
            Log.e("FirebaseFamilyRepository", errorMsg, e)
            throw Exception(errorMsg, e)
        }

        // Adım 3: users/{auth.uid} güncelleme
        try {
            Log.d("FirebaseFamilyRepository", "STEP: Adım 3: Kullanıcı Aile İlişkilendirmesi | path='users/${user.uid}' | auth.uid='$authUid' | ownerUid='${user.uid}' | memberUid='null' | groupId='$groupId' | yazılan data='$userData'")
            userRef.update(userData).await()
            Log.d("FirebaseFamilyRepository", "STEP BAŞARILI: Adım 3 | path='users/${user.uid}'")
        } catch (e: Exception) {
            val codeStr = (e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code?.name ?: "UNKNOWN"
            val errorMsg = "STEP HATA: Adım 3: Kullanıcı Aile İlişkilendirmesi | path='users/${user.uid}' | auth.uid='$authUid' | ownerUid='${user.uid}' | memberUid='null' | groupId='$groupId' | yazılan data='$userData' | hata kodu='$codeStr' | hata mesajı='${e.message}'"
            Log.e("FirebaseFamilyRepository", errorMsg, e)
            throw Exception(errorMsg, e)
        }

        group
    }

    override suspend fun joinFamilyGroup(user: AppUser, inviteCode: String): Result<FamilyGroup> = runCatching {
        val cleanCode = inviteCode.trim().uppercase()
        require(cleanCode.isNotBlank()) { "Davet kodu boş olamaz." }
        require(user.uid.isNotBlank()) { "Geçersiz kullanıcı kimliği." }

        val querySnapshot = firestore.collection("family_groups")
            .whereEqualTo("inviteCode", cleanCode)
            .get()
            .await()

        val groupDoc = querySnapshot.documents.firstOrNull()
            ?: throw IllegalArgumentException("Geçersiz davet kodu. Lütfen tekrar deneyin.")

        val group = groupDoc.toFamilyGroup()
            ?: throw IllegalStateException("Grup bilgileri okunamadı.")

        val now = System.currentTimeMillis()
        val member = FamilyMember(
            uid = user.uid,
            displayName = user.displayName.ifBlank { "Misafir Kullanıcı" },
            email = user.email,
            role = FamilyRole.MEMBER,
            joinedAt = now
        )

        val memberRef = groupDoc.reference.collection("members").document(user.uid)
        val userRef = firestore.collection("users").document(user.uid)

        val authUid = auth.currentUser?.uid ?: "AUTHENTICATION_NULL"
        val memberData = member.toFirestoreMap()

        try {
            Log.d("FirebaseFamilyRepository", "STEP: Aile Grubuna Katılma Üyeliği | path='family_groups/${group.id}/members/${user.uid}' | auth.uid='$authUid' | ownerUid='${group.ownerUid}' | memberUid='${user.uid}' | groupId='${group.id}' | yazılan data='$memberData'")
            memberRef.set(memberData).await()
            Log.d("FirebaseFamilyRepository", "STEP BAŞARILI: Aile Grubuna Katılma Üyeliği | path='family_groups/${group.id}/members/${user.uid}'")
        } catch (e: Exception) {
            val codeStr = (e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code?.name ?: "UNKNOWN"
            val errorMsg = "STEP HATA: Aile Grubuna Katılma Üyeliği | path='family_groups/${group.id}/members/${user.uid}' | auth.uid='$authUid' | ownerUid='${group.ownerUid}' | memberUid='${user.uid}' | groupId='${group.id}' | yazılan data='$memberData' | hata kodu='$codeStr' | hata mesajı='${e.message}'"
            Log.e("FirebaseFamilyRepository", errorMsg, e)
            throw Exception(errorMsg, e)
        }

        try {
            Log.d("FirebaseFamilyRepository", "STEP: Aile Grubuna Katılma Kullanıcı Güncellemesi | path='users/${user.uid}' | auth.uid='$authUid' | ownerUid='${group.ownerUid}' | memberUid='null' | groupId='${group.id}' | yazılan data='{familyGroupId=${group.id}}'")
            userRef.update("familyGroupId", group.id).await()
            Log.d("FirebaseFamilyRepository", "STEP BAŞARILI: Aile Grubuna Katılma Kullanıcı Güncellemesi | path='users/${user.uid}'")
        } catch (e: Exception) {
            val codeStr = (e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code?.name ?: "UNKNOWN"
            val errorMsg = "STEP HATA: Aile Grubuna Katılma Kullanıcı Güncellemesi | path='users/${user.uid}' | auth.uid='$authUid' | ownerUid='${group.ownerUid}' | memberUid='null' | groupId='${group.id}' | yazılan data='{familyGroupId=${group.id}}' | hata kodu='$codeStr' | hata mesajı='${e.message}'"
            Log.e("FirebaseFamilyRepository", errorMsg, e)
            throw Exception(errorMsg, e)
        }

        group
    }

    private fun DocumentSnapshot.toFamilyGroup(): FamilyGroup? {
        if (!exists()) return null
        return FamilyGroup(
            id = getString("id").orEmpty(),
            ownerUid = getString("ownerUid").orEmpty(),
            name = getString("name").orEmpty(),
            inviteCode = getString("inviteCode").orEmpty(),
            createdAt = getLong("createdAt") ?: 0L,
            updatedAt = getLong("updatedAt") ?: 0L,
        )
    }

    private fun FamilyGroup.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "ownerUid" to ownerUid,
        "name" to name,
        "inviteCode" to inviteCode,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
    )

    private fun DocumentSnapshot.toFamilyMember(): FamilyMember? {
        if (!exists()) return null
        return FamilyMember(
            uid = getString("uid").orEmpty(),
            displayName = getString("displayName").orEmpty(),
            email = getString("email"),
            phone = getString("phone"),
            role = FamilyRole.from(getString("role")),
            joinedAt = getLong("joinedAt") ?: 0L,
            lastStatusType = getString("lastStatusType"),
            lastStatusMessage = getString("lastStatusMessage"),
            lastStatusLatitude = getDouble("lastStatusLatitude"),
            lastStatusLongitude = getDouble("lastStatusLongitude"),
            lastStatusAt = getLong("lastStatusAt"),
        )
    }

    private fun FamilyMember.toFirestoreMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "displayName" to displayName,
        "email" to email,
        "phone" to phone,
        "role" to role.value,
        "joinedAt" to joinedAt,
        "lastStatusType" to lastStatusType,
        "lastStatusMessage" to lastStatusMessage,
        "lastStatusLatitude" to lastStatusLatitude,
        "lastStatusLongitude" to lastStatusLongitude,
        "lastStatusAt" to lastStatusAt,
    )

    override suspend fun leaveFamilyGroup(user: AppUser, groupId: String): Result<Unit> = runCatching {
        val trimmedGroupId = groupId.trim()
        require(trimmedGroupId.isNotBlank()) { "Grup kimliği boş olamaz." }
        require(user.uid.isNotBlank()) { "Geçersiz kullanıcı kimliği." }

        val authUid = auth.currentUser?.uid ?: "AUTHENTICATION_NULL"
        Log.d("FirebaseFamilyRepository", "START: leaveFamilyGroup | groupId='$trimmedGroupId' | auth.uid='$authUid' | memberUid='${user.uid}'")

        try {
            firestore.runBatch { batch ->
                val memberRef = firestore.collection("family_groups").document(trimmedGroupId)
                    .collection("members").document(user.uid)
                Log.d("FirebaseFamilyRepository", "BATCH DELETE member: path='family_groups/$trimmedGroupId/members/${user.uid}'")
                batch.delete(memberRef)

                val userRef = firestore.collection("users").document(user.uid)
                Log.d("FirebaseFamilyRepository", "BATCH UPDATE user: path='users/${user.uid}'")
                batch.update(userRef, mapOf(
                    "familyGroupId" to null,
                    "familyInviteCode" to null
                ))
            }.await()
            Log.d("FirebaseFamilyRepository", "STEP BAŞARILI: leaveFamilyGroup batch | groupId='$trimmedGroupId'")
        } catch (e: Exception) {
            val codeStr = (e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code?.name ?: "UNKNOWN"
            Log.e("FirebaseFamilyRepository", "Firestore HATA Detayları:")
            Log.e("FirebaseFamilyRepository", "- İşlem: DELETE / UPDATE (Gruptan Ayrıl)")
            Log.e("FirebaseFamilyRepository", "- Path: family_groups/$trimmedGroupId/members/${user.uid} ve users/${user.uid}")
            Log.e("FirebaseFamilyRepository", "- Kullanıcı UID: ${user.uid}")
            Log.e("FirebaseFamilyRepository", "- Hata Kodu: $codeStr")
            Log.e("FirebaseFamilyRepository", "- Hata Mesajı: ${e.message}", e)
            throw Exception("Gruptan ayrılırken yetki veya sunucu hatası oluştu. Lütfen bağlantınızı kontrol edin.")
        }
    }

    override suspend fun deleteFamilyGroup(user: AppUser, groupId: String): Result<Unit> = runCatching {
        val trimmedGroupId = groupId.trim()
        // STEP 1: groupId okunuyor mu?
        Log.d("FirebaseFamilyRepository", "STEP 1: groupId okunuyor mu? -> groupId='$trimmedGroupId'")
        require(trimmedGroupId.isNotBlank()) { "Grup kimliği boş olamaz." }

        // STEP 2: currentUserUid okunuyor mu?
        val currentUserUid = user.uid
        Log.d("FirebaseFamilyRepository", "STEP 2: currentUserUid okunuyor mu? -> currentUserUid='$currentUserUid'")
        require(currentUserUid.isNotBlank()) { "Geçersiz kullanıcı kimliği." }

        // STEP 3: family_groups/{groupId} ownerUid okunuyor mu?
        val groupSnapshot = try {
            firestore.collection("family_groups").document(trimmedGroupId).get().await()
        } catch (e: Exception) {
            val codeStr = (e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code?.name ?: "UNKNOWN"
            Log.e("FirebaseFamilyRepository", "Firestore HATA Detayları:")
            Log.e("FirebaseFamilyRepository", "- İşlem: GET (Grup Bilgisi)")
            Log.e("FirebaseFamilyRepository", "- Path: family_groups/$trimmedGroupId")
            Log.e("FirebaseFamilyRepository", "- Kullanıcı UID: $currentUserUid")
            Log.e("FirebaseFamilyRepository", "- Hata Kodu: $codeStr")
            Log.e("FirebaseFamilyRepository", "- Hata Mesajı: ${e.message}", e)
            throw Exception("Grup bilgileri veritabanından okunurken hata oluştu.")
        }

        val ownerUid = groupSnapshot.getString("ownerUid").orEmpty()
        Log.d("FirebaseFamilyRepository", "STEP 3: family_groups/{groupId} ownerUid okunuyor mu? -> ownerUid='$ownerUid'")

        // STEP 4: ownerUid == currentUserUid mi?
        val isOwner = ownerUid == currentUserUid
        Log.d("FirebaseFamilyRepository", "STEP 4: ownerUid == currentUserUid mi? -> isOwner=$isOwner")
        if (!isOwner) {
            Log.e("FirebaseFamilyRepository", "HATA: Grubu silme yetkiniz yok! ownerUid='$ownerUid', currentUserUid='$currentUserUid'")
            throw Exception("Grubu silmek için yönetici yetkiniz bulunmamaktadır.")
        }

        // STEP 5: members listesi okunuyor mu?
        val membersSnapshot = try {
            firestore.collection("family_groups").document(trimmedGroupId)
                .collection("members").get().await()
        } catch (e: Exception) {
            val codeStr = (e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code?.name ?: "UNKNOWN"
            Log.e("FirebaseFamilyRepository", "Firestore HATA Detayları:")
            Log.e("FirebaseFamilyRepository", "- İşlem: GET (Üye Listesi)")
            Log.e("FirebaseFamilyRepository", "- Path: family_groups/$trimmedGroupId/members")
            Log.e("FirebaseFamilyRepository", "- Kullanıcı UID: $currentUserUid")
            Log.e("FirebaseFamilyRepository", "- Hata Kodu: $codeStr")
            Log.e("FirebaseFamilyRepository", "- Hata Mesajı: ${e.message}", e)
            throw Exception("Gruptaki üyeler okunurken yetki hatası oluştu.")
        }
        Log.d("FirebaseFamilyRepository", "STEP 5: members listesi okunuyor mu? -> üye sayısı=${membersSnapshot.size()}")

        // 2. Perform atomic batch deletion
        try {
            firestore.runBatch { batch ->
                // STEP 6: her member dokümanı siliniyor mu?
                for (doc in membersSnapshot.documents) {
                    val memberPath = "family_groups/$trimmedGroupId/members/${doc.id}"
                    Log.d("FirebaseFamilyRepository", "STEP 6: BATCH DELETE member -> path='$memberPath'")
                    batch.delete(doc.reference)
                }

                // STEP 7: family_groups/{groupId} siliniyor mu?
                val groupRef = firestore.collection("family_groups").document(trimmedGroupId)
                Log.d("FirebaseFamilyRepository", "STEP 7: BATCH DELETE group -> path='family_groups/$trimmedGroupId'")
                batch.delete(groupRef)

                // STEP 8: users/{currentUserUid} familyGroupId/familyInviteCode temizleniyor mu?
                val userRef = firestore.collection("users").document(currentUserUid)
                Log.d("FirebaseFamilyRepository", "STEP 8: BATCH UPDATE owner user -> path='users/$currentUserUid'")
                batch.update(userRef, mapOf(
                    "familyGroupId" to com.google.firebase.firestore.FieldValue.delete(),
                    "familyInviteCode" to com.google.firebase.firestore.FieldValue.delete()
                ))
            }.await()
            Log.d("FirebaseFamilyRepository", "STEP BAŞARILI: deleteFamilyGroup batch | groupId='$trimmedGroupId'")
        } catch (e: Exception) {
            val codeStr = (e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code?.name ?: "UNKNOWN"
            Log.e("FirebaseFamilyRepository", "Firestore HATA Detayları:")
            Log.e("FirebaseFamilyRepository", "- İşlem: DELETE / UPDATE")
            Log.e("FirebaseFamilyRepository", "- Path: family_groups/$trimmedGroupId ve users/$currentUserUid")
            Log.e("FirebaseFamilyRepository", "- Kullanıcı UID: $currentUserUid")
            Log.e("FirebaseFamilyRepository", "- Hata Kodu: $codeStr")
            Log.e("FirebaseFamilyRepository", "- Hata Mesajı: ${e.message}", e)
            throw Exception("Grup silinemedi. Lütfen veritabanı kurallarınızı (Firestore Rules) kontrol edin.")
        }
    }
}
