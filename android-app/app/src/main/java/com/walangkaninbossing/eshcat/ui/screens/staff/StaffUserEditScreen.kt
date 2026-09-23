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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.core.AccountStatuses
import com.walangkaninbossing.eshcat.core.Roles
import com.walangkaninbossing.eshcat.data.local.entity.RoleEntity
import com.walangkaninbossing.eshcat.data.local.entity.StaffUserEntity
import com.walangkaninbossing.eshcat.ui.components.FilterChipsRow
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.LabeledField
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
fun StaffUserEditScreen(
    nav: NavController,
    userId: Int,
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
    var roles by remember { mutableStateOf<List<RoleEntity>>(emptyList()) }
    var offices by remember { mutableStateOf<List<OfficeRow>>(emptyList()) }
    var existing by remember { mutableStateOf<StaffUserEntity?>(null) }
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var roleId by remember { mutableStateOf(0) }
    var officeId by remember { mutableStateOf(0) }
    var status by remember { mutableStateOf(AccountStatuses.ACTIVE) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        val loadedRoles = adminVm.rolesOnce()
        val loadedOffices = adminVm.officesOnce()
        roles = loadedRoles
        offices = loadedOffices
        if (userId > 0) {
            val row = adminVm.usersOnce().find { it.user.id == userId }
            existing = row?.user
            fullName = row?.user?.fullName ?: ""
            username = row?.user?.username ?: ""
            roleId = row?.user?.roleId ?: 0
            officeId = row?.user?.officeId ?: 0
            status = row?.user?.status ?: AccountStatuses.ACTIVE
        }
        loading = false
    }

    val isEdit = userId > 0
    val eligibleRoles = roles.filter { it.name != Roles.SUPER_ADMIN || session.role?.name == Roles.SUPER_ADMIN }
    val roleOptions = eligibleRoles.map { it.name }
    val selectedRoleName = eligibleRoles.find { it.id == roleId }?.name ?: ""
    val officeOptions = offices.map { it.office.name }
    val selectedOfficeName = offices.find { it.office.id == officeId }?.office?.name ?: ""
    val departmentId = offices.find { it.office.id == officeId }?.department?.id ?: existing?.departmentId ?: 0
    val canSave = !loading && fullName.isNotBlank() && username.isNotBlank() && roleId > 0 && officeId > 0 &&
        (isEdit || password.isNotBlank())

    if (loading) {
        com.walangkaninbossing.eshcat.ui.components.LoadingState(Modifier.fillMaxSize())
        return
    }

    fun save() {
        if (!canSave) return
        scope.launch {
            saving = true
            if (!isEdit) {
                val err = adminVm.createUser(actor, fullName, username, password, roleId, officeId, departmentId)
                if (err != null) {
                    Toast.makeText(ctx, err, Toast.LENGTH_SHORT).show()
                    saving = false
                } else {
                    Toast.makeText(ctx, "Account created.", Toast.LENGTH_SHORT).show()
                    nav.popBackStack()
                }
            } else {
                val base = existing ?: run {
                    Toast.makeText(ctx, "User not found.", Toast.LENGTH_SHORT).show()
                    saving = false
                    return@launch
                }
                val err = adminVm.updateUser(
                    actor,
                    base.copy(
                        fullName = fullName.trim(),
                        username = username.trim(),
                        roleId = roleId,
                        officeId = officeId,
                        departmentId = departmentId,
                        status = status,
                    ),
                )
                if (err != null) {
                    Toast.makeText(ctx, err, Toast.LENGTH_SHORT).show()
                    saving = false
                    return@launch
                }
                if (newPassword.isNotBlank()) {
                    val passErr = adminVm.changePassword(actor, userId, newPassword)
                    if (passErr != null) {
                        Toast.makeText(ctx, passErr, Toast.LENGTH_SHORT).show()
                        saving = false
                        return@launch
                    }
                }
                Toast.makeText(ctx, "Account updated.", Toast.LENGTH_SHORT).show()
                nav.popBackStack()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(horizontal = ScreenPadding, vertical = 16.dp)),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenHeader(if (isEdit) "Edit Account" else "New Account", onBack = { nav.popBackStack() })
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(EshcatRadius.md),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(EshcatRadius.md),
                    singleLine = true,
                )
                if (isEdit) {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password (leave blank to keep)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(EshcatRadius.md),
                        singleLine = true,
                    )
                } else {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(EshcatRadius.md),
                        singleLine = true,
                    )
                }
            }
        }
        SectionHeader("Role")
        FilterChipsRow(roleOptions, selectedRoleName, { name ->
            eligibleRoles.find { it.name == name }?.let { roleId = it.id }
        })
        SectionHeader("Office")
        FilterChipsRow(officeOptions, selectedOfficeName, { name ->
            offices.find { it.office.name == name }?.let { officeId = it.office.id }
        })
        if (isEdit) {
            SectionHeader("Status")
            FilterChipsRow(AccountStatuses.all, status, { status = it })
            LabeledField("Role", Roles.label(selectedRoleName))
        }
        Spacer(Modifier.height(4.dp))
        PrimaryButton("Save", onClick = { save() }, enabled = canSave, loading = saving)
    }
}