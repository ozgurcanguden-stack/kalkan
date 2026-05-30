package com.kalkan.app.data.repository

import com.kalkan.app.data.remote.EarthquakeApiService
import com.kalkan.app.domain.model.Earthquake
import com.kalkan.app.domain.repository.EarthquakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AfadEarthquakeRepository @Inject constructor(
    private val apiService: EarthquakeApiService,
) : EarthquakeRepository {
    private val cachedEarthquakes = MutableStateFlow<List<Earthquake>>(emptyList())

    override fun observeRecentEarthquakes(): Flow<List<Earthquake>> =
        cachedEarthquakes.asStateFlow()

    override suspend fun refreshFromAfad(): Result<List<Earthquake>> = runCatching {
        val now = LocalDateTime.now()
        val start = now.minusDays(7).format(AFAD_DATE_FORMAT)
        val end = now.format(AFAD_DATE_FORMAT)

        apiService
            .getEarthquakes(start = start, end = end)
            .map { it.toDomain() }
            .sortedByDescending { it.dateTime }
            .also { cachedEarthquakes.value = it }
    }

    private companion object {
        val AFAD_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    }
}
