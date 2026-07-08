package com.labdroid.app.feature.search

import com.labdroid.app.data.sensors.SensorInfo

sealed class SearchResult {
    data class SensorResult(val sensorInfo: SensorInfo) : SearchResult()
    data class HardwareResult(val label: String) : SearchResult()
    data class SettingsResult(val label: String) : SearchResult()
}
