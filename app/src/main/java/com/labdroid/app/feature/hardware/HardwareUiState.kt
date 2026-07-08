package com.labdroid.app.feature.hardware

import androidx.annotation.DrawableRes

data class HardwareSection(
    val title: String,
    val rows: List<Pair<String, String>>,
    val permissionRows: Map<String, String> = emptyMap(),
    @DrawableRes val iconRes: Int? = null,
)

data class HardwareUiState(val sections: List<HardwareSection> = emptyList())
