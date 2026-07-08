package com.labdroid.app.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.navigation.compose.hiltViewModel
import com.labdroid.app.R
import com.labdroid.app.core.designsystem.component.StatusDot
import com.labdroid.app.core.designsystem.theme.ThemeMode
import com.labdroid.app.core.locale.AppLanguage
import com.labdroid.app.core.locale.LocaleManager
import com.labdroid.app.data.sensors.SensorDelayOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val preferences by viewModel.preferences.collectAsState()
    var showResetConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var currentLanguage by remember { mutableStateOf(LocaleManager.getLanguage(context)) }

    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
            viewModel.setExportFolderUri(uri.toString())
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        item {
            SettingsCard(title = stringResource(R.string.settings_theme)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = preferences.themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                        ) {
                            Text(
                                stringResource(
                                    if (mode == ThemeMode.LIGHT) R.string.settings_theme_light else R.string.settings_theme_dark,
                                ),
                            )
                        }
                    }
                }
            }
        }

        item {
            SettingsCard(title = stringResource(R.string.settings_units)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !preferences.useImperialUnits,
                        onClick = { viewModel.setUseImperialUnits(false) },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                    ) { Text(stringResource(R.string.settings_units_metric)) }
                    SegmentedButton(
                        selected = preferences.useImperialUnits,
                        onClick = { viewModel.setUseImperialUnits(true) },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                    ) { Text(stringResource(R.string.settings_units_imperial)) }
                }
            }
        }

        item {
            SettingsCard(title = stringResource(R.string.settings_sampling_speed)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SensorDelayOption.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = preferences.defaultSamplingSpeed == option,
                            onClick = { viewModel.setDefaultSamplingSpeed(option) },
                            shape = SegmentedButtonDefaults.itemShape(index, SensorDelayOption.entries.size),
                        ) { Text(stringResource(option.labelRes)) }
                    }
                }
            }
        }

        item {
            SettingsCard(title = stringResource(R.string.settings_permissions)) {
                Column {
                    viewModel.permissionStatuses.forEachIndexed { index, status ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(status.label, style = MaterialTheme.typography.bodyMedium)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusDot(color = if (status.granted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline)
                                Text(
                                    text = stringResource(
                                        if (status.granted) R.string.settings_permission_granted else R.string.settings_permission_not_granted,
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (index != viewModel.permissionStatuses.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        }

        item {
            SettingsCard(title = stringResource(R.string.settings_export_folder)) {
                val promptEachTime = stringResource(R.string.settings_export_folder_prompt)
                val folderName = preferences.exportFolderUri?.let { uriString ->
                    runCatching { DocumentFile.fromTreeUri(context, Uri.parse(uriString))?.name }.getOrNull()
                } ?: promptEachTime
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(folderName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { folderLauncher.launch(null) }) {
                            Text(stringResource(R.string.settings_choose_folder))
                        }
                        if (preferences.exportFolderUri != null) {
                            OutlinedButton(onClick = { viewModel.setExportFolderUri(null) }) {
                                Text(stringResource(R.string.settings_clear_folder))
                            }
                        }
                    }
                }
            }
        }

        item {
            SettingsCard(title = stringResource(R.string.settings_language)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    AppLanguage.entries.forEachIndexed { index, language ->
                        SegmentedButton(
                            selected = currentLanguage == language,
                            onClick = {
                                LocaleManager.setLanguage(context, language)
                                currentLanguage = language
                                LocaleManager.restartProcess(context)
                            },
                            shape = SegmentedButtonDefaults.itemShape(index, AppLanguage.entries.size),
                        ) {
                            Text(
                                "${language.flagEmoji} " + stringResource(
                                    if (language == AppLanguage.ENGLISH) R.string.language_english else R.string.language_polish,
                                ),
                            )
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = { showResetConfirm = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_reset_preferences))
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "LabDroid · v${viewModel.appVersion}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(stringResource(R.string.settings_reset_confirm_title)) },
            text = { Text(stringResource(R.string.settings_reset_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetPreferences()
                    showResetConfirm = false
                }) { Text(stringResource(R.string.settings_reset_confirm_action)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text(stringResource(R.string.settings_cancel)) }
            },
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}
