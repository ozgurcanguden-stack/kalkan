package com.zgrcan.kalkan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zgrcan.kalkan.data.family.FamilyRepository
import com.zgrcan.kalkan.model.AppUser
import com.zgrcan.kalkan.model.FamilyGroup
import com.zgrcan.kalkan.model.FamilyMember
import com.zgrcan.kalkan.util.GuestFeatureMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FamilyGroupViewModel @Inject constructor(
    private val familyRepository: FamilyRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyGroupUiState())
    val uiState: StateFlow<FamilyGroupUiState> = _uiState.asStateFlow()

    private var activeUser: AppUser? = null
    private var observeGroupJob: Job? = null
    private var observeMembersJob: Job? = null

    fun loadFamilyGroup(user: AppUser?) {
        if (user != null) {
            activeUser = user
        }
        val currentUser = user ?: activeUser
        if (currentUser == null) {
            resetWithoutGroup()
            return
        }

        if (currentUser.familyGroupId.isNullOrBlank()) {
            resetWithoutGroup()
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    hasGroup = false,
                    familyGroup = null,
                    members = emptyList(),
                    error = null,
                )
            }

            val effectiveUser = resolveUserAfterStaleGroupCheck(currentUser)
            val groupId = effectiveUser.familyGroupId
            if (groupId.isNullOrBlank()) {
                resetWithoutGroup()
                return@launch
            }

            observeFamilyGroupData(groupId, effectiveUser)
        }
    }

    private suspend fun resolveUserAfterStaleGroupCheck(user: AppUser): AppUser {
        val wasCleared = familyRepository.clearStaleFamilyGroupIfMissing(user).getOrDefault(false)
        if (!wasCleared) {
            return user
        }
        val clearedUser = user.copy(familyGroupId = null, familyInviteCode = null)
        activeUser = clearedUser
        _uiState.update {
            it.copy(actionSuccessMessage = "Aile grubu bilgisi güncellendi.")
        }
        return clearedUser
    }

    private fun observeFamilyGroupData(groupId: String, user: AppUser) {
        viewModelScope.launch {
            familyRepository.syncCurrentMemberProfile(user, groupId)
        }

        observeGroupJob?.cancel()
        observeGroupJob = viewModelScope.launch {
            familyRepository.observeFamilyGroup(groupId)
                .catch {
                    _uiState.update {
                        it.copy(
                            error = "Grup bilgisi yüklenemedi.",
                            isLoading = false,
                            hasGroup = false,
                            familyGroup = null,
                            members = emptyList(),
                        )
                    }
                }
                .collect { group ->
                    _uiState.update {
                        it.copy(
                            familyGroup = group,
                            isLoading = false,
                            hasGroup = group != null,
                            members = if (group == null) emptyList() else it.members,
                        )
                    }
                }
        }

        observeMembersJob?.cancel()
        observeMembersJob = viewModelScope.launch {
            familyRepository.observeFamilyMembers(groupId)
                .catch {
                    _uiState.update {
                        it.copy(
                            error = "Üye listesi yüklenemedi.",
                            isLoading = false,
                        )
                    }
                }
                .collect { members ->
                    _uiState.update {
                        it.copy(
                            members = members.sortedByEmergencyPriority(),
                            isLoading = false,
                        )
                    }
                }
        }
    }

    private fun resetWithoutGroup() {
        observeGroupJob?.cancel()
        observeMembersJob?.cancel()
        _uiState.update {
            it.copy(
                familyGroup = null,
                members = emptyList(),
                hasGroup = false,
                isLoading = false,
            )
        }
    }

    fun createFamilyGroup(groupName: String) {
        val user = requireGoogleSignedInUser() ?: return
        if (groupName.isBlank()) {
            _uiState.update { it.copy(error = "Grup adı boş olamaz.") }
            return
        }

        _uiState.update { it.copy(isActionLoading = true, error = null) }
        viewModelScope.launch {
            familyRepository.createFamilyGroup(user, groupName)
                .onSuccess { group ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            familyGroup = group,
                            hasGroup = true,
                            actionSuccessMessage = "Aile grubu başarıyla oluşturuldu.",
                        )
                    }
                    loadFamilyGroup(user.copy(familyGroupId = group.id, familyInviteCode = group.inviteCode))
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            error = err.message ?: "Grup oluşturulurken hata oluştu.",
                        )
                    }
                }
        }
    }

    fun joinFamilyGroup(inviteCode: String) {
        val user = requireGoogleSignedInUser() ?: return
        if (inviteCode.isBlank()) {
            _uiState.update { it.copy(error = "Davet kodu boş olamaz.") }
            return
        }

        _uiState.update { it.copy(isActionLoading = true, error = null) }
        viewModelScope.launch {
            familyRepository.joinFamilyGroup(user, inviteCode)
                .onSuccess { group ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            familyGroup = group,
                            hasGroup = true,
                            actionSuccessMessage = "Aile grubuna başarıyla katıldınız.",
                        )
                    }
                    loadFamilyGroup(user.copy(familyGroupId = group.id))
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            error = err.message ?: "Gruba katılırken hata oluştu. Davet kodunu kontrol edin.",
                        )
                    }
                }
        }
    }

    fun leaveFamilyGroup(groupId: String) {
        val user = activeUser
        if (user == null || user.uid.isBlank() || groupId.isBlank()) {
            _uiState.update { it.copy(error = "Geçersiz işlem veya oturum.") }
            return
        }

        _uiState.update { it.copy(isActionLoading = true, error = null) }
        viewModelScope.launch {
            familyRepository.leaveFamilyGroup(user, groupId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            familyGroup = null,
                            members = emptyList(),
                            hasGroup = false,
                            actionSuccessMessage = "Aile grubundan başarıyla ayrıldınız.",
                        )
                    }
                    loadFamilyGroup(user.copy(familyGroupId = null, familyInviteCode = null))
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            error = err.message ?: "Gruptan ayrılırken hata oluştu.",
                        )
                    }
                }
        }
    }

    fun deleteFamilyGroup(groupId: String) {
        val user = activeUser
        if (user == null || user.uid.isBlank() || groupId.isBlank()) {
            _uiState.update { it.copy(error = "Geçersiz işlem veya oturum.") }
            return
        }

        _uiState.update { it.copy(isActionLoading = true, error = null) }
        viewModelScope.launch {
            familyRepository.deleteFamilyGroup(user, groupId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            familyGroup = null,
                            members = emptyList(),
                            hasGroup = false,
                            actionSuccessMessage = "Aile grubu başarıyla silindi.",
                        )
                    }
                    loadFamilyGroup(user.copy(familyGroupId = null, familyInviteCode = null))
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            error = err.message ?: "Grup silinirken hata oluştu.",
                        )
                    }
                }
        }
    }

    fun clearActionSuccessMessage() {
        _uiState.update { it.copy(actionSuccessMessage = null) }
    }

    fun refresh() {
        loadFamilyGroup(activeUser)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun requestFamilyStatusCheck() {
        val user = requireGoogleSignedInUser() ?: return
        val groupId = user.familyGroupId
        if (groupId.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Durum kontrolü için önce bir aile grubuna katılın.") }
            return
        }

        _uiState.update { it.copy(isActionLoading = true, error = null) }
        viewModelScope.launch {
            familyRepository.requestFamilyStatusCheck(user, groupId)
                .onSuccess { result ->
                    if (result.accepted) {
                        _uiState.update {
                            it.copy(
                                isActionLoading = false,
                                actionSuccessMessage = "Durum kontrol isteği aile üyelerinize gönderildi.",
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isActionLoading = false,
                                error = "Durum kontrol isteği kısa süre önce gönderildi. Tekrar göndermek için ${formatRemaining(result.remainingMs)} bekleyin.",
                            )
                        }
                    }
                }
                .onFailure { error ->
                    val message = error.localizedMessage?.takeIf { it.isNotBlank() }
                        ?: "Durum kontrol isteği gönderilemedi. Lütfen tekrar deneyin."
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            error = message,
                        )
                    }
                }
        }
    }

    private fun requireGoogleSignedInUser(): AppUser? {
        val user = activeUser
        return when {
            user == null || user.uid.isBlank() -> {
                _uiState.update { it.copy(error = "Lütfen giriş yapın.") }
                null
            }
            user.isGuest -> {
                _uiState.update { it.copy(error = GuestFeatureMessages.SIGN_IN_REQUIRED) }
                null
            }
            else -> user
        }
    }

    private fun formatRemaining(remainingMs: Long): String {
        val totalSeconds = ((remainingMs + 999L) / 1000L).coerceAtLeast(1L)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return if (minutes > 0) {
            "$minutes dakika $seconds saniye"
        } else {
            "$seconds saniye"
        }
    }
}

data class FamilyGroupUiState(
    val familyGroup: FamilyGroup? = null,
    val members: List<FamilyMember> = emptyList(),
    val hasGroup: Boolean = false,
    val isLoading: Boolean = true,
    val isActionLoading: Boolean = false,
    val error: String? = null,
    val actionSuccessMessage: String? = null,
)

private fun List<FamilyMember>.sortedByEmergencyPriority(): List<FamilyMember> =
    sortedWith(
        compareBy<FamilyMember> { it.statusPriority }
            .thenByDescending { it.lastStatusAt ?: 0L },
    )
