package com.labdroid.app.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SensorCountRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun getSensorCount(): Int {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return sensorManager.getSensorList(Sensor.TYPE_ALL).size
    }
}
