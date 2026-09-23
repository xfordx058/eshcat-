package com.walangkaninbossing.eshcat.ui.screens.citizen

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.AnnouncementCard
import com.walangkaninbossing.eshcat.ui.components.AnnouncementCardSkeleton
import com.walangkaninbossing.eshcat.ui.components.EshcatSearchBar
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.SectionHeader
import com.walangkaninbossing.eshcat.ui.components.ServiceCard
import com.walangkaninbossing.eshcat.ui.components.ServiceCardSkeleton
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.Primary
import com.walangkaninbossing.eshcat.ui.theme.PrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.Secondary
import com.walangkaninbossing.eshcat.ui.theme.SecondaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.WarningAccent
import com.walangkaninbossing.eshcat.ui.theme.WarningContainerAccent
import com.walangkaninbossing.eshcat.viewmodel.CitizenViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer
import java.util.Calendar

@Composable
fun HomeScreen(nav: NavController) {
    val vm: CitizenViewModel = viewModel { CitizenViewModel(eshcatContainer()) }
    val announcements by vm.announcements.collectAsState()
    val pinned by vm.pinnedAnnouncements.collectAsState()
    val features by vm.featuresRow.collectAsState()
    val isDataReady by vm.isDataReady.collectAsState()

    val pinnedRows = if (pinned.isNotEmpty()) pinned else announcements.take(3)
    var query by remember { mutableStateOf("") }

    val openTab: (String) -> Unit = { route ->
        nav.navigate(route) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        in 17..21 -> "Good evening,"
        else -> "Good day,"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A1226)),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = ScreenPadding, vertical = EshcatSpacing.xl),
            ) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB6C2D4),
                )
                Text(
                    text = "Catarman",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Your municipal e-services, one app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8494A8),
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = EshcatSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md),
        ) {
            item {
                EshcatSearchBar(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Search services",
                    keyboardActions = KeyboardActions(onSearch = { openTab(Routes.SERVICES) }),
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.md)) {
                        QuickActionTile(
                            icon = Icons.Filled.Assignment,
                            label = "Apply Online",
                            tint = Primary,
                            tintContainer = PrimaryContainerLight,
                            onClick = { openTab(Routes.SERVICES) },
                            modifier = Modifier.weight(1f),
                        )
                        QuickActionTile(
                            icon = Icons.Filled.History,
                            label = "Track Application",
                            tint = Secondary,
                            tintContainer = SecondaryContainerLight,
                            onClick = { openTab(Routes.TRACK) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.md)) {
                        QuickActionTile(
                            icon = Icons.Filled.CalendarMonth,
                            label = "Book Appointment",
                            tint = Secondary,
                            tintContainer = SecondaryContainerLight,
                            onClick = { nav.navigate(Routes.APPOINTMENTS) },
                            modifier = Modifier.weight(1f),
                        )
                        QuickActionTile(
                            icon = Icons.Filled.ReportProblem,
                            label = "Report Concern",
                            tint = WarningAccent,
                            tintContainer = WarningContainerAccent,
                            onClick = { nav.navigate(Routes.REPORTS) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Latest Announcements",
                    actionLabel = "See all",
                    onAction = { nav.navigate(Routes.ANNOUNCEMENTS) },
                )
            }
            if (!isDataReady) {
                items(2) { AnnouncementCardSkeleton() }
            } else if (pinnedRows.isEmpty()) {
                item { Text("No announcements yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(pinnedRows, key = { it.announcement.id }) { row ->
                    AnnouncementCard(
                        ann = row.announcement,
                        departmentName = row.department?.name ?: "LGU Catarman",
                        onClick = { nav.navigate(Routes.announcementDetail(row.announcement.id)) },
                    )
                }
            }

            item {
                SectionHeader(
                    title = "City Services",
                    actionLabel = "Browse all",
                    onAction = { openTab(Routes.SERVICES) },
                )
            }
            if (!isDataReady) {
                items(3) { ServiceCardSkeleton() }
            } else if (features.isEmpty()) {
                item { Text("No online services are available yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(features, key = { it.service.id }) { row ->
                    ServiceCard(
                        service = row.service,
                        officeName = row.office?.name ?: "",
                        onClick = { nav.navigate(Routes.serviceDetail(row.service.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    tint: Color,
    tintContainer: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier, onClick = onClick, contentPadding = PaddingValues(EshcatSpacing.md)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tintContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
