package com.labdroid.app.core.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Copyright
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.labdroid.app.BuildConfig
import com.labdroid.app.R
import com.labdroid.app.core.designsystem.theme.VividGreen

const val WEBSITE_URL = "https://spotrobotics.app"
const val GITHUB_URL = "https://github.com/moskil2/LabDroid"
const val SUPPORT_FORM_URL = "https://spotrobotics.app/support/"

@Composable
fun AppDrawerContent(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onContactEmail: () -> Unit,
) {
    val appVersionName = BuildConfig.VERSION_NAME
    val drawerIconGreen = VividGreen

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            ElevatedCard(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Image(
                            painter = painterResource(R.mipmap.ic_launcher),
                            contentDescription = null,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp)),
                        )
                        Column {
                            Text(text = "LabDroid", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = stringResource(R.string.drawer_version, appVersionName),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = stringResource(R.string.drawer_dark_theme), style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = darkTheme, onCheckedChange = { onToggleTheme() })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoCard {
                    Text(
                        text = stringResource(R.string.drawer_created_by_line, stringResource(R.string.drawer_author_name)),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                InfoCard {
                    InfoRow(label = stringResource(R.string.drawer_version_label), value = appVersionName)
                    InfoDivider()
                    InfoRow(label = stringResource(R.string.drawer_build_label), value = BuildConfig.BUILD_STAMP)
                }

                InfoCard {
                    InfoRow(
                        label = stringResource(R.string.drawer_contact_label),
                        value = stringResource(R.string.drawer_contact_email),
                        onClick = onContactEmail,
                    )
                    InfoDivider()
                    InfoRow(
                        label = stringResource(R.string.drawer_website_label),
                        value = stringResource(R.string.drawer_author_site),
                        onClick = { onOpenUrl(WEBSITE_URL) },
                    )
                    InfoDivider()
                    InfoRow(
                        label = stringResource(R.string.drawer_github_label),
                        value = stringResource(R.string.drawer_github_display),
                        onClick = { onOpenUrl(GITHUB_URL) },
                    )
                }

                PillMenuTile(
                    icon = Icons.Outlined.PrivacyTip,
                    label = stringResource(R.string.drawer_privacy_label),
                    iconTint = drawerIconGreen,
                ) {
                    TileBody(stringResource(R.string.drawer_privacy_body, appVersionName))
                }

                PillMenuTile(
                    icon = Icons.Outlined.Description,
                    label = stringResource(R.string.drawer_terms_label),
                    iconTint = drawerIconGreen,
                ) {
                    TileBody(stringResource(R.string.drawer_terms_body))
                }

                PillMenuTile(
                    icon = Icons.Outlined.Feedback,
                    label = stringResource(R.string.drawer_support_label),
                    iconTint = drawerIconGreen,
                ) {
                    TileBody(stringResource(R.string.drawer_support_body))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.drawer_support_link),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = drawerIconGreen,
                        modifier = Modifier.clickable { onOpenUrl(SUPPORT_FORM_URL) },
                    )
                }

                PillMenuTile(
                    icon = Icons.Outlined.VerifiedUser,
                    label = stringResource(R.string.drawer_compatibility_label),
                    iconTint = drawerIconGreen,
                ) {
                    TileBody(stringResource(R.string.drawer_compatibility_body))
                }

                PillMenuTile(
                    icon = Icons.Outlined.Copyright,
                    label = stringResource(R.string.drawer_copyright_label),
                    iconTint = drawerIconGreen,
                ) {
                    TileBody(stringResource(R.string.drawer_copyright_body))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    val cardShape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), cardShape)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        content = content,
    )
}

@Composable
private fun InfoRow(label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (onClick != null) VividGreen else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun InfoDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun TileBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun PillMenuTile(
    icon: ImageVector,
    label: String,
    iconTint: Color,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val tileShape = RoundedCornerShape(28.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(tileShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), tileShape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint)
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.2.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp)) {
                content()
            }
        }
    }
}
