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
import androidx.compose.material.icons.filled.Business
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.auth.Permissions
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.EshcatSearchBar
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.InfoRow
import com.walangkaninbossing.eshcat.ui.components.PermissionGate
import com.walangkaninbossing.eshcat.ui.components.PrimaryButton
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.RefreshButton
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.Primary
import com.walangkaninbossing.eshcat.ui.theme.PrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.TextMutedLight
import com.walangkaninbossing.eshcat.viewmodel.AdminViewModel
import com.walangkaninbossing.eshcat.viewmodel.OfficeRow
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer

@Composable
fun StaffOfficesScreen(nav: NavController, sessionVm: StaffSessionViewModel) {
    val adminVm: AdminViewModel = viewModel { AdminViewModel(eshcatContainer()) }
    val session by sessionVm.state.collectAsState()
    if (session.user == null) {
        PermissionGate(enabled = false) {}
        return
    }
    var offices by remember { mutableStateOf<List<OfficeRow>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var reloadKey by remember { mutableStateOf(0) }
    LaunchedEffect(reloadKey) { offices = adminVm.officesOnce() }
    val filtered = remember(offices, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) offices
        else offices.filter {
            it.office.name.lowercase().contains(q) || it.office.location.lowercase().contains(q)
        }
    }

    PermissionGate(enabled = sessionVm.has(Permissions.MANAGE_OFFICES)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ScreenHeader(
                    "Offices",
                    subtitle = "${filtered.size} office${if (filtered.size == 1) "" else "s"}",
                    trailing = { RefreshButton(onRefresh = { reloadKey++ }) },
                )
            }
            item {
                EshcatSearchBar(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Search by name or location",
                )
            }
            items(filtered, key = { it.office.id }) { row ->
                val office = row.office
                GlassCard(onClick = { nav.navigate(Routes.staffOfficeEdit(office.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(46.dp).clip(CircleShape).background(PrimaryContainerLight),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.Business, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(EshcatSpacing.md))
                        Column(Modifier.weight(1f)) {
                            Text(office.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(row.department.name, style = MaterialTheme.typography.labelSmall, color = TextMutedLight)
                        }
                    }
                    Spacer(Modifier.height(EshcatSpacing.sm))
                    InfoRow("Location", office.location.ifBlank { "-" })
                    InfoRow("Contact", office.contactNumber.ifBlank { "-" })
                    InfoRow("Hours", office.officeHours.ifBlank { "-" })
                }
            }
            item {
                PrimaryButton("Add Office") { nav.navigate(Routes.staffOfficeEdit(-1)) }
            }
        }
    }
}