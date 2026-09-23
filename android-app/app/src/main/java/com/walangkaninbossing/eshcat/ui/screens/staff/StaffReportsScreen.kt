package com.walangkaninbossing.eshcat.ui.screens.staff

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.auth.Permissions
import com.walangkaninbossing.eshcat.core.ReportStatuses
import com.walangkaninbossing.eshcat.ui.components.EmptyState
import com.walangkaninbossing.eshcat.ui.components.FilterChipsRow
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.InfoRow
import com.walangkaninbossing.eshcat.ui.components.PermissionGate
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.StatusBadge
import com.walangkaninbossing.eshcat.ui.components.statusVisual
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.util.TimeUtil
import com.walangkaninbossing.eshcat.viewmodel.AdminViewModel as AdminVm
import com.walangkaninbossing.eshcat.viewmodel.OfficeRow
import com.walangkaninbossing.eshcat.viewmodel.ReportRow
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel
import com.walangkaninbossing.eshcat.viewmodel.StaffViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer
import kotlinx.coroutines.launch

@Composable
fun StaffReportsScreen(nav: NavController, sessionVm: StaffSessionViewModel) {
    val staffVm: StaffViewModel = viewModel { StaffViewModel(eshcatContainer()) }
    val adminVm: AdminVm = viewModel { AdminVm(eshcatContainer()) }
    val session by sessionVm.state.collectAsState()
    val actor = session.user
    if (actor == null) {
        PermissionGate(enabled = false) {}
        return
    }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var rows by remember { mutableStateOf<List<ReportRow>>(emptyList()) }
    var offices by remember { mutableStateOf<List<OfficeRow>>(emptyList()) }
    var reloadKey by remember { mutableStateOf(0) }
    var filter by remember { mutableStateOf("All") }
    var officePickerFor by remember { mutableStateOf<Int?>(null) }
    var chosenOfficeId by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(reloadKey) {
        offices = adminVm.officesOnce()
        rows = staffVm.reportsOnce()
    }
    val options = listOf("All") + ReportStatuses.all
    val filtered = remember(rows, filter) {
        if (filter == "All") rows else rows.filter { it.report.status == filter }
    }
    val plural = if (filtered.size == 1) "" else "s"

    fun updateStatus(reportId: Int, status: String, officeId: Int?) {
        scope.launch {
            val err = staffVm.updateReportStatus(reportId, status, actor, officeId)
            Toast.makeText(ctx, err ?: "Report updated.", Toast.LENGTH_SHORT).show()
            reloadKey++
        }
    }

    PermissionGate(enabled = sessionVm.has(Permissions.VIEW_REPORTS)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ScreenHeader("Reports", subtitle = "${filtered.size} report$plural") }
            item { FilterChipsRow(options, filter, { filter = it }) }
            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.ReportProblem,
                        "No reports",
                        "No community reports match this filter.",
                    )
                }
            }
            items(filtered, key = { it.report.id }) { row ->
                val report = row.report
                GlassCard {
                    Text(report.category, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(report.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(6.dp))
                    InfoRow("Location", report.location)
                    InfoRow("Reporter", "${report.fullName} · ${report.contactNumber}")
                    InfoRow("Office", row.officeName ?: "-")
                    InfoRow("Date", TimeUtil.displayDateTime(report.createdDate))
                    Spacer(Modifier.height(8.dp))
                    StatusBadge(statusVisual(report.status))
                    if (sessionVm.has(Permissions.MANAGE_REPORTS)) {
                        Spacer(Modifier.height(12.dp))
                        when (report.status) {
                            ReportStatuses.PENDING -> {
                                StaffChip("Start", false, {
                                    chosenOfficeId = report.officeId
                                    officePickerFor = report.id
                                })
                            }
                            ReportStatuses.IN_PROGRESS -> StaffChip("Resolve", false, {
                                updateStatus(report.id, ReportStatuses.RESOLVED, report.officeId)
                            })
                            ReportStatuses.RESOLVED -> StaffChip("Close", false, {
                                updateStatus(report.id, ReportStatuses.CLOSED, report.officeId)
                            })
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    officePickerFor?.let { reportId ->
        AlertDialog(
            onDismissRequest = { officePickerFor = null },
            title = { Text("Assign office") },
            text = {
                Column {
                    Text("Choose the office handling this report.")
                    Spacer(Modifier.height(EshcatSpacing.md))
                    if (offices.isEmpty()) {
                        Text("No offices available.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                            items(offices, key = { it.office.id }) { off ->
                                StaffChip(
                                    off.office.name,
                                    chosenOfficeId == off.office.id,
                                    { chosenOfficeId = off.office.id },
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    updateStatus(reportId, ReportStatuses.IN_PROGRESS, chosenOfficeId)
                    officePickerFor = null
                }) { Text("Start") }
            },
            dismissButton = {
                TextButton(onClick = { officePickerFor = null }) { Text("Cancel") }
            },
        )
    }
}