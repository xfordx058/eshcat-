package com.walangkaninbossing.eshcat.viewmodel

import com.walangkaninbossing.eshcat.AppContainer
import com.walangkaninbossing.eshcat.core.Statuses
import com.walangkaninbossing.eshcat.data.local.entity.StaffUserEntity
import com.walangkaninbossing.eshcat.util.TimeUtil

class StaffViewModel(app: AppContainer) : AppViewModel(app) {

    suspend fun dashboardStats(): DashboardStats? {
        app.awaitSeeded()
        val applicationCounts = app.applications.statusCounts()
        val appointmentCounts = app.community.appointmentStatusCounts()
        val reportCounts = app.community.reportStatusCounts()
        return DashboardStats(
            total = app.applications.countApplications(),
            submitted = applicationCounts[Statuses.SUBMITTED] ?: 0,
            underReview = (applicationCounts[Statuses.UNDER_REVIEW] ?: 0) +
                (applicationCounts[Statuses.FOR_VERIFICATION] ?: 0),
            approved = applicationCounts[Statuses.APPROVED] ?: 0,
            ready = applicationCounts[Statuses.READY] ?: 0,
            completed = applicationCounts[Statuses.COMPLETED] ?: 0,
            pendingAppointments = appointmentCounts["PENDING"] ?: 0,
            pendingReports = reportCounts["PENDING"] ?: 0,
            totalUsers = app.auth.countUsers(),
            totalServices = app.catalog.countServices(),
        )
    }

    suspend fun applicationsOnce(scope: AppScope, currentUser: StaffUserEntity?): List<ApplicationRow> {
        app.awaitSeeded()
        val all = app.applications.allApplicationsOnce()
        val filtered = when (scope) {
            AppScope.ALL -> all
            AppScope.OFFICE -> all.filter { currentUser == null || it.officeId == currentUser.officeId }
            AppScope.MINE -> all.filter { it.assignedToUserId != null && it.assignedToUserId == currentUser?.id }
        }
        return Joiners.applicationRows(app, filtered)
    }

    suspend fun applicationDetail(id: Int): StaffApplicationDetail? {
        app.awaitSeeded()
        val entity = app.applications.applicationByIdOnce(id) ?: return null
        val service = app.catalog.serviceByIdOnce(entity.serviceId)
        val office = app.catalog.officeByIdOnce(entity.officeId)
        val requirements = service?.let { app.catalog.requirementsForOnce(it.id) } ?: emptyList()
        val history = app.applications.historyOnce(entity.id)
        val assignments = app.applications.assignmentsFor(entity.id)
        val assignableUsers = app.auth.usersOnce()
            .filter { it.status == "ACTIVE" && it.officeId == entity.officeId }
            .sortedBy { it.fullName }
        val staffNames = app.auth.usersOnce().associate { it.id to it.fullName }
        return StaffApplicationDetail(
            app = entity,
            serviceName = service?.name ?: "Unknown service",
            officeName = office?.name ?: "Unknown office",
            requirements = requirements,
            history = history,
            assignments = assignments,
            assignableUsers = assignableUsers,
            staffNames = staffNames,
        )
    }

    suspend fun updateStatus(
        appId: Int,
        newStatus: String,
        note: String?,
        actor: StaffUserEntity,
        actorRoleName: String,
    ): String? {
        val ok = app.applications.updateStatus(
            applicationId = appId,
            newStatus = newStatus,
            note = note,
            actor = actor,
            actorRole = app.auth.roleById(actor.roleId),
            actorOfficeId = actor.officeId,
        )
        return if (ok) null else "Unable to update application status."
    }

    suspend fun assignApplication(appId: Int, staffId: Int, actor: StaffUserEntity): String? {
        val target = app.auth.userById(staffId) ?: return "Staff account not found."
        if (target.status != "ACTIVE") return "Cannot assign to an inactive account."
        app.applications.assign(
            applicationId = appId,
            staffUserId = staffId,
            actor = actor,
            roleName = app.auth.roleById(actor.roleId)?.name,
            actorOfficeId = actor.officeId,
        )
        return null
    }

    suspend fun appointmentsOnce(): List<AppointmentRow> =
        Joiners.appointmentRows(app, app.community.appointmentsOnce())

    suspend fun updateAppointmentStatus(id: Int, status: String, actor: StaffUserEntity): String? {
        app.community.updateAppointmentStatus(
            id = id,
            status = status,
            actor = actor,
            roleName = app.auth.roleById(actor.roleId)?.name,
            actorOfficeId = actor.officeId,
        )
        return null
    }

    suspend fun reportsOnce(): List<ReportRow> =
        Joiners.reportRows(app, app.community.reportsOnce())

    suspend fun updateReportStatus(
        id: Int,
        status: String,
        actor: StaffUserEntity,
        officeId: Int?,
    ): String? {
        app.community.updateReportStatus(
            id = id,
            status = status,
            actor = actor,
            roleName = app.auth.roleById(actor.roleId)?.name,
            actorOfficeId = actor.officeId,
            officeId = officeId,
        )
        return null
    }

    suspend fun staffByOffice(officeId: Int): List<StaffUserEntity> =
        app.auth.usersOnce().filter { it.officeId == officeId && it.status == "ACTIVE" }

    suspend fun staffName(id: Int): String = app.auth.userById(id)?.fullName ?: "System"

    suspend fun notificationsOnce(): List<NotificationItem> {
        app.awaitSeeded()
        val logs = app.audit.logsOnce().take(20)
        val names = app.auth.usersOnce().associate { it.id to it.fullName }
        return logs.map { log ->
            NotificationItem(
                kind = log.action,
                title = actionTitle(log.action),
                description = "${names[log.userId] ?: "System"} · ${log.targetId.ifBlank { "-" }}",
                timestamp = TimeUtil.displayDateTime(log.timestamp),
            )
        }
    }

    suspend fun suggestionNotifications(): List<NotificationItem> {
        val now = com.walangkaninbossing.eshcat.util.TimeUtil.displayDate(TimeUtil.now())
        return listOf(
            NotificationItem(
                kind = "INFO",
                title = "eSHCAT is ready",
                description = "All municipal services are available online. Some services require an office visit.",
                timestamp = now,
            )
        )
    }

    private fun actionTitle(action: String): String = when (action) {
        "LOGIN" -> "Staff signed in"
        "APPLICATION_STATUS_UPDATED" -> "Application status updated"
        "APPLICATION_ASSIGNED" -> "Application assigned"
        "APPOINTMENT_STATUS_UPDATED" -> "Appointment updated"
        "REPORT_STATUS_UPDATED" -> "Community report updated"
        "USER_CREATED" -> "New staff account created"
        "USER_UPDATED" -> "Staff account updated"
        "USER_STATUS_CHANGED" -> "Staff account status changed"
        "ROLE_PERMISSIONS_UPDATED" -> "Role permissions updated"
        "SERVICE_CREATED" -> "New service published"
        "SERVICE_UPDATED" -> "Service updated"
        "OFFICE_CREATED" -> "New office created"
        "OFFICE_UPDATED" -> "Office updated"
        "ANNOUNCEMENT_CREATED" -> "Announcement published"
        "ANNOUNCEMENT_UPDATED" -> "Announcement updated"
        else -> action.replace('_', ' ').replaceFirstChar { it.uppercase() }
    }
}

object CommunityActions {
    const val APPOINTMENT_CONFIRM = "APPROVED"
    const val APPOINTMENT_COMPLETE = "COMPLETED"
    const val APPOINTMENT_CANCEL = "CANCELLED"
    const val REPORT_ASSIGN = "IN_PROGRESS"
    const val REPORT_RESOLVE = "RESOLVED"
    const val REPORT_CLOSE = "CLOSED"

    val appointmentStatuses = listOf(
        com.walangkaninbossing.eshcat.core.AppointmentStatuses.PENDING,
        com.walangkaninbossing.eshcat.core.AppointmentStatuses.APPROVED,
        com.walangkaninbossing.eshcat.core.AppointmentStatuses.RESCHEDULED,
        com.walangkaninbossing.eshcat.core.AppointmentStatuses.COMPLETED,
        com.walangkaninbossing.eshcat.core.AppointmentStatuses.CANCELLED,
    )

    val reportStatuses = listOf(
        com.walangkaninbossing.eshcat.core.ReportStatuses.PENDING,
        com.walangkaninbossing.eshcat.core.ReportStatuses.IN_PROGRESS,
        com.walangkaninbossing.eshcat.core.ReportStatuses.RESOLVED,
        com.walangkaninbossing.eshcat.core.ReportStatuses.CLOSED,
    )
}
