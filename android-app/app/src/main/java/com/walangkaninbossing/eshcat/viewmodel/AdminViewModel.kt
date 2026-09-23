package com.walangkaninbossing.eshcat.viewmodel

import com.walangkaninbossing.eshcat.AppContainer
import com.walangkaninbossing.eshcat.auth.PasswordHasher
import com.walangkaninbossing.eshcat.data.local.entity.AnnouncementEntity
import com.walangkaninbossing.eshcat.data.local.entity.OfficeEntity
import com.walangkaninbossing.eshcat.data.local.entity.RoleEntity
import com.walangkaninbossing.eshcat.data.local.entity.ServiceEntity
import com.walangkaninbossing.eshcat.data.local.entity.ServiceRequirementEntity
import com.walangkaninbossing.eshcat.data.local.entity.StaffUserEntity
import com.walangkaninbossing.eshcat.util.TimeUtil

class AdminViewModel(app: AppContainer) : AppViewModel(app) {

    // ---------- Users ----------

    suspend fun usersOnce(): List<StaffUserRow> {
        app.awaitSeeded()
        return Joiners.staffUserRows(app, app.auth.usersOnce().sortedBy { it.fullName })
    }

    suspend fun createUser(
        actor: StaffUserEntity,
        fullName: String,
        username: String,
        password: String,
        roleId: Int,
        officeId: Int,
        departmentId: Int,
    ): String? {
        val cleanUsername = username.trim().lowercase()
        if (fullName.isBlank() || cleanUsername.isBlank() || password.isBlank() || roleId <= 0 || officeId <= 0) {
            return "Please complete all required fields."
        }
        if (password.length < 6) return "Password must be at least 6 characters."
        if (app.auth.userByUsername(cleanUsername) != null) return "That username is already taken."
        val user = StaffUserEntity(
            fullName = fullName.trim(),
            username = cleanUsername,
            passwordHash = PasswordHasher.hash(password),
            roleId = roleId,
            officeId = officeId,
            departmentId = departmentId,
            status = "ACTIVE",
            createdAt = TimeUtil.now(),
        )
        app.auth.createUser(user)
        val created = app.auth.userByUsername(cleanUsername) ?: return "Unable to create account."
        app.audit.logUserCreated(actor, created)
        return null
    }

    suspend fun updateUser(actor: StaffUserEntity, user: StaffUserEntity): String? {
        val existing = app.auth.userById(user.id) ?: return "User not found."
        if (existing.username != user.username && app.auth.userByUsername(user.username.trim().lowercase()) != null) {
            return "That username is already taken."
        }
        val old = "${existing.fullName} (${existing.username}) role=${existing.roleId} office=${existing.officeId}"
        app.auth.updateUser(user.copy(username = user.username.trim().lowercase()))
        app.permissionManager.clear(user.id)
        app.audit.logUserUpdated(actor, user.username, old, "role=${user.roleId} office=${user.officeId}")
        return null
    }

    suspend fun setUserStatus(actor: StaffUserEntity, userId: Int, status: String): String? {
        val target = app.auth.userById(userId) ?: return "User not found."
        val old = target.status
        app.auth.setUserStatus(userId, status)
        app.permissionManager.clear(userId)
        app.audit.logUserStatusChanged(actor, target.username, old, status)
        return null
    }

    suspend fun changePassword(actor: StaffUserEntity, userId: Int, newPassword: String): String? {
        if (newPassword.length < 6) return "Password must be at least 6 characters."
        val target = app.auth.userById(userId) ?: return "User not found."
        app.auth.updateUser(target.copy(passwordHash = PasswordHasher.hash(newPassword)))
        app.permissionManager.clear(userId)
        app.audit.logUserUpdated(actor, target.username, null, "password reset")
        return null
    }

    // ---------- Roles & permissions ----------

    suspend fun rolesOnce(): List<RoleEntity> {
        app.awaitSeeded()
        return app.auth.rolesOnce()
    }

    suspend fun roleDetail(roleId: Int): RoleDetail? {
        app.awaitSeeded()
        val role = app.auth.roleById(roleId) ?: return null
        val allPerms = app.auth.permissionsOnce().sortedBy { it.name }
        val granted = app.auth.permissionIdsForRole(roleId)
        return RoleDetail(role, allPerms, granted.sorted())
    }

    suspend fun setRolePermissions(actor: StaffUserEntity, roleId: Int, permissionIds: List<String>): String? {
        val role = app.auth.roleById(roleId) ?: return "Role not found."
        app.auth.setRolePermissions(roleId, permissionIds)
        app.audit.logRolePermissionsUpdated(actor, role.name)
        return null
    }

    // ---------- Offices ----------

    suspend fun officesOnce(): List<OfficeRow> {
        app.awaitSeeded()
        val offices = app.catalog.officesOnce()
        val depts = app.catalog.departmentsOnce().associateBy { it.id }
        val fallback = com.walangkaninbossing.eshcat.data.local.entity.DepartmentEntity(0, "Catarman LGU")
        return offices.map { OfficeRow(it, depts[it.departmentId] ?: fallback) }
    }

    suspend fun departmentsOnce() = app.catalog.departmentsOnce()

    suspend fun createOffice(
        actor: StaffUserEntity,
        name: String,
        departmentId: Int,
        location: String,
        contact: String,
        email: String,
        hours: String,
    ): String? {
        if (name.isBlank() || departmentId <= 0) return "Office name is required."
        val office = OfficeEntity(
            name = name.trim(),
            departmentId = departmentId,
            location = location.trim(),
            contactNumber = contact.trim(),
            email = email.trim(),
            officeHours = hours.trim(),
            status = "ACTIVE",
        )
        app.catalog.insertOffice(office)
        app.audit.logCatalogChanged(actor, "OFFICE_CREATED", "OFFICE", name.trim(), location.trim())
        return null
    }

    suspend fun updateOffice(actor: StaffUserEntity, office: OfficeEntity): String? {
        val existing = app.catalog.officeByIdOnce(office.id) ?: return "Office not found."
        if (office.name.isBlank()) return "Office name is required."
        val old = existing.name
        app.catalog.updateOffice(office)
        app.audit.logCatalogChanged(actor, "OFFICE_UPDATED", "OFFICE", office.name, old)
        return null
    }

    // ---------- Services ----------

    suspend fun servicesOnce(): List<ServiceRow> {
        app.awaitSeeded()
        val services = app.catalog.servicesOnce()
        val offices = app.catalog.officesOnce().associateBy { it.id }
        return services.map { ServiceRow(it, offices[it.officeId]) }
    }

    suspend fun requirementsFor(serviceId: Int): List<ServiceRequirementEntity> =
        app.catalog.requirementsForOnce(serviceId)

    suspend fun createService(
        actor: StaffUserEntity,
        name: String,
        category: String,
        officeId: Int,
        description: String,
        processingInfo: String,
        requirements: List<String>,
        onlineAvailable: Boolean,
    ): String? {
        if (name.isBlank() || category.isBlank() || officeId <= 0) return "Name, category and office are required."
        val service = ServiceEntity(
            name = name.trim(),
            category = category.trim(),
            officeId = officeId,
            description = description.trim(),
            processingInfo = processingInfo.trim(),
            onlineAvailable = onlineAvailable,
            status = "ACTIVE",
        )
        val id = app.catalog.insertService(service).toInt()
        requirements.map { it.trim() }.filter { it.isNotEmpty() }.forEach {
            app.catalog.insertRequirement(ServiceRequirementEntity(serviceId = id, description = it))
        }
        app.audit.logCatalogChanged(actor, "SERVICE_CREATED", "SERVICE", name.trim(), description.trim())
        return null
    }

    suspend fun updateService(
        actor: StaffUserEntity,
        service: ServiceEntity,
        requirements: List<String>,
    ): String? {
        val existing = app.catalog.serviceByIdOnce(service.id) ?: return "Service not found."
        if (service.name.isBlank()) return "Service name is required."
        val old = existing.name
        app.catalog.updateService(service)
        val current = app.catalog.requirementsForOnce(service.id).map { it.description }
        val newList = requirements.map { it.trim() }.filter { it.isNotEmpty() }
        if (current != newList) {
            app.catalog.requirementsForOnce(service.id).forEach { app.catalog.deleteRequirement(it) }
            newList.forEach {
                app.catalog.insertRequirement(ServiceRequirementEntity(serviceId = service.id, description = it))
            }
        }
        app.audit.logCatalogChanged(actor, "SERVICE_UPDATED", "SERVICE", service.name, old)
        return null
    }

    // ---------- Announcements ----------

    suspend fun announcementsOnce(): List<AnnouncementRow> {
        app.awaitSeeded()
        val anns = app.catalog.announcementsOnce()
        val depts = app.catalog.departmentsOnce().associateBy { it.id }
        return anns.map { AnnouncementRow(it, depts[it.departmentId]) }
    }

    suspend fun createAnnouncement(
        actor: StaffUserEntity,
        title: String,
        description: String,
        departmentId: Int,
        pinned: Boolean,
    ): String? {
        if (title.isBlank() || description.isBlank()) return "Title and description are required."
        val ann = AnnouncementEntity(
            title = title.trim(),
            description = description.trim(),
            departmentId = departmentId,
            date = TimeUtil.now(),
            pinned = pinned,
            status = "PUBLISHED",
        )
        app.catalog.insertAnnouncement(ann)
        app.audit.logCatalogChanged(actor, "ANNOUNCEMENT_CREATED", "ANNOUNCEMENT", title.trim(), null)
        return null
    }

    suspend fun updateAnnouncement(actor: StaffUserEntity, ann: AnnouncementEntity): String? {
        if (ann.title.isBlank() || ann.description.isBlank()) return "Title and description are required."
        app.catalog.updateAnnouncement(ann)
        app.audit.logCatalogChanged(actor, "ANNOUNCEMENT_UPDATED", "ANNOUNCEMENT", ann.title, null)
        return null
    }

    // ---------- Audit ----------

    suspend fun auditLogsOnce(): List<AuditRow> {
        app.awaitSeeded()
        return Joiners.auditRows(app, app.audit.logsOnce())
    }
}