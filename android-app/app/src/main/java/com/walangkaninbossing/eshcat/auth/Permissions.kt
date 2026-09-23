package com.walangkaninbossing.eshcat.auth

import com.walangkaninbossing.eshcat.core.Roles

object Permissions {
    const val VIEW_DASHBOARD = "VIEW_DASHBOARD"
    const val VIEW_APPLICATIONS = "VIEW_APPLICATIONS"
    const val VIEW_ALL_APPLICATIONS = "VIEW_ALL_APPLICATIONS"
    const val VIEW_OFFICE_APPLICATIONS = "VIEW_OFFICE_APPLICATIONS"
    const val VIEW_ASSIGNED_APPLICATIONS = "VIEW_ASSIGNED_APPLICATIONS"
    const val CREATE_APPLICATION = "CREATE_APPLICATION"
    const val UPDATE_APPLICATION = "UPDATE_APPLICATION"
    const val UPDATE_APPLICATION_STATUS = "UPDATE_APPLICATION_STATUS"
    const val APPROVE_APPLICATION = "APPROVE_APPLICATION"
    const val REJECT_APPLICATION = "REJECT_APPLICATION"
    const val REQUEST_REQUIREMENTS = "REQUEST_REQUIREMENTS"
    const val ASSIGN_APPLICATIONS = "ASSIGN_APPLICATIONS"
    const val VIEW_APPOINTMENTS = "VIEW_APPOINTMENTS"
    const val MANAGE_APPOINTMENTS = "MANAGE_APPOINTMENTS"
    const val VIEW_REPORTS = "VIEW_REPORTS"
    const val MANAGE_REPORTS = "MANAGE_REPORTS"
    const val VIEW_SERVICES = "VIEW_SERVICES"
    const val MANAGE_SERVICES = "MANAGE_SERVICES"
    const val MANAGE_REQUIREMENTS = "MANAGE_REQUIREMENTS"
    const val VIEW_ANNOUNCEMENTS = "VIEW_ANNOUNCEMENTS"
    const val MANAGE_ANNOUNCEMENTS = "MANAGE_ANNOUNCEMENTS"
    const val VIEW_OFFICES = "VIEW_OFFICES"
    const val MANAGE_OFFICES = "MANAGE_OFFICES"
    const val MANAGE_USERS = "MANAGE_USERS"
    const val MANAGE_ROLES = "MANAGE_ROLES"
    const val MANAGE_PERMISSIONS = "MANAGE_PERMISSIONS"
    const val VIEW_AUDIT_LOGS = "VIEW_AUDIT_LOGS"
    const val VIEW_STATISTICS = "VIEW_STATISTICS"
    const val MANAGE_SETTINGS = "MANAGE_SETTINGS"

    val all = listOf(
        VIEW_DASHBOARD,
        VIEW_APPLICATIONS,
        VIEW_ALL_APPLICATIONS,
        VIEW_OFFICE_APPLICATIONS,
        VIEW_ASSIGNED_APPLICATIONS,
        CREATE_APPLICATION,
        UPDATE_APPLICATION,
        UPDATE_APPLICATION_STATUS,
        APPROVE_APPLICATION,
        REJECT_APPLICATION,
        REQUEST_REQUIREMENTS,
        ASSIGN_APPLICATIONS,
        VIEW_APPOINTMENTS,
        MANAGE_APPOINTMENTS,
        VIEW_REPORTS,
        MANAGE_REPORTS,
        VIEW_SERVICES,
        MANAGE_SERVICES,
        MANAGE_REQUIREMENTS,
        VIEW_ANNOUNCEMENTS,
        MANAGE_ANNOUNCEMENTS,
        VIEW_OFFICES,
        MANAGE_OFFICES,
        MANAGE_USERS,
        MANAGE_ROLES,
        MANAGE_PERMISSIONS,
        VIEW_AUDIT_LOGS,
        VIEW_STATISTICS,
        MANAGE_SETTINGS
    )

    fun description(p: String): String = when (p) {
        VIEW_DASHBOARD -> "View the staff dashboard"
        VIEW_APPLICATIONS -> "View applications"
        VIEW_ALL_APPLICATIONS -> "View all applications across offices"
        VIEW_OFFICE_APPLICATIONS -> "View applications of the assigned office"
        VIEW_ASSIGNED_APPLICATIONS -> "View applications assigned to me"
        CREATE_APPLICATION -> "Create applications"
        UPDATE_APPLICATION -> "Update application details"
        UPDATE_APPLICATION_STATUS -> "Update application status"
        APPROVE_APPLICATION -> "Approve applications"
        REJECT_APPLICATION -> "Reject applications"
        REQUEST_REQUIREMENTS -> "Request additional requirements"
        ASSIGN_APPLICATIONS -> "Assign applications to staff"
        VIEW_APPOINTMENTS -> "View appointments"
        MANAGE_APPOINTMENTS -> "Manage appointments"
        VIEW_REPORTS -> "View community reports"
        MANAGE_REPORTS -> "Manage community reports"
        VIEW_SERVICES -> "View services and requirements"
        MANAGE_SERVICES -> "Create and edit services"
        MANAGE_REQUIREMENTS -> "Manage service requirements"
        VIEW_ANNOUNCEMENTS -> "View announcements"
        MANAGE_ANNOUNCEMENTS -> "Publish and manage announcements"
        VIEW_OFFICES -> "View offices"
        MANAGE_OFFICES -> "Manage offices"
        MANAGE_USERS -> "Manage staff accounts"
        MANAGE_ROLES -> "Manage roles"
        MANAGE_PERMISSIONS -> "Manage role permissions"
        VIEW_AUDIT_LOGS -> "View audit logs"
        VIEW_STATISTICS -> "View system statistics"
        MANAGE_SETTINGS -> "Manage system settings"
        else -> p
    }
}

object RoleDefaults {

    fun defaultPermissions(roleName: String): List<String> = when (roleName) {
        Roles.SUPER_ADMIN -> Permissions.all
        Roles.ADMIN -> Permissions.all - listOf(
            Permissions.MANAGE_ROLES,
            Permissions.MANAGE_PERMISSIONS,
            Permissions.MANAGE_SETTINGS
        )
        Roles.DEPARTMENT_HEAD -> listOf(
            Permissions.VIEW_DASHBOARD,
            Permissions.VIEW_APPLICATIONS,
            Permissions.VIEW_OFFICE_APPLICATIONS,
            Permissions.UPDATE_APPLICATION,
            Permissions.UPDATE_APPLICATION_STATUS,
            Permissions.APPROVE_APPLICATION,
            Permissions.REJECT_APPLICATION,
            Permissions.REQUEST_REQUIREMENTS,
            Permissions.ASSIGN_APPLICATIONS,
            Permissions.VIEW_APPOINTMENTS,
            Permissions.MANAGE_APPOINTMENTS,
            Permissions.VIEW_REPORTS,
            Permissions.MANAGE_REPORTS,
            Permissions.VIEW_SERVICES,
            Permissions.MANAGE_REQUIREMENTS,
            Permissions.VIEW_ANNOUNCEMENTS,
            Permissions.VIEW_OFFICES
        )
        Roles.STAFF -> listOf(
            Permissions.VIEW_DASHBOARD,
            Permissions.VIEW_APPLICATIONS,
            Permissions.VIEW_OFFICE_APPLICATIONS,
            Permissions.VIEW_ASSIGNED_APPLICATIONS,
            Permissions.UPDATE_APPLICATION,
            Permissions.UPDATE_APPLICATION_STATUS,
            Permissions.REQUEST_REQUIREMENTS,
            Permissions.VIEW_APPOINTMENTS,
            Permissions.MANAGE_APPOINTMENTS,
            Permissions.VIEW_REPORTS,
            Permissions.MANAGE_REPORTS,
            Permissions.VIEW_SERVICES,
            Permissions.VIEW_ANNOUNCEMENTS,
            Permissions.VIEW_OFFICES
        )
        Roles.AUDITOR -> listOf(
            Permissions.VIEW_DASHBOARD,
            Permissions.VIEW_APPLICATIONS,
            Permissions.VIEW_ALL_APPLICATIONS,
            Permissions.VIEW_APPOINTMENTS,
            Permissions.VIEW_REPORTS,
            Permissions.VIEW_SERVICES,
            Permissions.VIEW_ANNOUNCEMENTS,
            Permissions.VIEW_OFFICES,
            Permissions.VIEW_AUDIT_LOGS,
            Permissions.VIEW_STATISTICS
        )
        else -> listOf(Permissions.VIEW_DASHBOARD)
    }
}