package com.truesensor.app.core.designsystem.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.truesensor.app.data.export.ExportFormat

@Composable
fun ExportMenuButton(
    formats: List<ExportFormat>,
    fileNameFor: (ExportFormat) -> String,
    onFormatChosen: (Uri, ExportFormat) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String = "Export",
) {
    var showMenu by remember { mutableStateOf(false) }
    var pendingFormat by remember { mutableStateOf<ExportFormat?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*"),
    ) { uri ->
        val format = pendingFormat
        if (uri != null && format != null) {
            onFormatChosen(uri, format)
        }
        pendingFormat = null
    }

    Box(modifier = modifier) {
        Button(
            onClick = { showMenu = true },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(label)
        }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            formats.forEach { format ->
                DropdownMenuItem(
                    text = { Text(format.label) },
                    onClick = {
                        showMenu = false
                        pendingFormat = format
                        launcher.launch(fileNameFor(format))
                    },
                )
            }
        }
    }
}
