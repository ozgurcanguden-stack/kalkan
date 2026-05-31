package com.zgrcan.kalkan.data.location

interface LocationRepository {
    suspend fun getCurrentLocation(hasPermission: Boolean): LocationFetchResult
}
