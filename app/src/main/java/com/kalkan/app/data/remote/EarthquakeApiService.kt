package com.kalkan.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface EarthquakeApiService {
    @GET("apiv2/event/latest")
    suspend fun getLatestEarthquakes(): List<AfadEarthquakeDto>

    @GET("apiv2/event/filter")
    suspend fun getEarthquakes(
        @Query("start") start: String,
        @Query("end") end: String,
        @Query("limit") limit: Int = 100,
        @Query("orderby") orderBy: String = "timedesc",
        @Query("format") format: String = "json",
    ): List<AfadEarthquakeDto>
}
