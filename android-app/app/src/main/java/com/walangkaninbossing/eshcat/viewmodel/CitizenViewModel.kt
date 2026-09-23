package com.walangkaninbossing.eshcat.viewmodel

import androidx.lifecycle.viewModelScope
import com.walangkaninbossing.eshcat.data.local.entity.AppointmentEntity
import com.walangkaninbossing.eshcat.data.local.entity.CommunityReportEntity
import com.walangkaninbossing.eshcat.util.TimeUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class AppointmentDraft(
    val officeId: Int,
    val fullName: String,
    val email: String,
    val mobile: String,
    val date: String,
    val time: String,
    val purpose: String,
    val submissionKey: String = UUID.randomUUID().toString(),
)

data class ReportDraft(
    val category: String,
    val location: String,
    val description: String,
    val fullName: String,
    val contactNumber: String,
)

class CitizenViewModel(app: com.walangkaninbossing.eshcat.AppContainer) : AppViewModel(app) {

    /** Lets screens distinguish initial database seeding from an actual empty result. */
    val isDataReady: StateFlow<Boolean> = app.seedingDone

    val services: StateFlow<List<ServiceRow>> = app.catalog.services()
        .combine(app.catalog.offices()) { services, offices ->
            val byId = offices.associateBy { it.id }
            services.map { ServiceRow(it, byId[it.officeId]) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuresRow: StateFlow<List<ServiceRow>> = app.catalog.services()
        .combine(app.catalog.offices()) { services, offices ->
            val byId = offices.associateBy { it.id }
            services.take(6).map { ServiceRow(it, byId[it.officeId]) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offices: StateFlow<List<OfficeRow>> = app.catalog.offices()
        .combine(app.catalog.departments()) { offices, depts ->
            val byId = depts.associateBy { it.id }
            val fallback = com.walangkaninbossing.eshcat.data.local.entity.DepartmentEntity(0, "Catarman LGU")
            offices.map { OfficeRow(it, byId[it.departmentId] ?: fallback) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<AnnouncementRow>> = app.catalog.announcements()
        .combine(app.catalog.departments()) { anns, depts ->
            val byId = depts.associateBy { it.id }
            anns.map { AnnouncementRow(it, byId[it.departmentId]) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pinnedAnnouncements: StateFlow<List<AnnouncementRow>> = announcements
        .map { list -> list.filter { it.announcement.pinned } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<AppointmentRow>> = app.community.appointments()
        .combine(app.catalog.offices()) { list, offices ->
            val byId = offices.associateBy { it.id }
            list.take(100).map { AppointmentRow(it, byId[it.officeId]?.name ?: "Unknown office") }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<ReportRow>> = app.community.reports()
        .combine(app.catalog.offices()) { list, offices ->
            val byId = offices.associateBy { it.id }
            list.take(100).map { ReportRow(it, it.officeId?.let { id -> byId[id]?.name }) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun servicesByCategory(category: String): List<ServiceRow> {
        val services = app.catalog.servicesOnce()
        val byId = app.catalog.officesOnce().associateBy { it.id }
        return services.filter { it.category == category }.map { ServiceRow(it, byId[it.officeId]) }
    }

    suspend fun detailsFor(serviceId: Int): ServiceDetailData? {
        app.awaitSeeded()
        val service = app.catalog.serviceByIdOnce(serviceId) ?: return null
        val requirements = app.catalog.requirementsForOnce(serviceId)
        val office = app.catalog.officeByIdOnce(service.officeId)
        val servicePrefix = ServiceRefCodes.forCategory(service.category)
        return ServiceDetailData(service, requirements, office, servicePrefix)
    }

    fun submitAppointment(draft: AppointmentDraft, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            if (draft.officeId <= 0 || draft.fullName.isBlank() || draft.date.isBlank() || draft.time.isBlank()) {
                onResult("Please complete all required fields.")
                return@launch
            }
            app.community.createAppointment(
                AppointmentEntity(
                    officeId = draft.officeId,
                    fullName = draft.fullName.trim(),
                    email = draft.email.trim(),
                    mobile = draft.mobile.trim(),
                    date = draft.date,
                    time = draft.time,
                    purpose = draft.purpose.trim(),
                    status = "PENDING",
                    createdDate = TimeUtil.now(),
                    submissionKey = draft.submissionKey,
                )
            )
            onResult(null)
        }
    }

    fun submitReport(draft: ReportDraft, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            if (draft.category.isBlank() || draft.location.isBlank() || draft.description.isBlank()) {
                onResult("Please complete all required fields.")
                return@launch
            }
            app.community.createReport(
                CommunityReportEntity(
                    category = draft.category.trim(),
                    location = draft.location.trim(),
                    description = draft.description.trim(),
                    fullName = draft.fullName.trim(),
                    contactNumber = draft.contactNumber.trim(),
                    status = "PENDING",
                    officeId = null,
                    createdDate = TimeUtil.now(),
                )
            )
            onResult(null)
        }
    }
}

private fun DepartmentEntityZero(name: String) =
    com.walangkaninbossing.eshcat.data.local.entity.DepartmentEntity(0, name)
