package com.truesensor.app.feature.hardware

data class HardwareSection(val title: String, val rows: List<Pair<String, String>>)

data class HardwareUiState(val sections: List<HardwareSection> = emptyList())
