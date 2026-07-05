package com.truesensor.app.feature.dashboard

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truesensor.app.core.util.formatUsageSummary
import com.truesensor.app.data.battery.BatteryRepository
import com.truesensor.app.data.camera.CameraCountRepository
import com.truesensor.app.data.connectivity.ConnectivityRepository
import com.truesensor.app.data.device.DeviceInfoRepository
import com.truesensor.app.data.memory.MemoryRepository
import com.truesensor.app.data.sensors.SensorCountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val deviceInfoRepository: DeviceInfoRepository,
    private val memoryRepository: MemoryRepository,
    private val batteryRepository: BatteryRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val sensorCountRepository: SensorCountRepository,
    private val cameraCountRepository: CameraCountRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val device = deviceInfoRepository.getDeviceInfo()
            val ram = memoryRepository.getRamSnapshot()
            val storage = memoryRepository.getStorageSnapshot()
            val battery = batteryRepository.getBatterySnapshot()
            val connectivity = connectivityRepository.getSnapshot()
            val gpsAvailable = context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

            _uiState.value = DashboardUiState(
                deviceModel = device.model,
                manufacturer = device.manufacturer,
                androidVersion = device.androidVersion,
                kernelVersion = device.kernelVersion,
                buildNumber = device.buildNumber,
                cpuAbi = device.cpuAbi,
                ramSummary = formatUsageSummary(ram.totalBytes, ram.availableBytes),
                storageSummary = formatUsageSummary(storage.totalBytes, storage.availableBytes),
                batterySummary = "${battery.percent}% · ${"%.1f".format(battery.temperatureCelsius)}°C",
                chargingSummary = if (battery.isCharging) "Charging" else "Not charging",
                gpsSummary = if (gpsAvailable) "Available" else "Unavailable",
                sensorsSummary = "${sensorCountRepository.getSensorCount()} available",
                camerasSummary = "${cameraCountRepository.getCameraCount()} available",
                connectivitySummary = connectivity.activeConnectionLabel,
            )
        }
    }
}
