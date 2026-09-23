package com.walangkaninbossing.eshcat.ui.screens.staff

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.auth.Permissions
import com.walangkaninbossing.eshcat.core.AccountStatuses
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.EshcatSearchBar
import com.walangkaninbossing.eshcat.ui.components.EmptyState
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.PermissionGate
import com.walangkaninbossing.eshcat.ui.components.PrimaryButton
import com.walangkaninbossing.eshcat.ui.components.RoleBadge
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.StatusBadge
import com.walangkaninbossing.eshcat.ui.components.statusVisual
import com.walangkaninbossing.eshcat.ui.theme.EshcatRadius
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.OnPrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.PrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.TextMutedLight
import com.walangkaninbossing.eshcat.ui.theme.TextSecondaryLight
import com.walangkaninbossing.eshcat.viewmodel.AdminViewModel
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel
import com.walangkaninbossing.eshcat.viewmodel.StaffUserRow
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer
import kotlinx.coroutines.launch

@Composable
fun StaffUsersScreen(nav: NavController, sessionVm: StaffSessionViewModel) {
    val adminVm: AdminViewModel = viewModel { AdminViewModel(eshcatContainer()) }
    val session by sessionVm.state.collectAsState()
    val actor = session.user
    if (actor == null) {
        PermissionGate(enabled = false) {}
        return
    }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var users by remember { mutableStateOf<List<StaffUserRow>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var reloadKey by remember { mutableStateOf(0) }
    var pendingToggleId by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(reloadKey) { users = adminVm.usersOnce() }
    val filtered = remember(users, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) users
        else users.filter {
            it.user.fullName.lowercase().contains(q) || it.user.username.lowercase().contains(q)
        }
    }
    val plural = if (filtered.size == 1) "" else "s"

    val toggleTarget = pendingToggleId?.let { id -> users.find { it.user.id == id } }

    PermissionGate(enabled = sessionVm.has(Permissions.MANAGE_USERS)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ScreenHeader(
                    "Staff Accounts",
                    subtitle = "${filtered.size} account$plural",
                    trailing = {
                        com.walangkaninbossing.eshcat.ui.components.RefreshButton(onRefresh = { reloadKey++ })
                    },
                )
            }
            item {
                EshcatSearchBar(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Search by name or username",
                )
            }
            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.People,
                        "No staff accounts",
                        "No accounts match your search.",
                    )
                }
            }
            items(filtered, key = { it.user.id }) { row ->
                val user = row.user
                val initials = user.fullName.trim().split(" ")
                    .filter { it.isNotBlank() }.take(2).map { it.first() }.joinToString("").uppercase()
                val isSelf = user.id == actor.id
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(PrimaryContainerLight),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                initials,
                                color = OnPrimaryContainerLight,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.width(EshcatSpacing.md))
                        Column(Modifier.weight(1f)) {
                            Text(user.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(2.dp))
                            Text(user.username, style = MaterialTheme.typography.bodySmall, color = TextMutedLight)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RoleBadge(row.roleName)
                                Spacer(Modifier.width(EshcatSpacing.sm))
                                Text(row.officeName, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight, maxLines = 1)
                            }
                            if (user.status != AccountStatuses.ACTIVE) {
                                Spacer(Modifier.height(6.dp))
                                StatusBadge(statusVisual(user.status))
                            }
                        }
                    }
                    Spacer(Modifier.height(EshcatSpacing.md))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { nav.navigate(Routes.staffUserEdit(user.id)) },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(EshcatRadius.md),
                        ) { Text("Edit") }
                        if (!isSelf) {
                            val disabling = user.status == AccountStatuses.ACTIVE
                            Button(
                                onClick = { pendingToggleId = user.id },
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(EshcatRadius.md),
                            ) { Text(if (disabling) "Disable" else "Enable") }
                        }
                    }
                }
            }
            item {
                PrimaryButton("Add Account") { nav.navigate(Routes.staffUserEdit(-1)) }
            }
        }
    }

    toggleTarget?.let { target ->
        val disabling = target.user.status == AccountStatuses.ACTIVE
        val newStatus = if (disabling) AccountStatuses.INACTIVE else AccountStatuses.ACTIVE
        AlertDialog(
            onDismissRequest = { pendingToggleId = null },
            title = { Text(if (disabling) "Disable account?" else "Enable account?") },
            text = { Text("${target.user.fullName} will be marked ${AccountStatuses.label(newStatus)}.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val err = adminVm.setUserStatus(actor, target.user.id, newStatus)
                        Toast.makeText(ctx, err ?: "Account status updated.", Toast.LENGTH_SHORT).show()
                        pendingToggleId = null
                        reloadKey++
                    }
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { pendingToggleId = null }) { Text("Cancel") }
            },
        )
    }
}