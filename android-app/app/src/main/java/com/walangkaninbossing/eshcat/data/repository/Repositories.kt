package com.walangkaninbossing.eshcat.data.repository

import com.walangkaninbossing.eshcat.data.local.AppDatabase
import com.walangkaninbossing.eshcat.data.local.entity.AnnouncementEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationAssignmentEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationHistoryEntity
import com.walangkaninbossing.eshcat.data.local.entity.AppointmentEntity
import com.walangkaninbossing.eshcat.data.local.entity.AuditLogEntity
import com.walangkaninbossing.eshcat.data.local.entity.CommunityReportEntity
import com.walangkaninbossing.eshcat.data.local.entity.RoleEntity
import com.walangkaninbossing.eshcat.data.local.entity.StaffUserEntity
import com.walangkaninbossing.eshcat.util.TimeUtil
import kotlinx.coroutines.flow.Flow

class CatalogRepository(private val db: AppDatabase) {

    private val ref = db.referenceDao()
    private val req = db.requestDao()

    fun departments(): Flow<List<com.walangkaninbossing.eshcat.data.local.entity.DepartmentEntity>> = ref.allDepartments()
    suspend fun departmentsOnce() = ref.allDepartmentsOnce()

    fun offices(): Flow<List<com.walangkaninbossing.eshcat.data.local.entity.OfficeEntity>> = ref.allOffices()
    suspend fun officesOnce() = ref.allOfficesOnce()
    fun officeById(id: Int): Flow<com.walangkaninbossing.eshcat.data.local.entity.OfficeEntity?> = ref.officeById(id)
    suspend fun officeByIdOnce(id: Int) = ref.officeByIdOnce(id)
    suspend fun insertOffice(office: com.walangkaninbossing.eshcat.data.local.entity.OfficeEntity) = ref.insertOffice(office)
    suspend fun updateOffice(office: com.walangkaninbossing.eshcat.data.local.entity.OfficeEntity) = ref.updateOffice(office)

    fun services(): Flow<List<com.walangkaninbossing.eshcat.data.local.entity.ServiceEntity>> = ref.allServices()
    suspend fun servicesOnce() = ref.allServicesOnce()
    suspend fun countServices() = ref.countServices()
    fun servicesByOffice(officeId: Int) = ref.servicesByOffice(officeId)
    fun serviceById(id: Int): Flow<com.walangkaninbossing.eshcat.data.local.entity.ServiceEntity?> = ref.serviceById(id)
    suspend fun serviceByIdOnce(id: Int) = ref.serviceByIdOnce(id)
    suspend fun insertService(service: com.walangkaninbossing.eshcat.data.local.entity.ServiceEntity) = ref.insertService(service)
    suspend fun updateService(service: com.walangkaninbossing.eshcat.data.local.entity.ServiceEntity) = ref.updateService(service)

    fun requirementsFor(serviceId: Int): Flow<List<com.walangkaninbossing.eshcat.data.local.entity.ServiceRequirementEntity>> =
        ref.requirementsFor(serviceId)
    suspend fun requirementsForOnce(serviceId: Int) = ref.requirementsForOnce(serviceId)
    suspend fun insertRequirement(req0: com.walangkaninbossing.eshcat.data.local.entity.ServiceRequirementEntity) = ref.insertRequirement(req0)
    suspend fun deleteRequirement(req0: com.walangkaninbossing.eshcat.data.local.entity.ServiceRequirementEntity) = ref.deleteRequirement(req0)

    fun announcements(): Flow<List<AnnouncementEntity>> = req.allAnnouncements()
    suspend fun announcementsOnce() = req.allAnnouncementsOnce()
    suspend fun insertAnnouncement(a: AnnouncementEntity) = req.insertAnnouncement(a)
    suspend fun updateAnnouncement(a: AnnouncementEntity) = req.updateAnnouncement(a)
}

class AuthRepository(private val db: AppDatabase) {

    private val auth = db.authDao()

    fun users(): Flow<List<StaffUserEntity>> = auth.allUsers()
    suspend fun usersOnce() = auth.allUsersOnce()
    suspend fun userById(id: Int) = auth.userById(id)
    fun userByIdFlow(id: Int): Flow<StaffUserEntity?> = auth.userByIdFlow(id)
    suspend fun userByUsername(username: String) = auth.userByUsername(username)
    suspend fun createUser(user: StaffUserEntity) = auth.insertUser(user)
    suspend fun updateUser(user: StaffUserEntity) = auth.updateUser(user)
    suspend fun setUserStatus(id: Int, status: String) = auth.updateUserStatus(id, status)
    suspend fun updateLastLogin(id: Int, ts: String) = auth.updateLastLogin(id, ts)

    fun roles(): Flow<List<RoleEntity>> = auth.allRoles()
    suspend fun rolesOnce() = auth.allRolesOnce()
    suspend fun roleById(id: Int) = auth.roleById(id)
    suspend fun createRole(role: RoleEntity) = auth.insertRole(role)
    suspend fun updateRole(role: RoleEntity) = auth.updateRole(role)

    fun permissions() = auth.allPermissions()
    suspend fun permissionsOnce() = auth.allPermissionsOnce()
    suspend fun permissionIdsForRole(roleId: Int) = auth.permissionIdsForRole(roleId)
    suspend fun setRolePermissions(roleId: Int, permissionIds: List<String>) {
        auth.deleteRolePermissions(roleId)
        auth.insertRolePermissions(permissionIds.map { com.walangkaninbossing.eshcat.data.local.entity.RolePermissionEntity(roleId, it) })
    }

    suspend fun countUsers() = auth.countUsers()
}

class ApplicationRepository(private val db: AppDatabase) {

    private val req = db.requestDao()
    private val audit = db.auditDao()

    fun allApplications(): Flow<List<ApplicationEntity>> = req.allApplications()
    fun application(id: Int): Flow<ApplicationEntity?> = req.applicationById(id)
    fun history(applicationId: Int): Flow<List<ApplicationHistoryEntity>> = req.historyFor(applicationId)

    suspend fun allApplicationsOnce() = req.allApplicationsOnce()
    suspend fun applicationByIdOnce(id: Int) = req.applicationByIdOnce(id)
    suspend fun applicationByRef(ref: String) = req.applicationByRef(ref)
    suspend fun historyOnce(applicationId: Int) = req.historyForOnce(applicationId)
    suspend fun assignmentsFor(applicationId: Int) = req.assignmentsFor(applicationId)
    suspend fun allAssignments() = req.allAssignments()
    suspend fun assignmentBy(staffUserId: Int, applicationId: Int) = req.assignmentFor(staffUserId, applicationId)

    suspend fun countApplications() = req.countApplications()
    suspend fun countByStatus(status: String) = req.countApplicationsByStatus(status)
    suspend fun statusCounts() = req.applicationStatusCounts().associate { it.status to it.count }

    suspend fun submit(application: ApplicationEntity, actor: StaffUserEntity?): ApplicationEntity {
        req.applicationBySubmissionKey(application.submissionKey)?.let { return it }
        val id = req.insertApplication(application)
        if (id == -1L) {
            return requireNotNull(req.applicationBySubmissionKey(application.submissionKey))
        }
        val saved = application.copy(id = id.toInt())
        req.insertHistory(
            ApplicationHistoryEntity(
                applicationId = saved.id,
                status = saved.status,
                note = "Application submitted.",
                changedByUserId = actor?.id,
                changedDate = TimeUtil.now()
            )
        )
        val assigned = saved.assignedToUserId != null
        if (assigned) {
            req.insertAssignment(
                ApplicationAssignmentEntity(
                    applicationId = saved.id,
                    staffUserId = saved.assignedToUserId!!,
                    assignedDate = TimeUtil.now()
                )
            )
        }
        if (!saved.email.isNullOrBlank()) {
            val service = db.referenceDao().serviceByIdOnce(saved.serviceId)
            val serviceName = service?.name ?: "Municipal Service"
            val html = com.walangkaninbossing.eshcat.util.EmailService.buildApplicationReceivedHtml(
                fullName = saved.fullName,
                referenceNumber = saved.referenceNumber,
                serviceName = serviceName,
                status = saved.status,
                submittedDate = saved.createdDate
            )
            com.walangkaninbossing.eshcat.util.EmailService.sendHtmlEmail(
                toEmail = saved.email,
                subject = "eSHCAT - Application Received [${saved.referenceNumber}]",
                htmlContent = html
            )
        }
        return saved
    }

    suspend fun updateStatus(
        applicationId: Int,
        newStatus: String,
        note: String?,
        actor: StaffUserEntity?,
        actorRole: RoleEntity?,
        actorOfficeId: Int,
        assignToStaff: Int? = null
    ): Boolean {
        val app = req.applicationByIdOnce(applicationId) ?: return false
        val oldStatus = app.status
        // A retried status update must not create duplicate history, audit logs, or emails.
        if (oldStatus == newStatus && (note.isNullOrBlank() || note == app.processingNotes)) return true
        val updated = app.copy(
            status = newStatus,
            lastUpdated = TimeUtil.now(),
            processingNotes = note ?: app.processingNotes,
            assignedToUserId = assignToStaff ?: app.assignedToUserId
        )
        req.updateApplication(updated)
        req.insertHistory(
            ApplicationHistoryEntity(
                applicationId = app.id,
                status = newStatus,
                note = note ?: "Status updated.",
                changedByUserId = actor?.id,
                changedDate = TimeUtil.now()
            )
        )
        if (actor != null) {
            audit.insertLog(
                AuditLogEntity(
                    userId = actor.id,
                    role = actorRole?.name ?: "UNKNOWN",
                    officeId = actorOfficeId,
                    action = "APPLICATION_STATUS_UPDATED",
                    targetType = "APPLICATION",
                    targetId = app.referenceNumber,
                    oldValue = oldStatus,
                    newValue = newStatus,
                    timestamp = TimeUtil.now()
                )
            )
        }
        if (assignToStaff != null) {
            req.insertAssignment(
                ApplicationAssignmentEntity(
                    applicationId = app.id,
                    staffUserId = assignToStaff,
                    assignedDate = TimeUtil.now()
                )
            )
        }
        if (!app.email.isNullOrBlank()) {
            val service = db.referenceDao().serviceByIdOnce(app.serviceId)
            val serviceName = service?.name ?: "Municipal Service"
            val html = com.walangkaninbossing.eshcat.util.EmailService.buildStatusUpdateHtml(
                fullName = app.fullName,
                referenceNumber = app.referenceNumber,
                serviceName = serviceName,
                newStatus = newStatus,
                notes = note
            )
            com.walangkaninbossing.eshcat.util.EmailService.sendHtmlEmail(
                toEmail = app.email,
                subject = "eSHCAT - Application Status Updated [${app.referenceNumber}]",
                htmlContent = html
            )
        }
        return true
    }

    suspend fun assign(applicationId: Int, staffUserId: Int, actor: StaffUserEntity?, roleName: String?, actorOfficeId: Int) {
        val app = req.applicationByIdOnce(applicationId) ?: return
        req.updateApplication(app.copy(assignedToUserId = staffUserId))
        req.insertAssignment(
            ApplicationAssignmentEntity(applicationId = app.id, staffUserId = staffUserId, assignedDate = TimeUtil.now())
        )
        if (actor != null) {
            audit.insertLog(
                AuditLogEntity(
                    userId = actor.id,
                    role = roleName ?: "UNKNOWN",
                    officeId = actorOfficeId,
                    action = "APPLICATION_ASSIGNED",
                    targetType = "APPLICATION",
                    targetId = app.referenceNumber,
                    oldValue = null,
                    newValue = staffUserId.toString(),
                    timestamp = TimeUtil.now()
                )
            )
        }
    }
}

class CommunityRepository(private val db: AppDatabase) {

    private val req = db.requestDao()
    private val audit = db.auditDao()

    fun appointments(): Flow<List<AppointmentEntity>> = req.allAppointments()
    suspend fun appointmentsOnce() = req.allAppointmentsOnce()
    suspend fun appointmentById(id: Int) = req.appointmentById(id)
    suspend fun createAppointment(a: AppointmentEntity): Long {
        req.appointmentBySubmissionKey(a.submissionKey)?.let { return it.id.toLong() }
        val id = req.insertAppointment(a)
        if (id == -1L) {
            return requireNotNull(req.appointmentBySubmissionKey(a.submissionKey)).id.toLong()
        }
        if (!a.email.isNullOrBlank()) {
            val office = db.referenceDao().officeByIdOnce(a.officeId)
            val officeName = office?.name ?: "Municipal Office"
            com.walangkaninbossing.eshcat.util.EmailService.sendEmail(
                toEmail = a.email,
                subject = "eSHCAT - Appointment Scheduled",
                bodyText = """
                    Dear ${a.fullName},

                    Your appointment request with $officeName has been booked.

                    Date: ${a.date}
                    Time: ${a.time}
                    Purpose: ${a.purpose}
                    Status: ${a.status}

                    Thank you,
                    eSHCAT Municipal Portal
                """.trimIndent()
            )
        }
        return id
    }
    suspend fun updateAppointmentStatus(id: Int, status: String, actor: StaffUserEntity?, roleName: String?, actorOfficeId: Int) {
        val appt = req.appointmentById(id) ?: return
        val old = appt.status
        req.updateAppointment(appt.copy(status = status))
        if (actor != null) {
            audit.insertLog(
                AuditLogEntity(
                    userId = actor.id,
                    role = roleName ?: "UNKNOWN",
                    officeId = actorOfficeId,
                    action = "APPOINTMENT_STATUS_UPDATED",
                    targetType = "APPOINTMENT",
                    targetId = id.toString(),
                    oldValue = old,
                    newValue = status,
                    timestamp = TimeUtil.now()
                )
            )
        }
    }
    suspend fun countAppointments() = req.countAppointments()
    suspend fun countAppointmentsByStatus(s: String) = req.countAppointmentsByStatus(s)
    suspend fun appointmentStatusCounts() = req.appointmentStatusCounts().associate { it.status to it.count }

    fun reports(): Flow<List<CommunityReportEntity>> = req.allReports()
    suspend fun reportsOnce() = req.allReportsOnce()
    suspend fun reportById(id: Int) = req.reportById(id)
    suspend fun createReport(r: CommunityReportEntity) = req.insertReport(r)
    suspend fun updateReportStatus(id: Int, status: String, actor: StaffUserEntity?, roleName: String?, actorOfficeId: Int, officeId: Int?) {
        val report = req.reportById(id) ?: return
        val old = report.status
        req.updateReport(report.copy(status = status, officeId = officeId ?: report.officeId))
        if (actor != null) {
            audit.insertLog(
                AuditLogEntity(
                    userId = actor.id,
                    role = roleName ?: "UNKNOWN",
                    officeId = actorOfficeId,
                    action = "REPORT_STATUS_UPDATED",
                    targetType = "REPORT",
                    targetId = id.toString(),
                    oldValue = old,
                    newValue = status,
                    timestamp = TimeUtil.now()
                )
            )
        }
    }
    suspend fun countReports() = req.countReports()
    suspend fun countReportsByStatus(s: String) = req.countReportsByStatus(s)
    suspend fun reportStatusCounts() = req.reportStatusCounts().associate { it.status to it.count }
}

class AuditRepository(private val db: AppDatabase) {

    private val audit = db.auditDao()

    fun logs(): Flow<List<AuditLogEntity>> = audit.allLogs()
    suspend fun logsOnce() = audit.allLogsOnce()
    suspend fun logUserCreated(actor: StaffUserEntity, createdUser: StaffUserEntity) {
        audit.insertLog(
            AuditLogEntity(
                userId = actor.id,
                role = actorRole(actor),
                officeId = actor.officeId,
                action = "USER_CREATED",
                targetType = "USER",
                targetId = createdUser.username,
                oldValue = null,
                newValue = "${createdUser.fullName} (${createdUser.username})",
                timestamp = TimeUtil.now()
            )
        )
    }

    private suspend fun actorRole(actor: StaffUserEntity): String =
        db.authDao().roleById(actor.roleId)?.name ?: "UNKNOWN"

    suspend fun logUserUpdated(actor: StaffUserEntity, targetUsername: String, oldValue: String?, newValue: String?) {
        audit.insertLog(
            AuditLogEntity(
                userId = actor.id,
                role = actorRole(actor),
                officeId = actor.officeId,
                action = "USER_UPDATED",
                targetType = "USER",
                targetId = targetUsername,
                oldValue = oldValue,
                newValue = newValue,
                timestamp = TimeUtil.now()
            )
        )
    }

    suspend fun logUserStatusChanged(actor: StaffUserEntity, targetUsername: String, oldStatus: String, newStatus: String) {
        audit.insertLog(
            AuditLogEntity(
                userId = actor.id,
                role = actorRole(actor),
                officeId = actor.officeId,
                action = "USER_STATUS_CHANGED",
                targetType = "USER",
                targetId = targetUsername,
                oldValue = oldStatus,
                newValue = newStatus,
                timestamp = TimeUtil.now()
            )
        )
    }

    suspend fun logRolePermissionsUpdated(actor: StaffUserEntity, roleName: String) {
        audit.insertLog(
            AuditLogEntity(
                userId = actor.id,
                role = actorRole(actor),
                officeId = actor.officeId,
                action = "ROLE_PERMISSIONS_UPDATED",
                targetType = "ROLE",
                targetId = roleName,
                oldValue = null,
                newValue = null,
                timestamp = TimeUtil.now()
            )
        )
    }

    suspend fun logCatalogChanged(actor: StaffUserEntity, action: String, targetType: String, targetId: String, newValue: String?) {
        audit.insertLog(
            AuditLogEntity(
                userId = actor.id,
                role = actorRole(actor),
                officeId = actor.officeId,
                action = action,
                targetType = targetType,
                targetId = targetId,
                oldValue = null,
                newValue = newValue,
                timestamp = TimeUtil.now()
            )
        )
    }

    suspend fun logLogin(actorId: Int, username: String) {
        val actor = db.authDao().userById(actorId)
        audit.insertLog(
            AuditLogEntity(
                userId = actorId,
                role = actor?.let { db.authDao().roleById(it.roleId)?.name } ?: "UNKNOWN",
                officeId = actor?.officeId ?: 0,
                action = "LOGIN",
                targetType = "SESSION",
                targetId = username,
                oldValue = null,
                newValue = null,
                timestamp = TimeUtil.now()
            )
        )
    }

    suspend fun countLogs() = audit.countLogs()
    suspend fun countLogsByUser(userId: Int) = audit.countLogsByUser(userId)
    suspend fun countLogsByAction(action: String) = audit.countLogsByAction(action)
}
