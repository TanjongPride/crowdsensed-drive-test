package com.example.crowdsenseddt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crowdsenseddt.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.crowdsenseddt.model.SignalPoint
import com.example.crowdsenseddt.model.toUiModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.example.crowdsenseddt.ui.SignalHeatmapOSM



@Composable
fun DashboardScreen(
    paddingValues: PaddingValues,
    onStartDrive: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val networkData = viewModel.networkData
    val signalPoints by viewModel.livePoints.collectAsState()

    val rsrp = networkData?.rsrp?.let { "$it dBm" } ?: "-"
    val rsrq = networkData?.rsrq?.let { "$it dB" } ?: "-"
    val sinr = networkData?.sinr?.let { "$it dB" } ?: "-"
    val networkType = networkData?.networkType ?: "-"


    // ----------------------------
    // State
    // ----------------------------


    var selectedParameter by remember {
        mutableStateOf(SignalParameter.RSRP)
    }

    // ----------------------------
    // Fetch backend data
    // ----------------------------


    // ----------------------------
    // UI
    // ----------------------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {

        // Top Bar
        Text(
            text = "Drive Test Dashboard",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // KPI Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            KpiCard("RSRP", rsrp)
            KpiCard("RSRQ", rsrq)
        }

        Spacer(modifier = Modifier.height(12.dp))

        ParameterSelector(
            selected = selectedParameter,
            onSelected = { selectedParameter = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            KpiCard("SINR", sinr)
            KpiCard("Network", networkType)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Drive Test Controls
        Button(
            onClick = onStartDrive,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("GO TO DRIVE TEST")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ----------------------------
        // Map + Heatmap (LIVE DATA)
        // ----------------------------
        SignalHeatmapOSM(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            signalPoints = signalPoints,
            parameter = selectedParameter
        )

        Spacer(modifier = Modifier.height(8.dp))

        HeatmapLegend(parameter = selectedParameter)
    }
}

@Composable
fun KpiCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(90.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
