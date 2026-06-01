package com.zgrcan.kalkan.feature.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.zgrcan.kalkan.domain.model.Earthquake
import com.zgrcan.kalkan.feature.earthquakes.hasValidCoordinates
import com.zgrcan.kalkan.model.FamilyMember
import com.zgrcan.kalkan.model.SafetyStatusType
import com.zgrcan.kalkan.util.formattedMagnitude

private val DEFAULT_CENTER = LatLng(39.9334, 32.8597)
private const val DEFAULT_ZOOM = 5.5f
private const val SINGLE_MEMBER_ZOOM = 13.5f
private const val BOUNDS_PADDING_PX = 120

@Composable
fun KalkanGoogleMapContent(
    earthquakes: List<Earthquake>,
    familyMembers: List<FamilyMember>,
    showEarthquakeMarkers: Boolean,
    showFamilyMarkers: Boolean,
    selectedItem: MapSelectedItem?,
    onMarkerClick: (MapSelectedItem) -> Unit,
    familyFocusRequestKey: Int = 0,
    locatableFamilyMembers: List<FamilyMember> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(DEFAULT_CENTER, DEFAULT_ZOOM)
    }

    LaunchedEffect(familyFocusRequestKey, locatableFamilyMembers) {
        if (familyFocusRequestKey == 0) return@LaunchedEffect
        val positions = locatableFamilyMembers.map {
            LatLng(it.lastStatusLatitude!!, it.lastStatusLongitude!!)
        }
        when (positions.size) {
            0 -> Unit
            1 -> {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(positions.first(), SINGLE_MEMBER_ZOOM),
                )
            }
            else -> {
                val builder = LatLngBounds.builder()
                positions.forEach { builder.include(it) }
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(builder.build(), BOUNDS_PADDING_PX),
                )
            }
        }
    }

    LaunchedEffect(selectedItem) {
        val item = selectedItem ?: return@LaunchedEffect
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(item.latitude, item.longitude),
                9f,
            ),
        )
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = false),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
        ),
    ) {
        if (showEarthquakeMarkers) {
            earthquakes
                .filter { it.hasValidCoordinates() }
                .forEach { earthquake ->
                    val position = LatLng(earthquake.latitude, earthquake.longitude)
                    Marker(
                        state = MarkerState(position = position),
                        title = earthquake.location.ifBlank { "Deprem" },
                        snippet = earthquake.formattedMagnitude(),
                        icon = BitmapDescriptorFactory.defaultMarker(earthquake.markerHue()),
                        onClick = {
                            onMarkerClick(MapSelectedItem.EarthquakeItem(earthquake))
                            true
                        },
                    )
                }
        }

        if (showFamilyMarkers) {
            familyMembers
                .filter { it.hasValidSharedLocation() }
                .forEach { member ->
                    val latitude = member.lastStatusLatitude ?: return@forEach
                    val longitude = member.lastStatusLongitude ?: return@forEach
                    val position = LatLng(latitude, longitude)
                    val statusType = SafetyStatusType.from(member.lastStatusType)
                    Marker(
                        state = MarkerState(position = position),
                        title = member.displayName.ifBlank { "Aile üyesi" },
                        snippet = statusType.familyMapStatusLabel(),
                        icon = BitmapDescriptorFactory.defaultMarker(statusType.markerHue()),
                        onClick = {
                            onMarkerClick(MapSelectedItem.FamilyItem(member))
                            true
                        },
                    )
                }
        }
    }
}
