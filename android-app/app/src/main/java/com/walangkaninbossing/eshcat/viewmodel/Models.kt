package com.walangkaninbossing.eshcat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import com.walangkaninbossing.eshcat.AppContainer
import com.walangkaninbossing.eshcat.ESHCATApplication
import com.walangkaninbossing.eshcat.data.local.entity.AnnouncementEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationAssignmentEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationHistoryEntity
import com.walangkaninbossing.eshcat.data.local.entity.AppointmentEntity
import com.walangkaninbossing.eshcat.data.local.entity.AuditLogEntity
import com.walangkaninbossing.eshcat.data.local.entity.CommunityReportEntity
import com.walangkaninbossing.eshcat.data.local.entity.DepartmentEntity
import com.walangkaninbossing.eshcat.data.local.entity.OfficeEntity
import com.walangkaninbossing.eshcat.data.local.entity.PermissionEntity
import com.walangkaninbossing.eshcat.data.local.entity.RoleEntity
import com.walangkaninbossing.eshcat.data.local.entity.ServiceEntity
import com.walangkaninbossing.eshcat.data.local.entity.ServiceRequirementEntity
import com.walangkaninbossing.eshcat.data.local.entity.StaffUserEntity

fun CreationExtras.eshcatContainer(): AppContainer =
    checkNotNull(this[AndroidViewModelFactory.APPLICATION_KEY] as? ESHCATApplication).container

open class AppViewModel(protected val app: AppContainer) : ViewModel()

// ---------- Joined row models ----------

data class ServiceRow(
    val service: ServiceEntity,
    val office: OfficeEntity?,
)

data class AnnouncementRow(
    val announcement: AnnouncementEntity,
    val department: DepartmentEntity?,
)

data class OfficeRow(
    val office: OfficeEntity,
    val department: DepartmentEntity,
)

data class StaffUserRow(
    val user: StaffUserEntity,
    val roleName: String,
    val officeName: String,
)

data class ApplicationRow(
    val app: ApplicationEntity,
    val serviceName: String,
    val officeName: String,
    val assignedName: String?,
)

data class StaffApplicationDetail(
    val app: ApplicationEntity,
    val serviceName: String,
    val officeName: String,
    val requirements: List<ServiceRequirementEntity>,
    val history: List<ApplicationHistoryEntity>,
    val assignments: List<ApplicationAssignmentEntity>,
    val assignableUsers: List<StaffUserEntity>,
    val staffNames: Map<Int, String>,
)

data class AppointmentRow(
    val appointment: AppointmentEntity,
    val officeName: String,
)

data class ReportRow(
    val report: CommunityReportEntity,
    val officeName: String?,
)

data class AuditRow(
    val log: AuditLogEntity,
    val actorName: String,
)

data class NotificationItem(
    val kind: String,
    val title: String,
    val description: String,
    val timestamp: String,
)

data class RoleDetail(
    val role: RoleEntity,
    val allPermissions: List<PermissionEntity>,
    val granted: List<String>,
)

enum class AppScope { ALL, OFFICE, MINE }

data class ServiceDetailData(
    val service: ServiceEntity,
    val requirements: List<ServiceRequirementEntity>,
    val office: OfficeEntity?,
    val prefix: String,
)

object ServiceRefCodes {
    private val categoryCodes = mapOf(
        "Civil Registry" to "CR",
        "Engineering" to "ENG",
        "Business" to "BIZ",
        "Social Welfare" to "MSW",
        "Treasury" to "TRS",
        "Cedula & Taxes" to "CED",
        "Health" to "HTH",
        "Agriculture" to "AGR",
        "Planning" to "PDO",
        "General Services" to "GSO",
    )

    fun forCategory(category: String): String = categoryCodes[category] ?: "APP"
}

data class DashboardStats(
    val total: Int,
    val submitted: Int,
    val underReview: Int,
    val approved: Int,
    val ready: Int,
    val completed: Int,
    val pendingAppointments: Int,
    val pendingReports: Int,
    val totalUsers: Int,
    val totalServices: Int,
)

object Joiners {

    suspend fun serviceRowMap(app: AppContainer): Map<Int, ServiceRow> {
        val services = app.catalog.servicesOnce()
        val offices = app.catalog.officesOnce().associateBy { it.id }
        return services.associate { s -> s.id to ServiceRow(s, offices[s.officeId]) }
    }

    suspend fun applicationRows(app: AppContainer, list: List<ApplicationEntity>): List<ApplicationRow> {
        val services = app.catalog.servicesOnce().associateBy { it.id }
        val offices = app.catalog.officesOnce().associateBy { it.id }
        val users = app.auth.usersOnce().associateBy { it.id }
        return list.map { a ->
            ApplicationRow(
                app = a,
                serviceName = services[a.serviceId]?.name ?: "Unknown service",
                officeName = offices[a.officeId]?.name ?: "Unknown office",
                assignedName = a.assignedToUserId?.let { users[it]?.fullName },
            )
        }
    }

    suspend fun staffUserRows(app: AppContainer, list: List<StaffUserEntity>): List<StaffUserRow> {
        val roles = app.auth.rolesOnce().associateBy { it.id }
        val offices = app.catalog.officesOnce().associateBy { it.id }
        return list.map { u ->
            StaffUserRow(
                user = u,
                roleName = roles[u.roleId]?.name ?: "UNKNOWN",
                officeName = offices[u.officeId]?.name ?: "Unknown office",
            )
        }
    }

    suspend fun appointmentRows(app: AppContainer, list: List<AppointmentEntity>): List<AppointmentRow> {
        val offices = app.catalog.officesOnce().associateBy { it.id }
        return list.map { a -> AppointmentRow(a, offices[a.officeId]?.name ?: "Unknown office") }
    }

    suspend fun reportRows(app: AppContainer, list: List<CommunityReportEntity>): List<ReportRow> {
        val offices = app.catalog.officesOnce().associateBy { it.id }
        return list.map { r -> ReportRow(r, r.officeId?.let { offices[it]?.name }) }
    }

    suspend fun auditRows(app: AppContainer, list: List<AuditLogEntity>): List<AuditRow> {
        val users = app.auth.usersOnce().associateBy { it.id }
        return list.map { l -> AuditRow(l, users[l.userId]?.fullName ?: "System") }
    }
}