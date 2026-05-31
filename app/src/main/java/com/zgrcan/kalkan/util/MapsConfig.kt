package com.zgrcan.kalkan.util

import com.zgrcan.kalkan.BuildConfig

object MapsConfig {
    val apiKey: String = BuildConfig.MAPS_API_KEY.trim()

    val isConfigured: Boolean
        get() = apiKey.isNotBlank() && apiKey != "YOUR_MAPS_API_KEY"
}
