package com.truesensor.app.feature.gps

import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truesensor.app.data.location.LocationRepository
import com.truesensor.app.data.sensors.SensorRepository
import com.truesensor.app.data.sensors.representativeValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GpsViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val sensorRepository: SensorRepository,
) : ViewModel() {

    private val _locationState = MutableStateFlow(LocationUiState())
    val locationState: StateFlow<LocationUiState> = _locationState.asStateFlow()

    private val _compassState = MutableStateFlow(CompassUiState())
    val compassState: StateFlow<CompassUiState> = _compassState.asStateFlow()

    private var lastLocation: Location? = null

    init {
        viewModelScope.launch {
            locationRepository.observeLocation().collect { location ->
                lastLocation = location
                _locationState.update {
                    it.copy(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        altitude = location.altitude,
                        speed = location.speed,
                        bearing = location.bearing,
                        accuracyHorizontal = location.accuracy,
                        accuracyVertical = if (location.hasVerticalAccuracy()) location.verticalAccuracyMeters else null,
                        provider = location.provider ?: "Unknown",
                    )
                }
            }
        }

        viewModelScope.launch {
            locationRepository.observeGnssStatus().collect { status ->
                _locationState.update {
                    it.copy(satellitesInView = status.satellitesInView, satellitesUsed = status.satellitesUsed)
                }
            }
        }

        sensorRepository.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.let { rotationSensor ->
            viewModelScope.launch {
                sensorRepository.observeSensorReadings(rotationSensor, SensorManager.SENSOR_DELAY_UI).collect { reading ->
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, reading.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val magneticHeading = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
                    val declination = lastLocation?.let { loc ->
                        GeomagneticField(
                            loc.latitude.toFloat(),
                            loc.longitude.toFloat(),
                            loc.altitude.toFloat(),
                            System.currentTimeMillis(),
                        ).declination
                    } ?: 0f
                    val trueHeading = (magneticHeading + declination + 360f) % 360f
                    _compassState.update {
                        it.copy(magneticHeading = magneticHeading, trueHeading = trueHeading, accuracy = reading.accuracy)
                    }
                }
            }
        }

        sensorRepository.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let { magneticSensor ->
            viewModelScope.launch {
                sensorRepository.observeSensorReadings(magneticSensor, SensorManager.SENSOR_DELAY_UI).collect { reading ->
                    _compassState.update { it.copy(fieldStrengthMicroTesla = representativeValue(reading.values)) }
                }
            }
        }
    }

    fun toggleTracking() {
        _locationState.update { it.copy(isTracking = !it.isTracking) }
    }
}
