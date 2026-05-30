package com.kalkan.app.data.location

interface LocationRepository {
    suspend fun getCurrentLocation(hasPermission: Boolean): LocationFetchResult
}
