package com.truesensor.app.feature.hardware

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truesensor.app.core.util.formatBytesAsGb
import com.truesensor.app.data.battery.BatteryRepository
import com.truesensor.app.data.connectivity.ConnectivityRepository
import com.truesensor.app.data.hardware.AudioRepository
import com.truesensor.app.data.hardware.CpuRepository
import com.truesensor.app.data.hardware.DisplayRepository
import com.truesensor.app.data.hardware.GpuRepository
import com.truesensor.app.data.memory.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val audioRepository: AudioRepository,
    private val connectivityRepository: ConnectivityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HardwareUiState())
    val uiState: StateFlow<HardwareUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val cpu = cpuRepository.getCpuSnapshot()
            val gpu = gpuRepository.getGpuSnapshot()
            val ram = memoryRepository.getRamSnapshot()
            val storage = memoryRepository.getStorageSnapshot()
            val battery = batteryRepository.getBatterySnapshot()
            val display = displayRepository.getDisplaySnapshot()
            val audio = audioRepository.getAudioSnapshot()
            val connectivity = connectivityRepository.getSnapshot()

            _uiState.value = HardwareUiState(
                sections = listOf(
                    HardwareSection(
                        title = "CPU",
                        rows = listOf(
                            "Cores" to cpu.coreCount.toString(),
                            "ABI" to cpu.abis.joinToString(", "),
                            "Max frequency" to (cpu.maxFrequencyMHz?.let { "$it MHz" } ?: "Unavailable"),
                        ),
                    ),
                    HardwareSection(
                        title = "GPU",
                        rows = listOf(
                            "Renderer" to gpu.renderer,
                            "Vendor" to gpu.vendor,
                            "GL version" to gpu.glVersion,
                            "Vulkan" to if (gpu.vulkanSupported) "Supported" else "Not supported",
                        ),
                    ),
                    HardwareSection(
                        title = "Memory",
                        rows = listOf(
                            "Total" to formatBytesAsGb(ram.totalBytes),
                            "Available" to formatBytesAsGb(ram.availableBytes),
                        ),
                    ),
                    HardwareSection(
                        title = "Storage",
                        rows = listOf(
                            "Total" to formatBytesAsGb(storage.totalBytes),
                            "Free" to formatBytesAsGb(storage.availableBytes),
                        ),
                    ),
                    HardwareSection(
                        title = "Battery",
                        rows = listOf(
                            "Level" to "${battery.percent}%",
                            "Status" to if (battery.isCharging) "Charging" else "Not charging",
                            "Health" to battery.health,
                            "Temperature" to "%.1f°C".format(battery.temperatureCelsius),
                            "Voltage" to "${battery.voltageMilliVolts} mV",
                        ),
                    ),
                    HardwareSection(
                        title = "Display",
                        rows = listOf(
                            "Resolution" to "${display.widthPx} × ${display.heightPx}",
                            "Density" to "${display.density}x",
                            "Refresh rate" to "${display.refreshRateHz} Hz",
                            "HDR" to if (display.hdrSupported) "Supported" else "Not supported",
                        ),
                    ),
                    HardwareSection(
                        title = "Audio",
                        rows = listOf(
                            "Output devices" to audio.outputDeviceCount.toString(),
                            "Input devices" to audio.inputDeviceCount.toString(),
                        ),
                    ),
                    HardwareSection(
                        title = "Connectivity",
                        rows = listOf(
                            "Active connection" to connectivity.activeConnectionLabel,
                            "Bluetooth" to if (connectivity.bluetoothAvailable) "Available" else "Unavailable",
                            "NFC" to if (connectivity.nfcAvailable) "Available" else "Unavailable",
                        ),
                    ),
                ),
            )
        }
    }
}
