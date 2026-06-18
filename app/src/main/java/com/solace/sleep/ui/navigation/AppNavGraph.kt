package com.solace.sleep.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.solace.sleep.R
import com.solace.sleep.ui.calendar.CalendarScreen
import com.solace.sleep.ui.daydetail.DayDetailScreen
import com.solace.sleep.ui.export.ExportScreen
import com.solace.sleep.ui.insights.InsightsScreen
import com.solace.sleep.ui.onboarding.OnboardingScreen
import com.solace.sleep.ui.profile.ProfileScreen
import com.solace.sleep.ui.settings.SettingsScreen
import java.time.LocalDate

@Composable
fun AppNavGraph(
    startDestination: Route,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem(Route.Calendar, R.string.nav_calendar, Icons.Filled.CalendarMonth),
        BottomNavItem(Route.Insights, R.string.nav_insights, Icons.Filled.BarChart),
        BottomNavItem(Route.Profile, R.string.nav_profile, Icons.Filled.Person),
        BottomNavItem(Route.Settings, R.string.nav_settings, Icons.Filled.Settings)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.hasRoute(item.route::class)
                        } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = stringResource(item.labelRes)
                                )
                            },
                            label = { Text(stringResource(item.labelRes)) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Route.Onboarding> {
                OnboardingScreen(
                    onOnboardingComplete = {
                        navController.navigate(Route.Calendar) {
                            popUpTo<Route.Onboarding> { inclusive = true }
                        }
                    }
                )
            }
            composable<Route.Calendar> {
                CalendarScreen(
                    onDaySelected = { date ->
                        navController.navigate(Route.DayDetail(date.toEpochDay()))
                    }
                )
            }
            composable<Route.Insights> {
                InsightsScreen()
            }
            composable<Route.Profile> {
                ProfileScreen()
            }
            composable<Route.Settings> {
                SettingsScreen(
                    onExportClick = { navController.navigate(Route.Export) }
                )
            }
            composable<Route.Export> {
                ExportScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Route.DayDetail> { backStackEntry ->
                val route: Route.DayDetail = backStackEntry.toRoute()
                DayDetailScreen(
                    date = LocalDate.ofEpochDay(route.dateEpochDay),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: Route,
    val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
