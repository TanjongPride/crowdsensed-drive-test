package com.example.crowdsenseddt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import com.example.crowdsenseddt.ui.LoginScreen
import androidx.compose.material3.Scaffold
import androidx.navigation.compose.rememberNavController
import com.example.crowdsenseddt.navigation.AppNavGraph
import com.example.crowdsenseddt.ui.BottomNavBar
import android.Manifest
import androidx.core.app.ActivityCompat



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        org.osmdroid.config.Configuration.getInstance().userAgentValue =
            packageName
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            ),
            1
        )


        setContent {
            val navController = rememberNavController()

            Scaffold(
                bottomBar = {
                    BottomNavBar(navController = navController)
                }
            ) { paddingValues ->
                AppNavGraph(
                    navController = navController,
                    paddingValues = paddingValues
                )
            }
        }
    }
}

