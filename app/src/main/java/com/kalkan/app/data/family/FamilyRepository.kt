package com.kalkan.app.data.family

import com.kalkan.app.model.AppUser
import com.kalkan.app.model.FamilyGroup
import com.kalkan.app.model.FamilyMember
import kotlinx.coroutines.flow.Flow

interface FamilyRepository {
    fun observeFamilyGroup(groupId: String): Flow<FamilyGroup?>
    fun observeFamilyMembers(groupId: String): Flow<List<FamilyMember>>
    suspend fun createFamilyGroup(user: AppUser, groupName: String): Result<FamilyGroup>
    suspend fun joinFamilyGroup(user: AppUser, inviteCode: String): Result<FamilyGroup>
    suspend fun leaveFamilyGroup(user: AppUser, groupId: String): Result<Unit>
    suspend fun deleteFamilyGroup(user: AppUser, groupId: String): Result<Unit>
}
