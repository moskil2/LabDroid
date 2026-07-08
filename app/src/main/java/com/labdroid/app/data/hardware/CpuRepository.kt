package com.labdroid.app.data.hardware

import android.os.Build
import java.io.File
import javax.inject.Inject

data class CpuSnapshot(
    val coreCount: Int,
    val abis: List<String>,
    val maxFrequencyMHz: Int?,
    val clusterSummary: String,
    val hardwarePlatform: String,
    val socManufacturer: String?,
    val socModel: String?,
)

class CpuRepository @Inject constructor() {

    fun getCpuSnapshot(): CpuSnapshot {
        val coreCount = Runtime.getRuntime().availableProcessors()
        val perCoreMaxFreq = (0 until coreCount).map { core -> readCoreMaxFreqMHz(core) }
        val maxFrequency = perCoreMaxFreq.filterNotNull().maxOrNull()
        return CpuSnapshot(
            coreCount = coreCount,
            abis = Build.SUPPORTED_ABIS.toList(),
            maxFrequencyMHz = maxFrequency,
            clusterSummary = summarizeClusters(perCoreMaxFreq),
            hardwarePlatform = Build.HARDWARE,
            socManufacturer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MANUFACTURER else null,
            socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL else null,
        )
    }

    private fun readCoreMaxFreqMHz(core: Int): Int? = runCatching {
        File("/sys/devices/system/cpu/cpu$core/cpufreq/cpuinfo_max_freq").readText().trim().toInt() / 1000
    }.getOrNull()

    private fun summarizeClusters(perCoreMaxFreq: List<Int?>): String {
        val known = perCoreMaxFreq.filterNotNull()
        if (known.isEmpty()) return "Unknown"
        val grouped = known.groupingBy { it }.eachCount().toSortedMap(compareByDescending { it })
        return grouped.entries.joinToString(" + ") { (freqMHz, count) ->
            "%d× %.2f GHz".format(count, freqMHz / 1000f)
        }
    }
}
