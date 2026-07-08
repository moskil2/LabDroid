package com.labdroid.app.data.device

import android.os.Build
import java.io.File
import javax.inject.Inject

data class DeviceInfo(
    val model: String,
    val marketingName: String?,
    val manufacturer: String,
    val androidVersion: String,
    val androidCodename: String?,
    val securityPatch: String?,
    val kernelVersion: String,
    val buildNumber: String,
    val cpuAbi: String,
)

class DeviceInfoRepository @Inject constructor() {

    fun getDeviceInfo(): DeviceInfo = DeviceInfo(
        model = Build.MODEL,
        marketingName = samsungMarketingNames[Build.MODEL],
        manufacturer = Build.MANUFACTURER,
        androidVersion = "${Build.VERSION.RELEASE} · API ${Build.VERSION.SDK_INT}",
        androidCodename = androidCodenames[Build.VERSION.SDK_INT],
        securityPatch = Build.VERSION.SECURITY_PATCH.takeIf { it.isNotBlank() },
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

    private companion object {
        // Best-effort lookup for common Samsung Galaxy S-series model codes.
        // Not exhaustive — unrecognized models simply fall back to the raw Build.MODEL string.
        val samsungMarketingNames = mapOf(
            "SM-G991B" to "Galaxy S21",
            "SM-G996B" to "Galaxy S21+",
            "SM-G998B" to "Galaxy S21 Ultra",
            "SM-S901B" to "Galaxy S22",
            "SM-S901U" to "Galaxy S22",
            "SM-S906B" to "Galaxy S22+",
            "SM-S908B" to "Galaxy S22 Ultra",
            "SM-S911B" to "Galaxy S23",
            "SM-S911U" to "Galaxy S23",
            "SM-S916B" to "Galaxy S23+",
            "SM-S918B" to "Galaxy S23 Ultra",
            "SM-S921B" to "Galaxy S24",
            "SM-S926B" to "Galaxy S24+",
            "SM-S928B" to "Galaxy S24 Ultra",
        )

        // Android SDK level -> public codename, for API levels this app supports (minSdk 26+).
        val androidCodenames = mapOf(
            26 to "Oreo",
            27 to "Oreo",
            28 to "Pie",
            29 to "Q",
            30 to "R",
            31 to "S",
            32 to "S_V2",
            33 to "Tiramisu",
            34 to "Upside Down Cake",
            35 to "Vanilla Ice Cream",
            36 to "Baklava",
        )
    }
}
