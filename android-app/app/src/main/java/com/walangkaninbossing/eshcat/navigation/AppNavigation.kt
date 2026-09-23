package com.walangkaninbossing.eshcat.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.walangkaninbossing.eshcat.ESHCATApplication
import com.walangkaninbossing.eshcat.R
import com.walangkaninbossing.eshcat.ui.screens.citizen.AboutScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.AnnouncementDetailScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.AnnouncementsScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.AppointmentFormScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.AppointmentsScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.ApplyFormScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.ApplyReviewScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.ApplySuccessScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.CitizenSettingsScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.CommunityReportsScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.HomeScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.MoreScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.OfficesScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.ReportFormScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.ServiceDetailScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.ServicesScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.StaffLoginScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.TrackResultScreen
import com.walangkaninbossing.eshcat.ui.screens.citizen.TrackScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.AccessDeniedScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffAnnouncementsScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffApplicationDetailScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffApplicationsScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffAppointmentsScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffAuditScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffDashboardScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffMoreScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffNotificationsScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffOfficeEditScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffOfficesScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffProfileScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffReportsScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffRoleDetailScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffRolesScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffServiceEditScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffServicesScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffSettingsScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffUserEditScreen
import com.walangkaninbossing.eshcat.ui.screens.staff.StaffUsersScreen
import com.walangkaninbossing.eshcat.ui.theme.Primary
import com.walangkaninbossing.eshcat.ui.theme.TextMutedLight
import com.walangkaninbossing.eshcat.viewmodel.StaffSessionViewModel

private data class BottomTab(
    val route: String,
    val label: String,
    val selected: ImageVector,
    val unselected: ImageVector,
)

private val citizenTabs = listOf(
    BottomTab(Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomTab(Routes.SERVICES, "Services", Icons.Filled.Apps, Icons.Outlined.Apps),
    BottomTab(Routes.TRACK, "Track", Icons.Filled.Search, Icons.Outlined.Search),
    BottomTab(Routes.MORE, "More", Icons.Filled.Menu, Icons.Outlined.Menu),
)

private val staffTabs = listOf(
    BottomTab(Routes.STAFF_DASHBOARD, "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    BottomTab(Routes.STAFF_APPLICATIONS, "Applications", Icons.Filled.Description, Icons.Outlined.Description),
    BottomTab(Routes.STAFF_APPOINTMENTS, "Appointments", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    BottomTab(Routes.STAFF_REPORTS, "Reports", Icons.Filled.ReportProblem, Icons.Outlined.ReportProblem),
    BottomTab(Routes.STAFF_MORE, "More", Icons.Filled.Menu, Icons.Outlined.Menu),
)

@Composable
fun AppNavigation() {
    val app = LocalContext.current.applicationContext as ESHCATApplication
    val sessionVm: StaffSessionViewModel = viewModel {
        StaffSessionViewModel(app.container)
    }
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val sessionState by sessionVm.state.collectAsState()

    LaunchedEffect(sessionVm.isLoggedIn, route) {
        if (sessionState.ready) {
            if (sessionVm.isLoggedIn && route == Routes.HOME) {
                navController.navigate(Routes.STAFF_DASHBOARD) { launchSingleTop = true }
            } else if (!sessionVm.isLoggedIn && route != null && route.startsWith("staff") && route != Routes.STAFF_LOGIN) {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.HOME) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    val showCitizenBar = route in Routes.citizenTabs
    val showStaffBar = route in Routes.staffModuleTabs

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            when {
                showCitizenBar -> BottomBar(navController, citizenTabs, route)
                showStaffBar -> BottomBar(navController, staffTabs, route)
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            NavHost(navController = navController, startDestination = Routes.HOME) {
                // Citizen
                composable(Routes.HOME) { HomeScreen(navController) }
                composable(Routes.SERVICES) { ServicesScreen(navController) }
                composable(Routes.TRACK) { TrackScreen(navController) }
                composable(Routes.MORE) { MoreScreen(navController) }
                composable(Routes.SERVICE_DETAIL) { entry ->
                    val id = entry.arguments?.getString("serviceId")?.toIntOrNull() ?: -1
                    ServiceDetailScreen(navController, id)
                }
                composable(Routes.APPLY) { entry ->
                    val id = entry.arguments?.getString("serviceId")?.toIntOrNull() ?: -1
                    ApplyFormScreen(navController, id)
                }
                composable(Routes.APPLY_REVIEW) { entry ->
                    val id = entry.arguments?.getString("serviceId")?.toIntOrNull() ?: -1
                    ApplyReviewScreen(navController, id)
                }
                composable(Routes.APPLY_SUCCESS) { entry ->
                    val ref = entry.arguments?.getString("ref").orEmpty()
                    ApplySuccessScreen(navController, ref)
                }
                composable(Routes.TRACK_RESULT) { entry ->
                    val ref = entry.arguments?.getString("ref").orEmpty()
                    TrackResultScreen(navController, ref)
                }
                composable(Routes.APPOINTMENTS) { AppointmentsScreen(navController) }
                composable(Routes.APPOINTMENT_FORM) { AppointmentFormScreen(navController) }
                composable(Routes.REPORTS) { CommunityReportsScreen(navController) }
                composable(Routes.REPORT_FORM) { ReportFormScreen(navController) }
                composable(Routes.ANNOUNCEMENTS) { AnnouncementsScreen(navController) }
                composable(Routes.ANNOUNCEMENT_DETAIL) { entry ->
                    val id = entry.arguments?.getString("id")?.toIntOrNull() ?: -1
                    AnnouncementDetailScreen(navController, id)
                }
                composable(Routes.OFFICES) { OfficesScreen(navController) }
                composable(Routes.ABOUT) { AboutScreen(navController) }
                composable(Routes.SETTINGS) { CitizenSettingsScreen(navController) }
                composable(Routes.STAFF_LOGIN) { StaffLoginScreen(navController, sessionVm) }

                // Staff
                composable(Routes.STAFF_DASHBOARD) { StaffDashboardScreen(navController, sessionVm) }
                composable(Routes.STAFF_APPLICATIONS) { StaffApplicationsScreen(navController, sessionVm) }
                composable(Routes.STAFF_APPLICATION_DETAIL) { entry ->
                    val id = entry.arguments?.getString("id")?.toIntOrNull() ?: -1
                    StaffApplicationDetailScreen(navController, id, sessionVm)
                }
                composable(Routes.STAFF_APPOINTMENTS) { StaffAppointmentsScreen(navController, sessionVm) }
                composable(Routes.STAFF_REPORTS) { StaffReportsScreen(navController, sessionVm) }
                composable(Routes.STAFF_MORE) { StaffMoreScreen(navController, sessionVm) }
                composable(Routes.STAFF_PROFILE) { StaffProfileScreen(navController, sessionVm) }
                composable(Routes.STAFF_NOTIFICATIONS) { StaffNotificationsScreen(navController, sessionVm) }
                composable(Routes.STAFF_SETTINGS) { StaffSettingsScreen(navController, sessionVm) }
                composable(Routes.STAFF_USERS) { StaffUsersScreen(navController, sessionVm) }
                composable(Routes.STAFF_USER_EDIT) { entry ->
                    val userId = entry.arguments?.getString("userId")?.toIntOrNull() ?: -1
                    StaffUserEditScreen(navController, userId, sessionVm)
                }
                composable(Routes.STAFF_ROLES) { StaffRolesScreen(navController, sessionVm) }
                composable(Routes.STAFF_ROLE_DETAIL) { entry ->
                    val roleId = entry.arguments?.getString("roleId")?.toIntOrNull() ?: -1
                    StaffRoleDetailScreen(navController, roleId, sessionVm)
                }
                composable(Routes.STAFF_OFFICES) { StaffOfficesScreen(navController, sessionVm) }
                composable(Routes.STAFF_OFFICE_EDIT) { entry ->
                    val officeId = entry.arguments?.getString("officeId")?.toIntOrNull() ?: -1
                    StaffOfficeEditScreen(navController, officeId, sessionVm)
                }
                composable(Routes.STAFF_SERVICES) { StaffServicesScreen(navController, sessionVm) }
                composable(Routes.STAFF_SERVICE_EDIT) { entry ->
                    val serviceId = entry.arguments?.getString("serviceId")?.toIntOrNull() ?: -1
                    StaffServiceEditScreen(navController, serviceId, sessionVm)
                }
                composable(Routes.STAFF_ANNOUNCEMENTS) { StaffAnnouncementsScreen(navController, sessionVm) }
                composable(Routes.STAFF_AUDIT) { StaffAuditScreen(navController, sessionVm) }
                composable(Routes.ACCESS_DENIED) { AccessDeniedScreen(navController) }
            }

            if (!sessionState.ready) {
                SplashScreen()
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavController, tabs: List<BottomTab>, currentRoute: String?) {
    val backStack by navController.currentBackStackEntryAsState()
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        tabs.forEach { tab ->
            val selected = backStack?.destination?.hierarchy?.any { it.route == tab.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(tab.route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selected else tab.unselected,
                        contentDescription = tab.label,
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    indicatorColor = Primary.copy(alpha = 0.12f),
                    unselectedIconColor = TextMutedLight,
                    unselectedTextColor = TextMutedLight,
                ),
            )
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color(0xFF0A1226)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.eshcat_logo),
                contentDescription = "eSHCAT logo",
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(36.dp)),
                contentScale = ContentScale.Crop,
            )
            Text(
                text = "eSHCAT",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.padding(top = 20.dp),
            )
            Text(
                text = "Catarman will be better, together.",
                style = MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
            )
        }
    }
}