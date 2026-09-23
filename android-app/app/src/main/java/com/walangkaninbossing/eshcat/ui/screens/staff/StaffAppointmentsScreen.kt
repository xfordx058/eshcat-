package com.walangkaninbossing.eshcat.ui.screens.staff

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.walangkaninbossing.eshcat.core.AppointmentStatuses
import com.walangkaninbossing.eshcat.ui.components.EmptyState
import com.walangkaninbossing.eshcat.ui.components.FilterChipsRow
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.InfoRow
import com.walangkaninbossing.eshcat.ui.components.PermissionGate
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.StatusBadge
import com.walangkaninbossing.eshcat.ui.components.statusVisual
import com.walangkaninbossing.eshcat.ui.theme.EshcatRadius
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.util.TimeUtil
import com.walangkaninbossing.eshcat.viewmodel.AppointmentRow
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel
import com.walangkaninbossing.eshcat.viewmodel.StaffViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer
import kotlinx.coroutines.launch

@Composable
fun StaffAppointmentsScreen(nav: NavController, sessionVm: StaffSessionViewModel) {
    val staffVm: StaffViewModel = viewModel { StaffViewModel(eshcatContainer()) }
    val session by sessionVm.state.collectAsState()
    val actor = session.user
    if (actor == null) {
        PermissionGate(enabled = false) {}
        return
    }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var rows by remember { mutableStateOf<List<AppointmentRow>>(emptyList()) }
    var reloadKey by remember { mutableStateOf(0) }
    var filter by remember { mutableStateOf("All") }
    LaunchedEffect(reloadKey) { rows = staffVm.appointmentsOnce() }
    val options = listOf("All") + AppointmentStatuses.all
    val filtered = remember(rows, filter) {
        if (filter == "All") rows else rows.filter { it.appointment.status == filter }
    }
    val plural = if (filtered.size == 1) "" else "s"

    fun setStatus(appointmentId: Int, status: String) {
        scope.launch {
            val err = staffVm.updateAppointmentStatus(appointmentId, status, actor)
            Toast.makeText(ctx, err ?: "Appointment updated.", Toast.LENGTH_SHORT).show()
            reloadKey++
        }
    }

    PermissionGate(enabled = sessionVm.has(Permissions.VIEW_APPOINTMENTS)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { ScreenHeader("Appointments", subtitle = "${filtered.size} appointment$plural") }
            item { FilterChipsRow(options, filter, { filter = it }) }
            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.CalendarMonth,
                        "No appointments",
                        "No appointments match this filter.",
                    )
                }
            }
            items(filtered, key = { it.appointment.id }) { row ->
                val app = row.appointment
                GlassCard {
                    Text(row.officeName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(app.fullName, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(2.dp))
                    InfoRow("Schedule", "${TimeUtil.displayDate(app.date)} · ${app.time}")
                    InfoRow("Purpose", app.purpose)
                    Spacer(Modifier.height(8.dp))
                    StatusBadge(statusVisual(app.status))
                    if (sessionVm.has(Permissions.MANAGE_APPOINTMENTS)) {
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            when (app.status) {
                                AppointmentStatuses.PENDING -> {
                                    OutlinedButton(
                                        onClick = { setStatus(app.id, AppointmentStatuses.APPROVED) },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        shape = RoundedCornerShape(EshcatRadius.md),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    ) { Text("Confirm") }
                                    OutlinedButton(
                                        onClick = { setStatus(app.id, AppointmentStatuses.CANCELLED) },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        shape = RoundedCornerShape(EshcatRadius.md),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    ) { Text("Cancel", color = MaterialTheme.colorScheme.error) }
                                }
                                AppointmentStatuses.APPROVED -> {
                                    OutlinedButton(
                                        onClick = { setStatus(app.id, AppointmentStatuses.COMPLETED) },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        shape = RoundedCornerShape(EshcatRadius.md),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    ) { Text("Complete") }
                                    OutlinedButton(
                                        onClick = { setStatus(app.id, AppointmentStatuses.CANCELLED) },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        shape = RoundedCornerShape(EshcatRadius.md),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    ) { Text("Cancel", color = MaterialTheme.colorScheme.error) }
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}