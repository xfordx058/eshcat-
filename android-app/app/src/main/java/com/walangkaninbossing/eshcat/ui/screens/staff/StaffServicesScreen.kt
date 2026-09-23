package com.walangkaninbossing.eshcat.ui.screens.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.auth.Permissions
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.EmptyState
import com.walangkaninbossing.eshcat.ui.components.EshcatSearchBar
import com.walangkaninbossing.eshcat.ui.components.PermissionGate
import com.walangkaninbossing.eshcat.ui.components.PrimaryButton
import com.walangkaninbossing.eshcat.ui.components.RefreshButton
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.ServiceCard
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.viewmodel.AdminViewModel
import com.walangkaninbossing.eshcat.viewmodel.ServiceRow
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer

@Composable
fun StaffServicesScreen(nav: NavController, sessionVm: StaffSessionViewModel) {
    val adminVm: AdminViewModel = viewModel { AdminViewModel(eshcatContainer()) }
    val session by sessionVm.state.collectAsState()
    if (session.user == null) {
        PermissionGate(enabled = false) {}
        return
    }
    var services by remember { mutableStateOf<List<ServiceRow>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var reloadKey by remember { mutableStateOf(0) }
    LaunchedEffect(reloadKey) { services = adminVm.servicesOnce() }
    val filtered = remember(services, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) services
        else services.filter {
            it.service.name.lowercase().contains(q) ||
                (it.office?.name?.lowercase()?.contains(q) ?: false)
        }
    }

    PermissionGate(enabled = sessionVm.has(Permissions.MANAGE_SERVICES)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ScreenHeader(
                    "Services",
                    subtitle = "${filtered.size} service${if (filtered.size == 1) "" else "s"}",
                    trailing = { RefreshButton(onRefresh = { reloadKey++ }) },
                )
            }
            item {
                EshcatSearchBar(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Search by service or office",
                )
            }
            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.Apps,
                        "No services",
                        "No services match your search.",
                    )
                }
            }
            items(filtered, key = { it.service.id }) { row ->
                ServiceCard(
                    service = row.service,
                    officeName = row.office?.name ?: "Unknown office",
                    onClick = { nav.navigate(Routes.staffServiceEdit(row.service.id)) },
                )
            }
            item {
                PrimaryButton("Add Service") { nav.navigate(Routes.staffServiceEdit(-1)) }
            }
        }
    }
}