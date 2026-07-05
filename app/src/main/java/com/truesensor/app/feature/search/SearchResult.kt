package com.truesensor.app.feature.search

import com.truesensor.app.data.sensors.SensorInfo

sealed class SearchResult {
    data class SensorResult(val sensorInfo: SensorInfo) : SearchResult()
    data class HardwareResult(val label: String) : SearchResult()
    data class SettingsResult(val label: String) : SearchResult()
}
