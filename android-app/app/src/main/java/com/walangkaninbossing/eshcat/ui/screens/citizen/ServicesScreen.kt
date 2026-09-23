package com.walangkaninbossing.eshcat.ui.screens.citizen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.EmptyState
import com.walangkaninbossing.eshcat.ui.components.EshcatSearchBar
import com.walangkaninbossing.eshcat.ui.components.FilterChipsRow
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.ServiceCard
import com.walangkaninbossing.eshcat.ui.components.ServiceCardSkeleton
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.viewmodel.CitizenViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer

@Composable
fun ServicesScreen(nav: NavController) {
    val vm: CitizenViewModel = viewModel { CitizenViewModel(eshcatContainer()) }
    val services by vm.services.collectAsState()
    val isDataReady by vm.isDataReady.collectAsState()
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }

    val categories = remember(services) {
        listOf("All") + services.map { it.service.category }.distinct()
    }

    val filtered = services.filter { row ->
        val matchesCategory = category == "All" || row.service.category == category
        val q = query.trim()
        val matchesQuery = q.isEmpty() ||
            row.service.name.contains(q, ignoreCase = true) ||
            row.service.category.contains(q, ignoreCase = true) ||
            (row.office?.name ?: "").contains(q, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Services", subtitle = "Everything Catarman offers")

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
                )
            }
            item {
                FilterChipsRow(
                    options = categories,
                    selected = category,
                    onSelect = { category = it },
                )
            }
            if (!isDataReady) {
                items(5) { ServiceCardSkeleton() }
            } else if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Search,
                        title = if (query.isBlank() && category == "All") "No services available" else "No services found",
                        subtitle = if (query.isBlank() && category == "All") "Please check back later." else "Try a different search or category.",
                    )
                }
            } else {
                items(filtered, key = { it.service.id }) { row ->
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
