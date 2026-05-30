package com.kalkan.app.data.repository

import android.util.Log
import com.kalkan.app.data.remote.AfadEarthquakeDto
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
    private val lastUpdatedAt = MutableStateFlow<Long?>(null)

    override fun observeRecentEarthquakes(): Flow<List<Earthquake>> =
        cachedEarthquakes.asStateFlow()

    override fun observeLastUpdatedAt(): Flow<Long?> = lastUpdatedAt.asStateFlow()

    override suspend fun refreshFromAfad(): Result<List<Earthquake>> = runCatching {
        val now = LocalDateTime.now()
        val start = now.minusDays(7).format(AFAD_DATE_FORMAT)
        val end = now.format(AFAD_DATE_FORMAT)

        val earthquakes = fetchAllPages(start = start, end = end)
            .distinctBy { it.eventId?.takeIf { id -> id.isNotBlank() } ?: "${it.date}_${it.latitude}_${it.longitude}" }
            .map { it.toDomain() }
            .filter { it.dateTime > 0L }
            .sortedByDescending { it.dateTime }
            .also { parsed ->
                Log.d(
                    TAG,
                    "AFAD deprem sayisi=${parsed.size}, en guncel=${parsed.firstOrNull()?.dateTime}, aralik=$start..$end",
                )
                cachedEarthquakes.value = parsed
                lastUpdatedAt.value = System.currentTimeMillis()
            }

        if (earthquakes.isEmpty()) {
            error("Guncel AFAD deprem verisi bulunamadi.")
        } else {
            earthquakes
        }
    }

    private suspend fun fetchAllPages(start: String, end: String): List<AfadEarthquakeDto> {
        val merged = mutableListOf<AfadEarthquakeDto>()
        var offset = 0

        while (offset <= MAX_OFFSET) {
            val page = apiService.getEarthquakes(
                start = start,
                end = end,
                limit = PAGE_SIZE,
                offset = offset,
            )
            if (page.isEmpty()) break

            merged.addAll(page)
            if (page.size < PAGE_SIZE) break
            offset += page.size
        }

        return merged
    }

    private companion object {
        const val TAG = "AfadEarthquakeRepo"
        const val PAGE_SIZE = 500
        const val MAX_OFFSET = 5_000
        val AFAD_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    }
}
