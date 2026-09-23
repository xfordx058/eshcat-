package com.walangkaninbossing.eshcat.ui.screens.staff

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.auth.Permissions
import com.walangkaninbossing.eshcat.core.AccountStatuses
import com.walangkaninbossing.eshcat.core.Roles
import com.walangkaninbossing.eshcat.data.local.entity.DepartmentEntity
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.LabeledField
import com.walangkaninbossing.eshcat.ui.components.PermissionGate
import com.walangkaninbossing.eshcat.ui.components.RoleBadge
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.SectionHeader
import com.walangkaninbossing.eshcat.ui.theme.EshcatRadius
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.OnPrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.PrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.SurfaceVariantLight
import com.walangkaninbossing.eshcat.ui.theme.TextMutedLight
import com.walangkaninbossing.eshcat.ui.theme.TextSecondaryLight
import com.walangkaninbossing.eshcat.util.TimeUtil
import com.walangkaninbossing.eshcat.viewmodel.AdminViewModel
import com.walangkaninbossing.eshcat.viewmodel.OfficeRow
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer

@Composable
fun StaffProfileScreen(nav: NavController, sessionVm: StaffSessionViewModel) {
    val adminVm: AdminViewModel = viewModel { AdminViewModel(eshcatContainer()) }
    val session by sessionVm.state.collectAsState()
    val actor = session.user
    if (actor == null) {
        PermissionGate(enabled = false) {}
        return
    }
    var offices by remember { mutableStateOf<List<OfficeRow>>(emptyList()) }
    var departments by remember { mutableStateOf<List<DepartmentEntity>>(emptyList()) }
    LaunchedEffect(Unit) {
        offices = adminVm.officesOnce()
        departments = adminVm.departmentsOnce()
    }
    val initials = actor.fullName.trim().split(" ")
        .filter { it.isNotBlank() }.take(2).map { it.first() }.joinToString("").uppercase()
    val roleName = session.role?.name ?: ""
    val officeName = offices.find { it.office.id == actor.officeId }?.office?.name ?: "Unknown office"
    val deptName = departments.find { it.id == actor.departmentId }?.name ?: "Unknown department"
    val lastLogin = actor.lastLogin?.let { TimeUtil.displayDateTime(it) } ?: "Never"
    val perms = session.permissions.sorted()
    val visiblePerms = perms.take(12)
    val extraPerms = perms.size - visiblePerms.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader("My Profile", onBack = { nav.popBackStack() }) }
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(88.dp).clip(CircleShape).background(PrimaryContainerLight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        initials,
                        color = OnPrimaryContainerLight,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(EshcatSpacing.md))
                Text(actor.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(actor.username, style = MaterialTheme.typography.bodyMedium, color = TextMutedLight)
                Spacer(Modifier.height(8.dp))
                RoleBadge(roleName)
            }
        }
        item {
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md)) {
                    LabeledField("Full Name", actor.fullName)
                    LabeledField("Username", actor.username, mono = true)
                    LabeledField("Role", Roles.label(roleName))
                    LabeledField("Office", officeName)
                    LabeledField("Department", deptName)
                    LabeledField("Status", AccountStatuses.label(actor.status))
                    LabeledField("Last Login", lastLogin)
                }
            }
        }
        item { SectionHeader("Permissions") }
        if (visiblePerms.isEmpty()) {
            item {
                Text(
                    "No permissions assigned to this account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = EshcatSpacing.sm),
                )
            }
        } else {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                    visiblePerms.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                            pair.forEach { perm ->
                                PermissionPill(Permissions.description(perm), Modifier.weight(1f))
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                    if (extraPerms > 0) {
                        Text(
                            "+$extraPerms more",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMutedLight,
                        )
                    }
                }
            }
        }
        item {
            LabeledField(
                "Note",
                "Role permissions are managed by administrators.",
            )
        }
    }
}

@Composable
private fun PermissionPill(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(EshcatRadius.sm),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            maxLines = 2,
        )
    }
}