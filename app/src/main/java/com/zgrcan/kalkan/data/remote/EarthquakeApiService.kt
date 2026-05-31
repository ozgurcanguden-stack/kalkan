package com.zgrcan.kalkan.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface EarthquakeApiService {
    @GET("apiv2/event/filter")
    suspend fun getEarthquakes(
        @Query("start") start: String,
        @Query("end") end: String,
        @Query("limit") limit: Int = 500,
        @Query("offset") offset: Int = 0,
        @Query("orderby") orderBy: String = "timedesc",
        @Query("format") format: String = "json",
    ): List<AfadEarthquakeDto>
}
