package com.walangkaninbossing.eshcat.ui.screens.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.auth.Permissions
import com.walangkaninbossing.eshcat.ui.components.EmptyState
import com.walangkaninbossing.eshcat.ui.components.FilterChipsRow
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.PermissionGate
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.RefreshButton
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.TextMutedLight
import com.walangkaninbossing.eshcat.ui.theme.TextSecondaryLight
import com.walangkaninbossing.eshcat.util.TimeUtil
import com.walangkaninbossing.eshcat.viewmodel.AdminViewModel
import com.walangkaninbossing.eshcat.viewmodel.AuditRow
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer

@Composable
fun StaffAuditScreen(nav: NavController, sessionVm: StaffSessionViewModel) {
    val adminVm: AdminViewModel = viewModel { AdminViewModel(eshcatContainer()) }
    val session by sessionVm.state.collectAsState()
    if (session.user == null) {
        PermissionGate(enabled = false) {}
        return
    }
    var rows by remember { mutableStateOf<List<AuditRow>>(emptyList()) }
    var filter by remember { mutableStateOf("All") }
    var reloadKey by remember { mutableStateOf(0) }
    LaunchedEffect(reloadKey) { rows = adminVm.auditLogsOnce() }
    val actions = remember(rows) { listOf("All") + rows.map { it.log.action }.distinct().take(8) }
    val filtered = remember(rows, filter) {
        if (filter == "All") rows else rows.filter { it.log.action == filter }
    }

    PermissionGate(enabled = sessionVm.has(Permissions.VIEW_AUDIT_LOGS)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ScreenHeader(
                    "Audit Logs",
                    subtitle = "${filtered.size} log${if (filtered.size == 1) "" else "s"}",
                    trailing = { RefreshButton(onRefresh = { reloadKey++ }) },
                )
            }
            item { FilterChipsRow(actions, filter, { filter = it }) }
            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.History,
                        "No audit logs",
                        "No audit activity matches this filter.",
                    )
                }
            }
            items(filtered, key = { it.log.id }) { row ->
                val log = row.log
                GlassCard {
                    Column {
                        Text(
                            log.action.replace('_', ' '),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${row.actorName} · ${log.targetId.ifBlank { "-" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryLight,
                        )
                        Text(
                            "${log.oldValue ?: "-"} → ${log.newValue ?: "-"}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondaryLight,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            TimeUtil.displayDateTime(log.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMutedLight,
                        )
                    }
                }
            }
        }
    }
}