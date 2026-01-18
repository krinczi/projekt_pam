package com.example.projectapki.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.projectapki.ui.screens.DashboardScreen
import com.example.projectapki.ui.screens.DetailScreen
import com.example.projectapki.ui.screens.HistoryScreen
import com.example.projectapki.ui.screens.ZonesScreen
import com.example.projectapki.viewmodel.MainViewModel

@Composable
fun AppNavHost(padding: PaddingValues) {
    val nav = rememberNavController()
    val vm: MainViewModel = viewModel()

    NavHost(
        navController = nav,
        startDestination = Route.Dashboard
    ) {
        composable<Route.Dashboard> {
            DashboardScreen(
                vm = vm,
                padding = padding,
                goHistory = { nav.navigate(Route.History) },
                goZones = { nav.navigate(Route.Zones) },
                goDetail = { id -> nav.navigate(Route.Detail(id)) }
            )
        }

        composable<Route.History> {
            HistoryScreen(
                vm = vm,
                padding = padding,
                goDetail = { id -> nav.navigate(Route.Detail(id)) }
            )
        }

        composable<Route.Zones> {
            ZonesScreen(
                vm = vm,
                padding = padding
            )
        }

        composable<Route.Detail> { entry ->
            val args = entry.toRoute<Route.Detail>()
            DetailScreen(
                vm = vm,
                padding = padding,
                measurementId = args.id,
                goBack = { nav.popBackStack() }
            )
        }
    }
}
