package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.CurrentRecordScreen
import com.example.ui.screens.HolidayManagementScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaveManagementScreen
import com.example.ui.screens.PreviousRecordsScreen
import com.example.ui.screens.SettingsScreen
import com.example.viewmodel.OfficeHoursViewModel

object AppDestinations {
    const val HOME = "home"
    const val CURRENT_RECORD = "current_record"
    const val PREVIOUS_RECORDS = "previous_records"
    const val SETTINGS = "settings"
    const val HOLIDAYS = "holidays"
    const val LEAVES = "leaves"
    const val ABOUT = "about"
}

@Composable
fun AppNavHost(
    viewModel: OfficeHoursViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME,
        modifier = modifier
    ) {
        composable(AppDestinations.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateCurrentRecord = { navController.navigate(AppDestinations.CURRENT_RECORD) },
                onNavigatePreviousRecords = { navController.navigate(AppDestinations.PREVIOUS_RECORDS) },
                onNavigateSettings = { navController.navigate(AppDestinations.SETTINGS) },
                onNavigateAbout = { navController.navigate(AppDestinations.ABOUT) },
                onNavigateHolidays = { navController.navigate(AppDestinations.HOLIDAYS) },
                onNavigateLeaves = { navController.navigate(AppDestinations.LEAVES) }
            )
        }

        composable(AppDestinations.CURRENT_RECORD) {
            CurrentRecordScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppDestinations.PREVIOUS_RECORDS) {
            PreviousRecordsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onPeriodSelected = {
                    navController.navigate(AppDestinations.CURRENT_RECORD) {
                        popUpTo(AppDestinations.HOME)
                    }
                }
            )
        }

        composable(AppDestinations.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateHolidays = { navController.navigate(AppDestinations.HOLIDAYS) },
                onNavigateLeaves = { navController.navigate(AppDestinations.LEAVES) },
                onNavigateAbout = { navController.navigate(AppDestinations.ABOUT) }
            )
        }

        composable(AppDestinations.HOLIDAYS) {
            HolidayManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppDestinations.LEAVES) {
            LeaveManagementScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppDestinations.ABOUT) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
