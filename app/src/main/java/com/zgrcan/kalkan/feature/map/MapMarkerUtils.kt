package com.zgrcan.kalkan.feature.map

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.zgrcan.kalkan.model.SafetyStatusType
import com.zgrcan.kalkan.util.EarthquakeMagnitudeLevel
import com.zgrcan.kalkan.util.EarthquakeMagnitudeUtils
import com.zgrcan.kalkan.domain.model.Earthquake

fun Earthquake.markerHue(): Float =
    when (EarthquakeMagnitudeUtils.classify(safeMagnitude())) {
        EarthquakeMagnitudeLevel.CRITICAL -> BitmapDescriptorFactory.HUE_RED
        EarthquakeMagnitudeLevel.CAUTION -> BitmapDescriptorFactory.HUE_ORANGE
        EarthquakeMagnitudeLevel.MILD -> BitmapDescriptorFactory.HUE_YELLOW
        EarthquakeMagnitudeLevel.NORMAL -> BitmapDescriptorFactory.HUE_AZURE
    }

private fun Earthquake.safeMagnitude(): Double = EarthquakeMagnitudeUtils.safeMagnitude(magnitude)

fun SafetyStatusType?.markerHue(): Float =
    when (this) {
        SafetyStatusType.SOS -> BitmapDescriptorFactory.HUE_RED
        SafetyStatusType.NEED_HELP -> BitmapDescriptorFactory.HUE_ORANGE
        SafetyStatusType.SAFE -> BitmapDescriptorFactory.HUE_GREEN
        SafetyStatusType.SHARE_LOCATION -> BitmapDescriptorFactory.HUE_BLUE
        null -> BitmapDescriptorFactory.HUE_VIOLET
    }
