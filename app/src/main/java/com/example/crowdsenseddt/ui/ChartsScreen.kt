package com.example.crowdsenseddt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.crowdsenseddt.data.mockRsrpSamples

@Composable
fun ChartsScreen(paddingValues: PaddingValues) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {

        Text(
            text = "Signal Quality Over Time",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        SignalLineChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            samples = mockRsrpSamples,
            label = "RSRP (dBm)"
        )
    }
}
