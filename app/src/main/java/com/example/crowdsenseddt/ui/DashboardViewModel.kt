package com.example.crowdsenseddt.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crowdsenseddt.collectors.LocationCollector
import com.example.crowdsenseddt.collectors.NetworkCollector
import com.example.crowdsenseddt.collectors.NetworkInfoData
import com.example.crowdsenseddt.data.SignalPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val networkCollector = NetworkCollector(application)
    private val locationCollector = LocationCollector(application)

    private val _livePoints = MutableStateFlow<List<SignalPoint>>(emptyList())
    val livePoints: StateFlow<List<SignalPoint>> = _livePoints

    var networkData by mutableStateOf<NetworkInfoData?>(null)
        private set

    init {
        startLiveCollection()
    }

    private fun startLiveCollection() {
        viewModelScope.launch {
            while (true) {

                val network = networkCollector.collect()
                networkData = network

                locationCollector.collect { location ->

                    if (location.latitude != null &&
                        location.longitude != null
                    ) {

                        val newPoint = SignalPoint(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            rsrp = network.rsrp?.toDouble(),
                            rsrq = network.rsrq?.toDouble(),
                            sinr = network.sinr?.toDouble(),
                            rssi = network.rssi?.toDouble(),
                            networkType = network.networkType ?: "Unknown",
                            operatorName = network.operatorName ?: "Unknown"
                        )

                        _livePoints.value = _livePoints.value + newPoint
                    }
                }

                delay(3000)
            }
        }
    }
}
