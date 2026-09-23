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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.ui.components.EmptyState
import com.walangkaninbossing.eshcat.ui.components.EshcatSearchBar
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.TextMutedLight
import com.walangkaninbossing.eshcat.ui.theme.TextSecondaryLight
import com.walangkaninbossing.eshcat.viewmodel.CitizenViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer

@Composable
fun OfficesScreen(nav: NavController) {
    val vm: CitizenViewModel = viewModel { CitizenViewModel(eshcatContainer()) }
    val offices by vm.offices.collectAsState()
    var query by remember { mutableStateOf("") }

    val filtered = offices.filter { row ->
        val q = query.trim()
        q.isEmpty() ||
            row.office.name.contains(q, ignoreCase = true) ||
            row.office.location.contains(q, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Offices")

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = EshcatSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md),
        ) {
            item {
                EshcatSearchBar(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Search offices or locations",
                )
            }
            if (filtered.isEmpty()) {
                item {
                    EmptyState(icon = Icons.Filled.Business, title = "No offices listed")
                }
            } else {
                items(filtered, key = { it.office.id }) { row ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = row.department.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMutedLight,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = row.office.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(EshcatSpacing.sm))
                        OfficeInfoRow(icon = Icons.Filled.LocationOn, value = row.office.location)
                        OfficeInfoRow(icon = Icons.Filled.Call, value = row.office.contactNumber)
                        OfficeInfoRow(icon = Icons.Filled.Schedule, value = row.office.officeHours)
                    }
                }
            }
        }
    }
}

@Composable
private fun OfficeInfoRow(icon: ImageVector, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondaryLight, modifier = Modifier.size(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}