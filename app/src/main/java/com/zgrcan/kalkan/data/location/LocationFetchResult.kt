package com.zgrcan.kalkan.data.location

import com.zgrcan.kalkan.model.UserLocation

sealed interface LocationFetchResult {
    data class Success(val location: UserLocation) : LocationFetchResult

    data object PermissionDenied : LocationFetchResult

    data object Unavailable : LocationFetchResult
}
