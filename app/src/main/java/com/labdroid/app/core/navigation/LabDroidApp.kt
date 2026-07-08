package com.labdroid.app.core.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.labdroid.app.R
import com.labdroid.app.core.designsystem.theme.DarkBottomNavBackground
import com.labdroid.app.core.designsystem.theme.ThemeMode
import com.labdroid.app.core.designsystem.theme.ThemeViewModel
import com.labdroid.app.core.designsystem.theme.LabDroidTheme
import com.labdroid.app.core.designsystem.theme.VividGreen
import com.labdroid.app.core.locale.AppLanguage
import com.labdroid.app.core.locale.LocaleManager
import kotlinx.coroutines.launch

private data class BottomNavEntry(val labelRes: Int, val route: String, val iconRes: Int)

private val bottomNavEntries: List<BottomNavEntry> =
    LabDroidDestination.entries
        .filter { it != LabDroidDestination.SETTINGS }
        .map { BottomNavEntry(it.labelRes, it.route, it.iconRes) } +
        listOf(
            BottomNavEntry(R.string.nav_gps, "gps", R.drawable.ic_ph_gps),
            BottomNavEntry(R.string.nav_cameras, "cameras", R.drawable.ic_ph_cameras),
            BottomNavEntry(R.string.nav_battery, "battery", R.drawable.ic_ph_battery),
            BottomNavEntry(
                LabDroidDestination.SETTINGS.labelRes,
                LabDroidDestination.SETTINGS.route,
                LabDroidDestination.SETTINGS.iconRes,
            ),
        )

@Composable
private fun CompactBottomNav(currentRoute: String?, darkTheme: Boolean, onEntryClick: (BottomNavEntry) -> Unit) {
    val backgroundColor = if (darkTheme) DarkBottomNavBackground else MaterialTheme.colorScheme.surfaceContainer
    Surface(color = backgroundColor, tonalElevation = 3.dp) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            bottomNavEntries.chunked(4).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    row.forEach { entry ->
                        val selected = currentRoute == entry.route
                        val tint = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        val iconAlpha = if (!darkTheme || selected) 1f else 0.55f
                        val label = stringResource(entry.labelRes)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(onClick = { onEntryClick(entry) })
                                .padding(vertical = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Image(
                                painter = painterResource(entry.iconRes),
                                contentDescription = label,
                                modifier = Modifier
                                    .size(51.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .alpha(iconAlpha),
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = tint,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val TopBarButtonSize = 46.dp
private val TopBarButtonShape = RoundedCornerShape(14.dp)

@Composable
private fun FramedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .padding(2.dp)
            .size(TopBarButtonSize)
            .border(1.dp, borderColor, TopBarButtonShape),
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabDroidApp(
    windowSizeClass: WindowSizeClass,
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val useNavigationRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    val context = LocalContext.current
    val supportGreen = VividGreen

    LabDroidTheme(darkTheme = darkTheme, dynamicColor = false) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val isTopLevelRoute = currentRoute == null ||
            LabDroidDestination.entries.any { it.route == currentRoute }
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()
        var showSupportDialog by remember { mutableStateOf(false) }
        var showLanguageMenu by remember { mutableStateOf(false) }
        var currentLanguage by remember { mutableStateOf(LocaleManager.getLanguage(context)) }

        fun navigateToTab(route: String) {
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(
                    darkTheme = darkTheme,
                    onToggleTheme = { themeViewModel.toggleTheme(darkTheme) },
                    onOpenSpotRobotics = {
                        coroutineScope.launch { drawerState.close() }
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://spotrobotics.app")),
                        )
                    },
                    onContactEmail = {
                        coroutineScope.launch { drawerState.close() }
                        context.startActivity(
                            Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:tomasz.pieczara@gazeta.pl")),
                        )
                    },
                    onClose = { coroutineScope.launch { drawerState.close() } },
                )
            },
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            if (isTopLevelRoute) {
                                FramedIconButton(
                                    onClick = { coroutineScope.launch { drawerState.open() } },
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Menu,
                                        contentDescription = stringResource(R.string.topbar_menu),
                                    )
                                }
                            } else {
                                FramedIconButton(
                                    onClick = { navController.navigateUp() },
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                        contentDescription = stringResource(R.string.topbar_back),
                                    )
                                }
                            }
                        },
                        actions = {
                            Row(
                                modifier = Modifier
                                    .padding(end = 2.dp)
                                    .height(TopBarButtonSize)
                                    .clip(TopBarButtonShape)
                                    .border(1.dp, supportGreen.copy(alpha = 0.7f), TopBarButtonShape)
                                    .clickable { showSupportDialog = true }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = null,
                                    tint = supportGreen,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = stringResource(R.string.topbar_support_project),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = supportGreen,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Box {
                                FramedIconButton(
                                    onClick = { showLanguageMenu = true },
                                ) {
                                    Text(
                                        text = currentLanguage.flagEmoji,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.3f,
                                        ),
                                    )
                                }
                                DropdownMenu(expanded = showLanguageMenu, onDismissRequest = { showLanguageMenu = false }) {
                                    AppLanguage.entries.forEach { language ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "${language.flagEmoji} " + when (language) {
                                                        AppLanguage.ENGLISH -> stringResource(R.string.language_english)
                                                        AppLanguage.POLISH -> stringResource(R.string.language_polish)
                                                    },
                                                )
                                            },
                                            onClick = {
                                                showLanguageMenu = false
                                                LocaleManager.setLanguage(context, language)
                                                currentLanguage = language
                                                LocaleManager.restartProcess(context)
                                            },
                                        )
                                    }
                                }
                            }
                            FramedIconButton(
                                onClick = { (context as? Activity)?.finish() },
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = stringResource(R.string.topbar_close),
                                )
                            }
                        },
                    )
                },
                bottomBar = {
                    if (isTopLevelRoute && !useNavigationRail) {
                        CompactBottomNav(
                            currentRoute = currentRoute,
                            darkTheme = darkTheme,
                            onEntryClick = { entry ->
                                if (LabDroidDestination.entries.any { it.route == entry.route }) {
                                    navigateToTab(entry.route)
                                } else {
                                    navController.navigate(entry.route)
                                }
                            },
                        )
                    }
                },
            ) { innerPadding ->
                if (isTopLevelRoute && useNavigationRail) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        NavigationRail(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
                            LabDroidDestination.entries.forEach { destination ->
                                val label = stringResource(destination.labelRes)
                                val selected = currentRoute == destination.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToTab(destination.route) },
                                    icon = {
                                        Image(
                                            painter = painterResource(destination.iconRes),
                                            contentDescription = label,
                                            modifier = Modifier
                                                .size(51.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .alpha(if (!darkTheme || selected) 1f else 0.55f),
                                        )
                                    },
                                    label = { Text(label) },
                                )
                            }
                        }
                        LabDroidNavHost(
                            navController = navController,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    LabDroidNavHost(navController = navController, contentPadding = innerPadding)
                }
            }
        }

        if (showSupportDialog) {
            AlertDialog(
                onDismissRequest = { showSupportDialog = false },
                icon = { Icon(imageVector = Icons.Filled.Favorite, contentDescription = null, tint = supportGreen) },
                title = { Text(stringResource(R.string.support_dialog_title)) },
                text = { Text(stringResource(R.string.support_dialog_body)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSupportDialog = false
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://spotrobotics.app")),
                            )
                        },
                    ) {
                        Text(stringResource(R.string.support_dialog_link), color = supportGreen)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSupportDialog = false }) {
                        Text(stringResource(R.string.support_dialog_dismiss))
                    }
                },
            )
        }
    }
}
