package com.kalkan.app.data.repository

import com.kalkan.app.domain.model.Earthquake
import com.kalkan.app.domain.repository.EarthquakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class AfadEarthquakeRepository @Inject constructor() : EarthquakeRepository {
    override fun observeRecentEarthquakes(): Flow<List<Earthquake>> = flowOf(emptyList())

    override suspend fun refreshFromAfad(): Result<Unit> = runCatching {
        // AFAD endpoint integration will be added with the remote data source layer.
    }
}
