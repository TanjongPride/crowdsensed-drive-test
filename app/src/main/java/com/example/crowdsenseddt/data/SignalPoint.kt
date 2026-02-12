package com.example.crowdsenseddt.data

data class SignalPoint(
    val latitude: Double,
    val longitude: Double,

    // Primary signal metrics
    val rsrp: Double?,
    val rsrq: Double?,
    val sinr: Double?,
    val rssi: Double?,

    // Context (read-only, inferred)
    val networkType: String,
    val operatorName: String
)
