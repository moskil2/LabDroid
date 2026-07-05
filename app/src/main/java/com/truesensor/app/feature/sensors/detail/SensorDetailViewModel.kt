package com.truesensor.app.feature.sensors.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truesensor.app.data.sensors.SensorDelayOption
import com.truesensor.app.data.sensors.SensorInfo
import com.truesensor.app.data.sensors.SensorRepository
import com.truesensor.app.data.sensors.representativeValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SensorDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sensorRepository: SensorRepository,
) : ViewModel() {

    private val sensorType: Int = checkNotNull(savedStateHandle["type"])
    val sensorInfo: SensorInfo? = sensorRepository.getSensorInfo(sensorType)

    private val history = ArrayDeque<Float>()

    private val _uiState = MutableStateFlow(SensorDetailUiState())
    val uiState: StateFlow<SensorDetailUiState> = _uiState.asStateFlow()

    private var collectionJob: Job? = null

    init {
        startCollecting()
    }

    private fun startCollecting() {
        collectionJob?.cancel()
        val sensor = sensorInfo?.sensor ?: return
        collectionJob = viewModelScope.launch {
            sensorRepository.observeSensorReadings(sensor, _uiState.value.delayOption.periodUs).collect { reading ->
                if (_uiState.value.isPaused) return@collect
                onSample(representativeValue(reading.values))
            }
        }
    }

    private fun onSample(value: Float) {
        history.addLast(value)
        if (history.size > HISTORY_SIZE) history.removeFirst()
        val list = history.toList()
        val avg = list.average().toFloat()
        val variance = list.sumOf { ((it - avg) * (it - avg)).toDouble() } / list.size
        _uiState.update {
            it.copy(
                latestValue = value,
                history = list,
                minValue = list.min(),
                maxValue = list.max(),
                average = avg,
                stdDev = kotlin.math.sqrt(variance).toFloat(),
            )
        }
    }

    fun selectDelay(option: SensorDelayOption) {
        _uiState.update { it.copy(delayOption = option) }
        startCollecting()
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun reset() {
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

    override fun onCleared() {
        collectionJob?.cancel()
    }

    private companion object {
        const val HISTORY_SIZE = 150
    }
}
