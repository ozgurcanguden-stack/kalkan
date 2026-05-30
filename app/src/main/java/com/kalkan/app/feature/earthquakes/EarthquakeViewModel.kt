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

    init {
        observeCache()
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
                    )
                }
        }
    }

    fun selectFilter(filter: EarthquakeFilter) {
        _selectedFilter.value = filter
        publishFilteredState()
    }

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

    private fun publishFilteredState() {
        val filtered = latestEarthquakes.applyCurrentFilter()
        _uiState.value = if (filtered.isEmpty()) {
            EarthquakeUiState.Empty
        } else {
            EarthquakeUiState.Success(earthquakes = filtered)
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
            EarthquakeUiState.Empty,
            EarthquakeUiState.Loading,
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
    data object Loading : EarthquakeUiState
    data object Empty : EarthquakeUiState
    data class Success(
        val earthquakes: List<Earthquake>,
        val isRefreshing: Boolean = false,
    ) : EarthquakeUiState
    data class Error(
        val message: String,
        val cachedEarthquakes: List<Earthquake> = emptyList(),
        val isRefreshing: Boolean = false,
    ) : EarthquakeUiState
}
