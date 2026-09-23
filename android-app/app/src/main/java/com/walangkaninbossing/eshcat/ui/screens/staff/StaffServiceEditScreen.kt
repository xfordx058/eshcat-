package com.walangkaninbossing.eshcat.ui.screens.staff

import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.walangkaninbossing.eshcat.data.local.entity.ServiceEntity
import com.walangkaninbossing.eshcat.ui.components.FilterChipsRow
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.LoadingState
import com.walangkaninbossing.eshcat.ui.components.PermissionGate
import com.walangkaninbossing.eshcat.ui.components.PrimaryButton
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.SectionHeader
import com.walangkaninbossing.eshcat.ui.theme.EshcatRadius
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.TextSecondaryLight
import com.walangkaninbossing.eshcat.viewmodel.AdminViewModel
import com.walangkaninbossing.eshcat.viewmodel.OfficeRow
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer
import kotlinx.coroutines.launch

private val serviceCategories = listOf(
    "Civil Registry",
    "Engineering",
    "Business",
    "Social Welfare",
    "Treasury",
    "Cedula & Taxes",
    "Health",
    "Agriculture",
    "Planning",
    "General Services",
)

@Composable
fun StaffServiceEditScreen(
    nav: NavController,
    serviceId: Int,
    sessionVm: StaffSessionViewModel,
) {
    val adminVm: AdminViewModel = viewModel { AdminViewModel(eshcatContainer()) }
    val session by sessionVm.state.collectAsState()
    val actor = session.user
    if (actor == null) {
        PermissionGate(enabled = false) {}
        return
    }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var offices by remember { mutableStateOf<List<OfficeRow>>(emptyList()) }
    var existing by remember { mutableStateOf<ServiceEntity?>(null) }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var officeId by remember { mutableStateOf(0) }
    var description by remember { mutableStateOf("") }
    var processing by remember { mutableStateOf("") }
    var online by remember { mutableStateOf(true) }
    var requirementsText by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(serviceId) {
        val loadedOffices = adminVm.officesOnce()
        offices = loadedOffices
        if (serviceId > 0) {
            val s = adminVm.servicesOnce().find { it.service.id == serviceId }
            existing = s?.service
            name = s?.service?.name ?: ""
            category = s?.service?.category ?: ""
            officeId = s?.service?.officeId ?: 0
            description = s?.service?.description ?: ""
            processing = s?.service?.processingInfo ?: ""
            online = s?.service?.onlineAvailable ?: false
            requirementsText = adminVm.requirementsFor(serviceId).joinToString("\n") { it.description }
        } else if (loadedOffices.isNotEmpty()) {
            officeId = loadedOffices.first().office.id
        }
        loading = false
    }

    val isEdit = serviceId > 0
    val officeOptions = offices.map { it.office.name }
    val selectedOfficeName = offices.find { it.office.id == officeId }?.office?.name ?: ""
    val canSave = !loading && name.isNotBlank() && category.isNotBlank() && officeId > 0 && description.isNotBlank()

    if (loading) {
        LoadingState(Modifier.fillMaxSize())
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(horizontal = ScreenPadding, vertical = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenHeader(if (isEdit) "Edit Service" else "New Service", onBack = { nav.popBackStack() })
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(EshcatRadius.md),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(EshcatRadius.md),
                    minLines = 3,
                )
                OutlinedTextField(
                    value = processing,
                    onValueChange = { processing = it },
                    label = { Text("Processing Info") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(EshcatRadius.md),
                    minLines = 2,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Online Available", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text("Citizens can apply through the portal", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                    }
                    Switch(checked = online, onCheckedChange = { online = it })
                }
            }
        }
        SectionHeader("Category")
        FilterChipsRow(serviceCategories, category, { category = it })
        SectionHeader("Office")
        FilterChipsRow(officeOptions, selectedOfficeName, { offName ->
            offices.find { it.office.name == offName }?.let { officeId = it.office.id }
        })
        SectionHeader("Requirements")
        OutlinedTextField(
            value = requirementsText,
            onValueChange = { requirementsText = it },
            label = { Text("Requirements (one per line)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(EshcatRadius.md),
            minLines = 4,
        )
        Spacer(Modifier.height(4.dp))
        PrimaryButton("Save", enabled = canSave, loading = saving) {
            scope.launch {
                saving = true
                val reqList = requirementsText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                val err = if (isEdit) {
                    val base = existing ?: run {
                        Toast.makeText(ctx, "Service not found.", Toast.LENGTH_SHORT).show()
                        saving = false
                        return@launch
                    }
                    adminVm.updateService(
                        actor,
                        base.copy(
                            name = name.trim(),
                            category = category.trim(),
                            officeId = officeId,
                            description = description.trim(),
                            processingInfo = processing.trim(),
                            onlineAvailable = online,
                        ),
                        reqList,
                    )
                } else {
                    adminVm.createService(
                        actor,
                        name,
                        category,
                        officeId,
                        description,
                        processing,
                        reqList,
                        online,
                    )
                }
                Toast.makeText(ctx, err ?: (if (isEdit) "Service updated." else "Service created."), Toast.LENGTH_SHORT).show()
                saving = false
                if (err == null) nav.popBackStack()
            }
        }
    }
}