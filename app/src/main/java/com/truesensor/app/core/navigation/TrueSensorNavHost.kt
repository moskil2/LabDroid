package com.truesensor.app.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.truesensor.app.feature.camera.CamerasScreen
import com.truesensor.app.feature.dashboard.DashboardScreen
import com.truesensor.app.feature.gps.GpsScreen
import com.truesensor.app.feature.hardware.HardwareScreen
import com.truesensor.app.feature.monitor.MonitorScreen
import com.truesensor.app.feature.search.SearchScreen
import com.truesensor.app.feature.sensors.SensorsScreen
import com.truesensor.app.feature.sensors.detail.SensorDetailScreen
import com.truesensor.app.feature.settings.SettingsScreen

const val SENSOR_DETAIL_ROUTE = "sensor_detail/{type}"
private val TAB_ROUTES = TrueSensorDestination.entries.map { it.route }.toSet()

@Composable
fun TrueSensorNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TrueSensorDestination.DASHBOARD.route,
        modifier = modifier,
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
            HardwareScreen(
                contentPadding = contentPadding,
                onNavigateToCameras = { navController.navigate("cameras") },
            )
        }
        composable(TrueSensorDestination.MONITOR.route) {
            MonitorScreen(contentPadding = contentPadding)
        }
        composable(TrueSensorDestination.SETTINGS.route) {
            SettingsScreen(contentPadding = contentPadding)
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
        composable("cameras") {
            CamerasScreen(contentPadding = contentPadding)
        }
        composable("search") {
            SearchScreen(
                contentPadding = contentPadding,
                onSensorClick = { type ->
                    navController.navigate("sensor_detail/$type") { popUpTo("search") { inclusive = true } }
                },
                onHardwareClick = {
                    navController.navigate(TrueSensorDestination.HARDWARE.route) { popUpTo("search") { inclusive = true } }
                },
                onSettingsClick = {
                    navController.navigate(TrueSensorDestination.SETTINGS.route) { popUpTo("search") { inclusive = true } }
                },
            )
        }
    }
}
