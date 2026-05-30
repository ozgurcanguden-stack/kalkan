package com.kalkan.app.core.navigation

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kalkan.app.feature.earthquakes.EarthquakesScreen
import com.kalkan.app.feature.family.FamilyScreen
import com.kalkan.app.feature.home.HomeScreen
import com.kalkan.app.feature.map.MapScreen
import com.kalkan.app.feature.profile.ProfileScreen
import com.kalkan.app.ui.screens.LoginScreen
import com.kalkan.app.ui.screens.admin.AdminDashboardScreen
import com.kalkan.app.viewmodel.AuthViewModel
import androidx.compose.runtime.collectAsState

private val bottomRoutes = listOf(
    BottomNavItem(KalkanRoute.Home, Icons.Rounded.Home, Icons.Outlined.Home),
    BottomNavItem(KalkanRoute.Earthquakes, Icons.Rounded.Public, Icons.Outlined.Public),
    BottomNavItem(KalkanRoute.Map, Icons.Rounded.Map, Icons.Outlined.Map),
    BottomNavItem(KalkanRoute.Family, Icons.Rounded.Groups, Icons.Outlined.Groups),
    BottomNavItem(KalkanRoute.Profile, Icons.Rounded.Person, Icons.Outlined.Person),
)

@Composable
fun KalkanNavHost() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()

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
            composable(KalkanRoute.Home.route) { HomeScreen() }
            composable(KalkanRoute.Earthquakes.route) { EarthquakesScreen() }
            composable(KalkanRoute.Map.route) { MapScreen() }
            composable(KalkanRoute.Family.route) { FamilyScreen() }
            composable(KalkanRoute.Profile.route) {
                ProfileScreen(
                    user = authState.user,
                    hasAdminAccess = authState.hasAdminAccess,
                    onAdminPanelClick = {
                        if (authState.hasAdminAccess) {
                            navController.navigate(KalkanRoute.AdminDashboard.route)
                        }
                    },
                    onSignOut = authViewModel::signOut,
                )
            }
            composable(KalkanRoute.AdminDashboard.route) {
                AdminDashboardScreen(
                    hasAdminAccess = authState.hasAdminAccess,
                    onBackClick = {
                        navController.navigate(KalkanRoute.Profile.route) {
                            popUpTo(KalkanRoute.Profile.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
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
