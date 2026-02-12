package com.example.crowdsenseddt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.crowdsenseddt.ui.components.InfoRow


@Composable
fun ProfileScreen(paddingValues: PaddingValues) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {

        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        InfoRow("Device ID", "e.g. 3f1c-92ab-xxxx")
        InfoRow("Operator", "MTN")
        InfoRow("Network Type", "LTE")
        InfoRow("Upload Status", "Online")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* toggle sensing later */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Disable Crowdsensing")
        }
    }
}
