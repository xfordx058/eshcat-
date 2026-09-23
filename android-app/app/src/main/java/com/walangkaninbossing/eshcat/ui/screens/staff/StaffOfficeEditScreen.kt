package com.walangkaninbossing.eshcat.ui.screens.staff

import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.core.AccountStatuses
import com.walangkaninbossing.eshcat.data.local.entity.DepartmentEntity
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
import com.walangkaninbossing.eshcat.viewmodel.AdminViewModel
import com.walangkaninbossing.eshcat.viewmodel.OfficeRow
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer
import kotlinx.coroutines.launch

@Composable
fun StaffOfficeEditScreen(
    nav: NavController,
    officeId: Int,
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
    var departments by remember { mutableStateOf<List<DepartmentEntity>>(emptyList()) }
    var offices by remember { mutableStateOf<List<OfficeRow>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var departmentId by remember { mutableStateOf(0) }
    var location by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(AccountStatuses.ACTIVE) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(officeId) {
        val loadedDepts = adminVm.departmentsOnce()
        val loadedOffices = adminVm.officesOnce()
        departments = loadedDepts
        offices = loadedOffices
        if (officeId > 0) {
            val row = loadedOffices.find { it.office.id == officeId }
            name = row?.office?.name ?: ""
            departmentId = row?.office?.departmentId ?: 0
            location = row?.office?.location ?: ""
            contact = row?.office?.contactNumber ?: ""
            email = row?.office?.email ?: ""
            hours = row?.office?.officeHours ?: ""
            status = row?.office?.status ?: AccountStatuses.ACTIVE
        } else if (loadedDepts.isNotEmpty()) {
            departmentId = loadedDepts.first().id
        }
        loading = false
    }

    val isEdit = officeId > 0
    val deptOptions = departments.map { it.name }
    val selectedDeptName = departments.find { it.id == departmentId }?.name ?: ""
    val canSave = !loading && name.isNotBlank() && departmentId > 0 && status.isNotBlank()

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
        ScreenHeader(if (isEdit) "Edit Office" else "New Office", onBack = { nav.popBackStack() })
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Office Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(EshcatRadius.md),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(EshcatRadius.md),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact Number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(EshcatRadius.md),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(EshcatRadius.md),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Office Hours") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(EshcatRadius.md),
                    singleLine = true,
                )
            }
        }
        SectionHeader("Department")
        FilterChipsRow(deptOptions, selectedDeptName, { dName ->
            departments.find { it.name == dName }?.let { departmentId = it.id }
        })
        SectionHeader("Status")
        FilterChipsRow(AccountStatuses.all, status, { status = it })
        Spacer(Modifier.height(4.dp))
        PrimaryButton("Save", enabled = canSave, loading = saving) {
            scope.launch {
                saving = true
                val err = if (isEdit) {
                    val office = offices.find { it.office.id == officeId }?.office
                    if (office == null) {
                        "Office not found."
                    } else {
                        adminVm.updateOffice(
                            actor,
                            office.copy(
                                name = name.trim(),
                                departmentId = departmentId,
                                location = location.trim(),
                                contactNumber = contact.trim(),
                                email = email.trim(),
                                officeHours = hours.trim(),
                                status = status,
                            ),
                        )
                    }
                } else {
                    adminVm.createOffice(actor, name, departmentId, location, contact, email, hours)
                }
                Toast.makeText(ctx, err ?: (if (isEdit) "Office updated." else "Office created."), Toast.LENGTH_SHORT).show()
                saving = false
                if (err == null) nav.popBackStack()
            }
        }
    }
}