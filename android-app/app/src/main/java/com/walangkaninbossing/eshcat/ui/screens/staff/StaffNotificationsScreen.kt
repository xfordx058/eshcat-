package com.walangkaninbossing.eshcat.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.walangkaninbossing.eshcat.ui.components.EmptyState
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.PermissionGate
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.SectionHeader
import com.walangkaninbossing.eshcat.ui.theme.InfoContainerAccent
import com.walangkaninbossing.eshcat.ui.theme.InfoAccent
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.Primary
import com.walangkaninbossing.eshcat.ui.theme.PrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.TextMutedLight
import com.walangkaninbossing.eshcat.ui.theme.TextSecondaryLight
import com.walangkaninbossing.eshcat.viewmodel.NotificationItem
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel
import com.walangkaninbossing.eshcat.viewmodel.StaffViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer

private fun notificationIcon(kind: String): ImageVector = when {
    kind == "LOGIN" -> Icons.Filled.Lock
    kind.contains("APPLICATION") || kind.contains("STATUS") -> Icons.Filled.Refresh
    kind.startsWith("APPOINTMENT") -> Icons.Filled.CalendarMonth
    kind.startsWith("REPORT") -> Icons.Filled.ReportProblem
    kind.startsWith("USER") -> Icons.Filled.People
    kind.startsWith("ROLE") -> Icons.Filled.AdminPanelSettings
    kind.startsWith("SERVICE") || kind.startsWith("OFFICE") || kind.startsWith("ANNOUNCEMENT") -> Icons.Filled.Campaign
    else -> Icons.Filled.Notifications
}

@Composable
private fun NotificationCard(item: NotificationItem) {
    GlassCard {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(PrimaryContainerLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(notificationIcon(item.kind.uppercase()), contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(EshcatSpacing.md))
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(item.description, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(item.timestamp, style = MaterialTheme.typography.labelSmall, color = TextMutedLight)
            }
        }
    }
}

@Composable
fun StaffNotificationsScreen(nav: NavController, sessionVm: StaffSessionViewModel) {
    val staffVm: StaffViewModel = viewModel { StaffViewModel(eshcatContainer()) }
    val session by sessionVm.state.collectAsState()
    if (session.user == null) {
        PermissionGate(enabled = false) {}
        return
    }
    var suggestions by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var history by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var reloadKey by remember { mutableStateOf(0) }
    LaunchedEffect(reloadKey) {
        suggestions = staffVm.suggestionNotifications()
        history = staffVm.notificationsOnce()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader("Notifications", onBack = { nav.popBackStack() }) }
        items(suggestions) { item ->
            GlassCard {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(InfoContainerAccent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Campaign, contentDescription = null, tint = InfoAccent, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(EshcatSpacing.md))
                    Column(Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(2.dp))
                        Text(item.description, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                        Spacer(Modifier.height(4.dp))
                        Text(item.timestamp, style = MaterialTheme.typography.labelSmall, color = TextMutedLight)
                    }
                }
            }
        }
        if (history.isNotEmpty()) {
            item { SectionHeader("Activity") }
        }
        items(history, key = { it.timestamp + it.title }) { item -> NotificationCard(item) }
        if (suggestions.isEmpty() && history.isEmpty()) {
            item {
                EmptyState(
                    Icons.Filled.Notifications,
                    "No notifications yet",
                    "Activity updates will appear here as they happen.",
                )
            }
        }
    }
}