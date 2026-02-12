package com.example.crowdsenseddt.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SignalParameter(val label: String) {
    RSRP("RSRP"),
    RSRQ("RSRQ"),
    SINR("SINR"),
    RSSI("RSSI")
}

@Composable
fun ParameterSelector(
    selected: SignalParameter,
    onSelected: (SignalParameter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SignalParameter.values().forEach { param ->
            FilterChip(
                selected = param == selected,
                onClick = { onSelected(param) },
                label = { Text(param.label) }
            )
        }
    }
}

@Composable
fun HeatmapLegend(parameter: SignalParameter) {

    val legendItems = when (parameter) {
        SignalParameter.RSRP -> listOf(
            "Poor" to Color(0xFFD32F2F),
            "Fair" to Color(0xFFFBC02D),
            "Good" to Color(0xFF388E3C)
        )
        SignalParameter.RSRQ -> listOf(
            "Bad" to Color.Red,
            "OK" to Color.Yellow,
            "Excellent" to Color.Green
        )
        SignalParameter.SINR -> listOf(
            "Low" to Color(0xFFB71C1C),
            "Medium" to Color(0xFFF57F17),
            "High" to Color(0xFF1B5E20)
        )
        SignalParameter.RSSI -> listOf(
            "Weak" to Color.Red,
            "Normal" to Color.Yellow,
            "Strong" to Color.Green
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        // Color bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            legendItems.forEach { (_, color) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            legendItems.forEach { (label, _) ->
                Text(
                    text = label,
                    fontSize = 12.sp
                )
            }
        }
    }
}

