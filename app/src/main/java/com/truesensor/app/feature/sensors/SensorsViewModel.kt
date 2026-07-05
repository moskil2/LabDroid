package com.truesensor.app.feature.sensors

import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import com.truesensor.app.data.sensors.SensorGroup
import com.truesensor.app.data.sensors.SensorInfo
import com.truesensor.app.data.sensors.SensorReading
import com.truesensor.app.data.sensors.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SensorsViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
) : ViewModel() {
    val groupedSensors: List<SensorGroup> = sensorRepository.getGroupedSensors()

    fun observeReadings(sensorInfo: SensorInfo): Flow<SensorReading> =
        sensorRepository.observeSensorReadings(sensorInfo.sensor, SensorManager.SENSOR_DELAY_UI)
}
