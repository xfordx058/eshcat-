package com.walangkaninbossing.eshcat.ui.screens.citizen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.EmptyState
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.InfoRow
import com.walangkaninbossing.eshcat.ui.components.LoadingState
import com.walangkaninbossing.eshcat.ui.components.PrimaryButton
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.SecondaryButton
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.Primary
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.Secondary
import com.walangkaninbossing.eshcat.ui.theme.SuccessAccent
import com.walangkaninbossing.eshcat.ui.theme.TextMutedLight
import com.walangkaninbossing.eshcat.viewmodel.CitizenViewModel
import com.walangkaninbossing.eshcat.viewmodel.ServiceDetailData
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer

@Composable
fun ServiceDetailScreen(nav: NavController, serviceId: Int) {
    val vm: CitizenViewModel = viewModel { CitizenViewModel(eshcatContainer()) }
    var detail by remember { mutableStateOf<ServiceDetailData?>(null) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(serviceId) {
        detail = vm.detailsFor(serviceId)
        loaded = true
    }

    if (!loaded) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(title = "Service", onBack = { nav.popBackStack() })
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingState()
            }
        }
        return
    }

    val data = detail
    if (data == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(title = "Service", onBack = { nav.popBackStack() })
            EmptyState(icon = Icons.Filled.Info, title = "Service not found")
        }
        return
    }

    val service = data.service
    val office = data.office

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = service.name, onBack = { nav.popBackStack() })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = EshcatSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md),
        ) {
            item {
                GlassCard(contentPadding = PaddingValues(horizontal = EshcatSpacing.lg, vertical = EshcatSpacing.sm)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                        Icon(
                            Icons.Filled.Category,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = service.category,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary,
                        )
                    }
                }
            }

            item {
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = Secondary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Processing Info",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(EshcatSpacing.sm))
                    Text(
                        text = service.processingInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                        Icon(Icons.Filled.Description, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "What to Prepare",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(EshcatSpacing.sm))
                    if (data.requirements.isEmpty()) {
                        Text(
                            text = "No requirements listed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        data.requirements.forEach { req ->
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = SuccessAccent,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(top = 2.dp),
                                )
                                Spacer(Modifier.width(EshcatSpacing.sm))
                                Text(
                                    text = req.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                        Icon(Icons.Filled.Business, contentDescription = null, tint = Secondary, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Office",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(EshcatSpacing.sm))
                    Text(
                        text = office?.name ?: "Catarman LGU",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(EshcatSpacing.sm))
                    if (office != null) {
                        InfoRow(label = "Location", value = office.location)
                        InfoRow(label = "Contact", value = office.contactNumber)
                        InfoRow(label = "Email", value = office.email)
                        InfoRow(label = "Hours", value = office.officeHours)
                    } else {
                        Text(
                            text = "Contact the LGU for details.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (service.onlineAvailable) {
                        PrimaryButton(
                            text = "Apply Online",
                            onClick = { nav.navigate(Routes.apply(service.id)) },
                        )
                        Text(
                            text = "Available online. Your details are validated on submission.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMutedLight,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        SecondaryButton(
                            text = "Walk-in only",
                            onClick = {},
                            enabled = false,
                        )
                        Text(
                            text = "This service is available at the municipal office only.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMutedLight,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}