package com.example.crowdsenseddt.collectors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.*
import android.os.Build
import androidx.core.content.ContextCompat

data class NetworkInfoData(
    val networkType: String,
    val operatorName: String?,
    val rsrp: Int?,      // LTE / 5G
    val rsrq: Int?,      // LTE / 5G
    val sinr: Int?,      // LTE / 5G
    val rscp: Int?,      // 3G
    val rssi: Int?,      // 2G
    val cellId: Long?,
    val mcc: String?,
    val mnc: String?
)

class NetworkCollector(private val context: Context) {

    fun collect(): NetworkInfoData {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            return NetworkInfoData(
                "Permission Denied",
                null, null, null, null,
                null, null,
                null, null, null
            )
        }

        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val operatorName = telephonyManager.networkOperatorName

        val networkType = when (telephonyManager.dataNetworkType) {
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_HSPAP,
            TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_GPRS -> "2G"
            else -> "Unknown"
        }

        var rsrp: Int? = null
        var rsrq: Int? = null
        var sinr: Int? = null
        var rscp: Int? = null
        var rssi: Int? = null
        var cellId: Long? = null
        var mcc: String? = null
        var mnc: String? = null

        val cellInfoList = telephonyManager.allCellInfo

        if (!cellInfoList.isNullOrEmpty()) {

            // 🔴 CRITICAL FIX: Use REGISTERED cell, not index 0
            val servingCell = cellInfoList.firstOrNull { it.isRegistered }

            when (servingCell) {

                is CellInfoLte -> {
                    val signal = servingCell.cellSignalStrength
                    rsrp = signal.rsrp
                    rsrq = signal.rsrq
                    sinr = signal.rssnr
                    cellId = servingCell.cellIdentity.ci.toLong()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        mcc = servingCell.cellIdentity.mccString
                        mnc = servingCell.cellIdentity.mncString
                    } else {
                        @Suppress("DEPRECATION")
                        mcc = servingCell.cellIdentity.mcc.toString()
                        @Suppress("DEPRECATION")
                        mnc = servingCell.cellIdentity.mnc.toString()
                    }
                }

                is CellInfoNr -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val signal = servingCell.cellSignalStrength as CellSignalStrengthNr
                        val identity = servingCell.cellIdentity as CellIdentityNr

                        rsrp = signal.ssRsrp
                        rsrq = signal.ssRsrq
                        sinr = signal.ssSinr
                        cellId = identity.nci
                        mcc = identity.mccString
                        mnc = identity.mncString
                    }
                }

                is CellInfoWcdma -> {
                    val signal = servingCell.cellSignalStrength
                    rscp = signal.dbm   // 3G strength
                    cellId = servingCell.cellIdentity.cid.toLong()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        mcc = servingCell.cellIdentity.mccString
                        mnc = servingCell.cellIdentity.mncString
                    }
                }

                is CellInfoGsm -> {
                    val signal = servingCell.cellSignalStrength
                    rssi = signal.dbm   // 2G strength
                    cellId = servingCell.cellIdentity.cid.toLong()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        mcc = servingCell.cellIdentity.mccString
                        mnc = servingCell.cellIdentity.mncString
                    }
                }
            }
        }

        return NetworkInfoData(
            networkType,
            operatorName,
            rsrp,
            rsrq,
            sinr,
            rscp,
            rssi,
            cellId,
            mcc,
            mnc
        )
    }
}