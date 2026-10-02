package com.labdroid.app.data.hardware

import android.content.Context
import android.hardware.display.DisplayManager
import android.util.DisplayMetrics
import android.view.Display
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.sqrt

data class DisplaySnapshot(
    val widthPx: Int,
    val heightPx: Int,
    val density: Float,
    val densityDpi: Int,
    val densityBucket: String,
    val xdpi: Float,
    val ydpi: Float,
    val diagonalInches: Float?,
    val refreshRateHz: Float,
    val supportedRefreshRates: List<Float>,
    val hdrTypes: List<String>,
    val wideColorGamut: Boolean,
)

class DisplayRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun getDisplaySnapshot(): DisplaySnapshot {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)

        val realMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        display?.getRealMetrics(realMetrics)

        val hdrTypes = runCatching {
            display?.hdrCapabilities?.supportedHdrTypes?.map(::hdrTypeName) ?: emptyList()
        }.getOrDefault(emptyList())

        val supportedRefreshRates = runCatching {
            display?.supportedModes?.map { it.refreshRate }?.distinct()?.sorted() ?: emptyList()
        }.getOrDefault(emptyList())

        val diagonalInches = runCatching {
            if (realMetrics.xdpi > 0f && realMetrics.ydpi > 0f) {
                val widthIn = realMetrics.widthPixels / realMetrics.xdpi
                val heightIn = realMetrics.heightPixels / realMetrics.ydpi
                sqrt(widthIn * widthIn + heightIn * heightIn)
            } else {
                null
            }
        }.getOrNull()

        return DisplaySnapshot(
            widthPx = realMetrics.widthPixels,
            heightPx = realMetrics.heightPixels,
            density = context.resources.displayMetrics.density,
            densityDpi = context.resources.displayMetrics.densityDpi,
            densityBucket = densityBucketName(context.resources.displayMetrics.densityDpi),
            xdpi = realMetrics.xdpi,
            ydpi = realMetrics.ydpi,
            diagonalInches = diagonalInches,
            refreshRateHz = display?.refreshRate ?: 0f,
            supportedRefreshRates = supportedRefreshRates,
            hdrTypes = hdrTypes,
            wideColorGamut = runCatching { display?.isWideColorGamut == true }.getOrDefault(false),
        )
    }

    private fun densityBucketName(densityDpi: Int): String = when {
        densityDpi <= DisplayMetrics.DENSITY_LOW -> "ldpi"
        densityDpi <= DisplayMetrics.DENSITY_MEDIUM -> "mdpi"
        densityDpi <= DisplayMetrics.DENSITY_HIGH -> "hdpi"
        densityDpi <= DisplayMetrics.DENSITY_XHIGH -> "xhdpi"
        densityDpi <= DisplayMetrics.DENSITY_XXHIGH -> "xxhdpi"
        else -> "xxxhdpi"
    }

    private fun hdrTypeName(type: Int): String = when (type) {
        Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> "Dolby Vision"
        Display.HdrCapabilities.HDR_TYPE_HDR10 -> "HDR10"
        Display.HdrCapabilities.HDR_TYPE_HLG -> "HLG"
        Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> "HDR10+"
        else -> "HDR ($type)"
    }
}
