package com.kalkan.app.domain.repository

import com.kalkan.app.domain.model.Earthquake
import kotlinx.coroutines.flow.Flow

interface EarthquakeRepository {
    fun observeRecentEarthquakes(): Flow<List<Earthquake>>
    fun observeLastUpdatedAt(): Flow<Long?>
    suspend fun refreshFromAfad(): Result<List<Earthquake>>
}
