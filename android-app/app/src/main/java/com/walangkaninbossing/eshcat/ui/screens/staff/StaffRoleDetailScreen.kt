package com.walangkaninbossing.eshcat.ui.screens.staff

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.auth.Permissions
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.LoadingState
import com.walangkaninbossing.eshcat.ui.components.PermissionGate
import com.walangkaninbossing.eshcat.ui.components.PrimaryButton
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.SectionHeader
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.TextMutedLight
import com.walangkaninbossing.eshcat.ui.theme.TextSecondaryLight
import com.walangkaninbossing.eshcat.viewmodel.AdminViewModel
import com.walangkaninbossing.eshcat.viewmodel.RoleDetail
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer
import kotlinx.coroutines.launch

@Composable
fun StaffRoleDetailScreen(
    nav: NavController,
    roleId: Int,
    sessionVm: StaffSessionViewModel,
) {
    val adminVm: AdminViewModel = viewModel { AdminViewModel(eshcatContainer()) }
    val session by sessionVm.state.collectAsState()
    val actor = session.user
    if (actor == null) {
        PermissionGate(enabled = false) {}
        return
    }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var detail by remember { mutableStateOf<RoleDetail?>(null) }
    var checkedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var saving by remember { mutableStateOf(false) }
    LaunchedEffect(roleId) {
        val d = adminVm.roleDetail(roleId)
        detail = d
        if (d != null) checkedIds = d.granted.toSet()
    }
    val canEdit = sessionVm.has(Permissions.MANAGE_PERMISSIONS)
    val d = detail

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        ScreenHeader(d?.role?.name ?: "Role", onBack = { nav.popBackStack() })
        if (d == null) {
            LoadingState(Modifier.fillMaxSize())
            return@Column
        }
        val groups = d.allPermissions.groupBy { it.id.substringBefore("_") }.toSortedMap()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    d.role.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight,
                )
            }
            if (!canEdit) {
                item {
                    Text(
                        "Read-only. Permissions for this role are modified by an administrator.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedLight,
                    )
                }
            }
            groups.forEach { (group, permissions) ->
                item { SectionHeader(group) }
                item {
                    GlassCard {
                        Column(verticalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                            permissions.forEach { perm ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = perm.id in checkedIds,
                                        onCheckedChange = if (canEdit) { checked ->
                                            checkedIds = if (checked) checkedIds + perm.id else checkedIds - perm.id
                                        } else null,
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(perm.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        Text(
                                            Permissions.description(perm.id),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondaryLight,
                                        )
                                        Text(
                                            perm.id,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextMutedLight,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (canEdit) {
                item {
                    PrimaryButton("Save permissions", enabled = !saving, loading = saving) {
                        scope.launch {
                            saving = true
                            val err = adminVm.setRolePermissions(actor, roleId, checkedIds.toList())
                            Toast.makeText(ctx, err ?: "Permissions updated.", Toast.LENGTH_SHORT).show()
                            saving = false
                            if (err == null) nav.popBackStack()
                        }
                    }
                }
            }
        }
    }
}