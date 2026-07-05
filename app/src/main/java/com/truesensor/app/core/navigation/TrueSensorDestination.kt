package com.truesensor.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class TrueSensorDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    DASHBOARD(route = "dashboard", label = "Dashboard", icon = Icons.Outlined.Dashboard),
    SENSORS(route = "sensors", label = "Sensors", icon = Icons.Outlined.Sensors),
    HARDWARE(route = "hardware", label = "Hardware", icon = Icons.Outlined.Memory),
    MONITOR(route = "monitor", label = "Monitor", icon = Icons.AutoMirrored.Outlined.ShowChart),
    SETTINGS(route = "settings", label = "Settings", icon = Icons.Outlined.Settings),
}
