package com.walangkaninbossing.eshcat.ui.screens.staff

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.auth.Permissions
import com.walangkaninbossing.eshcat.core.Roles
import com.walangkaninbossing.eshcat.core.Statuses
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.HistoryTimeline
import com.walangkaninbossing.eshcat.ui.components.LabeledField
import com.walangkaninbossing.eshcat.ui.components.LoadingState
import com.walangkaninbossing.eshcat.ui.components.PermissionGate
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.SecondaryButton
import com.walangkaninbossing.eshcat.ui.components.SectionHeader
import com.walangkaninbossing.eshcat.ui.components.StatusBadge
import com.walangkaninbossing.eshcat.ui.components.statusVisual
import com.walangkaninbossing.eshcat.ui.theme.EshcatRadius
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.OnPrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.Primary
import com.walangkaninbossing.eshcat.ui.theme.PrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.SuccessAccent
import com.walangkaninbossing.eshcat.ui.theme.SurfaceVariantLight
import com.walangkaninbossing.eshcat.ui.theme.TextMutedLight
import com.walangkaninbossing.eshcat.ui.theme.TextSecondaryLight
import com.walangkaninbossing.eshcat.viewmodel.StaffApplicationDetail
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel
import com.walangkaninbossing.eshcat.viewmodel.StaffViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer
import kotlinx.coroutines.launch

@Composable
fun StaffChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(EshcatRadius.pill),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun StaffApplicationDetailScreen(
    nav: NavController,
    id: Int,
    sessionVm: StaffSessionViewModel,
) {
    val staffVm: StaffViewModel = viewModel { StaffViewModel(eshcatContainer()) }
    val session by sessionVm.state.collectAsState()
    val actor = session.user
    if (actor == null) {
        PermissionGate(enabled = false) {}
        return
    }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var detail by remember { mutableStateOf<StaffApplicationDetail?>(null) }
    var reloadKey by remember { mutableStateOf(0) }
    var pendingStatus by remember { mutableStateOf<String?>(null) }
    var statusNote by remember { mutableStateOf("") }
    var selectedStaffId by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(id, reloadKey) { detail = staffVm.applicationDetail(id) }
    val roleName = session.role?.name ?: ""
    val roleIsAdmin = roleName == Roles.ADMIN || roleName == Roles.SUPER_ADMIN

    fun updateStatus(newStatus: String, note: String?) {
        scope.launch {
            val err = staffVm.updateStatus(id, newStatus, note, actor, roleName)
            Toast.makeText(ctx, err ?: "Status updated.", Toast.LENGTH_SHORT).show()
            pendingStatus = null
            statusNote = ""
            reloadKey++
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val d = detail
        item {
            ScreenHeader("Application ${d?.app?.referenceNumber ?: ""}", onBack = { nav.popBackStack() })
        }
        if (d == null) {
            item { LoadingState() }
        } else {
            item {
                GlassCard {
                    StatusBadge(statusVisual(d.app.status))
                    Spacer(Modifier.height(EshcatSpacing.sm))
                    Text(d.serviceName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text(d.app.fullName, style = MaterialTheme.typography.bodyMedium, color = TextSecondaryLight)
                    Spacer(Modifier.height(2.dp))
                    Text("Office: ${d.officeName}", style = MaterialTheme.typography.bodySmall, color = TextMutedLight)
                }
            }
            item {
                GlassCard {
                    Text("Applicant Information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(EshcatSpacing.md))
                    Column(verticalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                        LabeledField("Full name", d.app.fullName)
                        LabeledField("Email", d.app.email.ifBlank { "-" })
                        LabeledField("Mobile", d.app.mobile.ifBlank { "-" })
                        LabeledField("Address", d.app.address.ifBlank { "-" })
                        LabeledField("Request details", d.app.requestDetails.ifBlank { "-" })
                    }
                }
            }
            item {
                GlassCard {
                    Text("Service & Reference", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(EshcatSpacing.md))
                    Column(verticalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                        LabeledField("Service", d.serviceName)
                        LabeledField("Office", d.officeName)
                        LabeledField("Reference", d.app.referenceNumber, mono = true)
                    }
                }
            }
            item {
                GlassCard {
                    Text("Requirements", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(EshcatSpacing.md))
                    if (d.requirements.isEmpty()) {
                        Text("No additional requirements recorded.", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                            d.requirements.forEach { req ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = SuccessAccent,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(Modifier.width(EshcatSpacing.sm))
                                    Text(req.description, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
            if (sessionVm.has(Permissions.UPDATE_APPLICATION_STATUS)) {
                val nextStatuses = Statuses.nextStatuses(roleName, d.app.status)
                if (nextStatuses.isNotEmpty()) {
                    item { SectionHeader("Update Status") }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                            items(nextStatuses) { st ->
                                StaffChip(Statuses.label(st), false, {
                                    pendingStatus = st
                                    statusNote = ""
                                })
                            }
                        }
                    }
                }
                if (roleIsAdmin && sessionVm.has(Permissions.REQUEST_REQUIREMENTS)) {
                    item {
                        SecondaryButton("Request Additional Requirements") {
                            pendingStatus = Statuses.ADDITIONAL_REQUIREMENTS
                            statusNote = ""
                        }
                    }
                }
            }
            if (sessionVm.has(Permissions.ASSIGN_APPLICATIONS) && d.assignableUsers.isNotEmpty()) {
                item { SectionHeader("Route to staff") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                        items(d.assignableUsers, key = { it.id }) { u ->
                            StaffChip(u.fullName, selectedStaffId == u.id, { selectedStaffId = u.id })
                        }
                    }
                }
                item {
                    SecondaryButton("Assign", enabled = selectedStaffId != null) {
                        scope.launch {
                            val target = selectedStaffId ?: return@launch
                            val err = staffVm.assignApplication(id, target, actor)
                            Toast.makeText(ctx, err ?: "Application routed.", Toast.LENGTH_SHORT).show()
                            reloadKey++
                        }
                    }
                }
            }
            item { SectionHeader("History") }
            item {
                HistoryTimeline(d.history, userNameFor = { changedBy -> d.staffNames[changedBy ?: -1] ?: "Applicant" })
            }
        }
    }

    pendingStatus?.let { st ->
        AlertDialog(
            onDismissRequest = { pendingStatus = null },
            title = { Text("Update status") },
            text = {
                Column {
                    Text("Move this application to ${Statuses.label(st)}?")
                    Spacer(Modifier.height(EshcatSpacing.md))
                    OutlinedTextField(
                        value = statusNote,
                        onValueChange = { statusNote = it },
                        label = { Text("Note (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(EshcatRadius.md),
                        minLines = 2,
                        maxLines = 4,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { updateStatus(st, statusNote.trim().ifEmpty { null }) }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { pendingStatus = null }) { Text("Cancel") }
            },
        )
    }
}