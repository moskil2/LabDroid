package com.labdroid.app.feature.dashboard

import androidx.annotation.DrawableRes

data class DashboardInfoRow(
    val label: String,
    val value: String,
    @DrawableRes val iconRes: Int? = null,
)

data class DashboardUiState(
    val infoCards: List<DashboardInfoRow> = emptyList(),
)
