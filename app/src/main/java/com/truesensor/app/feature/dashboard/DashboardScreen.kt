package com.truesensor.app.feature.dashboard

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.truesensor.app.core.designsystem.component.InfoCard
import com.truesensor.app.core.navigation.TrueSensorDestination

private data class ShortcutChip(val label: String, val destination: TrueSensorDestination?)

private val shortcutChips = listOf(
    ShortcutChip("Live Monitor", TrueSensorDestination.MONITOR),
    ShortcutChip("GPS", null),
    ShortcutChip("Cameras", null),
    ShortcutChip("Battery", TrueSensorDestination.HARDWARE),
    ShortcutChip("Export", TrueSensorDestination.SETTINGS),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    contentPadding: PaddingValues,
    onNavigate: (TrueSensorDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(span = { GridItemSpan(2) }) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                shortcutChips.forEach { chip ->
                    FilterChip(
                        selected = false,
                        onClick = { chip.destination?.let(onNavigate) },
                        label = { Text(chip.label) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(),
                    )
                }
            }
        }

        itemsIndexed(uiState.infoCards) { _, (label, value) ->
            InfoCard(
                label = label,
                value = value,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
