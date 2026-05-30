package com.kalkan.app.feature.earthquakes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalkan.app.domain.model.Earthquake
import com.kalkan.app.domain.repository.EarthquakeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EarthquakeViewModel @Inject constructor(
    private val repository: EarthquakeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<EarthquakeUiState>(EarthquakeUiState.Loading)
    val uiState: StateFlow<EarthquakeUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow(EarthquakeFilter.ALL)
    val selectedFilter: StateFlow<EarthquakeFilter> = _selectedFilter.asStateFlow()

    private var latestEarthquakes: List<Earthquake> = emptyList()
    private var lastUpdatedAt: Long? = null

    init {
        observeCache()
        observeLastUpdatedAt()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            if (latestEarthquakes.isEmpty()) {
                _uiState.value = EarthquakeUiState.Loading
            } else {
                _uiState.updateRefreshing(true)
            }

            repository.refreshFromAfad()
                .onSuccess { earthquakes ->
                    latestEarthquakes = earthquakes
                    publishFilteredState()
                }
                .onFailure { error ->
                    _uiState.value = EarthquakeUiState.Error(
                        message = error.localizedMessage ?: "Deprem verileri alinamadi.",
                        cachedEarthquakes = latestEarthquakes.applyCurrentFilter(),
                        lastUpdatedAt = lastUpdatedAt,
                    )
                }
        }
    }

    fun selectFilter(filter: EarthquakeFilter) {
        _selectedFilter.value = filter
        publishFilteredState()
    }

    fun findEarthquake(earthquakeId: String): Earthquake? =
        latestEarthquakes.firstOrNull { it.id == earthquakeId }

    private fun observeCache() {
        viewModelScope.launch {
            repository.observeRecentEarthquakes().collect { earthquakes ->
                if (earthquakes.isNotEmpty()) {
                    latestEarthquakes = earthquakes
                    publishFilteredState()
                }
            }
        }
    }

    private fun observeLastUpdatedAt() {
        viewModelScope.launch {
            repository.observeLastUpdatedAt().collect { updatedAt ->
                lastUpdatedAt = updatedAt
                publishFilteredState()
            }
        }
    }

    private fun publishFilteredState() {
        val filtered = latestEarthquakes.applyCurrentFilter()
        _uiState.value = if (filtered.isEmpty()) {
            EarthquakeUiState.Empty(lastUpdatedAt = lastUpdatedAt)
        } else {
            EarthquakeUiState.Success(
                earthquakes = filtered,
                lastUpdatedAt = lastUpdatedAt,
            )
        }
    }

    private fun List<Earthquake>.applyCurrentFilter(): List<Earthquake> {
        val minMagnitude = when (_selectedFilter.value) {
            EarthquakeFilter.ALL -> 0.0
            EarthquakeFilter.M3_PLUS -> 3.0
            EarthquakeFilter.M4_PLUS -> 4.0
            EarthquakeFilter.M5_PLUS -> 5.0
        }
        return filter { it.magnitude >= minMagnitude }
    }
}

private fun MutableStateFlow<EarthquakeUiState>.updateRefreshing(isRefreshing: Boolean) {
    update { state ->
        when (state) {
            is EarthquakeUiState.Success -> state.copy(isRefreshing = isRefreshing)
            is EarthquakeUiState.Error -> state.copy(isRefreshing = isRefreshing)
            is EarthquakeUiState.Loading,
            is EarthquakeUiState.Empty,
            -> state
        }
    }
}

enum class EarthquakeFilter(val label: String) {
    ALL("Tumu"),
    M3_PLUS("3+"),
    M4_PLUS("4+"),
    M5_PLUS("5+"),
}

sealed interface EarthquakeUiState {
    val lastUpdatedAt: Long?

    data object Loading : EarthquakeUiState {
        override val lastUpdatedAt: Long? = null
    }

    data class Empty(
        override val lastUpdatedAt: Long? = null,
    ) : EarthquakeUiState

    data class Success(
        val earthquakes: List<Earthquake>,
        val isRefreshing: Boolean = false,
        override val lastUpdatedAt: Long? = null,
    ) : EarthquakeUiState

    data class Error(
        val message: String,
        val cachedEarthquakes: List<Earthquake> = emptyList(),
        val isRefreshing: Boolean = false,
        override val lastUpdatedAt: Long? = null,
    ) : EarthquakeUiState
}
