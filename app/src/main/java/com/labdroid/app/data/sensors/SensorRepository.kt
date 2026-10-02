package com.labdroid.app.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.labdroid.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class SensorRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun getGroupedSensors(): List<SensorGroup> {
        val infos = dedupedSensorInfos()
        return infos.groupBy { SensorCategory.categoryFor(it.type) }
            .map { (category, sensors) -> SensorGroup(category, sensors.sortedBy { it.name }) }
            .sortedBy { SensorCategory.sortIndex(it.category) }
    }

    fun getSensorsByPriority(): List<SensorInfo> {
        val infos = dedupedSensorInfos()
        return infos.sortedWith(compareBy({ sensorPriority(it.type) }, { isRawVariant(it.type) }, { it.name }))
    }

    /**
     * Raw `getSensorList(TYPE_ALL)` routinely contains several physical [Sensor] instances of the
     * same type (calibrated + wake-up duplicates being the most common), plus a pile of
     * unnamed OEM-internal composite sensors that never deliver events to a normal listener. Keep
     * one representative per known type, preferring the non-wake-up variant.
     */
    private fun dedupedSensorInfos(): List<SensorInfo> =
        sensorManager.getSensorList(Sensor.TYPE_ALL)
            .map(::toSensorInfo)
            .filter { isKnownSensorType(it.type) }
            .groupBy { it.type }
            .map { (_, duplicates) -> duplicates.minBy { if (it.isWakeUpSensor) 1 else 0 } }

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

    private fun reportingModeLabel(mode: Int): String = context.getString(
        when (mode) {
            Sensor.REPORTING_MODE_CONTINUOUS -> R.string.sensor_reporting_continuous
            Sensor.REPORTING_MODE_ON_CHANGE -> R.string.sensor_reporting_on_change
            Sensor.REPORTING_MODE_ONE_SHOT -> R.string.sensor_reporting_one_shot
            Sensor.REPORTING_MODE_SPECIAL_TRIGGER -> R.string.sensor_reporting_special_trigger
            else -> R.string.cameras_unknown
        },
    )
}
