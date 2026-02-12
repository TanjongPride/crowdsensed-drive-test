package com.example.crowdsenseddt.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crowdsenseddt.collectors.NetworkCollector
import com.example.crowdsenseddt.collectors.NetworkInfoData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val networkCollector = NetworkCollector(application)

    var networkData by mutableStateOf<NetworkInfoData?>(null)
        private set

    init {
        startRefreshing()
    }

    private fun startRefreshing() {
        viewModelScope.launch {
            while (true) {
                networkData = withContext(Dispatchers.IO) {
                    networkCollector.collect()
                }
                delay(2000)
            }
        }
    }
}
