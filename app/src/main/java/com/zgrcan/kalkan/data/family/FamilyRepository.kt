package com.zgrcan.kalkan.data.family

import com.zgrcan.kalkan.model.AppUser
import com.zgrcan.kalkan.model.FamilyGroup
import com.zgrcan.kalkan.model.FamilyMember
import kotlinx.coroutines.flow.Flow

interface FamilyRepository {
    fun observeFamilyGroup(groupId: String): Flow<FamilyGroup?>
    fun observeFamilyMembers(groupId: String): Flow<List<FamilyMember>>
    suspend fun createFamilyGroup(user: AppUser, groupName: String): Result<FamilyGroup>
    suspend fun joinFamilyGroup(user: AppUser, inviteCode: String): Result<FamilyGroup>
    suspend fun syncCurrentMemberProfile(user: AppUser, groupId: String): Result<Unit>
    suspend fun leaveFamilyGroup(user: AppUser, groupId: String): Result<Unit>
    suspend fun deleteFamilyGroup(user: AppUser, groupId: String): Result<Unit>

    /** Tek okuma: grup yoksa kullanıcıdaki familyGroupId / familyInviteCode temizlenir. */
    suspend fun clearStaleFamilyGroupIfMissing(user: AppUser): Result<Boolean>
}
