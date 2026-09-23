package com.walangkaninbossing.eshcat.ui.screens.citizen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.ui.components.EmptyState
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.LoadingState
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.Primary
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.TextMutedLight
import com.walangkaninbossing.eshcat.ui.theme.WarningAccent
import com.walangkaninbossing.eshcat.util.TimeUtil
import com.walangkaninbossing.eshcat.viewmodel.CitizenViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer

@Composable
fun AnnouncementDetailScreen(nav: NavController, id: Int) {
    val vm: CitizenViewModel = viewModel { CitizenViewModel(eshcatContainer()) }
    val announcements by vm.announcements.collectAsState()
    val row = announcements.find { it.announcement.id == id }

    Column(modifier = Modifier.fillMaxSize()) {
        if (row == null) {
            ScreenHeader(title = "Announcement", onBack = { nav.popBackStack() })
            if (announcements.isEmpty()) {
                LoadingState(modifier = Modifier.padding(top = EshcatSpacing.xxl))
            } else {
                EmptyState(
                    icon = Icons.Filled.Campaign,
                    title = "Announcement not found",
                )
            }
        } else {
            val ann = row.announcement
            ScreenHeader(title = ann.title, onBack = { nav.popBackStack() })

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = EshcatSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md),
            ) {
                if (ann.pinned) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = WarningAccent, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Pinned announcement",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = WarningAccent,
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "${row.department?.name ?: "LGU Catarman"} · ${TimeUtil.displayDate(ann.date)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedLight,
                    )
                }

                item {
                    Text(
                        text = ann.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                            Icon(Icons.Filled.Call, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                            Column {
                                Text(
                                    text = "Contact the LGU",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = "LGU Catarman",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}