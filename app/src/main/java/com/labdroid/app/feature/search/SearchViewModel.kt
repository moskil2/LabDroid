package com.labdroid.app.feature.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labdroid.app.R
import com.labdroid.app.data.sensors.SensorInfo
import com.labdroid.app.data.sensors.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    sensorRepository: SensorRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val hardwareEntries: List<String> by lazy {
        listOf(
            R.string.search_entry_cpu, R.string.search_entry_gpu, R.string.search_entry_memory,
            R.string.search_entry_storage, R.string.search_entry_battery, R.string.search_entry_display,
            R.string.search_entry_audio, R.string.search_entry_connectivity,
        ).map(context::getString)
    }
    private val settingsEntries: List<String> by lazy {
        listOf(
            R.string.settings_theme, R.string.settings_units, R.string.settings_sampling_speed,
            R.string.settings_permissions, R.string.settings_language, R.string.settings_export_folder,
            R.string.settings_reset_preferences,
        ).map(context::getString)
    }

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
        val hardwareResults = hardwareEntries
            .filter { it.lowercase().contains(query) }
            .map { SearchResult.HardwareResult(it) }
        val settingsResults = settingsEntries
            .filter { it.lowercase().contains(query) }
            .map { SearchResult.SettingsResult(it) }

        return sensorResults + hardwareResults + settingsResults
    }
}
