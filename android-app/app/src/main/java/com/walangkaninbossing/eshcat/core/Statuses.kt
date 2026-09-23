package com.walangkaninbossing.eshcat.core

object Roles {
    const val SUPER_ADMIN = "SUPER_ADMIN"
    const val ADMIN = "ADMIN"
    const val DEPARTMENT_HEAD = "DEPARTMENT_HEAD"
    const val STAFF = "STAFF"
    const val AUDITOR = "AUDITOR"

    fun label(name: String): String = when (name) {
        SUPER_ADMIN -> "Super Admin"
        ADMIN -> "Admin"
        DEPARTMENT_HEAD -> "Department Head"
        STAFF -> "Staff"
        AUDITOR -> "Auditor"
        else -> name
    }
}

object Statuses {

    const val SUBMITTED = "SUBMITTED"
    const val RECEIVED = "RECEIVED"
    const val UNDER_REVIEW = "UNDER_REVIEW"
    const val ADDITIONAL_REQUIREMENTS = "ADDITIONAL_REQUIREMENTS"
    const val FOR_VERIFICATION = "FOR_VERIFICATION"
    const val APPROVED = "APPROVED"
    const val REJECTED = "REJECTED"
    const val READY = "READY"
    const val COMPLETED = "COMPLETED"
    const val CANCELLED = "CANCELLED"

    val all = listOf(
        SUBMITTED, RECEIVED, UNDER_REVIEW, ADDITIONAL_REQUIREMENTS,
        FOR_VERIFICATION, APPROVED, READY, COMPLETED, REJECTED, CANCELLED
    )

    fun label(status: String): String = when (status) {
        SUBMITTED -> "Submitted"
        RECEIVED -> "Received"
        UNDER_REVIEW -> "Under Review"
        ADDITIONAL_REQUIREMENTS -> "Additional Requirements"
        FOR_VERIFICATION -> "For Verification"
        APPROVED -> "Approved"
        REJECTED -> "Rejected"
        READY -> "Ready for Release"
        COMPLETED -> "Completed"
        CANCELLED -> "Cancelled"
        else -> status.replace('_', ' ').replaceFirstChar { it.uppercase() }
    }

    fun nextStatuses(role: String, current: String): List<String> = when (role) {
        Roles.SUPER_ADMIN, Roles.ADMIN -> all.filter { it != current }
        Roles.DEPARTMENT_HEAD -> when (current) {
            UNDER_REVIEW -> listOf(FOR_VERIFICATION, ADDITIONAL_REQUIREMENTS)
            FOR_VERIFICATION -> listOf(APPROVED, REJECTED, READY)
            ADDITIONAL_REQUIREMENTS -> listOf(UNDER_REVIEW)
            READY -> listOf(COMPLETED)
            else -> emptyList()
        }
        Roles.STAFF -> when (current) {
            SUBMITTED -> listOf(RECEIVED)
            RECEIVED -> listOf(UNDER_REVIEW)
            UNDER_REVIEW -> listOf(ADDITIONAL_REQUIREMENTS)
            else -> emptyList()
        }
        else -> emptyList()
    }
}

object AppointmentStatuses {
    const val PENDING = "PENDING"
    const val APPROVED = "APPROVED"
    const val RESCHEDULED = "RESCHEDULED"
    const val CANCELLED = "CANCELLED"
    const val COMPLETED = "COMPLETED"

    val all = listOf(PENDING, APPROVED, RESCHEDULED, CANCELLED, COMPLETED)

    fun label(s: String) = s.replace('_', ' ').replaceFirstChar { it.uppercase() }
}

object ReportStatuses {
    const val PENDING = "PENDING"
    const val IN_PROGRESS = "IN_PROGRESS"
    const val RESOLVED = "RESOLVED"
    const val CLOSED = "CLOSED"

    val all = listOf(PENDING, IN_PROGRESS, RESOLVED, CLOSED)

    fun label(s: String) = when (s) {
        IN_PROGRESS -> "In Progress"
        else -> s.replace('_', ' ').replaceFirstChar { it.uppercase() }
    }
}

object AccountStatuses {
    const val ACTIVE = "ACTIVE"
    const val INACTIVE = "INACTIVE"
    const val SUSPENDED = "SUSPENDED"

    val all = listOf(ACTIVE, INACTIVE, SUSPENDED)

    fun label(s: String) = s.replaceFirstChar { it.uppercase() }
}