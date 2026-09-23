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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.ApplicationFlowTimeline
import com.walangkaninbossing.eshcat.ui.components.EmptyState
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.HistoryTimeline
import com.walangkaninbossing.eshcat.ui.components.LabeledField
import com.walangkaninbossing.eshcat.ui.components.LoadingState
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.SecondaryButton
import com.walangkaninbossing.eshcat.ui.components.StatusBadge
import com.walangkaninbossing.eshcat.ui.components.statusVisual
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.Primary
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.TextSecondaryLight
import com.walangkaninbossing.eshcat.viewmodel.ApplicationFlowViewModel
import com.walangkaninbossing.eshcat.viewmodel.TrackResult
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer

@Composable
fun TrackResultScreen(nav: NavController, ref: String) {
    val vm: ApplicationFlowViewModel = viewModel { ApplicationFlowViewModel(eshcatContainer()) }
    var result by remember { mutableStateOf<TrackResult?>(null) }
    var loading by remember { mutableStateOf(true) }
    var attempt by remember { mutableStateOf(0) }

    LaunchedEffect(ref, attempt) {
        loading = true
        result = vm.track(ref)
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Application Status", onBack = { nav.popBackStack() })

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingState()
            }
        } else if (result == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ScreenPadding, vertical = EshcatSpacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md),
            ) {
                EmptyState(
                    icon = Icons.Filled.Info,
                    title = "Not found",
                    subtitle = "No application matches that reference number.",
                )
                SecondaryButton(
                    text = "Try again",
                    onClick = { attempt++ },
                )
            }
        } else {
            val data = result!!
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = EshcatSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md),
            ) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        StatusBadge(statusVisual(data.app.status))
                        Spacer(Modifier.height(EshcatSpacing.md))
                        Text(
                            text = data.serviceName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = data.app.referenceNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondaryLight,
                        )
                    }
                }

                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(EshcatSpacing.sm))
                        ApplicationFlowTimeline(
                            currentStatus = data.app.status,
                            latestNote = data.history.lastOrNull()?.note,
                        )
                    }
                }

                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        LabeledField(label = "Service name", value = data.serviceName)
                        Spacer(Modifier.height(EshcatSpacing.sm))
                        LabeledField(label = "Reference", value = data.app.referenceNumber, mono = true)
                        Spacer(Modifier.height(EshcatSpacing.sm))
                        LabeledField(label = "Applicant", value = data.app.fullName)
                        Spacer(Modifier.height(EshcatSpacing.sm))
                        LabeledField(label = "Office", value = data.officeName)
                    }
                }

                if (!data.app.processingNotes.isNullOrBlank()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Processing notes",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(EshcatSpacing.sm))
                            Text(
                                text = data.app.processingNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Activity Log",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(EshcatSpacing.sm))
                        HistoryTimeline(
                            history = data.history,
                            userNameFor = { "Office Staff" },
                        )
                    }
                }

                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                            Icon(Icons.Filled.Call, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Contact your office",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.height(EshcatSpacing.sm))
                        Text(
                            text = "For updates, call ${data.officeName} or visit the municipal hall during office hours.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}