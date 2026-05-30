package com.kalkan.app.data.location

import com.kalkan.app.model.UserLocation

sealed interface LocationFetchResult {
    data class Success(val location: UserLocation) : LocationFetchResult

    data object PermissionDenied : LocationFetchResult

    data object Unavailable : LocationFetchResult
}
