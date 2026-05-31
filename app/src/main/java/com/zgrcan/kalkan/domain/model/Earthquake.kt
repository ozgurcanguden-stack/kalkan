package com.zgrcan.kalkan.domain.model

data class Earthquake(
    val id: String = "",
    val title: String = "",
    val location: String = "",
    val magnitude: Double = 0.0,
    val depth: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val dateTime: Long = 0L,
    val source: String = "AFAD",
)
