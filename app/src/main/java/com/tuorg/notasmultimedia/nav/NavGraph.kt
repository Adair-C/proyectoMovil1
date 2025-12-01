package com.tuorg.notasmultimedia.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tuorg.notasmultimedia.ui.screens.DetailScreen
import com.tuorg.notasmultimedia.ui.screens.EditScreen
import com.tuorg.notasmultimedia.ui.screens.HomeScreen
import com.tuorg.notasmultimedia.ui.screens.SettingsScreen

object Routes {
    const val HOME = "home"

    // Edit
    const val EDIT = "edit"            // crear nueva
    const val EDIT_WITH_ID = "edit/{id}" // editar existente

    // Detail
    const val DETAIL = "detail/{id}"

    // Settings
    const val SETTINGS = "settings"
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        // Home
        composable(Routes.HOME) {
            HomeScreen(nav = navController)
        }

        // Nueva nota/tarea
        composable(Routes.EDIT) {
            EditScreen(nav = navController, noteId = null)
        }

        // Editar existente
        composable(
            route = Routes.EDIT_WITH_ID,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStack ->
            val id = backStack.arguments?.getString("id")
            EditScreen(nav = navController, noteId = id)
        }

        // Detalle
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStack ->
            val id = backStack.arguments?.getString("id") ?: ""
            DetailScreen(nav = navController, id = id)
        }

        // Ajustes
        composable(Routes.SETTINGS) {
            SettingsScreen()
        }
    }
}
