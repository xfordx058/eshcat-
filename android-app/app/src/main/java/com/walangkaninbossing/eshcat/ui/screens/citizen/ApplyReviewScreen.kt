package com.walangkaninbossing.eshcat.ui.screens.citizen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.widget.Toast
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.EmptyState
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.LabeledField
import com.walangkaninbossing.eshcat.ui.components.LoadingState
import com.walangkaninbossing.eshcat.ui.components.PrimaryButton
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.theme.EshcatRadius
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.viewmodel.ApplicationDraft
import com.walangkaninbossing.eshcat.viewmodel.ApplicationFlowViewModel
import com.walangkaninbossing.eshcat.viewmodel.CitizenViewModel
import com.walangkaninbossing.eshcat.viewmodel.ServiceDetailData
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer
import kotlinx.coroutines.launch

@Composable
fun ApplyReviewScreen(nav: NavController, serviceId: Int) {
    val vm: CitizenViewModel = viewModel { CitizenViewModel(eshcatContainer()) }
    val appVm: ApplicationFlowViewModel = viewModel { ApplicationFlowViewModel(eshcatContainer()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var detail by remember { mutableStateOf<ServiceDetailData?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }

    LaunchedEffect(serviceId) {
        detail = vm.detailsFor(serviceId)
        loaded = true
    }

    if (!loaded) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(title = "Review Application", onBack = { nav.popBackStack() })
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingState()
            }
        }
        return
    }

    val data = detail
    if (data == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(title = "Review Application", onBack = { nav.popBackStack() })
            EmptyState(icon = Icons.Filled.Info, title = "Service not found")
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Review Application", onBack = { nav.popBackStack() })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = EshcatSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md),
        ) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    LabeledField(label = "Service", value = data.service.name)
                    Spacer(Modifier.height(EshcatSpacing.sm))
                    LabeledField(label = "Reference prefix", value = "CAT-${data.prefix}", mono = true)
                    Spacer(Modifier.height(EshcatSpacing.sm))
                    LabeledField(label = "Full name", value = CitizenApplyState.fullName)
                    Spacer(Modifier.height(EshcatSpacing.sm))
                    LabeledField(label = "Email", value = CitizenApplyState.email.ifBlank { "Not provided" })
                    Spacer(Modifier.height(EshcatSpacing.sm))
                    LabeledField(label = "Mobile", value = CitizenApplyState.mobile)
                    Spacer(Modifier.height(EshcatSpacing.sm))
                    LabeledField(label = "Address", value = CitizenApplyState.address)
                    Spacer(Modifier.height(EshcatSpacing.sm))
                    LabeledField(label = "Details", value = CitizenApplyState.requestDetails)
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                    Text(
                        text = "Requirements to prepare",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (CitizenApplyState.acknowledged.isEmpty()) {
                        Text(
                            text = "None acknowledged.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                            items(CitizenApplyState.acknowledged) { req ->
                                FilterChip(
                                    selected = true,
                                    onClick = {},
                                    label = { Text(req, maxLines = 1) },
                                    shape = RoundedCornerShape(EshcatRadius.pill),
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "By submitting you agree your details are true and correct.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                PrimaryButton(
                    text = "Submit Application",
                    loading = submitting,
                    onClick = {
                        submitting = true
                        scope.launch {
                            val (saved, error) = appVm.submit(
                                ApplicationDraft(
                                    serviceId = serviceId,
                                    officeId = data.service.officeId,
                                    departmentId = data.office?.departmentId ?: 0,
                                    prefix = data.prefix,
                                    fullName = CitizenApplyState.fullName,
                                    email = CitizenApplyState.email,
                                    mobile = CitizenApplyState.mobile,
                                    address = CitizenApplyState.address,
                                    requestDetails = CitizenApplyState.requestDetails,
                                )
                            )
                            submitting = false
                            if (error != null) {
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            } else if (saved != null) {
                                Toast.makeText(context, "Application submitted", Toast.LENGTH_SHORT).show()
                                CitizenApplyState.reset()
                                nav.navigate(Routes.applySuccess(saved.referenceNumber)) {
                                    popUpTo(Routes.SERVICE_DETAIL)
                                }
                            }
                        }
                    },
                )
            }
        }
    }
}