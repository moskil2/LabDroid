package com.truesensor.app.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class SensorRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun getGroupedSensors(): List<SensorGroup> {
        val infos = sensorManager.getSensorList(Sensor.TYPE_ALL).map(::toSensorInfo)
        return infos.groupBy { SensorCategory.categoryFor(it.type) }
            .map { (category, sensors) -> SensorGroup(category, sensors.sortedBy { it.name }) }
            .sortedBy { SensorCategory.sortIndex(it.category) }
    }

    fun getDefaultSensor(type: Int): Sensor? = sensorManager.getDefaultSensor(type)

    fun getSensorInfo(type: Int): SensorInfo? = getDefaultSensor(type)?.let(::toSensorInfo)

    fun observeSensorReadings(sensor: Sensor, samplingPeriodUs: Int): Flow<SensorReading> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(SensorReading(event.values.clone(), event.accuracy, event.timestamp))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        val registered = sensorManager.registerListener(listener, sensor, samplingPeriodUs)
        awaitClose { if (registered) sensorManager.unregisterListener(listener) }
    }

    private fun toSensorInfo(sensor: Sensor): SensorInfo = SensorInfo(
        sensor = sensor,
        name = sensor.name,
        type = sensor.type,
        stringType = sensor.stringType,
        vendor = sensor.vendor,
        version = sensor.version,
        resolution = sensor.resolution,
        maximumRange = sensor.maximumRange,
        power = sensor.power,
        minDelayUs = sensor.minDelay,
        fifoMaxEventCount = sensor.fifoMaxEventCount,
        isWakeUpSensor = sensor.isWakeUpSensor,
        reportingMode = reportingModeLabel(sensor.reportingMode),
    )

    private fun reportingModeLabel(mode: Int): String = when (mode) {
        Sensor.REPORTING_MODE_CONTINUOUS -> "Continuous"
        Sensor.REPORTING_MODE_ON_CHANGE -> "On change"
        Sensor.REPORTING_MODE_ONE_SHOT -> "One-shot"
        Sensor.REPORTING_MODE_SPECIAL_TRIGGER -> "Special trigger"
        else -> "Unknown"
    }
}
