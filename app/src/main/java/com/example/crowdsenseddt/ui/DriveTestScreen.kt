package com.example.crowdsenseddt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crowdsenseddt.collectors.NetworkCollector
import com.example.crowdsenseddt.collectors.LocationCollector
import com.example.crowdsenseddt.collectors.LocationData
import com.example.crowdsenseddt.collectors.NetworkInfoData
import com.example.crowdsenseddt.ui.components.InfoRow
import com.example.crowdsenseddt.ui.components.SectionTitle
import kotlinx.coroutines.delay

@Composable
fun DriveTestScreen(paddingValues: PaddingValues) {

    val context = LocalContext.current

    val networkCollector = remember { NetworkCollector(context) }
    val locationCollector = remember { LocationCollector(context) }

    var isRunning by remember { mutableStateOf(false) }

    var networkData by remember { mutableStateOf<NetworkInfoData?>(null) }
    var locationData by remember { mutableStateOf<LocationData?>(null) }

    // ----------------------------
    // DATA COLLECTION LOOP
    // ----------------------------
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (isRunning) {

                networkData = networkCollector.collect()

                locationCollector.collect {
                    locationData = it
                }

                delay(2000)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            text = "Drive Test",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ----------------------------
        // START / STOP CONTROLS
        // ----------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { isRunning = true },
                enabled = !isRunning
            ) {
                Text("START")
            }

            Button(
                onClick = { isRunning = false },
                enabled = isRunning,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("STOP")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ----------------------------
        // NETWORK INFO
        // ----------------------------
        SectionTitle("Network Info")

        InfoRow("Network Type", networkData?.networkType ?: "—")
        InfoRow("RSRP", networkData?.rsrp?.let { "$it dBm" } ?: "—")
        InfoRow("RSRQ", networkData?.rsrq?.let { "$it dB" } ?: "—")
        InfoRow("SINR", networkData?.sinr?.let { "$it dB" } ?: "—")

        Spacer(modifier = Modifier.height(16.dp))

        // ----------------------------
        // CELL INFO
        // ----------------------------
        SectionTitle("Cell Info")
        InfoRow("Operator", networkData?.operatorName ?: "-")
        InfoRow("RSRP", networkData?.rsrp?.toString() ?: "-")
        InfoRow("RSRQ", networkData?.rsrq?.toString() ?: "-")
        InfoRow("SINR", networkData?.sinr?.toString() ?: "-")
        InfoRow("RSCP (3G)", networkData?.rscp?.toString() ?: "-")
        InfoRow("RSSI (2G)", networkData?.rssi?.toString() ?: "-")
        InfoRow("Cell ID", networkData?.cellId?.toString() ?: "-")
        InfoRow("MCC", networkData?.mcc ?: "-")
        InfoRow("MNC", networkData?.mnc ?: "-")


        Spacer(modifier = Modifier.height(16.dp))

        // ----------------------------
        // LOCATION INFO
        // ----------------------------
        SectionTitle("Location Info")

        InfoRow("Latitude", locationData?.latitude?.toString() ?: "—")
        InfoRow("Longitude", locationData?.longitude?.toString() ?: "—")
        InfoRow("Speed", locationData?.speed?.let { "$it m/s" } ?: "—")
        InfoRow("Altitude", locationData?.altitude?.let { "$it m" } ?: "—")
    }
}