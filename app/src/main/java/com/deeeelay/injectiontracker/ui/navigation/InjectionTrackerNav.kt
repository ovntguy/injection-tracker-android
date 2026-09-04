package com.deeeelay.injectiontracker.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deeeelay.injectiontracker.R
import com.deeeelay.injectiontracker.data.repo.TrackerRepository
import com.deeeelay.injectiontracker.ui.screens.calendar.CalendarScreen
import com.deeeelay.injectiontracker.ui.screens.calendar.CalendarViewModel
import com.deeeelay.injectiontracker.ui.screens.home.HomeScreen
import com.deeeelay.injectiontracker.ui.screens.home.HomeViewModel
import com.deeeelay.injectiontracker.ui.screens.logdone.LogDoneScreen
import com.deeeelay.injectiontracker.ui.screens.logdone.LogDoneViewModel
import com.deeeelay.injectiontracker.ui.screens.regimen.EditRegimenScreen
import com.deeeelay.injectiontracker.ui.screens.regimen.EditRegimenViewModel
import com.deeeelay.injectiontracker.ui.screens.settings.SettingsScreen
import com.deeeelay.injectiontracker.ui.screens.settings.SettingsViewModel
import com.deeeelay.injectiontracker.ui.screens.setup.SetupScreen
import com.deeeelay.injectiontracker.ui.screens.setup.SetupViewModel

private object Routes {
    const val Setup = "setup"
    const val Main = "main"
    const val LogDone = "log_done"
    const val EditRegimen = "edit_regimen"
}

private enum class MainTab { Home, Calendar, Settings }

@Composable
fun InjectionTrackerNav(
    repository: TrackerRepository,
    factory: ViewModelProvider.Factory,
) {
    val snapshot by repository.snapshot.collectAsStateWithLifecycle(initialValue = null)
    if (snapshot == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (!snapshot!!.setupComplete) {
        val setupVm: SetupViewModel = viewModel(factory = factory)
        SetupScreen(viewModel = setupVm, onSaved = { /* snapshot flips to main */ })
        return
    }

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Main) {
        composable(Routes.Main) {
            MainTabs(
                factory = factory,
                remindersWanted = snapshot!!.dayBeforeEnabled || snapshot!!.atTimeEnabled,
                onLogInjection = { navController.navigate(Routes.LogDone) },
                onEditRegimen = { navController.navigate(Routes.EditRegimen) },
            )
        }
        composable(Routes.LogDone) {
            val vm: LogDoneViewModel = viewModel(factory = factory)
            LogDoneScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.EditRegimen) {
            val vm: EditRegimenViewModel = viewModel(factory = factory)
            EditRegimenScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun MainTabs(
    factory: ViewModelProvider.Factory,
    remindersWanted: Boolean,
    onLogInjection: () -> Unit,
    onEditRegimen: () -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(MainTab.Home) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == MainTab.Home,
                    onClick = { tab = MainTab.Home },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_home)) },
                )
                NavigationBarItem(
                    selected = tab == MainTab.Calendar,
                    onClick = { tab = MainTab.Calendar },
                    icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_calendar)) },
                )
                NavigationBarItem(
                    selected = tab == MainTab.Settings,
                    onClick = { tab = MainTab.Settings },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_settings)) },
                )
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                MainTab.Home -> {
                    val vm: HomeViewModel = viewModel(factory = factory)
                    HomeScreen(
                        viewModel = vm,
                        remindersWanted = remindersWanted,
                        onLogInjection = onLogInjection,
                        onEditRegimen = onEditRegimen,
                    )
                }
                MainTab.Calendar -> {
                    val vm: CalendarViewModel = viewModel(factory = factory)
                    CalendarScreen(viewModel = vm)
                }
                MainTab.Settings -> {
                    val vm: SettingsViewModel = viewModel(factory = factory)
                    SettingsScreen(viewModel = vm, onEditRegimen = onEditRegimen)
                }
            }
        }
    }
}
