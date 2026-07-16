package com.example.jagasehat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.jagasehat.ui.screens.*
import com.example.jagasehat.ui.theme.JagaSehatTheme
import com.example.jagasehat.viewmodel.AppViewModel
import com.example.jagasehat.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JagaSehatTheme {
                JagaSehatApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JagaSehatApp() {
    val context = LocalContext.current
    val viewModel: AppViewModel = viewModel(factory = AppViewModelFactory(context = context))
    val state by viewModel.state.collectAsState()

    var hasSeenTutorial by remember { mutableStateOf(!viewModel.checkIsFirstLaunch(context)) }

    if (state.currentUser == null) {
        if (!hasSeenTutorial) {
            OnboardingScreen(onFinish = {
                viewModel.completeOnboarding(context)
                hasSeenTutorial = true
            })
        } else {
            AuthScreen(viewModel = viewModel)
        }
    } else {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

        val isUserA = state.currentUser?.role == "User Biasa"

        Scaffold(
            containerColor = Color(0xFFF8FAFC),
            topBar = {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF10B981),
                                modifier = Modifier.size(46.dp),
                                shadowElevation = 4.dp
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Logo",
                                    tint = Color.White,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "JagaSehat",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "Pantau Kesehatan Keluarga",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.size(44.dp)
                        ) {
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifikasi",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            },

            bottomBar = {
                val userRole = state.currentUser?.role

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                ) {
                    Surface(
                        shadowElevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            modifier = Modifier.height(72.dp),
                            windowInsets = WindowInsets(0, 0, 0, 0)
                        ) {
                            NavigationBarItem(
                                selected = currentRoute == "dashboard",
                                onClick = { navController.navigate("dashboard") },
                                icon = { Icon(Icons.Default.AddCircle, contentDescription = "Dashboard") },
                                label = { Text(if (userRole == "Admin") "Dashboard" else "Input", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF10B981), unselectedIconColor = Color(0xFF94A3B8), selectedTextColor = Color(0xFF10B981), unselectedTextColor = Color(0xFF94A3B8), indicatorColor = Color(0xFFDCFCE7))
                            )

                            if (userRole == "Admin") {
                                NavigationBarItem(
                                    selected = currentRoute == "export",
                                    onClick = { navController.navigate("export") },
                                    icon = { Icon(Icons.Default.FileDownload, contentDescription = "Eksport") },
                                    label = { Text("Eksport", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF10B981), unselectedIconColor = Color(0xFF94A3B8), selectedTextColor = Color(0xFF10B981), unselectedTextColor = Color(0xFF94A3B8), indicatorColor = Color(0xFFDCFCE7))
                                )
                            }

                            if (userRole != "Admin") {
                                NavigationBarItem(
                                    selected = currentRoute == "family",
                                    onClick = { navController.navigate("family") },
                                    icon = { Icon(Icons.Default.People, contentDescription = "Keluarga") },
                                    label = { Text("Keluarga", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF10B981), unselectedIconColor = Color(0xFF94A3B8), selectedTextColor = Color(0xFF10B981), unselectedTextColor = Color(0xFF94A3B8), indicatorColor = Color(0xFFDCFCE7))
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "reminders",
                                    onClick = { navController.navigate("reminders") },
                                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Pengingat") },
                                    label = { Text("Pengingat", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF10B981), unselectedIconColor = Color(0xFF94A3B8), selectedTextColor = Color(0xFF10B981), unselectedTextColor = Color(0xFF94A3B8), indicatorColor = Color(0xFFDCFCE7))
                                )
                            }

                            NavigationBarItem(
                                selected = currentRoute == "articles",
                                onClick = { navController.navigate("articles") },
                                icon = { Icon(Icons.Default.MenuBook, contentDescription = "Artikel") },
                                label = { Text("Artikel", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF10B981), unselectedIconColor = Color(0xFF94A3B8), selectedTextColor = Color(0xFF10B981), unselectedTextColor = Color(0xFF94A3B8), indicatorColor = Color(0xFFDCFCE7))
                            )

                            NavigationBarItem(
                                selected = currentRoute == "profile",
                                onClick = { navController.navigate("profile") },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                                label = { Text("Profil", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF10B981), unselectedIconColor = Color(0xFF94A3B8), selectedTextColor = Color(0xFF10B981), unselectedTextColor = Color(0xFF94A3B8), indicatorColor = Color(0xFFDCFCE7))
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            NavHost(navController = navController, startDestination = "dashboard", modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                composable("dashboard") { DashboardScreen(viewModel = viewModel) }
                composable("family") { FamilyScreen(viewModel = viewModel) }
                composable("reminders") { RemindersScreen(viewModel = viewModel) }
                composable(route = "export") { ExportScreen(viewModel = viewModel) }
                composable("articles") { ArticlesScreen(viewModel = viewModel) }
                composable("profile") { ProfileScreen(viewModel = viewModel) }
            }
        }
    }
}
