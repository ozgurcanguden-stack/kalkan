package com.kalkan.app.data.repository

import android.util.Log
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
        val earthquakes = fetchLatestEarthquakes()
            .ifEmpty { fetchFilteredEarthquakes() }
            .map { it.toDomain() }
            .filter { it.dateTime > 0L }
            .sortedByDescending { it.dateTime }
            .also { parsed ->
                Log.d(TAG, "AFAD deprem sayisi=${parsed.size}, en guncel=${parsed.firstOrNull()?.dateTime}")
                cachedEarthquakes.value = parsed
            }

        if (earthquakes.isEmpty()) {
            error("Guncel AFAD deprem verisi bulunamadi.")
        } else {
            earthquakes
        }
    }

    private suspend fun fetchLatestEarthquakes() = runCatching {
        apiService.getLatestEarthquakes()
    }.onFailure { error ->
        Log.w(TAG, "AFAD latest endpoint basarisiz: ${error.message}")
    }.getOrDefault(emptyList())

    private suspend fun fetchFilteredEarthquakes(): List<com.kalkan.app.data.remote.AfadEarthquakeDto> {
        val now = LocalDateTime.now()
        val start = now.minusDays(7).format(AFAD_DATE_FORMAT)
        val end = now.format(AFAD_DATE_FORMAT)
        return apiService.getEarthquakes(start = start, end = end)
    }

    private companion object {
        const val TAG = "AfadEarthquakeRepo"
        val AFAD_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    }
}
