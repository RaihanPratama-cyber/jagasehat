package com.example.jagasehat.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import com.example.jagasehat.navigation.Screen
import com.example.jagasehat.ui.theme.Green500

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, Icons.Filled.Home, "Home"),
    BottomNavItem(Screen.Family, Icons.Filled.People, "Keluarga"),
    BottomNavItem(Screen.Reminders, Icons.Filled.Notifications, "Pengingat"),
    BottomNavItem(Screen.Export, Icons.Filled.Download, "Export"),
    BottomNavItem(Screen.Articles, Icons.Filled.Book, "Artikel")
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Green500,
                    selectedTextColor = Green500,
                    indicatorColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}
