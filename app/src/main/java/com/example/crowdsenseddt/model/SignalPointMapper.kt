package com.example.crowdsenseddt.model

import com.example.crowdsenseddt.data.SignalPoint as DataSignalPoint

fun DataSignalPoint.toUi(): SignalPoint {
    return SignalPoint(
        latitude = latitude,
        longitude = longitude,
        rsrp = rsrp ?: -120.0
    )
}
