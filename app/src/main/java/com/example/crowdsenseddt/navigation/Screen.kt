package com.example.crowdsenseddt.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Charts : Screen("charts")
    object Profile : Screen("profile")
    object DriveTest : Screen("drive_test")
}
