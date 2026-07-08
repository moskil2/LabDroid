package com.labdroid.app.feature.monitor

data class PinnedSensorState(val latestValue: Float? = null, val history: List<Float> = emptyList())
