package com.walangkaninbossing.eshcat.data.seed

import com.walangkaninbossing.eshcat.auth.PasswordHasher
import com.walangkaninbossing.eshcat.auth.RoleDefaults
import com.walangkaninbossing.eshcat.data.local.AppDatabase
import com.walangkaninbossing.eshcat.data.local.entity.AnnouncementEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationAssignmentEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationHistoryEntity
import com.walangkaninbossing.eshcat.data.local.entity.AppointmentEntity
import com.walangkaninbossing.eshcat.data.local.entity.AuditLogEntity
import com.walangkaninbossing.eshcat.data.local.entity.CommunityReportEntity
import com.walangkaninbossing.eshcat.data.local.entity.DepartmentEntity
import com.walangkaninbossing.eshcat.data.local.entity.OfficeEntity
import com.walangkaninbossing.eshcat.data.local.entity.PermissionEntity
import com.walangkaninbossing.eshcat.data.local.entity.RoleEntity
import com.walangkaninbossing.eshcat.data.local.entity.RolePermissionEntity
import com.walangkaninbossing.eshcat.data.local.entity.ServiceEntity
import com.walangkaninbossing.eshcat.data.local.entity.ServiceRequirementEntity
import com.walangkaninbossing.eshcat.data.local.entity.StaffUserEntity
import com.walangkaninbossing.eshcat.util.ReferenceGenerator
import com.walangkaninbossing.eshcat.util.TimeUtil

class DatabaseSeeder(private val db: AppDatabase) {

    suspend fun seedIfEmpty() {
        if (db.authDao().countRoles() > 0) return

        val departments = seedDepartments()
        val offices = seedOffices(departments)
        val roles = seedRoles()
        seedPermissions(roles)
        val users = seedUsers(roles, offices)
        val services = seedServices(offices)
        seedRequirements(services)
        seedAnnouncements(departments)
        seedApplications(services, offices, users)
        seedAppointments(offices)
        seedReports()
        seedAuditLogs(users)
    }

    private suspend fun seedDepartments(): List<DepartmentEntity> {
        val names = listOf(
            "Civil Registration",
            "Administration",
            "Engineering and Infrastructure",
            "Treasury",
            "Health",
            "Social Welfare",
            "Agriculture",
            "DRRM"
        )
        val list = names.map { DepartmentEntity(name = it) }
        val ids = db.referenceDao().insertDepartments(list)
        return list.mapIndexed { index, d -> d.copy(id = ids[index].toInt()) }
    }

    private suspend fun seedOffices(departments: List<DepartmentEntity>): List<OfficeEntity> {
        data class Row(
            val name: String,
            val departmentId: Int,
            val location: String,
            val contact: String,
            val email: String,
            val hours: String
        )
        val rows = listOf(
            Row("Civil Registry Office", departments[0].id, "Municipal Hall, 2F", "(055) 555-0001", "civilregistry@catarman-nsm.gov.ph", "Monday-Friday, 8:00 AM – 5:00 PM"),
            Row("Municipal Administration Office", departments[1].id, "Municipal Hall, 1F", "(055) 555-0002", "admin@catarman-nsm.gov.ph", "Monday-Friday, 8:00 AM – 5:00 PM"),
            Row("Engineering Office", departments[2].id, "Municipal Hall, Annex Bldg.", "(055) 555-0003", "engineering@catarman-nsm.gov.ph", "Monday-Friday, 8:00 AM – 5:00 PM"),
            Row("Municipal Treasurer's Office", departments[3].id, "Municipal Hall, 1F", "(055) 555-0004", "treasury@catarman-nsm.gov.ph", "Monday-Friday, 8:00 AM – 5:00 PM"),
            Row("Municipal Health Office", departments[4].id, "Rural Health Unit, Centro", "(055) 555-0005", "mho@catarman-nsm.gov.ph", "Monday-Friday, 8:00 AM – 5:00 PM"),
            Row("Social Welfare Office", departments[5].id, "Municipal Hall, 3F", "(055) 555-0006", "mswdo@catarman-nsm.gov.ph", "Monday-Friday, 8:00 AM – 5:00 PM"),
            Row("Agriculture Office", departments[6].id, "Extension Office, Poblacion", "(055) 555-0007", "agriculture@catarman-nsm.gov.ph", "Monday-Friday, 8:00 AM – 5:00 PM"),
            Row("DRRM Office", departments[7].id, "Emergency Operations Center", "(055) 555-0008", "drrmo@catarman-nsm.gov.ph", "Monday-Sunday, 24 Hours")
        )
        val list = rows.map {
            OfficeEntity(
                name = it.name,
                departmentId = it.departmentId,
                location = it.location,
                contactNumber = it.contact,
                email = it.email,
                officeHours = it.hours
            )
        }
        val ids = db.referenceDao().insertOffices(list)
        return list.mapIndexed { index, o -> o.copy(id = ids[index].toInt()) }
    }

    private suspend fun seedRoles(): List<RoleEntity> {
        data class Row(val name: String, val description: String)
        val rows = listOf(
            Row("SUPER_ADMIN", "Full system access with user, role, permission and configuration control"),
            Row("ADMIN", "Municipal operational access across offices"),
            Row("DEPARTMENT_HEAD", "Manages the applications and staff of an assigned office"),
            Row("STAFF", "Daily processing within the assigned office"),
            Row("AUDITOR", "Read-only oversight of audit logs and statistics")
        )
        val list = rows.map { RoleEntity(name = it.name, description = it.description) }
        val ids = db.authDao().insertRoles(list)
        return list.mapIndexed { index, r -> r.copy(id = ids[index].toInt()) }
    }

    private suspend fun seedPermissions(roles: List<RoleEntity>) {
        val permissionList = com.walangkaninbossing.eshcat.auth.Permissions.all.map { p ->
            PermissionEntity(
                id = p,
                name = p.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                description = com.walangkaninbossing.eshcat.auth.Permissions.description(p)
            )
        }
        db.authDao().insertPermissions(permissionList)

        val links = roles.flatMap { role ->
            RoleDefaults.defaultPermissions(role.name).map { RolePermissionEntity(role.id, it) }
        }
        db.authDao().insertRolePermissions(links)
    }

    private suspend fun seedUsers(
        roles: List<RoleEntity>,
        offices: List<OfficeEntity>
    ): List<StaffUserEntity> {
        data class Row(
            val fullName: String,
            val username: String,
            val roleName: String,
            val officeName: String
        )
        val rows = listOf(
            Row("Maria Santos", "superadmin", "SUPER_ADMIN", "Municipal Administration Office"),
            Row("Ramon Aquino", "admin", "ADMIN", "Municipal Administration Office"),
            Row("Pedro Reyes", "pedro.reyes", "DEPARTMENT_HEAD", "Civil Registry Office"),
            Row("Carmen Lopez", "carmen.lopez", "STAFF", "Civil Registry Office"),
            Row("Juan Dela Cruz", "juan.delacruz", "STAFF", "Engineering Office"),
            Row("Ana Cruz", "ana.cruz", "AUDITOR", "Municipal Administration Office")
        )
        val roleByName = roles.associateBy { it.name }
        val officeByName = offices.associateBy { it.name }
        val list = rows.map { r ->
            val role = roleByName.getValue(r.roleName)
            val office = officeByName.getValue(r.officeName)
            StaffUserEntity(
                fullName = r.fullName,
                username = r.username,
                passwordHash = PasswordHasher.hash("123456"),
                roleId = role.id,
                officeId = office.id,
                departmentId = office.departmentId,
                status = "ACTIVE",
                createdAt = TimeUtil.daysAgo(40)
            )
        }
        val ids = db.authDao().insertUsers(list)
        return list.mapIndexed { index, u -> u.copy(id = ids[index].toInt()) }
    }

    private suspend fun seedServices(offices: List<OfficeEntity>): List<ServiceEntity> {
        data class Row(
            val name: String,
            val category: String,
            val officeName: String,
            val description: String,
            val processingInfo: String,
            val online: Boolean,
            val prefix: String
        )
        val rows = listOf(
            Row("Death Certificate Application", "Certificates", "Civil Registry Office",
                "Request for a certified copy of a death certificate registered in Catarman.",
                "1–3 working days", true, "DC"),
            Row("Birth Certificate Application", "Certificates", "Civil Registry Office",
                "Request for a certified copy of a birth certificate or recognition of birth.",
                "1–3 working days", true, "BC"),
            Row("Marriage Certificate Application", "Certificates", "Civil Registry Office",
                "Request for a certified copy of a marriage certificate.",
                "1–3 working days", true, "MC"),
            Row("Certificate of Residency", "Certificates", "Civil Registry Office",
                "Issuance of a certificate confirming residency in the municipality.",
                "1 working day", true, "CR"),
            Row("Business Permit Application", "Business", "Municipal Treasurer's Office",
                "New application or renewal of mayor's/business permit and clearances.",
                "5–7 working days", true, "BP"),
            Row("Building Permit Application", "Permits", "Engineering Office",
                "Application for a building permit with technical plan submissions.",
                "10–15 working days", false, "BLDG"),
            Row("Certificate of Occupancy", "Permits", "Engineering Office",
                "Certificate confirming a structure is safe and fit for occupancy.",
                "5–10 working days", false, "COC"),
            Row("Barangay Health Certificate", "Health", "Municipal Health Office",
                "Health clearance issued by the Municipal Health Office.",
                "1 working day", true, "HC"),
            Row("Medical Records Request", "Health", "Municipal Health Office",
                "Request a copy of medical records from the Rural Health Unit.",
                "2–5 working days", false, "MR"),
            Row("Certificate of Indigency", "Social Services", "Social Welfare Office",
                "Certificate used for medical, educational, and livelihood assistance.",
                "1–2 working days", true, "IND"),
            Row("Social Case Study Report", "Social Services", "Social Welfare Office",
                "Report prepared by the Social Welfare Office for assistance programs.",
                "3–7 working days", false, "SCSR"),
            Row("Farm Input Subsidy Registration", "Agriculture", "Agriculture Office",
                "Registration of farmers for seed, fertilizer, and input subsidies.",
                "3–5 working days", true, "FARM")
        )
        val officeByName = offices.associateBy { it.name }
        val list = rows.map { r ->
            val office = officeByName.getValue(r.officeName)
            ServiceEntity(
                name = r.name,
                category = r.category,
                officeId = office.id,
                description = r.description,
                processingInfo = r.processingInfo,
                onlineAvailable = r.online
            )
        }
        val ids = db.referenceDao().insertServices(list)

        val prefixes = rows.map { it.prefix }
        return list.mapIndexed { index, s ->
            s.copy(
                id = ids[index].toInt(),
                name = s.name
            )
        }.also { _ ->
            ServicePrefixes.prefixByServiceId = list.mapIndexed { index, s ->
                s.copy(id = ids[index].toInt()) to prefixes[index]
            }
        }
    }

    private suspend fun seedRequirements(services: List<ServiceEntity>) {
        data class Row(val serviceName: String, val requirements: List<String>)
        val rows = listOf(
            Row("Death Certificate Application", listOf(
                "Valid government-issued ID",
                "Birth certificate of the deceased (if available)",
                "Letter of request from surviving relative"
            )),
            Row("Birth Certificate Application", listOf(
                "Valid government-issued ID of the requester",
                "Proof of relationship to the registrant",
                "Parent's valid ID (if applying for a minor)"
            )),
            Row("Marriage Certificate Application", listOf(
                "Valid government-issued ID",
                "Copy of marriage license",
                "Proof of relationship to the couple"
            )),
            Row("Certificate of Residency", listOf(
                "Valid ID",
                "Proof of residence (e.g., utility bill)",
                "Barangay endorsement (if required)"
            )),
            Row("Business Permit Application", listOf(
                "Accomplished business permit application form",
                "Valid ID of the owner",
                "Barangay clearance",
                "Proof of business address",
                "Fire safety inspection certificate (when applicable)"
            )),
            Row("Building Permit Application", listOf(
                "Accomplished building permit form",
                "Tax declaration of land",
                "Transfer certificate of title / land title",
                "Computed structural plans (3 sets)",
                "Locational clearance"
            )),
            Row("Certificate of Occupancy", listOf(
                "Final inspection certificate",
                "Signed building permit",
                "As-built plans"
            )),
            Row("Barangay Health Certificate", listOf(
                "Valid ID",
                "Medical check-up at the Rural Health Unit"
            )),
            Row("Medical Records Request", listOf(
                "Valid ID of the patient",
                "Consent form or authorization (if surrogate)"
            )),
            Row("Certificate of Indigency", listOf(
                "Barangay certification of indigency",
                "Valid ID",
                "Proof of income (if available)"
            )),
            Row("Social Case Study Report", listOf(
                "Letter request from the applicant",
                "Barangay referral",
                "Supporting documents of the case"
            )),
            Row("Farm Input Subsidy Registration", listOf(
                "Valid ID",
                "Proof of land ownership or tenancy",
                "Farm profile / planted area details"
            ))
        )
        val serviceByName = services.associateBy { it.name }
        val all = rows.flatMap { row ->
            val service = serviceByName.getValue(row.serviceName)
            row.requirements.map { ServiceRequirementEntity(serviceId = service.id, description = it) }
        }
        db.referenceDao().insertRequirements(all)
    }

    private suspend fun seedAnnouncements(departments: List<DepartmentEntity>) {
        val admin = departments.first { it.name == "Administration" }
        val engineering = departments.first { it.name == "Engineering and Infrastructure" }
        val health = departments.first { it.name == "Health" }
        val list = listOf(
            AnnouncementEntity(
                title = "Municipal Advisory: Regular Civil Registration Services",
                description = "The Civil Registry Office reminds residents that regular registration and copy-issuance services are available Monday to Friday. Online application submissions via eSHCAT are being processed within one to three working days.",
                departmentId = admin.id,
                date = TimeUtil.daysAgo(0),
                pinned = true
            ),
            AnnouncementEntity(
                title = "Road Concreting Schedule Along Poblacion",
                description = "The Engineering Office announces the scheduled road concreting works in the Poblacion area. Motorists are advised to take alternate routes during construction hours.",
                departmentId = engineering.id,
                date = TimeUtil.daysAgo(1),
                pinned = false
            ),
            AnnouncementEntity(
                title = "Community Cleanup Drive",
                description = "All residents are invited to join the municipality-wide cleanup drive this weekend. Assembly points will be announced by the barangay councils.",
                departmentId = health.id,
                date = TimeUtil.daysAgo(3),
                pinned = false
            )
        )
        db.requestDao().insertAnnouncements(list)
    }

    private suspend fun seedApplications(
        services: List<ServiceEntity>,
        offices: List<OfficeEntity>,
        users: List<StaffUserEntity>
    ) {
        val serviceByName = services.associateBy { it.name }
        val officeByName = offices.associateBy { it.name }
        val civil = officeByName.getValue("Civil Registry Office")
        val engineering = officeByName.getValue("Engineering Office")
        val dh = users.first { it.username == "pedro.reyes" }
        val carmen = users.first { it.username == "carmen.lopez" }
        val juan = users.first { it.username == "juan.delacruz" }

        data class Row(
            val service: ServiceEntity,
            val fullName: String,
            val email: String,
            val mobile: String,
            val address: String,
            val details: String,
            val status: String,
            val assigned: StaffUserEntity,
            val createdDaysAgo: Long,
            val history: List<Pair<String, Long>>
        )
        val rows = listOf(
            Row(serviceByName.getValue("Death Certificate Application"), "Rosario Dela Cruz",
                "rosario.dc@example.com", "0917 555 0111", "Brgy. Calachuchi, Catarman",
                "Requesting a certified copy of the death certificate of Juan Dela Cruz Sr.",
                "FOR_VERIFICATION", carmen, 6,
                listOf("SUBMITTED" to 6L, "RECEIVED" to 6L, "UNDER_REVIEW" to 4L, "FOR_VERIFICATION" to 2L)),
            Row(serviceByName.getValue("Birth Certificate Application"), "Mark Anthony Reyes",
                "mark.reyes@example.com", "0918 555 0222", "Brgy. Dalakit, Catarman",
                "Requesting a certified copy of a birth certificate for government ID purposes.",
                "UNDER_REVIEW", carmen, 3,
                listOf("SUBMITTED" to 3L, "RECEIVED" to 3L, "UNDER_REVIEW" to 1L)),
            Row(serviceByName.getValue("Building Permit Application"), "Leo Gonzales",
                "leo.gonzales@example.com", "0919 555 0333", "Brgy. Narra, Catarman",
                "Application for a building permit for a two-storey residential structure.",
                "RECEIVED", juan, 2,
                listOf("SUBMITTED" to 2L, "RECEIVED" to 1L)),
            Row(serviceByName.getValue("Certificate of Residency"), "Ana Corpuz",
                "ana.corpuz@example.com", "0920 555 0444", "Brgy. Molave, Catarman",
                "Requesting a certificate of residency for scholarship application.",
                "APPROVED", carmen, 8,
                listOf("SUBMITTED" to 8L, "RECEIVED" to 8L, "UNDER_REVIEW" to 6L, "FOR_VERIFICATION" to 4L, "APPROVED" to 3L))
        )

        rows.forEach { row ->
            val id = db.requestDao().insertApplication(
                ApplicationEntity(
                    referenceNumber = ReferenceGenerator.forService(servicePrefix(row.service)),
                    serviceId = row.service.id,
                    officeId = row.service.officeId,
                    departmentId = civilDepartment(row.service, civil, engineering),
                    fullName = row.fullName,
                    email = row.email,
                    mobile = row.mobile,
                    address = row.address,
                    requestDetails = row.details,
                    status = row.status,
                    createdDate = TimeUtil.daysAgo(row.createdDaysAgo),
                    lastUpdated = TimeUtil.daysAgo(row.history.last().second),
                    assignedToUserId = row.assigned.id
                )
            ).toInt()
            db.requestDao().insertAssignment(
                ApplicationAssignmentEntity(
                    applicationId = id,
                    staffUserId = row.assigned.id,
                    assignedDate = TimeUtil.daysAgo(row.createdDaysAgo)
                )
            )
            row.history.forEach { (status, daysAgo) ->
                val note = when (status) {
                    "SUBMITTED" -> "Application submitted by the citizen."
                    "RECEIVED" -> "Application received by the office."
                    "UNDER_REVIEW" -> "Application is being reviewed."
                    "FOR_VERIFICATION" -> "Ready for verification by the department head."
                    "APPROVED" -> "Application approved."
                    else -> "Status updated."
                }
                db.requestDao().insertHistory(
                    ApplicationHistoryEntity(
                        applicationId = id,
                        status = status,
                        note = note,
                        changedByUserId = if (status == "SUBMITTED") null else dh.id,
                        changedDate = TimeUtil.daysAgo(daysAgo)
                    )
                )
            }
            db.auditDao().insertLog(
                AuditLogEntity(
                    userId = dh.id,
                    role = "DEPARTMENT_HEAD",
                    officeId = dh.officeId,
                    action = "APPLICATION_STATUS_UPDATED",
                    targetType = "APPLICATION",
                    targetId = row.history.last().first,
                    oldValue = row.history.dropLast(1).lastOrNull()?.first,
                    newValue = row.status,
                    timestamp = TimeUtil.daysAgo(row.history.last().second)
                )
            )
        }
    }

    private fun civilDepartment(service: ServiceEntity, civil: OfficeEntity, engineering: OfficeEntity): Int =
        when (service.officeId) {
            civil.id -> civil.departmentId
            engineering.id -> engineering.departmentId
            else -> service.officeId
        }

    private suspend fun seedAppointments(offices: List<OfficeEntity>) {
        val civil = offices.first { it.name == "Civil Registry Office" }
        val engineering = offices.first { it.name == "Engineering Office" }
        val list = listOf(
            AppointmentEntity(
                officeId = civil.id,
                fullName = "Jenny Torres",
                email = "jenny.torres@example.com",
                mobile = "0921 555 0555",
                date = "2026-09-28",
                time = "09:00 AM",
                purpose = "Claim death certificate",
                status = "PENDING",
                createdDate = TimeUtil.daysAgo(1)
            ),
            AppointmentEntity(
                officeId = engineering.id,
                fullName = "Bob Marasigan",
                email = "bob.marasigan@example.com",
                mobile = "0922 555 0666",
                date = "2026-09-30",
                time = "02:00 PM",
                purpose = "Building permit consultation",
                status = "APPROVED",
                createdDate = TimeUtil.daysAgo(2)
            )
        )
        list.forEach { db.requestDao().insertAppointment(it) }
    }

    private suspend fun seedReports() {
        val list = listOf(
            CommunityReportEntity(
                category = "Road",
                location = "Brgy. Calachuchi, main road",
                description = "Large pothole near the barangay hall causing traffic slowdown.",
                fullName = "Conrad Ayala",
                contactNumber = "0923 555 0777",
                status = "PENDING",
                createdDate = TimeUtil.daysAgo(1)
            ),
            CommunityReportEntity(
                category = "Garbage",
                location = "Poblacion riverbank",
                description = "Uncollected garbage accumulating along the riverbank.",
                fullName = "Liza Maliksi",
                contactNumber = "0924 555 0888",
                status = "IN_PROGRESS",
                createdDate = TimeUtil.daysAgo(3)
            )
        )
        list.forEach { db.requestDao().insertReport(it) }
    }

    private suspend fun seedAuditLogs(users: List<StaffUserEntity>) {
        val admin = users.first { it.username == "admin" }
        val dh = users.first { it.username == "pedro.reyes" }
        val list = listOf(
            AuditLogEntity(
                userId = dh.id, role = "DEPARTMENT_HEAD", officeId = dh.officeId,
                action = "APPLICATION_STATUS_UPDATED", targetType = "APPLICATION",
                targetId = "CAT-DC-DEMO0001", oldValue = "UNDER_REVIEW", newValue = "FOR_VERIFICATION",
                timestamp = TimeUtil.daysAgo(1)
            ),
            AuditLogEntity(
                userId = admin.id, role = "ADMIN", officeId = admin.officeId,
                action = "USER_CREATED", targetType = "USER", targetId = "carmen.lopez",
                oldValue = null, newValue = "Carmen Lopez",
                timestamp = TimeUtil.daysAgo(5)
            ),
            AuditLogEntity(
                userId = dh.id, role = "DEPARTMENT_HEAD", officeId = dh.officeId,
                action = "APPLICATION_ASSIGNED", targetType = "APPLICATION",
                targetId = "CAT-DC-DEMO0001", oldValue = null, newValue = "STAFF",
                timestamp = TimeUtil.daysAgo(2)
            )
        )
        list.forEach { db.auditDao().insertLog(it) }
    }

    private fun servicePrefix(service: ServiceEntity): String =
        ServicePrefixes.prefixByServiceId.firstOrNull { it.first.id == service.id }?.second ?: "APP"
}

object ServicePrefixes {
    var prefixByServiceId: List<Pair<com.walangkaninbossing.eshcat.data.local.entity.ServiceEntity, String>> =
        emptyList()
}