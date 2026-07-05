package com.truesensor.app.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.truesensor.app.feature.dashboard.DashboardScreen
import com.truesensor.app.feature.hardware.HardwareScreen
import com.truesensor.app.feature.monitor.MonitorScreen
import com.truesensor.app.feature.sensors.SensorsScreen
import com.truesensor.app.feature.settings.SettingsScreen

@Composable
fun TrueSensorNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = TrueSensorDestination.DASHBOARD.route,
    ) {
        composable(TrueSensorDestination.DASHBOARD.route) {
            DashboardScreen(
                contentPadding = contentPadding,
                onNavigate = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
        composable(TrueSensorDestination.SENSORS.route) {
            SensorsScreen(modifier = Modifier.padding(contentPadding))
        }
        composable(TrueSensorDestination.HARDWARE.route) {
            HardwareScreen(contentPadding = contentPadding)
        }
        composable(TrueSensorDestination.MONITOR.route) {
            MonitorScreen(modifier = Modifier.padding(contentPadding))
        }
        composable(TrueSensorDestination.SETTINGS.route) {
            SettingsScreen(modifier = Modifier.padding(contentPadding))
        }
    }
}
