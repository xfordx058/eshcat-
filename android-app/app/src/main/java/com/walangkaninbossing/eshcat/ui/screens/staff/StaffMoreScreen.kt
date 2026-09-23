package com.walangkaninbossing.eshcat.ui.screens.staff

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.auth.Permissions
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.PermissionGate
import com.walangkaninbossing.eshcat.ui.components.RoleBadge
import com.walangkaninbossing.eshcat.ui.theme.DangerAccent
import com.walangkaninbossing.eshcat.ui.theme.DangerContainerAccent
import com.walangkaninbossing.eshcat.ui.theme.EshcatRadius
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.OnPrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.Primary
import com.walangkaninbossing.eshcat.ui.theme.PrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.TextMutedLight
import com.walangkaninbossing.eshcat.ui.theme.TextSecondaryLight
import com.walangkaninbossing.eshcat.viewmodel.AdminViewModel
import com.walangkaninbossing.eshcat.viewmodel.OfficeRow
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer

@Composable
private fun StaffMenuRow(
    icon: ImageVector,
    label: String,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(EshcatRadius.xl))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (danger) DangerContainerAccent else PrimaryContainerLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = if (danger) DangerAccent else Primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(EshcatSpacing.md))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (danger) DangerAccent else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMutedLight, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun StaffMoreScreen(nav: NavController, sessionVm: StaffSessionViewModel) {
    val adminVm: AdminViewModel = viewModel { AdminViewModel(eshcatContainer()) }
    val session by sessionVm.state.collectAsState()
    val actor = session.user
    if (actor == null) {
        PermissionGate(enabled = false) {}
        return
    }
    var offices by remember { mutableStateOf<List<OfficeRow>>(emptyList()) }
    var showLogout by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { offices = adminVm.officesOnce() }
    val initials = actor.fullName.trim().split(" ")
        .filter { it.isNotBlank() }.take(2).map { it.first() }.joinToString("").uppercase()
    val officeName = offices.find { it.office.id == actor.officeId }?.office?.name ?: "Catarman Municipal Office"
    val roleName = session.role?.name ?: ""

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            GlassCard(onClick = { nav.navigate(Routes.STAFF_PROFILE) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(54.dp).clip(CircleShape).background(PrimaryContainerLight),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            initials,
                            color = OnPrimaryContainerLight,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.width(EshcatSpacing.md))
                    Column(Modifier.weight(1f)) {
                        Text(actor.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RoleBadge(roleName)
                            Spacer(Modifier.width(EshcatSpacing.sm))
                            Text(officeName, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                        }
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextMutedLight)
                }
            }
        }
        item {
            GlassCard {
                StaffMenuRow(Icons.Filled.Person, "Profile") { nav.navigate(Routes.STAFF_PROFILE) }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                StaffMenuRow(Icons.Filled.Notifications, "Notifications") { nav.navigate(Routes.STAFF_NOTIFICATIONS) }
                if (sessionVm.has(Permissions.MANAGE_USERS)) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    StaffMenuRow(Icons.Filled.People, "Manage Users") { nav.navigate(Routes.STAFF_USERS) }
                }
                if (sessionVm.has(Permissions.MANAGE_ROLES) || sessionVm.has(Permissions.MANAGE_PERMISSIONS)) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    StaffMenuRow(Icons.Filled.AdminPanelSettings, "Roles & Permissions") { nav.navigate(Routes.STAFF_ROLES) }
                }
                if (sessionVm.has(Permissions.MANAGE_OFFICES)) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    StaffMenuRow(Icons.Filled.Business, "Offices") { nav.navigate(Routes.STAFF_OFFICES) }
                }
                if (sessionVm.has(Permissions.MANAGE_SERVICES)) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    StaffMenuRow(Icons.Filled.WorkOutline, "Services") { nav.navigate(Routes.STAFF_SERVICES) }
                }
                if (sessionVm.has(Permissions.MANAGE_ANNOUNCEMENTS)) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    StaffMenuRow(Icons.Filled.Campaign, "Announcements") { nav.navigate(Routes.STAFF_ANNOUNCEMENTS) }
                }
                if (sessionVm.has(Permissions.VIEW_AUDIT_LOGS)) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    StaffMenuRow(Icons.Filled.History, "Audit Logs") { nav.navigate(Routes.STAFF_AUDIT) }
                }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                StaffMenuRow(Icons.Filled.Settings, "Settings") { nav.navigate(Routes.STAFF_SETTINGS) }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                StaffMenuRow(Icons.Filled.Logout, "Sign Out", danger = true) { showLogout = true }
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