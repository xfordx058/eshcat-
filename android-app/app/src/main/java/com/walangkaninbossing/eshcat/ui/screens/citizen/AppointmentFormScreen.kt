package com.walangkaninbossing.eshcat.ui.screens.citizen

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.ui.components.PrimaryButton
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.SkeletonBlock
import com.walangkaninbossing.eshcat.ui.theme.EshcatRadius
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.TextMutedLight
import com.walangkaninbossing.eshcat.viewmodel.AppointmentDraft
import com.walangkaninbossing.eshcat.viewmodel.CitizenViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentFormScreen(nav: NavController) {
    val vm: CitizenViewModel = viewModel { CitizenViewModel(eshcatContainer()) }
    val offices by vm.offices.collectAsState()
    val isDataReady by vm.isDataReady.collectAsState()
    val context = LocalContext.current
    var selectedOfficeId by remember { mutableStateOf(-1) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    val submissionKey = rememberSaveable { java.util.UUID.randomUUID().toString() }
    var submitting by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Book Appointment", onBack = { nav.popBackStack() })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding, vertical = EshcatSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md),
        ) {
            Text(
                text = "Select Office",
                style = MaterialTheme.typography.labelMedium,
                color = TextMutedLight,
            )
            if (!isDataReady) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                    items(3) { SkeletonBlock(modifier = Modifier.fillMaxWidth(0.42f).padding(vertical = 10.dp)) }
                }
            } else if (offices.isEmpty()) {
                Text(
                    text = "No offices are available for appointments right now.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                    items(offices, key = { it.office.id }) { row ->
                        FilterChip(
                            selected = row.office.id == selectedOfficeId,
                            onClick = { selectedOfficeId = row.office.id },
                            label = { Text(row.office.name, maxLines = 1) },
                            shape = RoundedCornerShape(EshcatRadius.pill),
                        )
                    }
                }
            }

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
                label = { Text("Mobile") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(EshcatRadius.md),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )
            OutlinedTextField(
                value = date,
                onValueChange = {},
                label = { Text("Date*") },
                placeholder = { Text("Select a date") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(EshcatRadius.md),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Choose appointment date")
                    }
                },
            )
            OutlinedTextField(
                value = time,
                onValueChange = {},
                label = { Text("Time*") },
                placeholder = { Text("Select a time") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(EshcatRadius.md),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(Icons.Filled.AccessTime, contentDescription = "Choose appointment time")
                    }
                },
            )
            OutlinedTextField(
                value = purpose,
                onValueChange = { purpose = it },
                label = { Text("Purpose*") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(EshcatRadius.md),
            )

            PrimaryButton(
                text = "Submit Booking",
                enabled = selectedOfficeId > 0 && fullName.isNotBlank() && date.isNotBlank() && time.isNotBlank() && !submitting,
                loading = submitting,
                onClick = {
                    submitting = true
                    vm.submitAppointment(
                        AppointmentDraft(
                            officeId = selectedOfficeId,
                            fullName = fullName,
                            email = email,
                            mobile = mobile,
                            date = date,
                            time = time,
                            purpose = purpose,
                            submissionKey = submissionKey,
                        )
                    ) { error ->
                        submitting = false
                        if (error == null) {
                            Toast.makeText(context, "Appointment requested.", Toast.LENGTH_SHORT).show()
                            nav.popBackStack()
                        } else {
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selected ->
                        date = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(selected))
                    }
                    showDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select appointment time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val calendar = java.util.Calendar.getInstance().apply {
                        set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(java.util.Calendar.MINUTE, timePickerState.minute)
                    }
                    time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(calendar.time)
                    showTimePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
        )
    }
}
