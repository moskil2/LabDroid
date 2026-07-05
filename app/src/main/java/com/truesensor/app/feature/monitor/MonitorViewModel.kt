package com.truesensor.app.feature.monitor

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truesensor.app.data.export.ExportFormat
import com.truesensor.app.data.export.ExportRepository
import com.truesensor.app.data.export.SensorSampleRow
import com.truesensor.app.data.recording.RecordingDao
import com.truesensor.app.data.recording.RecordingSessionEntity
import com.truesensor.app.data.recording.SensorSampleEntity
import com.truesensor.app.data.sensors.SensorInfo
import com.truesensor.app.data.sensors.SensorRepository
import com.truesensor.app.data.sensors.representativeValue
import com.truesensor.app.data.settings.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonitorViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val recordingDao: RecordingDao,
    private val exportRepository: ExportRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val allSensors: List<SensorInfo> = sensorRepository.getGroupedSensors()
        .flatMap { it.sensors }
        .sortedBy { it.name }

    private val _pinnedTypes = MutableStateFlow<Set<Int>>(emptySet())
    val pinnedTypes: StateFlow<Set<Int>> = _pinnedTypes.asStateFlow()

    private val _pinnedStates = MutableStateFlow<Map<Int, PinnedSensorState>>(emptyMap())
    val pinnedStates: StateFlow<Map<Int, PinnedSensorState>> = _pinnedStates.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _lastSessionId = MutableStateFlow<Long?>(null)
    val lastSessionId: StateFlow<Long?> = _lastSessionId.asStateFlow()

    val exportFolderUri: StateFlow<String?> = preferencesRepository.userPreferences
        .map { it.exportFolderUri }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val collectionJobs = mutableMapOf<Int, Job>()
    private var activeSessionId: Long? = null

    fun togglePinned(sensorInfo: SensorInfo) {
        val type = sensorInfo.type
        if (_pinnedTypes.value.contains(type)) {
            _pinnedTypes.update { it - type }
            collectionJobs.remove(type)?.cancel()
            _pinnedStates.update { it - type }
        } else {
            _pinnedTypes.update { it + type }
            collectionJobs[type] = viewModelScope.launch {
                val samplingPeriodUs = preferencesRepository.userPreferences.first().defaultSamplingSpeed.periodUs
                sensorRepository.observeSensorReadings(sensorInfo.sensor, samplingPeriodUs)
                    .collect { reading ->
                        val value = representativeValue(reading.values)
                        _pinnedStates.update { map ->
                            val current = map[type] ?: PinnedSensorState()
                            map + (type to current.copy(
                                latestValue = value,
                                history = (current.history + value).takeLast(HISTORY_SIZE),
                            ))
                        }
                        val sessionId = activeSessionId
                        if (_isRecording.value && sessionId != null) {
                            recordingDao.insertSensorSample(
                                SensorSampleEntity(
                                    sessionId = sessionId,
                                    timestampMillis = System.currentTimeMillis(),
                                    label = sensorInfo.name,
                                    value = value,
                                ),
                            )
                        }
                    }
            }
        }
    }

    fun toggleRecording() {
        viewModelScope.launch {
            if (_isRecording.value) {
                val sessionId = activeSessionId
                if (sessionId != null) {
                    recordingDao.getSession(sessionId)?.let { session ->
                        recordingDao.updateSession(session.copy(endedAtMillis = System.currentTimeMillis()))
                    }
                }
                _isRecording.value = false
                _lastSessionId.value = sessionId
                activeSessionId = null
            } else {
                val sessionId = recordingDao.insertSession(
                    RecordingSessionEntity(
                        kind = "monitor",
                        label = "Live Monitor",
                        startedAtMillis = System.currentTimeMillis(),
                    ),
                )
                activeSessionId = sessionId
                _isRecording.value = true
                _lastSessionId.value = null
            }
        }
    }

    fun exportSession(sessionId: Long, uri: Uri, format: ExportFormat) {
        viewModelScope.launch {
            val samples = recordingDao.getSensorSamples(sessionId)
            val rows = samples.map { SensorSampleRow(it.timestampMillis, it.label, it.value) }
            exportRepository.exportSensorSamples(uri, format, "Live Monitor", rows)
        }
    }

    override fun onCleared() {
        collectionJobs.values.forEach { it.cancel() }
    }

    private companion object {
        const val HISTORY_SIZE = 30
    }
}
