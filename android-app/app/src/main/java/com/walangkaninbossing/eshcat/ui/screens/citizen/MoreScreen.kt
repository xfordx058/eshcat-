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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.theme.EshcatRadius
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.Primary
import com.walangkaninbossing.eshcat.ui.theme.PrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.TextMutedLight
import com.walangkaninbossing.eshcat.ui.theme.TextSecondaryLight

private data class MoreItem(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
)

@Composable
fun MoreScreen(nav: NavController) {
    val publicServices = remember {
        listOf(
            MoreItem("Appointments", "Schedule an office visit", Icons.Filled.CalendarMonth, Routes.APPOINTMENTS),
            MoreItem("Report Concern", "Submit community reports or feedback", Icons.Filled.ReportProblem, Routes.REPORTS),
            MoreItem("Announcements", "Latest municipal news and updates", Icons.Filled.Campaign, Routes.ANNOUNCEMENTS),
            MoreItem("Offices Directory", "Contact info and operating hours", Icons.Filled.Business, Routes.OFFICES),
        )
    }

    val generalItems = remember {
        listOf(
            MoreItem("About eSHCAT", "App information & municipal details", Icons.Filled.Info, Routes.ABOUT),
            MoreItem("Settings", "App preferences & configurations", Icons.Filled.Settings, Routes.SETTINGS),
            MoreItem("Staff Portal", "Staff and administrator login", Icons.Filled.Lock, Routes.STAFF_LOGIN),
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "More Options")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding, vertical = EshcatSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md),
        ) {
            Text(
                text = "Services & Community",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
                Column {
                    publicServices.forEachIndexed { index, item ->
                        MoreListRow(
                            item = item,
                            onClick = { nav.navigate(item.route) },
                        )
                        if (index < publicServices.lastIndex) {
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(EshcatSpacing.xs))

            Text(
                text = "General & Portal Access",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )

            GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
                Column {
                    generalItems.forEachIndexed { index, item ->
                        MoreListRow(
                            item = item,
                            onClick = { nav.navigate(item.route) },
                        )
                        if (index < generalItems.lastIndex) {
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(EshcatSpacing.xs))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = TextMutedLight,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Catarman, Northern Samar Municipal Portal",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun MoreListRow(
    item: MoreItem,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = EshcatSpacing.md, vertical = EshcatSpacing.md),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(EshcatRadius.md))
                    .background(PrimaryContainerLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(EshcatSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(EshcatSpacing.xs))
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextMutedLight,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}