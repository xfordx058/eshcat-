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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.ApplicationCard
import com.walangkaninbossing.eshcat.ui.components.PermissionGate
import com.walangkaninbossing.eshcat.ui.components.RoleBadge
import com.walangkaninbossing.eshcat.ui.components.SectionHeader
import com.walangkaninbossing.eshcat.ui.components.StatCard
import com.walangkaninbossing.eshcat.ui.components.StatCardSkeleton
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.OnPrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.PrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.viewmodel.AppScope
import com.walangkaninbossing.eshcat.viewmodel.ApplicationRow
import com.walangkaninbossing.eshcat.viewmodel.DashboardStats
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel
import com.walangkaninbossing.eshcat.viewmodel.StaffViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer

@Composable
fun StaffDashboardScreen(nav: NavController, sessionVm: StaffSessionViewModel) {
    val staffVm: StaffViewModel = viewModel { StaffViewModel(eshcatContainer()) }
    val session by sessionVm.state.collectAsState()
    val actor = session.user
    if (actor == null) {
        PermissionGate(enabled = false) {}
        return
    }
    var stats by remember { mutableStateOf<DashboardStats?>(null) }
    var recent by remember { mutableStateOf<List<ApplicationRow>>(emptyList()) }
    var reloadKey by remember { mutableStateOf(0) }
    LaunchedEffect(reloadKey) {
        staffVm.dashboardStats()?.let { stats = it }
        recent = staffVm.applicationsOnce(AppScope.ALL, actor).take(4)
    }
    val firstName = actor.fullName.trim().substringBefore(" ")
    val initials = actor.fullName.trim().split(" ")
        .filter { it.isNotBlank() }.take(2).map { it.first() }.joinToString("").uppercase()
    val roleName = session.role?.name ?: ""
    val total = stats?.total ?: 0
    val underReview = stats?.underReview ?: 0
    val approved = stats?.approved ?: 0
    val ready = stats?.ready ?: 0
    val completed = stats?.completed ?: 0
    val pendingAppointments = stats?.pendingAppointments ?: 0
    val pendingReports = stats?.pendingReports ?: 0
    val totalUsers = stats?.totalUsers ?: 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(54.dp).clip(CircleShape).background(PrimaryContainerLight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        initials,
                        color = OnPrimaryContainerLight,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(EshcatSpacing.md))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Good day, $firstName",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(4.dp))
                    RoleBadge(roleName)
                }
            }
        }
        if (stats == null) {
            items(3) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCardSkeleton(Modifier.weight(1f))
                    StatCardSkeleton(Modifier.weight(1f))
                }
            }
        } else {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        Icons.Filled.Description,
                        "$total",
                        "Total Applications",
                        Modifier.weight(1f),
                        onClick = { nav.navigate(Routes.STAFF_APPLICATIONS) },
                    )
                    StatCard(Icons.Filled.Search, "$underReview", "For Review", Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Icons.Filled.CheckCircle, "$approved", "Approved", Modifier.weight(1f))
                    StatCard(Icons.Filled.Inventory, "$ready", "Ready for Release", Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Icons.Filled.TaskAlt, "$completed", "Completed", Modifier.weight(1f))
                    StatCard(
                        Icons.Filled.CalendarMonth,
                        "$pendingAppointments",
                        "Pending Appointments",
                        Modifier.weight(1f),
                        onClick = { nav.navigate(Routes.STAFF_APPOINTMENTS) },
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        Icons.Filled.ReportProblem,
                        "$pendingReports",
                        "New Reports",
                        Modifier.weight(1f),
                        onClick = { nav.navigate(Routes.STAFF_REPORTS) },
                    )
                    StatCard(Icons.Filled.People, "$totalUsers", "Site Users", Modifier.weight(1f))
                }
            }
        }
        item {
            SectionHeader(
                "Recent applications",
                actionLabel = "View all",
                onAction = { nav.navigate(Routes.STAFF_APPLICATIONS) },
            )
        }
        items(recent, key = { it.app.id }) { row ->
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
