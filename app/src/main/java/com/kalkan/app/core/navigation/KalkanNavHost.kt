package com.kalkan.app.core.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kalkan.app.feature.earthquakes.EarthquakesScreen
import com.kalkan.app.feature.family.AddEmergencyContactScreen
import com.kalkan.app.feature.family.FamilyScreen
import com.kalkan.app.feature.home.HomeScreen
import com.kalkan.app.feature.map.MapScreen
import com.kalkan.app.feature.profile.ProfileScreen
import com.kalkan.app.ui.screens.EmergencyProfileEditScreen
import com.kalkan.app.ui.screens.EmergencyProfileViewScreen
import com.kalkan.app.ui.screens.LoginScreen
import com.kalkan.app.ui.screens.AnnouncementDetailScreen
import com.kalkan.app.feature.earthquakes.EarthquakeViewModel
import com.kalkan.app.core.notification.NotificationHelper
import com.kalkan.app.core.notification.NotificationNavigationTarget
import com.kalkan.app.viewmodel.SettingsViewModel
import com.kalkan.app.ui.screens.admin.AdminDashboardScreen
import com.kalkan.app.ui.screens.admin.CreateAnnouncementScreen
import com.kalkan.app.viewmodel.AdminDashboardViewModel
import com.kalkan.app.viewmodel.AnnouncementsViewModel
import com.kalkan.app.viewmodel.AuthViewModel
import com.kalkan.app.viewmodel.EmergencyContactsViewModel
import com.kalkan.app.viewmodel.EmergencyProfileViewModel
import com.kalkan.app.viewmodel.SafetyStatusViewModel
import com.kalkan.app.viewmodel.FamilyGroupViewModel
import com.kalkan.app.model.BackupFrequency
import androidx.compose.runtime.collectAsState

private val bottomRoutes = listOf(
    BottomNavItem(KalkanRoute.Home, Icons.Rounded.Home, Icons.Outlined.Home),
    BottomNavItem(KalkanRoute.Earthquakes, Icons.Rounded.Public, Icons.Outlined.Public),
    BottomNavItem(KalkanRoute.Map, Icons.Rounded.Map, Icons.Outlined.Map),
    BottomNavItem(KalkanRoute.Family, Icons.Rounded.Groups, Icons.Outlined.Groups),
    BottomNavItem(KalkanRoute.Profile, Icons.Rounded.Person, Icons.Outlined.Person),
)

@Composable
fun KalkanNavHost(
    notificationNavigationTarget: NotificationNavigationTarget? = null,
    onNotificationNavigationHandled: () -> Unit = {},
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        authViewModel.updateNotificationPermission(granted)
    }
    val hasNotificationPermission = remember(authState.isAuthenticated) {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                authViewModel.updateNotificationPermission(hasNotificationPermission)
            }
        }
    }

    if (!authState.isAuthenticated) {
        LoginScreen(
            isLoading = authState.isLoading,
            errorMessage = authState.errorMessage,
            onGoogleToken = authViewModel::signInWithGoogleIdToken,
            onGoogleUnavailable = authViewModel::showGoogleSetupError,
            onGuestClick = authViewModel::signInAsGuest,
        )
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(notificationNavigationTarget, authState.isAuthenticated) {
        if (!authState.isAuthenticated || notificationNavigationTarget == null) return@LaunchedEffect

        val route = when (notificationNavigationTarget) {
            NotificationNavigationTarget.Home -> KalkanRoute.Home.route
            NotificationNavigationTarget.Family -> KalkanRoute.Family.route
            is NotificationNavigationTarget.AnnouncementDetail ->
                KalkanRoute.AnnouncementDetail.createRoute(notificationNavigationTarget.announcementId)
        }

        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = route == KalkanRoute.Home.route || route == KalkanRoute.Family.route
        }
        onNotificationNavigationHandled()
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = NavigationBarDefaults.Elevation,
            ) {
                bottomRoutes.forEach { item ->
                    val selected = currentRoute == item.route.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(text = item.route.title) },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.route.title,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.secondary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = KalkanRoute.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(durationMillis = 220)) },
            exitTransition = { fadeOut(animationSpec = tween(durationMillis = 180)) },
            popEnterTransition = { fadeIn(animationSpec = tween(durationMillis = 220)) },
            popExitTransition = { fadeOut(animationSpec = tween(durationMillis = 180)) },
        ) {
            composable(KalkanRoute.Home.route) {
                val announcementsViewModel: AnnouncementsViewModel = hiltViewModel()
                val safetyStatusViewModel: SafetyStatusViewModel = hiltViewModel()
                val earthquakeViewModel: EarthquakeViewModel = hiltViewModel()
                val emergencyContactsViewModel: EmergencyContactsViewModel = hiltViewModel()

                val announcementsState by announcementsViewModel.uiState.collectAsState()
                val safetyStatusState by safetyStatusViewModel.uiState.collectAsState()
                val earthquakeState by earthquakeViewModel.uiState.collectAsState()
                val contactsState by emergencyContactsViewModel.uiState.collectAsState()

                val user = authState.user
                val isGuest = user?.isGuest == true
                val isRegistered = user != null && !isGuest

                LaunchedEffect(isGuest, isRegistered) {
                    announcementsViewModel.loadAnnouncements(
                        isGuest = isGuest,
                        isRegistered = isRegistered,
                    )
                }

                LaunchedEffect(user?.uid) {
                    emergencyContactsViewModel.startObserving(user?.uid)
                }

                HomeScreen(
                    announcementsState = announcementsState,
                    onAnnouncementClick = { announcementId ->
                        navController.navigate(
                            KalkanRoute.AnnouncementDetail.createRoute(announcementId),
                        ) {
                            launchSingleTop = true
                        }
                    },
                    onRetryAnnouncements = {
                        announcementsViewModel.loadAnnouncements(
                            isGuest = isGuest,
                            isRegistered = isRegistered,
                        )
                    },
                    safetyStatusState = safetyStatusState,
                    onSubmitSafetyStatus = { statusType ->
                        safetyStatusViewModel.submitSafetyStatus(statusType, user)
                    },
                    onSubmitSafetyStatusWithLocation = { statusType, permissionGranted ->
                        safetyStatusViewModel.submitSafetyStatusWithLocation(
                            statusType = statusType,
                            user = user,
                            permissionGranted = permissionGranted,
                        )
                    },
                    onDismissSafetyMessage = safetyStatusViewModel::clearSnackbarMessage,
                    currentUser = user,
                    onSettingsClick = {
                        navController.navigate(KalkanRoute.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    earthquakesState = earthquakeState,
                    onSeeAllEarthquakesClick = {
                        navController.navigate(KalkanRoute.Earthquakes.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    contacts = contactsState.contacts,
                    onAddContactClick = {
                        navController.navigate(KalkanRoute.AddEmergencyContact.route)
                    }
                )
            }
            composable(
                route = KalkanRoute.AnnouncementDetail.route,
                arguments = listOf(
                    navArgument("announcementId") { type = NavType.StringType },
                ),
            ) {
                val user = authState.user
                val isGuest = user?.isGuest == true
                val isRegistered = user != null && !isGuest
                AnnouncementDetailScreen(
                    isGuest = isGuest,
                    isRegistered = isRegistered,
                    onBackClick = { navController.popBackStack() },
                    onDetailUnavailable = {
                        navController.navigate(KalkanRoute.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(KalkanRoute.Earthquakes.route) { EarthquakesScreen() }
            composable(KalkanRoute.Map.route) { MapScreen() }
            composable(KalkanRoute.Family.route) {
                val emergencyContactsViewModel: EmergencyContactsViewModel = hiltViewModel()
                val familyGroupViewModel: FamilyGroupViewModel = hiltViewModel()
                val contactsState by emergencyContactsViewModel.uiState.collectAsState()
                val familyGroupState by familyGroupViewModel.uiState.collectAsState()
                val user = authState.user
                val userUid = user?.uid

                LaunchedEffect(userUid) {
                    emergencyContactsViewModel.startObserving(userUid)
                }

                LaunchedEffect(user) {
                    familyGroupViewModel.loadFamilyGroup(user)
                }

                FamilyScreen(
                    contactsState = contactsState,
                    familyGroupState = familyGroupState,
                    currentUserUid = userUid.orEmpty(),
                    onAddContactClick = {
                        navController.navigate(KalkanRoute.AddEmergencyContact.route)
                    },
                    onDeleteContact = emergencyContactsViewModel::deleteContact,
                    onDismissMessage = emergencyContactsViewModel::clearSnackbarMessage,
                    onShowActionMessage = emergencyContactsViewModel::showActionMessage,
                    onCreateFamilyGroup = familyGroupViewModel::createFamilyGroup,
                    onJoinFamilyGroup = familyGroupViewModel::joinFamilyGroup,
                    onClearFamilyError = familyGroupViewModel::clearError,
                    onClearFamilySuccessMessage = familyGroupViewModel::clearActionSuccessMessage,
                    onLeaveFamilyGroup = familyGroupViewModel::leaveFamilyGroup,
                    onDeleteFamilyGroup = familyGroupViewModel::deleteFamilyGroup,
                )
            }
            composable(KalkanRoute.AddEmergencyContact.route) {
                val emergencyContactsViewModel: EmergencyContactsViewModel = hiltViewModel()
                val contactsState by emergencyContactsViewModel.uiState.collectAsState()
                val user = authState.user

                LaunchedEffect(user?.uid) {
                    emergencyContactsViewModel.startObserving(user?.uid)
                }

                LaunchedEffect(Unit) {
                    if (!emergencyContactsViewModel.prepareAddForm()) {
                        navController.popBackStack()
                    }
                }

                AddEmergencyContactScreen(
                    form = contactsState.form,
                    formError = contactsState.formError,
                    isSaving = contactsState.isSaving,
                    onBackClick = {
                        emergencyContactsViewModel.resetAddForm()
                        navController.popBackStack()
                    },
                    onNameChange = emergencyContactsViewModel::onNameChange,
                    onPhoneChange = emergencyContactsViewModel::onPhoneChange,
                    onRelationChange = emergencyContactsViewModel::onRelationChange,
                    onPrimaryChange = emergencyContactsViewModel::onPrimaryChange,
                    onSaveContact = {
                        emergencyContactsViewModel.saveContact {
                            navController.popBackStack()
                        }
                    },
                )
            }
            composable(KalkanRoute.EmergencyProfileView.route) {
                val emergencyProfileViewModel: EmergencyProfileViewModel = hiltViewModel()
                val emergencyProfileState by emergencyProfileViewModel.uiState.collectAsState()
                val user = authState.user

                LaunchedEffect(user?.uid) {
                    emergencyProfileViewModel.startObserving(user?.uid)
                }

                EmergencyProfileViewScreen(
                    uiState = emergencyProfileState,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = {
                        emergencyProfileViewModel.prepareEditForm()
                        navController.navigate(KalkanRoute.EmergencyProfileEdit.route)
                    },
                    onDeleteConfirmed = {
                        emergencyProfileViewModel.deleteProfile {
                            navController.popBackStack()
                        }
                    },
                    onClearMessages = emergencyProfileViewModel::clearMessages,
                )
            }
            composable(KalkanRoute.EmergencyProfileEdit.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(KalkanRoute.EmergencyProfileView.route)
                }
                val emergencyProfileViewModel: EmergencyProfileViewModel = hiltViewModel(parentEntry)
                val emergencyProfileState by emergencyProfileViewModel.uiState.collectAsState()

                EmergencyProfileEditScreen(
                    form = emergencyProfileState.form,
                    formError = emergencyProfileState.formError,
                    isSaving = emergencyProfileState.isSaving,
                    onBackClick = { navController.popBackStack() },
                    onFullNameChange = emergencyProfileViewModel::onFullNameChange,
                    onBloodTypeChange = emergencyProfileViewModel::onBloodTypeChange,
                    onAllergiesChange = emergencyProfileViewModel::onAllergiesChange,
                    onChronicDiseasesChange = emergencyProfileViewModel::onChronicDiseasesChange,
                    onMedicationsChange = emergencyProfileViewModel::onMedicationsChange,
                    onEmergencyNoteChange = emergencyProfileViewModel::onEmergencyNoteChange,
                    onPrimaryContactNameChange = emergencyProfileViewModel::onPrimaryContactNameChange,
                    onPrimaryContactPhoneChange = emergencyProfileViewModel::onPrimaryContactPhoneChange,
                    onSaveClick = {
                        emergencyProfileViewModel.saveProfile {
                            navController.popBackStack()
                        }
                    },
                )
            }
            composable(KalkanRoute.Profile.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                val settingsUiState by settingsViewModel.uiState.collectAsState()
                val user = authState.user

                LaunchedEffect(user?.uid) {
                    user?.uid?.let { settingsViewModel.loadBackupSettings(it) }
                }

                ProfileScreen(
                    user = user,
                    hasAdminAccess = authState.hasAdminAccess,
                    notificationPermissionGranted = hasNotificationPermission,
                    uiState = settingsUiState,
                    onAdminPanelClick = {
                        if (authState.hasAdminAccess) {
                            navController.navigate(KalkanRoute.AdminDashboard.route)
                        }
                    },
                    onSignOut = authViewModel::signOut,
                    onBackupClick = {
                        settingsViewModel.runManualBackup(user)
                    },
                    onSetBackupFrequency = { frequency ->
                        user?.uid?.let { settingsViewModel.setBackupFrequency(it, frequency) }
                    },
                    onDeleteAccountClick = {
                        settingsViewModel.deleteAccount(user) {
                            authViewModel.signOut()
                        }
                    },
                    onTestNotificationClick = {
                        NotificationHelper.showKalkanNotification(
                            context = context,
                            title = "🛡️ Kalkan Güvenlik Testi",
                            body = "Kalkan uygulamasından başarıyla test bildirimi aldınız. Acil durumlarda hazırız!"
                        )
                    },
                    onClearMessages = {
                        settingsViewModel.clearMessages()
                    },
                    onEmergencyProfileClick = {
                        navController.navigate(KalkanRoute.EmergencyProfileView.route)
                    },
                )
            }
            composable(KalkanRoute.AdminDashboard.route) { adminEntry ->
                val adminDashboardViewModel: AdminDashboardViewModel = hiltViewModel(adminEntry)
                val adminDashboardState by adminDashboardViewModel.uiState.collectAsState()
                AdminDashboardScreen(
                    hasAdminAccess = authState.hasAdminAccess,
                    recentAnnouncements = adminDashboardState.recentAnnouncements,
                    isLoadingAnnouncements = adminDashboardState.isLoadingAnnouncements,
                    announcementsError = adminDashboardState.announcementsError,
                    onBackClick = {
                        navController.navigate(KalkanRoute.Profile.route) {
                            popUpTo(KalkanRoute.Profile.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onCreateAnnouncementClick = {
                        if (authState.hasAdminAccess) {
                            navController.navigate(KalkanRoute.CreateAnnouncement.route)
                        }
                    },
                    onRefreshAnnouncements = adminDashboardViewModel::loadRecentAnnouncements,
                )
            }
            composable(KalkanRoute.CreateAnnouncement.route) {
                val adminEntry = remember {
                    navController.getBackStackEntry(KalkanRoute.AdminDashboard.route)
                }
                val adminDashboardViewModel: AdminDashboardViewModel = hiltViewModel(adminEntry)
                val user = authState.user
                CreateAnnouncementScreen(
                    hasAdminAccess = authState.hasAdminAccess,
                    createdByUid = user?.uid.orEmpty(),
                    createdByName = user?.displayName?.takeIf { it.isNotBlank() } ?: "Admin",
                    onBackClick = { navController.popBackStack() },
                    onCreatedSuccessfully = {
                        adminDashboardViewModel.loadRecentAnnouncements()
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}

private data class BottomNavItem(
    val route: KalkanRoute,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)
