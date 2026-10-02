package com.labdroid.app.data.sensors

import android.hardware.Sensor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.South
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

object SensorIconMap {
    fun iconFor(type: Int): ImageVector = when (type) {
        Sensor.TYPE_ACCELEROMETER,
        Sensor.TYPE_ACCELEROMETER_UNCALIBRATED,
        Sensor.TYPE_LINEAR_ACCELERATION,
        -> Icons.Outlined.Speed

        Sensor.TYPE_GYROSCOPE,
        Sensor.TYPE_GYROSCOPE_UNCALIBRATED,
        -> Icons.Outlined.ScreenRotation

        Sensor.TYPE_GRAVITY -> Icons.Outlined.South

        Sensor.TYPE_ROTATION_VECTOR,
        Sensor.TYPE_GAME_ROTATION_VECTOR,
        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
        Sensor.TYPE_MAGNETIC_FIELD,
        Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED,
        -> Icons.Outlined.Explore

        Sensor.TYPE_LIGHT -> Icons.Outlined.LightMode
        Sensor.TYPE_PRESSURE -> Icons.Outlined.Compress
        Sensor.TYPE_RELATIVE_HUMIDITY -> Icons.Outlined.WaterDrop
        Sensor.TYPE_AMBIENT_TEMPERATURE -> Icons.Outlined.Thermostat

        Sensor.TYPE_STEP_COUNTER -> Icons.Outlined.DirectionsWalk

        Sensor.TYPE_HEART_RATE -> Icons.Outlined.Favorite

        else -> Icons.Outlined.Sensors
    }
}
