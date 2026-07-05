package com.pawnsafe.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home       : BottomNavItem("home",       "Home",       Icons.Default.Home)
    object Dashboard  : BottomNavItem("dashboard",  "Dashboard",  Icons.Default.Dashboard)
    object Calculator : BottomNavItem("calculator", "Calculator", Icons.Default.Calculate)
    object Settings   : BottomNavItem("settings",   "Settings",   Icons.Default.Settings)
}