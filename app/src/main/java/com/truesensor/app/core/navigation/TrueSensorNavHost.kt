package com.truesensor.app.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.truesensor.app.feature.dashboard.DashboardScreen
import com.truesensor.app.feature.hardware.HardwareScreen
import com.truesensor.app.feature.monitor.MonitorScreen
import com.truesensor.app.feature.sensors.SensorsScreen
import com.truesensor.app.feature.sensors.detail.SensorDetailScreen
import com.truesensor.app.feature.settings.SettingsScreen

const val SENSOR_DETAIL_ROUTE = "sensor_detail/{type}"

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
            SensorsScreen(
                contentPadding = contentPadding,
                onSensorClick = { type -> navController.navigate("sensor_detail/$type") },
            )
        }
        composable(TrueSensorDestination.HARDWARE.route) {
            HardwareScreen(contentPadding = contentPadding)
        }
        composable(TrueSensorDestination.MONITOR.route) {
            MonitorScreen(contentPadding = contentPadding)
        }
        composable(TrueSensorDestination.SETTINGS.route) {
            SettingsScreen(modifier = Modifier.padding(contentPadding))
        }
        composable(
            route = SENSOR_DETAIL_ROUTE,
            arguments = listOf(navArgument("type") { type = NavType.IntType }),
        ) {
            SensorDetailScreen(contentPadding = contentPadding)
        }
    }
}
