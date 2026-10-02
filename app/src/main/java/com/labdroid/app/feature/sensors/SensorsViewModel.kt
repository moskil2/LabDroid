package com.labdroid.app.feature.sensors

import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import com.labdroid.app.data.sensors.GsmSignalRepository
import com.labdroid.app.data.sensors.SensorInfo
import com.labdroid.app.data.sensors.SensorReading
import com.labdroid.app.data.sensors.SensorRepository
import com.labdroid.app.data.sensors.SoundLevelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SensorsViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val soundLevelRepository: SoundLevelRepository,
    private val gsmSignalRepository: GsmSignalRepository,
) : ViewModel() {
    val sensors: List<SensorInfo> = sensorRepository.getSensorsByPriority()

    fun observeReadings(sensorInfo: SensorInfo): Flow<SensorReading> =
        sensorRepository.observeSensorReadings(sensorInfo.sensor, SensorManager.SENSOR_DELAY_UI)

    fun observeSoundLevel(): Flow<Float> = soundLevelRepository.observeDecibels(intervalMs = 200L)

    fun observeGsmSignal(): Flow<Float> = gsmSignalRepository.observeSignalDbm()
}
