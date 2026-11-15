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
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.flow.StateFlow
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
import com.deeeelay.injectiontracker.ui.screens.regimen.RegimenUpdatedSnackbar
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

enum class MainTab { Home, Calendar, Settings }

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
        composable(Routes.Main) { entry ->
            MainTabs(
                factory = factory,
                remindersWanted = snapshot!!.dayBeforeEnabled || snapshot!!.atTimeEnabled,
                onLogInjection = { navController.navigate(Routes.LogDone) },
                onEditRegimen = { navController.navigate(Routes.EditRegimen) },
                regimenUpdated = entry.savedStateHandle
                    .getStateFlow(RegimenUpdatedSnackbar.NAV_RESULT_KEY, false),
                onRegimenUpdatedShown = {
                    entry.savedStateHandle[RegimenUpdatedSnackbar.NAV_RESULT_KEY] = false
                },
            )
        }
        composable(Routes.LogDone) {
            val vm: LogDoneViewModel = viewModel(factory = factory)
            LogDoneScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.EditRegimen) {
            val vm: EditRegimenViewModel = viewModel(factory = factory)
            EditRegimenScreen(
                viewModel = vm,
                onFinished = { savedWithChanges ->
                    if (savedWithChanges) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(RegimenUpdatedSnackbar.NAV_RESULT_KEY, true)
                    }
                    navController.popBackStack()
                },
            )
        }
    }
}

@Composable
private fun MainTabs(
    factory: ViewModelProvider.Factory,
    remindersWanted: Boolean,
    onLogInjection: () -> Unit,
    onEditRegimen: () -> Unit,
    regimenUpdated: StateFlow<Boolean>,
    onRegimenUpdatedShown: () -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(MainTab.Home) }
    val snackbarHostState = remember { SnackbarHostState() }
    val showUpdated by regimenUpdated.collectAsStateWithLifecycle()
    val updatedMessage = stringResource(R.string.snackbar_regimen_updated)
    LaunchedEffect(showUpdated) {
        if (showUpdated) {
            onRegimenUpdatedShown()
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = updatedMessage,
                duration = SnackbarDuration.Short,
            )
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { MainNavigationBar(selected = tab, onSelect = { tab = it }) },
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

/**
 * Bottom nav (Design polish spec, item 11): indicator secondaryContainer, selected icon
 * onSecondaryContainer, selected label onSurface, unselected icon and label onSurfaceVariant,
 * all from the seed-#6750A4 scheme; no hex here.
 */
@Composable
fun MainNavigationBar(selected: MainTab, onSelect: (MainTab) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val colors = NavigationBarItemDefaults.colors(
        indicatorColor = scheme.secondaryContainer,
        selectedIconColor = scheme.onSecondaryContainer,
        selectedTextColor = scheme.onSurface,
        unselectedIconColor = scheme.onSurfaceVariant,
        unselectedTextColor = scheme.onSurfaceVariant,
    )
    NavigationBar {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.label)) },
                colors = colors,
            )
        }
    }
}

private val MainTab.icon: ImageVector
    get() = when (this) {
        MainTab.Home -> Icons.Filled.Home
        MainTab.Calendar -> Icons.Filled.CalendarMonth
        MainTab.Settings -> Icons.Filled.Settings
    }

private val MainTab.label: Int
    get() = when (this) {
        MainTab.Home -> R.string.nav_home
        MainTab.Calendar -> R.string.nav_calendar
        MainTab.Settings -> R.string.nav_settings
    }
