package com.labdroid.app.data.hardware

import android.content.Context
import android.hardware.display.DisplayManager
import android.util.DisplayMetrics
import android.view.Display
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class DisplaySnapshot(
    val widthPx: Int,
    val heightPx: Int,
    val density: Float,
    val refreshRateHz: Float,
    val hdrSupported: Boolean,
)

class DisplayRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun getDisplaySnapshot(): DisplaySnapshot {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)

        val realMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        display?.getRealMetrics(realMetrics)
        val hdrSupported = runCatching {
            display?.hdrCapabilities?.supportedHdrTypes?.isNotEmpty() == true
        }.getOrDefault(false)

        return DisplaySnapshot(
            widthPx = realMetrics.widthPixels,
            heightPx = realMetrics.heightPixels,
            density = context.resources.displayMetrics.density,
            refreshRateHz = display?.refreshRate ?: 0f,
            hdrSupported = hdrSupported,
        )
    }
}
