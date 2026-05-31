package com.zgrcan.kalkan.model

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val provider: String = PROVIDER_FUSED,
) {
    companion object {
        const val PROVIDER_FUSED = "fused"
    }
}
