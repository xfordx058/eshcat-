package com.walangkaninbossing.eshcat.ui.screens.citizen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.EmptyState
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.LoadingState
import com.walangkaninbossing.eshcat.ui.components.PrimaryButton
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.theme.EshcatRadius
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.Primary
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.viewmodel.CitizenViewModel
import com.walangkaninbossing.eshcat.viewmodel.ServiceDetailData
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer

object CitizenApplyState {
    var fullName: String = ""
    var email: String = ""
    var mobile: String = ""
    var address: String = ""
    var requestDetails: String = ""
    var acknowledged: List<String> = emptyList()
    var attachedDocumentUri: Uri? = null

    fun reset() {
        fullName = ""
        email = ""
        mobile = ""
        address = ""
        requestDetails = ""
        acknowledged = emptyList()
        attachedDocumentUri = null
    }
}

@Composable
fun ApplyFormScreen(nav: NavController, serviceId: Int) {
    val vm: CitizenViewModel = viewModel { CitizenViewModel(eshcatContainer()) }
    val context = LocalContext.current
    var detail by remember { mutableStateOf<ServiceDetailData?>(null) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(serviceId) {
        detail = vm.detailsFor(serviceId)
        loaded = true
    }

    if (!loaded) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(title = "Apply", onBack = { nav.popBackStack() }) { }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingState()
            }
        }
        return
    }

    val service = detail?.service
    if (service == null) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(title = "Apply", onBack = { nav.popBackStack() }) { }
            EmptyState(icon = Icons.Filled.Info, title = "Service not found")
        }
        return
    }

    val requirements = detail!!.requirements.map { it.description }
    var step by remember { mutableStateOf(1) }
    var fullName by remember { mutableStateOf(CitizenApplyState.fullName) }
    var email by remember { mutableStateOf(CitizenApplyState.email) }
    var mobile by remember { mutableStateOf(CitizenApplyState.mobile) }
    var address by remember { mutableStateOf(CitizenApplyState.address) }
    var requestDetails by remember { mutableStateOf(CitizenApplyState.requestDetails) }
    var acknowledged by remember { mutableStateOf(CitizenApplyState.acknowledged.toSet()) }
    var attachedDocumentUri by remember { mutableStateOf<Uri?>(CitizenApplyState.attachedDocumentUri) }

    val docPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        attachedDocumentUri = uri
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Apply: ${service.name}",
            onBack = {
                if (step == 2) step = 1 else nav.popBackStack()
            },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding, vertical = EshcatSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md),
        ) {
            StepIndicator(step = step)

            if (step == 1) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(EshcatRadius.md),
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(EshcatRadius.md),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(EshcatRadius.md),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(EshcatRadius.md),
                )
                OutlinedTextField(
                    value = requestDetails,
                    onValueChange = { requestDetails = it },
                    label = { Text("Brief details of request*") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(EshcatRadius.md),
                )
                PrimaryButton(
                    text = "Continue",
                    onClick = {
                        if (fullName.isBlank() || mobile.isBlank() || address.isBlank() || requestDetails.isBlank()) {
                            Toast.makeText(context, "Please complete all required fields.", Toast.LENGTH_SHORT).show()
                        } else {
                            CitizenApplyState.fullName = fullName
                            CitizenApplyState.email = email
                            CitizenApplyState.mobile = mobile
                            CitizenApplyState.address = address
                            CitizenApplyState.requestDetails = requestDetails
                            step = 2
                        }
                    },
                )
            } else {
                Text(
                    text = "Checklist of Requirements",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                requirements.forEach { req ->
                    val checked = req in acknowledged
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(EshcatRadius.md))
                            .clickable {
                                acknowledged = if (checked) acknowledged - req else acknowledged + req
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { on ->
                                acknowledged = if (on) acknowledged + req else acknowledged - req
                            },
                        )
                        Text(
                            text = req,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                // File / Document Attachment Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.UploadFile,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(EshcatSpacing.sm))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Upload Supporting Document / ID",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "Attach photo or file of required documents (Optional)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Spacer(Modifier.height(EshcatSpacing.sm))

                        if (attachedDocumentUri != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(EshcatRadius.md))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = EshcatSpacing.md, vertical = EshcatSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AttachFile,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(EshcatSpacing.sm))
                                Text(
                                    text = "Document Attached",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(onClick = { attachedDocumentUri = null }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove file",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { docPicker.launch("*/*") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(EshcatRadius.md)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.UploadFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(EshcatSpacing.sm))
                                Text("Choose File or Photo")
                            }
                        }
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Note",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(EshcatSpacing.sm))
                    Text(
                        text = "Your details and the items you prepare will be validated when we process your application.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                PrimaryButton(
                    text = "Review Application",
                    enabled = requirements.all { it in acknowledged },
                    onClick = {
                        CitizenApplyState.fullName = fullName
                        CitizenApplyState.email = email
                        CitizenApplyState.mobile = mobile
                        CitizenApplyState.address = address
                        CitizenApplyState.requestDetails = requestDetails
                        CitizenApplyState.acknowledged = requirements.filter { it in acknowledged }
                        CitizenApplyState.attachedDocumentUri = attachedDocumentUri
                        nav.navigate(Routes.applyReview(serviceId))
                    },
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(step: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
        StepDot(number = "1", label = "Details", active = step >= 1)
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(2.dp)
                .background(if (step > 1) Primary else MaterialTheme.colorScheme.outline),
        )
        StepDot(number = "2", label = "Requirements", active = step >= 2)
    }
}

@Composable
private fun StepDot(number: String, label: String, active: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (active) Primary else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}