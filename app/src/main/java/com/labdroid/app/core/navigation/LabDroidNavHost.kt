package com.labdroid.app.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.labdroid.app.feature.battery.BatteryScreen
import com.labdroid.app.feature.camera.CamerasScreen
import com.labdroid.app.feature.dashboard.DashboardScreen
import com.labdroid.app.feature.gps.GpsScreen
import com.labdroid.app.feature.hardware.HardwareScreen
import com.labdroid.app.feature.monitor.MonitorScreen
import com.labdroid.app.feature.search.SearchScreen
import com.labdroid.app.feature.sensors.SensorsScreen
import com.labdroid.app.feature.sensors.detail.SensorDetailScreen
import com.labdroid.app.feature.sensors.detail.SoundLevelDetailScreen
import com.labdroid.app.feature.settings.SettingsScreen

const val SENSOR_DETAIL_ROUTE = "sensor_detail/{type}"

@Composable
fun LabDroidNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = LabDroidDestination.DASHBOARD.route,
        modifier = modifier,
    ) {
        composable(LabDroidDestination.DASHBOARD.route) {
            DashboardScreen(contentPadding = contentPadding)
        }
        composable(LabDroidDestination.SENSORS.route) {
            SensorsScreen(
                contentPadding = contentPadding,
                onSensorClick = { type -> navController.navigate("sensor_detail/$type") },
                onSoundLevelClick = { navController.navigate("sound_level") },
            )
        }
        composable(LabDroidDestination.HARDWARE.route) {
            HardwareScreen(contentPadding = contentPadding)
        }
        composable(LabDroidDestination.MONITOR.route) {
            MonitorScreen(contentPadding = contentPadding)
        }
        composable(LabDroidDestination.SETTINGS.route) {
            SettingsScreen(contentPadding = contentPadding)
        }
        composable(
            route = SENSOR_DETAIL_ROUTE,
            arguments = listOf(navArgument("type") { type = NavType.IntType }),
        ) {
            SensorDetailScreen(contentPadding = contentPadding)
        }
        composable("sound_level") {
            SoundLevelDetailScreen(contentPadding = contentPadding)
        }
        composable("gps") {
            GpsScreen(contentPadding = contentPadding)
        }
        composable("cameras") {
            CamerasScreen(contentPadding = contentPadding)
        }
        composable("battery") {
            BatteryScreen(contentPadding = contentPadding)
        }
        composable("search") {
            SearchScreen(
                contentPadding = contentPadding,
                onSensorClick = { type ->
                    navController.navigate("sensor_detail/$type") { popUpTo("search") { inclusive = true } }
                },
                onHardwareClick = {
                    navController.navigate(LabDroidDestination.HARDWARE.route) { popUpTo("search") { inclusive = true } }
                },
                onSettingsClick = {
                    navController.navigate(LabDroidDestination.SETTINGS.route) { popUpTo("search") { inclusive = true } }
                },
            )
        }
    }
}
