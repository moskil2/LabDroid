package com.labdroid.app.data.sensors

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.annotation.StringRes
import com.labdroid.app.R
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

enum class SensorDelayOption(@StringRes val labelRes: Int, val periodUs: Int) {
    FAST(R.string.sensor_delay_fast, SensorManager.SENSOR_DELAY_FASTEST),
    NORMAL(R.string.sensor_delay_normal, SensorManager.SENSOR_DELAY_GAME),
    SLOW(R.string.sensor_delay_slow, SensorManager.SENSOR_DELAY_NORMAL),
}

fun representativeValue(values: FloatArray): Float = when (values.size) {
    0 -> 0f
    1 -> values[0]
    else -> sqrt(values.sumOf { (it * it).toDouble() }).toFloat()
}

private val priorityOrder = listOf(
    Sensor.TYPE_LIGHT,
    Sensor.TYPE_PRESSURE,
    Sensor.TYPE_GYROSCOPE,
    Sensor.TYPE_ORIENTATION,
    Sensor.TYPE_MAGNETIC_FIELD,
    Sensor.TYPE_ACCELEROMETER,
    Sensor.TYPE_GRAVITY,
    Sensor.TYPE_LINEAR_ACCELERATION,
    Sensor.TYPE_ROTATION_VECTOR,
)

fun sensorPriority(type: Int): Int = priorityOrder.indexOf(type).let { if (it == -1) priorityOrder.size else it }

/**
 * Types this app can name and interpret. Anything outside this set is almost always an
 * OEM-internal composite/virtual sensor (e.g. Samsung's proprietary gesture or auto-brightness
 * helpers) that never delivers meaningful data to a standard SensorEventListener — those are
 * filtered out of the sensor list rather than shown as unnamed, permanently-dead rows.
 */
private val knownSensorTypes = setOf(
    Sensor.TYPE_LIGHT,
    Sensor.TYPE_PRESSURE,
    Sensor.TYPE_GYROSCOPE,
    Sensor.TYPE_GYROSCOPE_UNCALIBRATED,
    Sensor.TYPE_ORIENTATION,
    Sensor.TYPE_MAGNETIC_FIELD,
    Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED,
    Sensor.TYPE_ACCELEROMETER,
    Sensor.TYPE_ACCELEROMETER_UNCALIBRATED,
    Sensor.TYPE_GRAVITY,
    Sensor.TYPE_LINEAR_ACCELERATION,
    Sensor.TYPE_ROTATION_VECTOR,
    Sensor.TYPE_GAME_ROTATION_VECTOR,
    Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
    Sensor.TYPE_PROXIMITY,
    Sensor.TYPE_AMBIENT_TEMPERATURE,
    Sensor.TYPE_RELATIVE_HUMIDITY,
    Sensor.TYPE_HEART_RATE,
    Sensor.TYPE_STEP_COUNTER,
    Sensor.TYPE_STEP_DETECTOR,
    Sensor.TYPE_SIGNIFICANT_MOTION,
)

fun isKnownSensorType(type: Int): Boolean = type in knownSensorTypes

@StringRes
fun friendlyNameResFor(type: Int): Int = when (type) {
    Sensor.TYPE_LIGHT -> R.string.sensor_name_light
    Sensor.TYPE_PRESSURE -> R.string.sensor_name_barometer
    Sensor.TYPE_GYROSCOPE -> R.string.sensor_name_gyroscope
    Sensor.TYPE_GYROSCOPE_UNCALIBRATED -> R.string.sensor_name_gyroscope_uncalibrated
    Sensor.TYPE_ORIENTATION -> R.string.sensor_name_orientation
    Sensor.TYPE_MAGNETIC_FIELD -> R.string.sensor_name_magnetometer
    Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> R.string.sensor_name_magnetometer_uncalibrated
    Sensor.TYPE_ACCELEROMETER -> R.string.sensor_name_accelerometer
    Sensor.TYPE_ACCELEROMETER_UNCALIBRATED -> R.string.sensor_name_accelerometer_uncalibrated
    Sensor.TYPE_GRAVITY -> R.string.sensor_name_gravity
    Sensor.TYPE_LINEAR_ACCELERATION -> R.string.sensor_name_linear_acceleration
    Sensor.TYPE_ROTATION_VECTOR -> R.string.sensor_name_rotation_vector
    Sensor.TYPE_GAME_ROTATION_VECTOR -> R.string.sensor_name_rotation_vector_game
    Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> R.string.sensor_name_rotation_vector_geomagnetic
    Sensor.TYPE_PROXIMITY -> R.string.sensor_name_proximity
    Sensor.TYPE_AMBIENT_TEMPERATURE -> R.string.sensor_name_ambient_temperature
    Sensor.TYPE_RELATIVE_HUMIDITY -> R.string.sensor_name_humidity
    Sensor.TYPE_HEART_RATE -> R.string.sensor_name_heart_rate
    Sensor.TYPE_STEP_COUNTER -> R.string.sensor_name_step_counter
    Sensor.TYPE_STEP_DETECTOR -> R.string.sensor_name_step_detector
    Sensor.TYPE_SIGNIFICANT_MOTION -> R.string.sensor_name_significant_motion
    else -> R.string.sensor_name_generic
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
