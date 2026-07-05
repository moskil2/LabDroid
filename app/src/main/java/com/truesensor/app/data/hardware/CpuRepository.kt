package com.truesensor.app.data.hardware

import android.os.Build
import java.io.File
import javax.inject.Inject

data class CpuSnapshot(
    val coreCount: Int,
    val abis: List<String>,
    val maxFrequencyMHz: Int?,
)

class CpuRepository @Inject constructor() {

    fun getCpuSnapshot(): CpuSnapshot {
        val coreCount = Runtime.getRuntime().availableProcessors()
        val maxFrequency = (0 until coreCount)
            .mapNotNull { core -> readCoreMaxFreqMHz(core) }
            .maxOrNull()
        return CpuSnapshot(
            coreCount = coreCount,
            abis = Build.SUPPORTED_ABIS.toList(),
            maxFrequencyMHz = maxFrequency,
        )
    }

    private fun readCoreMaxFreqMHz(core: Int): Int? = runCatching {
        File("/sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_max_freq").readText().trim().toInt() / 1000
    }.getOrNull()
}
