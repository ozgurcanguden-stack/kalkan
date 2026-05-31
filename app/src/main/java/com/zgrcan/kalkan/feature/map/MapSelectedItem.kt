package com.zgrcan.kalkan.feature.map

import com.zgrcan.kalkan.domain.model.Earthquake
import com.zgrcan.kalkan.model.FamilyMember

sealed interface MapSelectedItem {
    val latitude: Double
    val longitude: Double

    data class EarthquakeItem(val earthquake: Earthquake) : MapSelectedItem {
        override val latitude: Double = earthquake.latitude
        override val longitude: Double = earthquake.longitude
    }

    data class FamilyItem(val member: FamilyMember) : MapSelectedItem {
        override val latitude: Double = member.lastStatusLatitude ?: 0.0
        override val longitude: Double = member.lastStatusLongitude ?: 0.0
    }
}
