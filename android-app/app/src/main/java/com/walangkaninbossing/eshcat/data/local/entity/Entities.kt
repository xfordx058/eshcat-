package com.walangkaninbossing.eshcat.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "departments")
data class DepartmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(tableName = "offices")
data class OfficeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val departmentId: Int,
    val location: String,
    val contactNumber: String,
    val email: String,
    val officeHours: String,
    val status: String = "ACTIVE"
)

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val officeId: Int,
    val description: String,
    val processingInfo: String,
    val onlineAvailable: Boolean = true,
    val status: String = "ACTIVE"
)

@Entity(tableName = "service_requirements")
data class ServiceRequirementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serviceId: Int,
    val description: String
)

@Entity(tableName = "roles")
data class RoleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String
)

@Entity(tableName = "permissions")
data class PermissionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String
)

@Entity(tableName = "role_permissions", primaryKeys = ["roleId", "permissionId"])
data class RolePermissionEntity(
    val roleId: Int,
    val permissionId: String
)

@Entity(tableName = "staff_users")
data class StaffUserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val username: String,
    val passwordHash: String,
    val roleId: Int,
    val officeId: Int,
    val departmentId: Int,
    val status: String = "ACTIVE",
    val createdAt: String,
    val lastLogin: String? = null
)

@Entity(tableName = "applications", indices = [Index(value = ["submissionKey"], unique = true)])
data class ApplicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val referenceNumber: String,
    val serviceId: Int,
    val officeId: Int,
    val departmentId: Int,
    val fullName: String,
    val email: String,
    val mobile: String,
    val address: String,
    val requestDetails: String,
    val status: String = "SUBMITTED",
    val createdDate: String,
    val lastUpdated: String,
    val assignedToUserId: Int? = null,
    val processingNotes: String? = null,
    /** Stable client-generated key used to make a retried submission safe. */
    val submissionKey: String = UUID.randomUUID().toString(),
)

@Entity(tableName = "application_history")
data class ApplicationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val applicationId: Int,
    val status: String,
    val note: String?,
    val changedByUserId: Int?,
    val changedDate: String
)

@Entity(tableName = "application_assignments")
data class ApplicationAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val applicationId: Int,
    val staffUserId: Int,
    val assignedDate: String
)

@Entity(tableName = "appointments", indices = [Index(value = ["submissionKey"], unique = true)])
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val officeId: Int,
    val fullName: String,
    val email: String,
    val mobile: String,
    val date: String,
    val time: String,
    val purpose: String,
    val status: String = "PENDING",
    val createdDate: String,
    /** Stable client-generated key used to make a retried booking safe. */
    val submissionKey: String = UUID.randomUUID().toString(),
)

@Entity(tableName = "community_reports")
data class CommunityReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val location: String,
    val description: String,
    val fullName: String,
    val contactNumber: String,
    val status: String = "PENDING",
    val officeId: Int? = null,
    val createdDate: String
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val departmentId: Int,
    val date: String,
    val pinned: Boolean = false,
    val status: String = "PUBLISHED"
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val role: String,
    val officeId: Int,
    val action: String,
    val targetType: String,
    val targetId: String,
    val oldValue: String?,
    val newValue: String?,
    val timestamp: String
)
