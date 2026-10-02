package com.labdroid.app.data.sensors

import javax.inject.Inject

/**
 * Counts only the sensors the app actually recognizes and lists on the Sensors tab (deduped,
 * known types). The raw `getSensorList(TYPE_ALL)` size includes OEM-internal composite sensors,
 * wake-up duplicates and types we deliberately hide — using it here made the Dashboard tile
 * disagree with what the Sensors tab actually shows.
 */
class SensorCountRepository @Inject constructor(private val sensorRepository: SensorRepository) {

    fun getSensorCount(): Int = sensorRepository.getSensorsByPriority().size
}
