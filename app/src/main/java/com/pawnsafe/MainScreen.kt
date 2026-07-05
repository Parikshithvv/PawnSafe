package com.pawnsafe

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pawnsafe.presentation.auth.PinSetupScreen
import com.pawnsafe.presentation.auth.SettingsScreen
import com.pawnsafe.presentation.calculator.CalculatorScreen
import com.pawnsafe.presentation.dashboard.DashboardScreen
import com.pawnsafe.presentation.navigation.BottomNavItem

@Composable
fun MainScreen() {
    val bottomNavController = rememberNavController()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Dashboard,
        BottomNavItem.Calculator,
        BottomNavItem.Settings
    )
    val currentBackStack by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.Home.route) {
                PawnSafeNavGraph()
            }
            composable(BottomNavItem.Dashboard.route) {
                DashboardScreen()
            }
            composable(BottomNavItem.Calculator.route) {
                CalculatorScreen()
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen(
                    onBack = { bottomNavController.popBackStack() },
                    onSetupPin = { bottomNavController.navigate("pin_setup") }
                )
            }
            composable("pin_setup") {
                PinSetupScreen(
                    onBack = { bottomNavController.popBackStack() },
                    onSetupComplete = { bottomNavController.popBackStack() }
                )
            }
        }
    }
}