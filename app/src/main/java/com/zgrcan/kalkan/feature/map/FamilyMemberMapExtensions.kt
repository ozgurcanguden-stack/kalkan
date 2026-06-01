package com.zgrcan.kalkan.feature.map

import com.zgrcan.kalkan.model.FamilyMember

fun FamilyMember.hasValidSharedLocation(): Boolean {
    val lat = lastStatusLatitude ?: return false
    val lng = lastStatusLongitude ?: return false
    if (lat == 0.0 && lng == 0.0) return false
    return lat in -90.0..90.0 && lng in -180.0..180.0
}

fun List<FamilyMember>.withSharedMapLocations(): List<FamilyMember> =
    filter { it.hasValidSharedLocation() }
