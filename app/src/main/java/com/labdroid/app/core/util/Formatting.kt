package com.labdroid.app.core.util

private const val BYTES_PER_GB = 1_073_741_824.0

fun formatBytesAsGb(bytes: Long): String = "%.1f GB".format(bytes / BYTES_PER_GB)

fun formatUsageSummary(totalBytes: Long, freeBytes: Long): String =
    "${formatBytesAsGb(totalBytes)} · ${"%.1f".format(freeBytes / BYTES_PER_GB)} free"
