package com.labdroid.app.feature.dashboard

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labdroid.app.R
import com.labdroid.app.core.util.formatUsageSummary
import com.labdroid.app.data.battery.BatteryRepository
import com.labdroid.app.data.camera.CameraCountRepository
import com.labdroid.app.data.connectivity.ConnectivityRepository
import com.labdroid.app.data.device.DeviceInfoRepository
import com.labdroid.app.data.memory.MemoryRepository
import com.labdroid.app.data.sensors.SensorCountRepository
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

    private fun s(resId: Int): String = context.getString(resId)

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val device = deviceInfoRepository.getDeviceInfo()
            val ram = memoryRepository.getRamSnapshot()
            val storage = memoryRepository.getStorageSnapshot()
            val battery = batteryRepository.getBatterySnapshot()
            val connectivity = connectivityRepository.getSnapshot()
            val gpsAvailable = context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

            val deviceModel = device.marketingName?.let { "${device.model} ($it)" } ?: device.model
            val manufacturer = device.manufacturer.replaceFirstChar { it.titlecase() }
            val batterySummary = "${battery.percent}% · ${"%.1f".format(battery.temperatureCelsius)}°C"
            val chargingSummary = s(if (battery.isCharging) R.string.battery_charging else R.string.battery_not_charging)
            val gpsSummary = s(if (gpsAvailable) R.string.connectivity_available else R.string.connectivity_unavailable)
            val sensorsSummary = s(R.string.dashboard_count_available).format(sensorCountRepository.getSensorCount())
            val camerasSummary = s(R.string.dashboard_count_available).format(cameraCountRepository.getCameraCount())

            _uiState.value = DashboardUiState(
                infoCards = listOf(
                    DashboardInfoRow(s(R.string.dashboard_device_model), deviceModel, R.drawable.ic_ph_model),
                    DashboardInfoRow(s(R.string.dashboard_manufacturer), manufacturer, R.drawable.ic_ph_manufacturer),
                    DashboardInfoRow(s(R.string.dashboard_android_version), device.androidVersion, R.drawable.ic_ph_android),
                ) + listOfNotNull(
                    device.androidCodename?.let {
                        DashboardInfoRow(s(R.string.dashboard_android_codename), it, R.drawable.ic_ph_android)
                    },
                    device.securityPatch?.let {
                        DashboardInfoRow(s(R.string.dashboard_security_patch), it, R.drawable.ic_ph_android)
                    },
                ) + listOf(
                    DashboardInfoRow(s(R.string.dashboard_kernel), device.kernelVersion, R.drawable.ic_ph_kernel),
                    DashboardInfoRow(s(R.string.dashboard_build_number), device.buildNumber, R.drawable.ic_ph_build),
                    DashboardInfoRow(s(R.string.dashboard_cpu_architecture), device.cpuAbi, R.drawable.ic_ph_architecture),
                    DashboardInfoRow(
                        s(R.string.dashboard_ram),
                        formatUsageSummary(ram.totalBytes, ram.availableBytes),
                        R.drawable.ic_ph_ram,
                    ),
                    DashboardInfoRow(
                        s(R.string.dashboard_storage),
                        formatUsageSummary(storage.totalBytes, storage.availableBytes),
                        R.drawable.ic_ph_storage,
                    ),
                    DashboardInfoRow(s(R.string.dashboard_battery), batterySummary, R.drawable.ic_ph_battery),
                    DashboardInfoRow(s(R.string.dashboard_charging), chargingSummary, R.drawable.ic_ph_charging),
                    DashboardInfoRow(s(R.string.dashboard_gps), gpsSummary, R.drawable.ic_ph_gps),
                    DashboardInfoRow(s(R.string.dashboard_sensors), sensorsSummary, R.drawable.ic_ph_sensors),
                    DashboardInfoRow(s(R.string.dashboard_cameras), camerasSummary, R.drawable.ic_ph_cameras),
                    DashboardInfoRow(
                        s(R.string.dashboard_connectivity),
                        connectivity.activeConnectionLabel,
                        R.drawable.ic_ph_connectivity,
                    ),
                ),
            )
        }
    }
}
