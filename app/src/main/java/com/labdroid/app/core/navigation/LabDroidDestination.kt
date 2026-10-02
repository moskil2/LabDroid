package com.labdroid.app.core.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.labdroid.app.R

enum class LabDroidDestination(
    val route: String,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
) {
    DASHBOARD(route = "dashboard", labelRes = R.string.nav_dashboard, iconRes = R.drawable.ic_ph_dashboard),
    HARDWARE(route = "hardware", labelRes = R.string.nav_hardware, iconRes = R.drawable.ic_ph_hardware),
    SENSORS(route = "sensors", labelRes = R.string.nav_sensors, iconRes = R.drawable.ic_ph_sensors),
    MONITOR(route = "monitor", labelRes = R.string.nav_monitor, iconRes = R.drawable.ic_ph_monitor),
    SETTINGS(route = "settings", labelRes = R.string.nav_settings, iconRes = R.drawable.ic_ph_settings),
}
