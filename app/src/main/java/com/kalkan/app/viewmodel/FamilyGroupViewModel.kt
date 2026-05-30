package com.kalkan.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalkan.app.data.family.FamilyRepository
import com.kalkan.app.model.AppUser
import com.kalkan.app.model.FamilyGroup
import com.kalkan.app.model.FamilyMember
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
        if (user == null || user.familyGroupId.isNullOrBlank()) {
            observeGroupJob?.cancel()
            observeMembersJob?.cancel()
            _uiState.update {
                it.copy(
                    familyGroup = null,
                    members = emptyList(),
                    hasGroup = false,
                    isLoading = false
                )
            }
            return
        }

        val groupId = user.familyGroupId
        _uiState.update { it.copy(isLoading = true, hasGroup = true, error = null) }

        // Observe Group details
        observeGroupJob?.cancel()
        observeGroupJob = viewModelScope.launch {
            familyRepository.observeFamilyGroup(groupId)
                .catch { err ->
                    _uiState.update { it.copy(error = "Grup bilgisi yüklenemedi.") }
                }
                .collect { group ->
                    _uiState.update { it.copy(familyGroup = group, isLoading = false) }
                }
        }

        // Observe Members details
        observeMembersJob?.cancel()
        observeMembersJob = viewModelScope.launch {
            familyRepository.observeFamilyMembers(groupId)
                .catch { err ->
                    _uiState.update { it.copy(error = "Üye listesi yüklenemedi.") }
                }
                .collect { members ->
                    _uiState.update { it.copy(members = members.sortedByEmergencyPriority()) }
                }
        }
    }

    fun createFamilyGroup(groupName: String) {
        val user = activeUser
        if (user == null || user.uid.isBlank()) {
            _uiState.update { it.copy(error = "Lütfen giriş yapın.") }
            return
        }
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
                            actionSuccessMessage = "Aile grubu başarıyla oluşturuldu."
                        )
                    }
                    loadFamilyGroup(user.copy(familyGroupId = group.id, familyInviteCode = group.inviteCode))
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            error = err.message ?: "Grup oluşturulurken hata oluştu."
                        )
                    }
                }
        }
    }

    fun joinFamilyGroup(inviteCode: String) {
        val user = activeUser
        if (user == null || user.uid.isBlank()) {
            _uiState.update { it.copy(error = "Lütfen giriş yapın.") }
            return
        }
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
                            actionSuccessMessage = "Aile grubuna başarıyla katıldınız."
                        )
                    }
                    loadFamilyGroup(user.copy(familyGroupId = group.id))
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            error = err.message ?: "Gruba katılırken hata oluştu. Davet kodunu kontrol edin."
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
                            actionSuccessMessage = "Aile grubundan başarıyla ayrıldınız."
                        )
                    }
                    loadFamilyGroup(user.copy(familyGroupId = null, familyInviteCode = null))
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            error = err.message ?: "Gruptan ayrılırken hata oluştu."
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
                            actionSuccessMessage = "Aile grubu başarıyla silindi."
                        )
                    }
                    loadFamilyGroup(user.copy(familyGroupId = null, familyInviteCode = null))
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            error = err.message ?: "Grup silinirken hata oluştu."
                        )
                    }
                }
        }
    }

    fun clearActionSuccessMessage() {
        _uiState.update { it.copy(actionSuccessMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
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
            .thenByDescending { it.lastStatusAt ?: 0L }
    )
