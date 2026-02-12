package com.example.crowdsenseddt.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.crowdsenseddt.navigation.Screen

@Composable
fun BottomNavBar(navController: NavController) {

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {

        // Dashboard
        NavigationBarItem(
            selected = currentRoute == Screen.Dashboard.route,
            onClick = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Dashboard.route) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Dashboard") }
        )

        // Drive Test (NEW)
        NavigationBarItem(
            selected = currentRoute == Screen.DriveTest.route,
            onClick = {
                navController.navigate(Screen.DriveTest.route) {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Drive Test") },
            label = { Text("Drive") }
        )

        // Charts
        NavigationBarItem(
            selected = currentRoute == Screen.Charts.route,
            onClick = {
                navController.navigate(Screen.Charts.route) {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Charts") },
            label = { Text("Charts") }
        )

        // Profile
        NavigationBarItem(
            selected = currentRoute == Screen.Profile.route,
            onClick = {
                navController.navigate(Screen.Profile.route) {
                    launchSingleTop = true
                }
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
