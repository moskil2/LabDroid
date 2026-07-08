package com.labdroid.app.feature.hardware

import android.Manifest
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labdroid.app.R
import com.labdroid.app.core.util.formatBytesAsGb
import com.labdroid.app.data.battery.BatteryRepository
import com.labdroid.app.data.connectivity.ConnectivityRepository
import com.labdroid.app.data.hardware.AudioRepository
import com.labdroid.app.data.hardware.CpuRepository
import com.labdroid.app.data.hardware.DisplayRepository
import com.labdroid.app.data.hardware.FlashlightRepository
import com.labdroid.app.data.hardware.GpuRepository
import com.labdroid.app.data.memory.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HardwareViewModel @Inject constructor(
    private val cpuRepository: CpuRepository,
    private val gpuRepository: GpuRepository,
    private val memoryRepository: MemoryRepository,
    private val batteryRepository: BatteryRepository,
    private val displayRepository: DisplayRepository,
    private val flashlightRepository: FlashlightRepository,
    private val audioRepository: AudioRepository,
    private val connectivityRepository: ConnectivityRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HardwareUiState())
    val uiState: StateFlow<HardwareUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    private fun s(resId: Int): String = context.getString(resId)

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val cpu = cpuRepository.getCpuSnapshot()
            val gpu = gpuRepository.getGpuSnapshot()
            val ram = memoryRepository.getRamSnapshot()
            val storage = memoryRepository.getStorageSnapshot()
            val battery = batteryRepository.getBatterySnapshot()
            val display = displayRepository.getDisplaySnapshot()
            val flashlight = flashlightRepository.getFlashlightSnapshot()
            val audio = audioRepository.getAudioSnapshot()
            val connectivity = connectivityRepository.getSnapshot()

            val yes = s(R.string.common_yes)
            val no = s(R.string.common_no)
            val unavailable = s(R.string.common_unavailable)
            val unknown = s(R.string.cameras_unknown)
            val supported = s(R.string.common_supported)
            val notSupported = s(R.string.common_not_supported)
            val available = s(R.string.connectivity_available)
            val unavailableShort = s(R.string.connectivity_unavailable)

            _uiState.value = HardwareUiState(
                sections = listOf(
                    HardwareSection(
                        title = s(R.string.hardware_section_cpu),
                        iconRes = R.drawable.ic_ph_cpu,
                        rows = listOf(
                            s(R.string.hardware_cores) to cpu.coreCount.toString(),
                            s(R.string.hardware_core_clusters) to cpu.clusterSummary,
                            s(R.string.hardware_abi) to cpu.abis.joinToString(", "),
                            s(R.string.hardware_max_frequency) to (cpu.maxFrequencyMHz?.let { "$it MHz" } ?: unavailable),
                            s(R.string.hardware_platform) to cpu.hardwarePlatform,
                            s(R.string.hardware_soc) to (
                                listOfNotNull(cpu.socManufacturer, cpu.socModel)
                                    .joinToString(" ")
                                    .ifBlank { unknown }
                                ),
                        ),
                    ),
                    HardwareSection(
                        title = s(R.string.hardware_section_gpu),
                        iconRes = R.drawable.ic_ph_gpu,
                        rows = listOf(
                            s(R.string.hardware_renderer) to gpu.renderer,
                            s(R.string.hardware_vendor) to gpu.vendor,
                            s(R.string.hardware_gl_version) to gpu.glVersion,
                            s(R.string.hardware_egl_version) to gpu.eglVersion,
                            s(R.string.hardware_gl_extensions) to s(R.string.hardware_extensions_count).format(gpu.glExtensionsCount),
                            s(R.string.hardware_vulkan) to if (gpu.vulkanSupported) {
                                gpu.vulkanVersion?.let { "$supported ($it)" } ?: supported
                            } else {
                                notSupported
                            },
                        ),
                    ),
                    HardwareSection(
                        title = s(R.string.hardware_section_memory),
                        iconRes = R.drawable.ic_ph_memory,
                        rows = listOf(
                            s(R.string.hardware_total) to formatBytesAsGb(ram.totalBytes),
                            s(R.string.hardware_available) to formatBytesAsGb(ram.availableBytes),
                        ) + listOfNotNull(
                            ram.kernelAvailableBytes?.let { s(R.string.hardware_available_kernel) to formatBytesAsGb(it) },
                            ram.thresholdBytes?.let { s(R.string.hardware_low_memory_threshold) to formatBytesAsGb(it) },
                            ram.isLowMemory?.let { s(R.string.hardware_low_memory_state) to if (it) yes else no },
                            ram.swapTotalBytes?.takeIf { it > 0 }?.let { s(R.string.hardware_swap_total) to formatBytesAsGb(it) },
                        ),
                    ),
                    HardwareSection(
                        title = s(R.string.hardware_section_storage),
                        iconRes = R.drawable.ic_ph_storage,
                        rows = listOf(
                            s(R.string.hardware_total) to formatBytesAsGb(storage.totalBytes),
                            s(R.string.hardware_free) to formatBytesAsGb(storage.availableBytes),
                        ),
                    ),
                    HardwareSection(
                        title = s(R.string.hardware_section_battery),
                        iconRes = R.drawable.ic_ph_battery,
                        rows = listOf(
                            s(R.string.battery_level) to "${battery.percent}%",
                            s(R.string.battery_status) to if (battery.isCharging) s(R.string.battery_charging) else s(R.string.battery_not_charging),
                            s(R.string.battery_health) to battery.health,
                            s(R.string.battery_temperature) to "%.1f°C".format(battery.temperatureCelsius),
                            s(R.string.battery_voltage) to "%.2f V".format(battery.voltageVolts),
                            s(R.string.battery_current_now) to (
                                battery.currentNowMilliAmps?.let { "%.0f mA".format(it) } ?: unavailable
                                ),
                        ),
                    ),
                    HardwareSection(
                        title = s(R.string.hardware_section_display),
                        iconRes = R.drawable.ic_ph_display,
                        rows = listOf(
                            s(R.string.hardware_resolution) to "${display.widthPx} × ${display.heightPx}",
                            s(R.string.hardware_density) to "${display.density}x",
                            s(R.string.hardware_refresh_rate) to "${display.refreshRateHz} Hz",
                            s(R.string.hardware_hdr) to if (display.hdrSupported) supported else notSupported,
                        ),
                    ),
                    HardwareSection(
                        title = s(R.string.hardware_section_flashlight),
                        iconRes = R.drawable.ic_ph_flashlight,
                        rows = listOf(
                            s(R.string.hardware_available_flag) to if (flashlight.available) yes else no,
                            s(R.string.hardware_max_strength_level) to (flashlight.maxStrengthLevel?.toString() ?: unavailable),
                            s(R.string.hardware_default_strength_level) to (flashlight.defaultStrengthLevel?.toString() ?: unavailable),
                        ),
                    ),
                    HardwareSection(
                        title = s(R.string.hardware_section_audio),
                        iconRes = R.drawable.ic_ph_audio,
                        rows = listOf(
                            s(R.string.hardware_output_devices) to audio.outputDevices.joinToString(", ").ifEmpty { s(R.string.hardware_none) },
                            s(R.string.hardware_input_devices) to audio.inputDevices.joinToString(", ").ifEmpty { s(R.string.hardware_none) },
                            s(R.string.hardware_output_sample_rate) to (audio.outputSampleRateHz?.let { "$it Hz" } ?: unknown),
                            s(R.string.hardware_frames_per_buffer) to (audio.outputFramesPerBuffer?.toString() ?: unknown),
                            s(R.string.hardware_low_latency_audio) to if (audio.lowLatencySupported) supported else notSupported,
                            s(R.string.hardware_pro_audio) to if (audio.proAudioSupported) supported else notSupported,
                        ),
                    ),
                    HardwareSection(
                        title = s(R.string.hardware_section_connectivity),
                        iconRes = R.drawable.ic_ph_connectivity,
                        rows = listOf(
                            s(R.string.hardware_active_connection) to connectivity.activeConnectionLabel,
                            s(R.string.hardware_metered) to connectivity.isMetered,
                            s(R.string.hardware_internet_validated) to connectivity.hasValidatedInternet,
                            s(R.string.hardware_wifi_ssid) to connectivity.wifiSsid,
                            s(R.string.hardware_wifi_signal) to connectivity.wifiSignal,
                            s(R.string.hardware_wifi_frequency) to connectivity.wifiFrequencyBand,
                            s(R.string.hardware_wifi_link_speed) to connectivity.wifiLinkSpeed,
                            s(R.string.hardware_wifi_standard) to connectivity.wifiStandard,
                            s(R.string.hardware_cellular_operator) to connectivity.cellularOperator,
                            s(R.string.hardware_cellular_generation) to connectivity.cellularGeneration,
                            s(R.string.hardware_cellular_roaming) to connectivity.cellularRoaming,
                            s(R.string.hardware_sim_state) to connectivity.simState,
                            s(R.string.hardware_bluetooth) to if (connectivity.bluetoothAvailable) available else unavailableShort,
                            s(R.string.hardware_bluetooth_adapter) to connectivity.bluetoothAdapterName,
                            s(R.string.hardware_bluetooth_enabled) to connectivity.bluetoothEnabled,
                            s(R.string.hardware_paired_devices) to connectivity.bluetoothPairedCount,
                            s(R.string.hardware_nfc) to if (connectivity.nfcAvailable) available else unavailableShort,
                        ),
                        permissionRows = mapOf(
                            s(R.string.hardware_cellular_generation) to Manifest.permission.READ_PHONE_STATE,
                            s(R.string.hardware_bluetooth_adapter) to Manifest.permission.BLUETOOTH_CONNECT,
                            s(R.string.hardware_paired_devices) to Manifest.permission.BLUETOOTH_CONNECT,
                        ),
                    ),
                ),
            )
        }
    }
}
