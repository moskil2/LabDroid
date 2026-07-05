package com.truesensor.app.feature.sensors.detail

import com.truesensor.app.data.sensors.SensorDelayOption

data class SensorDetailUiState(
    val latestValue: Float? = null,
    val history: List<Float> = emptyList(),
    val minValue: Float? = null,
    val maxValue: Float? = null,
    val average: Float? = null,
    val stdDev: Float? = null,
    val delayOption: SensorDelayOption = SensorDelayOption.UI,
    val isPaused: Boolean = false,
)
