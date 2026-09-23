package com.walangkaninbossing.eshcat.navigation

object Routes {
    // Citizen
    const val HOME = "home"
    const val SERVICES = "services"
    const val TRACK = "track"
    const val MORE = "more"
    const val SERVICE_DETAIL = "service/{serviceId}"
    const val APPLY = "apply/form/{serviceId}"
    const val APPLY_REVIEW = "apply/review/{serviceId}"
    const val APPLY_SUCCESS = "apply/success/{ref}"
    const val TRACK_RESULT = "track/result/{ref}"
    const val APPOINTMENTS = "appointments"
    const val APPOINTMENT_FORM = "appointments/new"
    const val REPORTS = "community-reports"
    const val REPORT_FORM = "community-reports/new"
    const val ANNOUNCEMENTS = "announcements"
    const val ANNOUNCEMENT_DETAIL = "announcement/{id}"
    const val OFFICES = "offices"
    const val ABOUT = "about"
    const val SETTINGS = "citizen-settings"
    const val STAFF_LOGIN = "staff/login"

    // Staff
    const val STAFF = "staff"
    const val STAFF_DASHBOARD = "staff/dashboard"
    const val STAFF_APPLICATIONS = "staff/applications"
    const val STAFF_APPLICATION_DETAIL = "staff/applications/{id}"
    const val STAFF_APPOINTMENTS = "staff/appointments"
    const val STAFF_REPORTS = "staff/reports"
    const val STAFF_MORE = "staff/more"
    const val STAFF_PROFILE = "staff/profile"
    const val STAFF_NOTIFICATIONS = "staff/notifications"
    const val STAFF_SETTINGS = "staff/settings"
    const val STAFF_USERS = "staff/users"
    const val STAFF_USER_EDIT = "staff/users/{userId}"
    const val STAFF_ROLES = "staff/roles"
    const val STAFF_ROLE_DETAIL = "staff/roles/{roleId}"
    const val STAFF_OFFICES = "staff/offices"
    const val STAFF_OFFICE_EDIT = "staff/offices/{officeId}"
    const val STAFF_SERVICES = "staff/services"
    const val STAFF_SERVICE_EDIT = "staff/services/{serviceId}"
    const val STAFF_ANNOUNCEMENTS = "staff/announcements"
    const val STAFF_AUDIT = "staff/audit"
    const val ACCESS_DENIED = "staff/access-denied"

    fun serviceDetail(id: Int) = "service/$id"
    fun apply(serviceId: Int) = "apply/form/$serviceId"
    fun applyReview(serviceId: Int) = "apply/review/$serviceId"
    fun applySuccess(ref: String) = "apply/success/$ref"
    fun trackResult(ref: String) = "track/result/$ref"
    fun announcementDetail(id: Int) = "announcement/$id"
    fun staffApplicationDetail(id: Int) = "staff/applications/$id"
    fun staffUserEdit(userId: Int) = "staff/users/$userId"
    fun staffRoleDetail(roleId: Int) = "staff/roles/$roleId"
    fun staffOfficeEdit(officeId: Int) = "staff/offices/$officeId"
    fun staffServiceEdit(serviceId: Int) = "staff/services/$serviceId"

    val citizenTabs = setOf(HOME, SERVICES, TRACK, MORE)
    val staffModuleTabs = setOf(
        STAFF_DASHBOARD, STAFF_APPLICATIONS, STAFF_APPOINTMENTS, STAFF_REPORTS, STAFF_MORE,
    )
}