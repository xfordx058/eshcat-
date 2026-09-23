package com.walangkaninbossing.eshcat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.walangkaninbossing.eshcat.core.Roles
import com.walangkaninbossing.eshcat.data.local.entity.AnnouncementEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationEntity
import com.walangkaninbossing.eshcat.data.local.entity.ApplicationHistoryEntity
import com.walangkaninbossing.eshcat.data.local.entity.ServiceEntity
import com.walangkaninbossing.eshcat.ui.theme.BorderLight
import com.walangkaninbossing.eshcat.ui.theme.EshcatRadius
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.Primary
import com.walangkaninbossing.eshcat.ui.theme.ScreenPadding
import com.walangkaninbossing.eshcat.ui.theme.SurfaceVariantLight
import com.walangkaninbossing.eshcat.ui.theme.TextMutedLight
import com.walangkaninbossing.eshcat.ui.theme.TextSecondaryLight
import com.walangkaninbossing.eshcat.util.TimeUtil

// ---------- App bar ----------

@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EshcatSpacing.sm, vertical = EshcatSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            trailing?.invoke(this)
        }
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    }
}

// ---------- Cards ----------

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(EshcatSpacing.lg),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(EshcatRadius.xl)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        ) {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, color = Primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ---------- Badges ----------

@Composable
fun StatusBadge(visual: StatusVisual, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(EshcatRadius.pill))
            .background(visual.container)
            .padding(horizontal = EshcatSpacing.md, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(visual.icon, contentDescription = null, tint = visual.color, modifier = Modifier.size(15.dp))
        Text(
            text = visual.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = visual.onContainer,
        )
    }
}

@Composable
fun RoleBadge(role: String, modifier: Modifier = Modifier) {
    val (bg, fg) = when (role) {
        Roles.SUPER_ADMIN, Roles.ADMIN -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        Roles.DEPARTMENT_HEAD -> com.walangkaninbossing.eshcat.ui.theme.WarningContainerAccent to com.walangkaninbossing.eshcat.ui.theme.OnWarningContainer
        Roles.AUDITOR -> com.walangkaninbossing.eshcat.ui.theme.SuccessContainerAccent to com.walangkaninbossing.eshcat.ui.theme.OnSuccessContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = Roles.label(role),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = fg,
        modifier = modifier
            .clip(RoundedCornerShape(EshcatRadius.pill))
            .background(bg)
            .padding(horizontal = EshcatSpacing.md, vertical = 5.dp),
    )
}

// ---------- Search ----------

@Composable
fun EshcatSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = TextMutedLight) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondaryLight) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(EshcatRadius.pill),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = BorderLight,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}

// ---------- Buttons ----------

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(EshcatRadius.lg),
        colors = ButtonDefaults.buttonColors(containerColor = Primary),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp,
            )
        } else {
            Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(EshcatRadius.lg),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun LabeledField(label: String, value: String, modifier: Modifier = Modifier, mono: Boolean = false) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMutedLight)
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = if (mono) androidx.compose.ui.text.font.FontFamily.Monospace else null,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun InfoRow(label: String, value: String?, modifier: Modifier = Modifier) {
    if (value.isNullOrBlank()) return
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
        Spacer(Modifier.width(EshcatSpacing.md))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ---------- Stats ----------

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    tint: Color = Primary,
    tintContainer: Color = com.walangkaninbossing.eshcat.ui.theme.PrimaryContainerLight,
    onClick: (() -> Unit)? = null,
) {
    GlassCard(modifier = modifier, onClick = onClick) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(tintContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(EshcatSpacing.md))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
    }
}

@Composable
fun StatCardSkeleton(modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        SkeletonBlock(modifier = Modifier.size(42.dp), shape = CircleShape)
        Spacer(Modifier.height(EshcatSpacing.md))
        SkeletonBlock(modifier = Modifier.fillMaxWidth(0.5f).height(28.dp))
        Spacer(Modifier.height(6.dp))
        SkeletonBlock(modifier = Modifier.fillMaxWidth(0.85f).height(12.dp))
    }
}

// ---------- Filters ----------

@Composable
fun FilterChipsRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
        items(options) { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(option, maxLines = 1) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary,
                    selectedLabelColor = Color.White,
                ),
                shape = RoundedCornerShape(EshcatRadius.pill),
            )
        }
    }
}

// ---------- Cards for data ----------

@Composable
fun ServiceCard(
    service: ServiceEntity,
    officeName: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(EshcatRadius.md))
                    .background(com.walangkaninbossing.eshcat.ui.theme.PrimaryContainerLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Verified, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(EshcatSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = officeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun ApplicationCard(
    app: ApplicationEntity,
    serviceName: String,
    applicant: String,
    modifier: Modifier = Modifier,
    assignedToName: String? = null,
    onClick: (() -> Unit)? = null,
) {
    GlassCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusBadge(statusVisual(app.status))
        }
        Spacer(Modifier.height(EshcatSpacing.sm))
        Text(
            text = serviceName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = app.referenceNumber,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = TextSecondaryLight,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(text = applicant, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        Text(
            text = TimeUtil.displayDateTime(app.createdDate),
            style = MaterialTheme.typography.labelSmall,
            color = TextMutedLight,
        )
        if (assignedToName != null) {
            Spacer(Modifier.height(4.dp))
            Text(text = "Routed to: $assignedToName", style = MaterialTheme.typography.labelSmall, color = TextSecondaryLight)
        }
    }
}

@Composable
fun AnnouncementCard(
    ann: AnnouncementEntity,
    departmentName: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    GlassCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Filled.Send,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(EshcatSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ann.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = ann.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryLight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${TimeUtil.displayDate(ann.date)} · $departmentName",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedLight,
                )
            }
        }
    }
}

// ---------- State ----------

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(EshcatSpacing.xxl), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/**
 * A layout-stable placeholder for data that is expected shortly. Use this for content
 * loading; reserve the spinner above for small, immediate actions such as form submission.
 */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(EshcatRadius.sm),
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.42f,
        targetValue = 0.78f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeletonAlpha",
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)),
    )
}

@Composable
fun ServiceCardSkeleton(modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonBlock(modifier = Modifier.size(46.dp), shape = RoundedCornerShape(EshcatRadius.md))
            Spacer(Modifier.width(EshcatSpacing.md))
            Column(verticalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp))
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.45f).height(12.dp))
            }
        }
    }
}

@Composable
fun AnnouncementCardSkeleton(modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            SkeletonBlock(modifier = Modifier.size(20.dp), shape = CircleShape)
            Spacer(Modifier.width(EshcatSpacing.sm))
            Column(verticalArrangement = Arrangement.spacedBy(EshcatSpacing.sm)) {
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.75f).height(16.dp))
                SkeletonBlock(modifier = Modifier.fillMaxWidth().height(12.dp))
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.55f).height(12.dp))
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(EshcatSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(EshcatSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SurfaceVariantLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = TextMutedLight, modifier = Modifier.size(34.dp))
        }
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(EshcatSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(EshcatSpacing.sm),
    ) {
        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (onRetry != null) {
            TextButton(onClick = onRetry) { Text("Try again", color = Primary) }
        }
    }
}

@Composable
fun RefreshButton(onRefresh: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    IconButton(onClick = onRefresh, enabled = enabled, modifier = modifier) {
        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
    }
}

// ---------- Access gate ----------

@Composable
fun PermissionGate(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (enabled) {
        content()
    } else {
        Column(
            modifier = modifier.fillMaxWidth().padding(EshcatSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(EshcatSpacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(com.walangkaninbossing.eshcat.ui.theme.DangerContainerAccent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = com.walangkaninbossing.eshcat.ui.theme.DangerAccent,
                    modifier = Modifier.size(34.dp),
                )
            }
            Text(
                "Access Restricted",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "You do not have the required permission for this module. Please contact the administrator.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryLight,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

// ---------- Timeline ----------

data class TimelineStep(
    val label: String,
    val note: String? = null,
    val phase: TimelinePhase = TimelinePhase.STANDARD,
)

enum class TimelinePhase { DONE, CURRENT, STANDARD }

@Composable
fun ApplicationFlowTimeline(
    currentStatus: String,
    modifier: Modifier = Modifier,
    latestNote: String? = null,
) {
    val chain = applicationFlowChain(currentStatus)
    Column(modifier = modifier) {
        chain.forEachIndexed { index, step ->
            val achieved = chain.indexOfFirst { it == currentStatus || additionalCovered(it, currentStatus) }
            val phase = when {
                index < achieved -> TimelinePhase.DONE
                index == achieved -> TimelinePhase.CURRENT
                else -> TimelinePhase.STANDARD
            }
            TimelineRow(
                label = StatusesAlias.labelOf(step),
                note = if (phase == TimelinePhase.CURRENT) latestNote else null,
                phase = phase,
                isFirst = index == 0,
                isLast = index == chain.lastIndex,
            )
        }
    }
}

private fun additionalCovered(chainElement: String, current: String): Boolean =
    current == StatusesAlias.ADDITIONAL_REQUIREMENTS && chainElement == StatusesAlias.RECEIVED

@Composable
fun HistoryTimeline(
    history: List<ApplicationHistoryEntity>,
    userNameFor: (Int?) -> String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        history.forEachIndexed { index, item ->
            val last = index == history.lastIndex
            TimelineRow(
                label = StatusesAlias.labelOf(item.status),
                note = item.note,
                caption = "${TimeUtil.displayDateTime(item.changedDate)} · ${userNameFor(item.changedByUserId)}",
                phase = if (last) TimelinePhase.CURRENT else TimelinePhase.DONE,
                isFirst = index == 0,
                isLast = last,
            )
        }
    }
}

@Composable
private fun TimelineRow(
    label: String,
    phase: TimelinePhase,
    isFirst: Boolean,
    isLast: Boolean,
    note: String? = null,
    caption: String? = null,
) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(32.dp)) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(14.dp)
                        .background(if (phase == TimelinePhase.STANDARD) BorderLight else Primary)
                )
            }
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        when (phase) {
                            TimelinePhase.DONE -> Primary
                            TimelinePhase.CURRENT -> Primary
                            TimelinePhase.STANDARD -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (phase == TimelinePhase.DONE) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                } else if (phase == TimelinePhase.CURRENT) {
                    Box(Modifier.size(9.dp).clip(CircleShape).background(Color.White))
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .height(12.dp)
                        .background(if (phase == TimelinePhase.STANDARD) BorderLight else Primary)
                )
            }
        }
        Column(modifier = Modifier.padding(start = EshcatSpacing.sm, bottom = EshcatSpacing.md)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (phase == TimelinePhase.CURRENT) FontWeight.SemiBold else FontWeight.Normal,
                color = if (phase == TimelinePhase.STANDARD) TextMutedLight else MaterialTheme.colorScheme.onSurface,
            )
            if (caption != null) {
                Text(text = caption, style = MaterialTheme.typography.labelSmall, color = TextMutedLight)
            }
            if (note != null) {
                Spacer(Modifier.height(2.dp))
                Text(text = note, style = MaterialTheme.typography.bodySmall, color = TextSecondaryLight)
            }
        }
    }
}

private object StatusesAlias {
    const val SUBMITTED = "SUBMITTED"
    const val RECEIVED = "RECEIVED"
    const val UNDER_REVIEW = "UNDER_REVIEW"
    const val ADDITIONAL_REQUIREMENTS = "ADDITIONAL_REQUIREMENTS"
    const val FOR_VERIFICATION = "FOR_VERIFICATION"
    const val APPROVED = "APPROVED"
    const val READY = "READY"
    const val COMPLETED = "COMPLETED"

    fun labelOf(s: String): String = com.walangkaninbossing.eshcat.core.Statuses.label(s)
}

fun applicationFlowChain(current: String): List<String> =
    if (current == com.walangkaninbossing.eshcat.core.Statuses.ADDITIONAL_REQUIREMENTS) {
        listOf(
            com.walangkaninbossing.eshcat.core.Statuses.SUBMITTED,
            com.walangkaninbossing.eshcat.core.Statuses.RECEIVED,
        )
    } else {
        val base = listOf(
            com.walangkaninbossing.eshcat.core.Statuses.SUBMITTED,
            com.walangkaninbossing.eshcat.core.Statuses.RECEIVED,
            com.walangkaninbossing.eshcat.core.Statuses.UNDER_REVIEW,
            com.walangkaninbossing.eshcat.core.Statuses.FOR_VERIFICATION,
            com.walangkaninbossing.eshcat.core.Statuses.APPROVED,
            com.walangkaninbossing.eshcat.core.Statuses.READY,
            com.walangkaninbossing.eshcat.core.Statuses.COMPLETED,
        )
        val idx = base.indexOf(current)
        if (idx >= 0) base.subList(0, idx + 1) else base
    }
