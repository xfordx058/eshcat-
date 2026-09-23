package com.walangkaninbossing.eshcat.ui.screens.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.auth.Permissions
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.ApplicationCard
import com.walangkaninbossing.eshcat.ui.components.EmptyState
import com.walangkaninbossing.eshcat.ui.components.EshcatSearchBar
import com.walangkaninbossing.eshcat.ui.components.FilterChipsRow
import com.walangkaninbossing.eshcat.ui.components.PermissionGate
import com.walangkaninbossing.eshcat.ui.components.RefreshButton
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.viewmodel.AppScope
import com.walangkaninbossing.eshcat.viewmodel.ApplicationRow
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel
import com.walangkaninbossing.eshcat.viewmodel.StaffViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer

@Composable
fun StaffApplicationsScreen(nav: NavController, sessionVm: StaffSessionViewModel) {
    val staffVm: StaffViewModel = viewModel { StaffViewModel(eshcatContainer()) }
    val session by sessionVm.state.collectAsState()
    val actor = session.user
    if (actor == null) {
        PermissionGate(enabled = false) {}
        return
    }
    val options = buildList {
        if (sessionVm.has(Permissions.VIEW_ALL_APPLICATIONS)) add("All")
        if (sessionVm.has(Permissions.VIEW_OFFICE_APPLICATIONS)) add("My Office")
        if (sessionVm.has(Permissions.VIEW_ASSIGNED_APPLICATIONS)) add("Assigned to me")
    }
    val defaultLabel = if ("All" in options) "All" else options.firstOrNull() ?: "Assigned to me"
    var scopeLabel by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var rows by remember { mutableStateOf<List<ApplicationRow>>(emptyList()) }
    var reloadKey by remember { mutableStateOf(0) }
    LaunchedEffect(reloadKey) {
        val target = if (scopeLabel in options) scopeLabel else options.firstOrNull() ?: defaultLabel
        scopeLabel = target
        val scope = when (target) {
            "All" -> AppScope.ALL
            "My Office" -> AppScope.OFFICE
            else -> AppScope.MINE
        }
        rows = staffVm.applicationsOnce(scope, actor)
    }
    val filtered = remember(rows, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) rows
        else rows.filter {
            it.app.referenceNumber.lowercase().contains(q) || it.app.fullName.lowercase().contains(q)
        }
    }
    val plural = if (filtered.size == 1) "" else "s"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader("Applications", subtitle = "${filtered.size} application$plural")
        }
        if (options.isNotEmpty()) {
            item { FilterChipsRow(options, scopeLabel, {
                scopeLabel = it
                reloadKey++
            }) }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EshcatSearchBar(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Search by reference or name",
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(EshcatSpacing.sm))
                RefreshButton(onRefresh = { reloadKey++ })
            }
        }
        if (filtered.isEmpty()) {
            item {
                EmptyState(
                    Icons.Filled.Description,
                    "No applications",
                    "No applications match your search.",
                )
            }
        }
        items(filtered, key = { it.app.id }) { row ->
            ApplicationCard(
                app = row.app,
                serviceName = row.serviceName,
                applicant = row.app.fullName,
                assignedToName = row.assignedName,
                onClick = { nav.navigate(Routes.staffApplicationDetail(row.app.id)) },
            )
        }
    }
}