package com.labdroid.app.data.sensors

import android.hardware.Sensor

object SensorCategory {
    const val MOTION = "Motion"
    const val POSITION = "Position"
    const val ENVIRONMENTAL = "Environmental"
    const val HEALTH = "Health"
    const val OTHER = "Other"

    private val displayOrder = listOf(MOTION, POSITION, ENVIRONMENTAL, HEALTH, OTHER)

    fun sortIndex(category: String): Int {
        val index = displayOrder.indexOf(category)
        return if (index == -1) displayOrder.size else index
    }

    fun categoryFor(type: Int): String = when (type) {
        Sensor.TYPE_ACCELEROMETER,
        Sensor.TYPE_GYROSCOPE,
        Sensor.TYPE_GRAVITY,
        Sensor.TYPE_LINEAR_ACCELERATION,
        Sensor.TYPE_ROTATION_VECTOR,
        Sensor.TYPE_GAME_ROTATION_VECTOR,
        Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
        Sensor.TYPE_SIGNIFICANT_MOTION,
        Sensor.TYPE_STEP_DETECTOR,
        Sensor.TYPE_STEP_COUNTER,
        Sensor.TYPE_GYROSCOPE_UNCALIBRATED,
        Sensor.TYPE_ACCELEROMETER_UNCALIBRATED,
        -> MOTION

        Sensor.TYPE_MAGNETIC_FIELD,
        Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED,
        Sensor.TYPE_PROXIMITY,
        -> POSITION

        Sensor.TYPE_LIGHT,
        Sensor.TYPE_PRESSURE,
        Sensor.TYPE_RELATIVE_HUMIDITY,
        Sensor.TYPE_AMBIENT_TEMPERATURE,
        -> ENVIRONMENTAL

        Sensor.TYPE_HEART_RATE,
        -> HEALTH

        else -> OTHER
    }
}
