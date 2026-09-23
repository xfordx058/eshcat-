package com.walangkaninbossing.eshcat.ui.screens.citizen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.AnnouncementCard
import com.walangkaninbossing.eshcat.ui.components.EmptyState
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.viewmodel.CitizenViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer

@Composable
fun AnnouncementsScreen(nav: NavController) {
    val vm: CitizenViewModel = viewModel { CitizenViewModel(eshcatContainer()) }
    val announcements by vm.announcements.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Announcements")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = EshcatSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md),
        ) {
            if (announcements.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Campaign,
                        title = "No announcements yet",
                        subtitle = "Check again later for updates from the LGU.",
                    )
                }
            } else {
                items(announcements, key = { it.announcement.id }) { row ->
                    AnnouncementCard(
                        ann = row.announcement,
                        departmentName = row.department?.name ?: "LGU Catarman",
                        onClick = { nav.navigate(Routes.announcementDetail(row.announcement.id)) },
                    )
                }
            }
        }
    }
}