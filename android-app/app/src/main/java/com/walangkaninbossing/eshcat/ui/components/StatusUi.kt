package com.walangkaninbossing.eshcat.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.Icons as MIcons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.walangkaninbossing.eshcat.core.AccountStatuses
import com.walangkaninbossing.eshcat.core.AppointmentStatuses
import com.walangkaninbossing.eshcat.core.ReportStatuses
import com.walangkaninbossing.eshcat.core.Statuses
import com.walangkaninbossing.eshcat.ui.theme.DangerAccent
import com.walangkaninbossing.eshcat.ui.theme.DangerContainerAccent
import com.walangkaninbossing.eshcat.ui.theme.InfoAccent
import com.walangkaninbossing.eshcat.ui.theme.InfoContainerAccent
import com.walangkaninbossing.eshcat.ui.theme.OnDangerContainer
import com.walangkaninbossing.eshcat.ui.theme.OnInfoContainer
import com.walangkaninbossing.eshcat.ui.theme.OnPrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.OnSuccessContainer
import com.walangkaninbossing.eshcat.ui.theme.OnWarningContainer
import com.walangkaninbossing.eshcat.ui.theme.Primary
import com.walangkaninbossing.eshcat.ui.theme.PrimaryContainerLight
import com.walangkaninbossing.eshcat.ui.theme.SuccessAccent
import com.walangkaninbossing.eshcat.ui.theme.SuccessContainerAccent
import com.walangkaninbossing.eshcat.ui.theme.WarningAccent
import com.walangkaninbossing.eshcat.ui.theme.WarningContainerAccent

data class StatusVisual(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val container: Color,
    val onContainer: Color,
)

fun statusVisual(status: String): StatusVisual = when (status) {
    Statuses.SUBMITTED,
    AppointmentStatuses.PENDING,
    ReportStatuses.PENDING,
    -> StatusVisual(
        statusLabel(status),
        MIcons.Filled.Send,
        Primary,
        PrimaryContainerLight,
        OnPrimaryContainerLight,
    )

    Statuses.RECEIVED -> StatusVisual(
        Statuses.label(Statuses.RECEIVED),
        MIcons.Filled.Inbox,
        InfoAccent,
        InfoContainerAccent,
        OnInfoContainer,
    )

    Statuses.UNDER_REVIEW,
    ReportStatuses.IN_PROGRESS,
    -> StatusVisual(
        statusLabel(status),
        MIcons.Filled.Search,
        InfoAccent,
        InfoContainerAccent,
        OnInfoContainer,
    )

    Statuses.ADDITIONAL_REQUIREMENTS -> StatusVisual(
        Statuses.label(Statuses.ADDITIONAL_REQUIREMENTS),
        MIcons.Filled.Warning,
        WarningAccent,
        WarningContainerAccent,
        OnWarningContainer,
    )

    Statuses.FOR_VERIFICATION,
    AppointmentStatuses.APPROVED,
    -> StatusVisual(
        statusLabel(status),
        MIcons.Filled.Verified,
        Primary,
        PrimaryContainerLight,
        OnPrimaryContainerLight,
    )

    Statuses.APPROVED,
    AccountStatuses.ACTIVE,
    -> StatusVisual(
        statusLabel(status),
        MIcons.Filled.CheckCircle,
        SuccessAccent,
        SuccessContainerAccent,
        OnSuccessContainer,
    )

    Statuses.REJECTED,
    AppointmentStatuses.CANCELLED,
    ReportStatuses.CLOSED,
    AccountStatuses.INACTIVE,
    -> StatusVisual(
        statusLabel(status),
        MIcons.Filled.Cancel,
        DangerAccent,
        DangerContainerAccent,
        OnDangerContainer,
    )

    Statuses.READY -> StatusVisual(
        Statuses.label(Statuses.READY),
        MIcons.Filled.Inventory,
        Primary,
        PrimaryContainerLight,
        OnPrimaryContainerLight,
    )

    Statuses.COMPLETED,
    AppointmentStatuses.COMPLETED,
    ReportStatuses.RESOLVED,
    -> StatusVisual(
        statusLabel(status),
        MIcons.Filled.TaskAlt,
        SuccessAccent,
        SuccessContainerAccent,
        OnSuccessContainer,
    )

    Statuses.CANCELLED,
    AccountStatuses.SUSPENDED,
    -> StatusVisual(
        statusLabel(status),
        MIcons.Filled.Block,
        DangerAccent,
        DangerContainerAccent,
        OnDangerContainer,
    )

    AppointmentStatuses.RESCHEDULED -> StatusVisual(
        AppointmentStatuses.label(AppointmentStatuses.RESCHEDULED),
        MIcons.Filled.Schedule,
        WarningAccent,
        WarningContainerAccent,
        OnWarningContainer,
    )

    "PENDING",
    "SCHEDULED",
    -> StatusVisual(
        statusLabel(status),
        MIcons.Filled.EventAvailable,
        InfoAccent,
        InfoContainerAccent,
        OnInfoContainer,
    )

    else -> StatusVisual(
        statusLabel(status),
        MIcons.Filled.PendingActions,
        InfoAccent,
        InfoContainerAccent,
        OnInfoContainer,
    )
}

private fun statusLabel(status: String): String = when (status) {
    Statuses.SUBMITTED, Statuses.RECEIVED, Statuses.UNDER_REVIEW,
    Statuses.ADDITIONAL_REQUIREMENTS, Statuses.FOR_VERIFICATION,
    Statuses.APPROVED, Statuses.REJECTED, Statuses.READY,
    Statuses.COMPLETED, Statuses.CANCELLED,
    -> Statuses.label(status)

    else -> status.replace('_', ' ').replaceFirstChar { it.uppercase() }
}