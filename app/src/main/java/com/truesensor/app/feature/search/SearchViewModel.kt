package com.truesensor.app.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truesensor.app.data.sensors.SensorInfo
import com.truesensor.app.data.sensors.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private val HARDWARE_ENTRIES = listOf("CPU", "GPU", "Memory", "Storage", "Battery", "Display", "Audio", "Connectivity")
private val SETTINGS_ENTRIES = listOf(
    "Theme", "Units", "Default sensor sampling speed", "Permissions", "Language", "Export folder", "Reset preferences",
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    sensorRepository: SensorRepository,
) : ViewModel() {

    private val allSensors: List<SensorInfo> = sensorRepository.getGroupedSensors().flatMap { it.sensors }

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val results: StateFlow<List<SearchResult>> = _query
        .map(::computeResults)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    private fun computeResults(rawQuery: String): List<SearchResult> {
        val query = rawQuery.trim().lowercase()
        if (query.isEmpty()) return emptyList()

        val sensorResults = allSensors
            .filter { it.name.lowercase().contains(query) }
            .map { SearchResult.SensorResult(it) }
        val hardwareResults = HARDWARE_ENTRIES
            .filter { it.lowercase().contains(query) }
            .map { SearchResult.HardwareResult(it) }
        val settingsResults = SETTINGS_ENTRIES
            .filter { it.lowercase().contains(query) }
            .map { SearchResult.SettingsResult(it) }

        return sensorResults + hardwareResults + settingsResults
    }
}
