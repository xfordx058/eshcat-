package com.walangkaninbossing.eshcat.viewmodel

import com.walangkaninbossing.eshcat.data.local.entity.ApplicationEntity
import com.walangkaninbossing.eshcat.util.ReferenceGenerator
import com.walangkaninbossing.eshcat.util.TimeUtil
import java.util.UUID

data class ApplicationDraft(
    val serviceId: Int,
    val officeId: Int,
    val departmentId: Int,
    val prefix: String,
    val fullName: String,
    val email: String,
    val mobile: String,
    val address: String,
    val requestDetails: String,
    val submissionKey: String = UUID.randomUUID().toString(),
)

data class TrackResult(
    val app: ApplicationEntity,
    val serviceName: String,
    val officeName: String,
    val history: List<com.walangkaninbossing.eshcat.data.local.entity.ApplicationHistoryEntity>,
)

class ApplicationFlowViewModel(app: com.walangkaninbossing.eshcat.AppContainer) : AppViewModel(app) {

    suspend fun submit(draft: ApplicationDraft): Pair<ApplicationEntity?, String?> {
        if (draft.fullName.isBlank() || draft.mobile.isBlank() || draft.address.isBlank() || draft.requestDetails.isBlank()) {
            return null to "Please complete all required fields."
        }
        if (!draft.email.isNullOrBlank() && !draft.email.contains("@")) {
            return null to "Please enter a valid email address."
        }
        val entity = ApplicationEntity(
            referenceNumber = ReferenceGenerator.forService(draft.prefix),
            serviceId = draft.serviceId,
            officeId = draft.officeId,
            departmentId = draft.departmentId,
            fullName = draft.fullName.trim(),
            email = draft.email.trim(),
            mobile = draft.mobile.trim(),
            address = draft.address.trim(),
            requestDetails = draft.requestDetails.trim(),
            status = "SUBMITTED",
            createdDate = TimeUtil.now(),
            lastUpdated = TimeUtil.now(),
            submissionKey = draft.submissionKey,
        )
        val saved = app.applications.submit(entity, actor = null)
        return saved to null
    }

    suspend fun track(ref: String): TrackResult? {
        app.awaitSeeded()
        val clean = ref.trim().uppercase()
        val appEntity = app.applications.applicationByRef(clean) ?: return null
        val service = app.catalog.serviceByIdOnce(appEntity.serviceId)
        val office = app.catalog.officeByIdOnce(appEntity.officeId)
        val history = app.applications.historyOnce(appEntity.id)
return TrackResult(
            app = appEntity,
            serviceName = service?.name ?: "Unknown service",
            officeName = office?.name ?: "Unknown office",
            history = history,
        )
    }
}
