package com.mrsuffix.remindly.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mrsuffix.remindly.presentation.addevent.AddEditEventScreen
import com.mrsuffix.remindly.presentation.home.HomeScreen
import com.mrsuffix.remindly.presentation.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddEvent : Screen("add_event")
    object EditEvent : Screen("edit_event/{eventId}") {
        fun createRoute(eventId: Long) = "edit_event/$eventId"
    }
    object Settings : Screen("settings")
}

@Composable
fun RemindlyNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddEvent = {
                    navController.navigate(Screen.AddEvent.route)
                },
                onNavigateToEditEvent = { eventId ->
                    navController.navigate(Screen.EditEvent.createRoute(eventId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.AddEvent.route) {
            AddEditEventScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.EditEvent.route,
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.LongType
                }
            )
        ) {
            AddEditEventScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
