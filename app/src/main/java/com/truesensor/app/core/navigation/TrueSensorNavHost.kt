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
import com.truesensor.app.feature.gps.GpsScreen
import com.truesensor.app.feature.hardware.HardwareScreen
import com.truesensor.app.feature.monitor.MonitorScreen
import com.truesensor.app.feature.sensors.SensorsScreen
import com.truesensor.app.feature.sensors.detail.SensorDetailScreen
import com.truesensor.app.feature.settings.SettingsScreen

const val SENSOR_DETAIL_ROUTE = "sensor_detail/{type}"
private val TAB_ROUTES = TrueSensorDestination.entries.map { it.route }.toSet()

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
                onNavigate = { route ->
                    if (route in TAB_ROUTES) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else {
                        navController.navigate(route)
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
        composable("gps") {
            GpsScreen(contentPadding = contentPadding)
        }
    }
}
