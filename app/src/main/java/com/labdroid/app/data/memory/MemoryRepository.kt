package com.labdroid.app.data.memory

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

data class MemorySnapshot(
    val totalBytes: Long,
    val availableBytes: Long,
    val thresholdBytes: Long? = null,
    val isLowMemory: Boolean? = null,
    val kernelAvailableBytes: Long? = null,
    val swapTotalBytes: Long? = null,
)

class MemoryRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun getRamSnapshot(): MemorySnapshot {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        val procMeminfo = readProcMeminfo()
        return MemorySnapshot(
            totalBytes = info.totalMem,
            availableBytes = info.availMem,
            thresholdBytes = info.threshold,
            isLowMemory = info.lowMemory,
            kernelAvailableBytes = procMeminfo["MemAvailable"],
            swapTotalBytes = procMeminfo["SwapTotal"],
        )
    }

    fun getStorageSnapshot(): MemorySnapshot {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.blockCountLong * stat.blockSizeLong
        val free = stat.availableBlocksLong * stat.blockSizeLong
        return MemorySnapshot(totalBytes = total, availableBytes = free)
    }

    private fun readProcMeminfo(): Map<String, Long> = runCatching {
        File("/proc/meminfo").readLines().mapNotNull { line ->
            val parts = line.split(":", limit = 2)
            if (parts.size < 2) return@mapNotNull null
            val key = parts[0].trim()
            val valueKb = parts[1].trim().removeSuffix("kB").trim().toLongOrNull() ?: return@mapNotNull null
            key to valueKb * 1024
        }.toMap()
    }.getOrDefault(emptyMap())
}
