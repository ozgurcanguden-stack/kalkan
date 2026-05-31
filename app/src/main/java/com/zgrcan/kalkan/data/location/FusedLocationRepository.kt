package com.zgrcan.kalkan.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.zgrcan.kalkan.model.UserLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FusedLocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationRepository {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    override suspend fun getCurrentLocation(hasPermission: Boolean): LocationFetchResult {
        if (!hasPermission) {
            return LocationFetchResult.PermissionDenied
        }
        if (!hasLocationPermissionGranted()) {
            return LocationFetchResult.PermissionDenied
        }

        return try {
            val location = fetchBestAvailableLocation()
            if (location == null) {
                LocationFetchResult.Unavailable
            } else {
                LocationFetchResult.Success(
                    UserLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = if (location.hasAccuracy()) location.accuracy else null,
                        provider = UserLocation.PROVIDER_FUSED,
                    ),
                )
            }
        } catch (securityException: SecurityException) {
            Log.e(TAG, "Location permission missing at fetch time", securityException)
            LocationFetchResult.PermissionDenied
        } catch (error: Exception) {
            Log.e(TAG, "Failed to fetch location", error)
            LocationFetchResult.Unavailable
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchBestAvailableLocation(): android.location.Location? {
        val lastLocation = fusedClient.lastLocation.await()
        if (lastLocation != null && isUsableLocation(lastLocation)) {
            return lastLocation
        }

        val cancellationTokenSource = CancellationTokenSource()
        val currentLocation = fusedClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            cancellationTokenSource.token,
        ).await()

        return currentLocation?.takeIf { isUsableLocation(it) }
    }

    private fun isUsableLocation(location: android.location.Location): Boolean =
        location.latitude != 0.0 || location.longitude != 0.0

    private fun hasLocationPermissionGranted(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    companion object {
        private const val TAG = "FusedLocationRepository"
    }
}
