package com.labdroid.app.data.memory

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.labdroid.app.R
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
    val swapFreeBytes: Long? = null,
    val cachedBytes: Long? = null,
    val buffersBytes: Long? = null,
    val memoryClassMb: Int? = null,
    val largeMemoryClassMb: Int? = null,
    val isLowRamDevice: Boolean? = null,
)

data class StorageVolumeInfo(
    val label: String,
    val totalBytes: Long,
    val availableBytes: Long,
)

data class StorageSnapshot(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val removableVolumes: List<StorageVolumeInfo> = emptyList(),
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
            swapFreeBytes = procMeminfo["SwapFree"],
            cachedBytes = procMeminfo["Cached"],
            buffersBytes = procMeminfo["Buffers"],
            memoryClassMb = activityManager.memoryClass,
            largeMemoryClassMb = activityManager.largeMemoryClass,
            isLowRamDevice = activityManager.isLowRamDevice,
        )
    }

    fun getStorageSnapshot(): StorageSnapshot {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.blockCountLong * stat.blockSizeLong
        val free = stat.availableBlocksLong * stat.blockSizeLong
        return StorageSnapshot(
            totalBytes = total,
            availableBytes = free,
            usedBytes = total - free,
            removableVolumes = removableStorageVolumes(),
        )
    }

    /**
     * The first entry from `getExternalFilesDirs` is always the primary (internal) volume;
     * anything after it is a genuinely separate, removable volume (SD card, USB-OTG drive).
     */
    private fun removableStorageVolumes(): List<StorageVolumeInfo> = runCatching {
        context.getExternalFilesDirs(null)
            .filterNotNull()
            .drop(1)
            .filter { Environment.isExternalStorageRemovable(it) }
            .mapIndexedNotNull { index, dir ->
                val stat = runCatching { StatFs(dir.path) }.getOrNull() ?: return@mapIndexedNotNull null
                val total = stat.blockCountLong * stat.blockSizeLong
                if (total <= 0L) return@mapIndexedNotNull null
                StorageVolumeInfo(
                    label = context.getString(R.string.hardware_storage_removable, index + 1),
                    totalBytes = total,
                    availableBytes = stat.availableBlocksLong * stat.blockSizeLong,
                )
            }
    }.getOrDefault(emptyList())

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
