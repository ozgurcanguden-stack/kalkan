package com.kalkan.app.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kalkan.app.feature.admin.AdminScreen
import com.kalkan.app.feature.earthquakes.EarthquakesScreen
import com.kalkan.app.feature.family.FamilyScreen
import com.kalkan.app.feature.home.HomeScreen
import com.kalkan.app.feature.map.MapScreen
import com.kalkan.app.feature.profile.ProfileScreen

private val bottomRoutes = listOf(
    KalkanRoute.Home,
    KalkanRoute.Earthquakes,
    KalkanRoute.Map,
    KalkanRoute.Family,
    KalkanRoute.Profile,
)

@Composable
fun KalkanNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomRoutes.forEach { route ->
                    NavigationBarItem(
                        selected = currentRoute == route.route,
                        onClick = {
                            navController.navigate(route.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(route.title) },
                        icon = { Text(route.title.first().toString()) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = KalkanRoute.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(tween(220)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(220),
                )
            },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = {
                fadeIn(tween(220)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(220),
                )
            },
            popExitTransition = { fadeOut(tween(180)) },
        ) {
            composable(KalkanRoute.Home.route) { HomeScreen() }
            composable(KalkanRoute.Earthquakes.route) { EarthquakesScreen() }
            composable(KalkanRoute.Map.route) { MapScreen() }
            composable(KalkanRoute.Family.route) { FamilyScreen() }
            composable(KalkanRoute.Profile.route) { ProfileScreen(onAdminClick = { navController.navigate(KalkanRoute.Admin.route) }) }
            composable(KalkanRoute.Admin.route) { AdminScreen() }
        }
    }
}
