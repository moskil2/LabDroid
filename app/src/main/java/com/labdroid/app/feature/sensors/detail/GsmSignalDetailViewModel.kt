package com.labdroid.app.feature.sensors.detail

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.labdroid.app.data.export.ExportFormat
import com.labdroid.app.data.export.ExportRepository
import com.labdroid.app.data.export.SensorSampleRow
import com.labdroid.app.data.sensors.GsmSignalRepository
import com.labdroid.app.data.sensors.SensorDelayOption
import com.labdroid.app.data.settings.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GsmSignalDetailViewModel @Inject constructor(
    private val gsmSignalRepository: GsmSignalRepository,
    private val exportRepository: ExportRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val history = ArrayDeque<Pair<Long, Float>>()

    private val _uiState = MutableStateFlow(SensorDetailUiState())
    val uiState: StateFlow<SensorDetailUiState> = _uiState.asStateFlow()

    val exportFolderUri: StateFlow<String?> = preferencesRepository.userPreferences
        .map { it.exportFolderUri }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private var collectionJob: Job? = null
    private var lastAcceptedAtMillis = 0L

    init {
        startCollecting()
    }

    fun restartCollecting() {
        collectionJob?.cancel()
        startCollecting()
    }

    /**
     * The telephony callback is registered exactly once; Fast/Normal/Slow only throttles which
     * pushed updates get *accepted* below, so [selectDelay] never re-registers the listener.
     */
    private fun startCollecting() {
        collectionJob = viewModelScope.launch {
            gsmSignalRepository.observeSignalDbm().collect { dbm ->
                if (_uiState.value.isPaused) return@collect
                val now = System.currentTimeMillis()
                val minIntervalMs = _uiState.value.delayOption.periodUs / 1000L
                if (now - lastAcceptedAtMillis < minIntervalMs) return@collect
                lastAcceptedAtMillis = now
                onSample(now, dbm)
            }
        }
    }

    private fun onSample(timestampMillis: Long, value: Float) {
        history.addLast(timestampMillis to value)
        if (history.size > SensorDetailViewModel.HISTORY_SIZE) history.removeFirst()
        val values = history.map { it.second }
        val avg = values.average().toFloat()
        val variance = values.sumOf { ((it - avg) * (it - avg)).toDouble() } / values.size
        _uiState.update {
            it.copy(
                latestValue = value,
                history = values,
                minValue = values.min(),
                maxValue = values.max(),
                average = avg,
                stdDev = kotlin.math.sqrt(variance).toFloat(),
            )
        }
    }

    fun selectDelay(option: SensorDelayOption) {
        _uiState.update { it.copy(delayOption = option) }
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun reset() {
        lastAcceptedAtMillis = 0L
        history.clear()
        _uiState.update {
            it.copy(
                history = emptyList(),
                latestValue = null,
                minValue = null,
                maxValue = null,
                average = null,
                stdDev = null,
            )
        }
    }

    fun exportHistory(uri: Uri, format: ExportFormat) {
        val rows = history.map { (timestamp, value) -> SensorSampleRow(timestamp, "gsm_signal", value) }
        viewModelScope.launch {
            exportRepository.exportSensorSamples(uri, format, "gsm_signal", rows)
        }
    }

    override fun onCleared() {
        collectionJob?.cancel()
    }
}
