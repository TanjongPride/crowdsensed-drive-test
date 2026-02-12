package com.example.crowdsenseddt.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.compose.rememberNavController
import com.example.crowdsenseddt.ui.ChartsScreen
import com.example.crowdsenseddt.ui.DashboardScreen
import com.example.crowdsenseddt.ui.ProfileScreen
import com.example.crowdsenseddt.navigation.Screen
import com.example.crowdsenseddt.ui.DriveTestScreen



@Composable
fun AppNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                paddingValues = paddingValues,
                onStartDrive = {
                    navController.navigate(Screen.DriveTest.route)
                }
            )
        }

        composable(Screen.Charts.route) {
            ChartsScreen(paddingValues)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(paddingValues)
        }
        composable(Screen.DriveTest.route) {
            DriveTestScreen(paddingValues)
        }
    }
}
