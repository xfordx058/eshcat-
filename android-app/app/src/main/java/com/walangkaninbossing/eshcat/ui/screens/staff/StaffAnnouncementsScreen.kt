package com.walangkaninbossing.eshcat.ui.screens.staff

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Star
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.auth.Permissions
import com.walangkaninbossing.eshcat.data.local.entity.DepartmentEntity
import com.walangkaninbossing.eshcat.ui.components.EmptyState
import com.walangkaninbossing.eshcat.ui.components.FilterChipsRow
import com.walangkaninbossing.eshcat.ui.components.GlassCard
import com.walangkaninbossing.eshcat.ui.components.PermissionGate
import com.walangkaninbossing.eshcat.ui.components.PrimaryButton
import com.walangkaninbossing.eshcat.ui.components.ScreenHeader
import com.walangkaninbossing.eshcat.ui.components.SectionHeader
import com.walangkaninbossing.eshcat.ui.theme.EshcatRadius
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.OnWarningContainer
import com.walangkaninbossing.eshcat.ui.theme.Primary
import com.walangkaninbossing.eshcat.ui.theme.PrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.TextMutedLight
import com.walangkaninbossing.eshcat.ui.theme.TextSecondaryLight
import com.walangkaninbossing.eshcat.ui.theme.WarningContainerAccent
import com.walangkaninbossing.eshcat.util.TimeUtil
import com.walangkaninbossing.eshcat.viewmodel.AdminViewModel
import com.walangkaninbossing.eshcat.viewmodel.AnnouncementRow
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel
import com.walangkaninbossing.eshcat.viewmodel.eshcatContainer
import kotlinx.coroutines.launch

@Composable
fun StaffAnnouncementsScreen(nav: NavController, sessionVm: StaffSessionViewModel) {
    val adminVm: AdminViewModel = viewModel { AdminViewModel(eshcatContainer()) }
    val session by sessionVm.state.collectAsState()
    val actor = session.user
    if (actor == null) {
        PermissionGate(enabled = false) {}
        return
    }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var announcements by remember { mutableStateOf<List<AnnouncementRow>>(emptyList()) }
    var departments by remember { mutableStateOf<List<DepartmentEntity>>(emptyList()) }
    var reloadKey by remember { mutableStateOf(0) }
    var showEditor by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf(0) }
    var editTitle by remember { mutableStateOf("") }
    var editDesc by remember { mutableStateOf("") }
    var editDeptId by remember { mutableStateOf(0) }
    var editPinned by remember { mutableStateOf(false) }
    LaunchedEffect(reloadKey) {
        val depts = adminVm.departmentsOnce()
        departments = depts
        announcements = adminVm.announcementsOnce()
        if (depts.isNotEmpty() && editDeptId == 0) editDeptId = depts.first().id
    }

    val deptOptions = departments.map { it.name }
    val selectedDeptName = departments.find { it.id == editDeptId }?.name ?: ""
    val canSaveEditor = editTitle.isNotBlank() && editDesc.isNotBlank()

    fun resetEditor() {
        showEditor = false
        editingId = 0
        editTitle = ""
        editDesc = ""
        editDeptId = departments.firstOrNull()?.id ?: 0
        editPinned = false
    }

    fun saveEditor() {
        if (!canSaveEditor) return
        scope.launch {
            val err = if (editingId > 0) {
                val base = announcements.find { it.announcement.id == editingId }?.announcement
                if (base == null) {
                    "Announcement not found."
                } else {
                    adminVm.updateAnnouncement(
                        actor,
                        base.copy(
                            title = editTitle.trim(),
                            description = editDesc.trim(),
                            departmentId = editDeptId,
                            pinned = editPinned,
                        ),
                    )
                }
            } else {
                adminVm.createAnnouncement(actor, editTitle, editDesc, editDeptId, editPinned)
            }
            Toast.makeText(ctx, err ?: "Announcement saved.", Toast.LENGTH_SHORT).show()
            if (err == null) {
                resetEditor()
                reloadKey++
            }
        }
    }

    PermissionGate(enabled = sessionVm.has(Permissions.MANAGE_ANNOUNCEMENTS)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = ScreenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ScreenHeader(
                    "Announcements",
                    subtitle = "${announcements.size} announcement${if (announcements.size == 1) "" else "s"}",
                )
            }
            item {
                GlassCard {
                    TextButton(onClick = {
                        if (showEditor && editingId == 0) resetEditor() else {
                            resetEditor()
                            showEditor = true
                        }
                    }) {
                        androidx.compose.material3.Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (showEditor && editingId == 0) "Cancel" else "Add Announcement", color = Primary, fontWeight = FontWeight.SemiBold)
                    }
                    if (showEditor) {
                        Column(verticalArrangement = Arrangement.spacedBy(EshcatSpacing.md)) {
                            OutlinedTextField(
                                value = editTitle,
                                onValueChange = { editTitle = it },
                                label = { Text("Title") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(EshcatRadius.md),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = editDesc,
                                onValueChange = { editDesc = it },
                                label = { Text("Body") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(EshcatRadius.md),
                                minLines = 4,
                            )
                            SectionHeader("Department")
                            FilterChipsRow(deptOptions, selectedDeptName, { dName ->
                                departments.find { it.name == dName }?.let { editDeptId = it.id }
                            })
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Pin to top", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text("Show this announcement prominently", style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
                                }
                                Switch(checked = editPinned, onCheckedChange = { editPinned = it })
                            }
                            PrimaryButton(if (editingId > 0) "Update Announcement" else "Publish", enabled = canSaveEditor) { saveEditor() }
                        }
                    }
                }
            }
            if (announcements.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.Campaign,
                        "No announcements",
                        "Publish an announcement to get started.",
                    )
                }
            }
            items(announcements, key = { it.announcement.id }) { row ->
                val ann = row.announcement
                GlassCard {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(PrimaryContainerLight),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.Icon(Icons.Filled.Campaign, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(EshcatSpacing.md))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    ann.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (ann.pinned) {
                                    Surface(
                                        shape = RoundedCornerShape(EshcatRadius.pill),
                                        color = WarningContainerAccent,
                                        contentColor = OnWarningContainer,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        ) {
                                            androidx.compose.material3.Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("PINNED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(ann.description, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${TimeUtil.displayDate(ann.date)} · ${row.department?.name ?: "Catarman LGU"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMutedLight,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(onClick = {
                                    editingId = ann.id
                                    editTitle = ann.title
                                    editDesc = ann.description
                                    editDeptId = ann.departmentId
                                    editPinned = ann.pinned
                                    showEditor = true
                                }) { Text("Edit", color = Primary) }
                            }
                        }
                    }
                }
            }
        }
    }
}