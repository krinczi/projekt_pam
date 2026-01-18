package com.example.projectapki.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.navOptions
import androidx.navigation.toRoute
import com.example.projectapki.ui.screens.DashboardScreen
import com.example.projectapki.ui.screens.DetailScreen
import com.example.projectapki.ui.screens.HistoryScreen
import com.example.projectapki.ui.screens.ZonesScreen
import com.example.projectapki.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val vm: MainViewModel = viewModel()

    val items = listOf(
        Triple(Route.Dashboard, "Dashboard", Icons.Default.Speed),
        Triple(Route.History, "Historia", Icons.Default.History),
        Triple(Route.Zones, "Strefy", Icons.Default.LocationOn),
    )

    val backStack by nav.currentBackStackEntryAsState()
    val currentDest = backStack?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { (route, label, icon) ->
                    val selected = currentDest.isOn(route)

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            nav.navigate(route, navOptions {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            })
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { pad ->
        NavHost(
            navController = nav,
            startDestination = Route.Dashboard
        ) {

            composable<Route.Dashboard> {
                DashboardScreen(
                    vm = vm,
                    padding = pad,
                    goHistory = { nav.navigate(Route.History) },
                    goZones = { nav.navigate(Route.Zones) },
                    goDetail = { id -> nav.navigate(Route.Detail(id)) }
                )
            }

            composable<Route.History> {
                HistoryScreen(
                    vm = vm,
                    padding = pad,
                    goDetail = { id -> nav.navigate(Route.Detail(id)) }
                )
            }

            composable<Route.Zones> {
                ZonesScreen(vm = vm, padding = pad)
            }

            composable<Route.Detail> { entry ->
                val args = entry.toRoute<Route.Detail>()
                DetailScreen(
                    vm = vm,
                    padding = pad,
                    measurementId = args.id,
                    goBack = { nav.popBackStack() }
                )
            }
        }
    }
}


private fun NavDestination?.isOn(route: Route): Boolean {
    val r = this?.route ?: return false
    return when (route) {
        Route.Dashboard -> r.contains("Dashboard", ignoreCase = true)
        Route.History -> r.contains("History", ignoreCase = true)
        Route.Zones -> r.contains("Zones", ignoreCase = true)
        is Route.Detail -> r.contains("Detail", ignoreCase = true)
    }
}
