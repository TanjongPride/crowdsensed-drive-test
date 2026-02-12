package com.example.crowdsenseddt.network

import retrofit2.http.GET
import retrofit2.http.Query

data class HeatmapPointDto(
    val lat: Double,
    val lon: Double,
    val rsrp: Double?,
    val network_type: String?,
    val operator_name: String?
)

interface ApiService {

    @GET("measurements/heatmap")
    suspend fun getHeatmapData(
        @Query("network_type") networkType: String? = null,
        @Query("operator_name") operator: String? = null
    ): List<HeatmapPointDto>
}