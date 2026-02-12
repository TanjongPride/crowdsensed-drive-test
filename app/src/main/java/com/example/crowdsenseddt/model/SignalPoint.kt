package com.example.crowdsenseddt.model

import com.example.crowdsenseddt.network.HeatmapPointDto

data class SignalPoint(
    val latitude: Double,
    val longitude: Double,
    val rsrp: Double
)

fun HeatmapPointDto.toUiModel(): SignalPoint? {
    return rsrp?.let {
        SignalPoint(
            latitude = lat,
            longitude = lon,
            rsrp = it
        )
    }
}
