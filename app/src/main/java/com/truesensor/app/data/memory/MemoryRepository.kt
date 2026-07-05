package com.truesensor.app.data.memory

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class MemorySnapshot(val totalBytes: Long, val availableBytes: Long)

class MemoryRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun getRamSnapshot(): MemorySnapshot {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        return MemorySnapshot(totalBytes = info.totalMem, availableBytes = info.availMem)
    }

    fun getStorageSnapshot(): MemorySnapshot {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.blockCountLong * stat.blockSizeLong
        val free = stat.availableBlocksLong * stat.blockSizeLong
        return MemorySnapshot(totalBytes = total, availableBytes = free)
    }
}
