package com.walangkaninbossing.eshcat.ui.screens.staff

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.ESHCATApplication
import com.walangkaninbossing.eshcat.auth.Permissions
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.PermissionGate
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.theme.DangerAccent
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.Primary
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.util.ThemeMode
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (danger) DangerAccent else Primary,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(EshcatSpacing.md))
        Column(Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (danger) DangerAccent else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun StaffSettingsScreen(nav: NavController, sessionVm: StaffSessionViewModel) {
    val session by sessionVm.state.collectAsState()
    if (session.user == null) {
        PermissionGate(enabled = false) {}
        return
    }
    val ctx = LocalContext.current
    val app = ctx.applicationContext as ESHCATApplication
    val settingsManager = app.container.settings

    val notificationsEnabled by settingsManager.notificationsEnabled.collectAsState()
    val themeMode by settingsManager.themeMode.collectAsState()

    var showLogout by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader("Settings", onBack = { nav.popBackStack() }) }

        // Notifications
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Notifications",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.size(2.dp))
                        Text(
                            if (notificationsEnabled) "Receiving activity updates on this device" else "Notifications muted",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            settingsManager.setNotificationsEnabled(enabled)
                            val msg = if (enabled) "Notifications enabled" else "Notifications muted"
                            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Appearance Mode
        item {
            GlassCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.DARK -> Icons.Filled.DarkMode
                                ThemeMode.LIGHT -> Icons.Filled.LightMode
                                else -> Icons.Filled.SettingsSuggest
                            },
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(EshcatSpacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Appearance Mode",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = when (themeMode) {
                                    ThemeMode.DARK -> "Dark Theme active"
                                    ThemeMode.LIGHT -> "Light Theme active"
                                    else -> "Following system theme"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(Modifier.height(EshcatSpacing.sm))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = themeMode == ThemeMode.SYSTEM,
                            onClick = {
                                settingsManager.setThemeMode(ThemeMode.SYSTEM)
                                Toast.makeText(ctx, "Theme set to System Default", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text("System") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        FilterChip(
                            selected = themeMode == ThemeMode.LIGHT,
                            onClick = {
                                settingsManager.setThemeMode(ThemeMode.LIGHT)
                                Toast.makeText(ctx, "Light Theme active", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text("Light") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        FilterChip(
                            selected = themeMode == ThemeMode.DARK,
                            onClick = {
                                settingsManager.setThemeMode(ThemeMode.DARK)
                                Toast.makeText(ctx, "Dark Theme active", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text("Dark") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            GlassCard {
                SettingsRow(Icons.Filled.Info, "Data & Storage", "Local database only · offline-first") {}
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                SettingsRow(Icons.Filled.Campaign, "About eSHCAT", "Municipal services portal details") { nav.navigate(Routes.ABOUT) }
            }
        }

        if (sessionVm.has(Permissions.MANAGE_SETTINGS)) {
            item {
                GlassCard {
                    Text("System Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.size(4.dp))
                    SettingsRow(Icons.Filled.Campaign, "Office hours notice", "Standing office hours message") {
                        Toast.makeText(ctx, "Settings stored locally", Toast.LENGTH_SHORT).show()
                    }
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    SettingsRow(Icons.Filled.FactCheck, "Issue posture", "Standing accommodation policy") {
                        Toast.makeText(ctx, "Settings stored locally", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        item {
            GlassCard {
                SettingsRow(Icons.Filled.Logout, "Sign Out", "End your staff session", danger = true) { showLogout = true }
            }
        }
    }

    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text("Sign out?") },
            text = { Text("End your staff session on this device?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogout = false
                    sessionVm.logout()
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }) { Text("Sign Out", color = DangerAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showLogout = false }) { Text("Cancel") }
            },
        )
    }
}