package com.walangkaninbossing.eshcat.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.walangkaninbossing.eshcat.navigation.Routes
import com.walangkaninbossing.eshcat.ui.components.PrimaryButton
import com.walangkaninbossing.eshcat.ui.theme.DangerAccent
import com.walangkaninbossing.eshcat.ui.theme.DangerContainerAccent
import com.walangkaninbossing.eshcat.ui.theme.EshcatSpacing
import com.walangkaninbossing.eshcat.ui.theme.TextSecondaryLight

@Composable
fun AccessDeniedScreen(nav: NavController) {
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(EshcatSpacing.lg),
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(DangerContainerAccent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = DangerAccent,
                    modifier = Modifier.size(34.dp),
                )
            }
            Text("Access Restricted", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "You do not have permission to view this module. Please contact your administrator.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryLight,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(EshcatSpacing.md))
            PrimaryButton("Back to Dashboard") {
                nav.navigate(Routes.STAFF_DASHBOARD) {
                    popUpTo(Routes.STAFF_MORE) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }
}