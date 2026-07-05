package com.truesensor.app.data.sensors

import android.hardware.Sensor
import android.hardware.SensorManager
import kotlin.math.sqrt

data class SensorInfo(
    val sensor: Sensor,
    val name: String,
    val type: Int,
    val stringType: String,
    val vendor: String,
    val version: Int,
    val resolution: Float,
    val maximumRange: Float,
    val power: Float,
    val minDelayUs: Int,
    val fifoMaxEventCount: Int,
    val isWakeUpSensor: Boolean,
    val reportingMode: String,
)

data class SensorGroup(val category: String, val sensors: List<SensorInfo>)

data class SensorReading(val values: FloatArray, val accuracy: Int, val timestampNanos: Long)

enum class SensorDelayOption(val label: String, val periodUs: Int) {
    FASTEST("Fastest", SensorManager.SENSOR_DELAY_FASTEST),
    GAME("Game", SensorManager.SENSOR_DELAY_GAME),
    UI("UI", SensorManager.SENSOR_DELAY_UI),
    NORMAL("Normal", SensorManager.SENSOR_DELAY_NORMAL),
}

fun representativeValue(values: FloatArray): Float = when (values.size) {
    0 -> 0f
    1 -> values[0]
    else -> sqrt(values.sumOf { (it * it).toDouble() }).toFloat()
}

fun unitFor(type: Int): String = when (type) {
    Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_GRAVITY -> "m/s²"
    Sensor.TYPE_GYROSCOPE, Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> "rad/s"
    Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> "μT"
    Sensor.TYPE_LIGHT -> "lx"
    Sensor.TYPE_PRESSURE -> "hPa"
    Sensor.TYPE_RELATIVE_HUMIDITY -> "%"
    Sensor.TYPE_AMBIENT_TEMPERATURE -> "°C"
    Sensor.TYPE_PROXIMITY -> "cm"
    Sensor.TYPE_STEP_COUNTER -> "steps"
    Sensor.TYPE_HEART_RATE -> "bpm"
    else -> ""
}
