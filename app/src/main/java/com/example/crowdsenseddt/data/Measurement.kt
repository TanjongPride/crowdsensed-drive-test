package com.example.crowdsenseddt.data

import java.time.Instant
import java.util.UUID

data class Measurement(
    val session_id: UUID,
    val device_id: UUID,
    val user_id: UUID,

    val timestamp: String,   // ISO-8601 string

    val network_type: String?,
    val operator_name: String?,
    val mcc: Int?,
    val mnc: Int?,

    val cell_id: Long?,
    val pci: Int?,
    val earfcn: Int?,
    val bandwidth_mhz: Int?,

    val rsrp: Float?,
    val rsrq: Float?,
    val sinr: Float?,
    val rssi: Float?,
    val cqi: Int?,
    val ta: Int?,

    val latitude: Double?,
    val longitude: Double?,
    val altitude: Double?,

    val speed: Float?,
    val heading: Float?,
    val location_accuracy: Float?,

    val is_roaming: Boolean?,
    val is_data_active: Boolean?
)
