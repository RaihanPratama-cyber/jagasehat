package com.example.jagasehat.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Family : Screen("family")
    object Reminders : Screen("reminders")
    object Export : Screen("export")
    object Articles : Screen("articles")
}
