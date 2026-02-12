package com.example.crowdsenseddt.collectors

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.*

data class LocationData(
    val latitude: Double?,
    val longitude: Double?,
    val speed: Float?,
    val altitude: Double?
)

class LocationCollector(context: Context) {

    private val fusedClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun collect(callback: (LocationData) -> Unit) {

        fusedClient.lastLocation.addOnSuccessListener { location: Location? ->

            if (location != null) {
                callback(
                    LocationData(
                        location.latitude,
                        location.longitude,
                        location.speed,
                        location.altitude
                    )
                )
            } else {
                callback(LocationData(null, null, null, null))
            }
        }
    }
}
