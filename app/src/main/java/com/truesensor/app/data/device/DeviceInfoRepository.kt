package com.truesensor.app.data.device

import android.os.Build
import java.io.File
import javax.inject.Inject

data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val androidVersion: String,
    val kernelVersion: String,
    val buildNumber: String,
    val cpuAbi: String,
)

class DeviceInfoRepository @Inject constructor() {

    fun getDeviceInfo(): DeviceInfo = DeviceInfo(
        model = Build.MODEL,
        manufacturer = Build.MANUFACTURER,
        androidVersion = "${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}",
        kernelVersion = readKernelVersion(),
        buildNumber = Build.DISPLAY,
        cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown",
    )

    private fun readKernelVersion(): String {
        val fromProc = runCatching {
            val text = File("/proc/version").readText()
            Regex("Linux version (\\S+)").find(text)?.groupValues?.get(1)
        }.getOrNull()
        return fromProc ?: System.getProperty("os.version") ?: "Unknown"
    }
}
