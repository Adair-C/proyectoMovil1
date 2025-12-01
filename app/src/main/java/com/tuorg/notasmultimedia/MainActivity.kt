@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.tuorg.notasmultimedia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tuorg.notasmultimedia.nav.AppNavHost
import com.tuorg.notasmultimedia.ui.common.LockLandscapeIf
import com.tuorg.notasmultimedia.ui.common.isTabletLandscape
import com.tuorg.notasmultimedia.ui.screens.AppDrawerContent
import com.tuorg.notasmultimedia.ui.screens.DetailScreenStandalone
import com.tuorg.notasmultimedia.ui.screens.EditScreen   // <-- usa tu EditScreen ORIGINAL
import com.tuorg.notasmultimedia.ui.screens.HomeListOnly
import com.tuorg.notasmultimedia.ui.screens.SettingsScreen
import com.tuorg.notasmultimedia.ui.screens.TabletDetailPlaceholder
import com.tuorg.notasmultimedia.ui.tablet.TabletHome
import com.tuorg.notasmultimedia.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.tuorg.notasmultimedia.di.Graph.init(applicationContext)
        setContent { App() }
    }
}

@Composable
fun App() {
    AppTheme {
        val isTablet = isTabletLandscape()
        LockLandscapeIf(isTablet)

        if (!isTablet) {
            // Flujo MÓVIL: igual que siempre
            val nav = rememberNavController()
            AppNavHost(nav)
            return@AppTheme
        }

        // --------- Flujo TABLET ----------
        var selectedId by remember { mutableStateOf<String?>(null) }
        var showSettings by remember { mutableStateOf(false) }
        var showEditor by remember { mutableStateOf(false) }

        TabletHome(
            drawerContent = {
                AppDrawerContent(
                    onHome = {
                        showSettings = false
                        selectedId = null
                    },
                    onNew = {
                        showSettings = false
                        showEditor = true
                    },
                    onSettings = {
                        selectedId = null
                        showSettings = true
                    }
                )
            },
            homePane = { onItemClick, onOpenDrawer, _ ->
                HomeListOnly(
                    onOpenDetail = { id ->
                        showSettings = false
                        selectedId = id
                    },
                    onCreateNew = {
                        showSettings = false
                        showEditor = true
                    },
                    onOpenDrawer = onOpenDrawer
                )
            },
            rightPane = {
                when {
                    showSettings      -> SettingsScreen()
                    selectedId != null -> DetailScreenStandalone(id = selectedId!!)
                    else              -> TabletDetailPlaceholder()
                }
            }
        )

        // --------- Editor en overlay (Dialog full-screen) ---------
        if (showEditor) {
            Dialog(
                onDismissRequest = { showEditor = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = { Text("Nueva nota/tarea") },
                                navigationIcon = {
                                    IconButton(onClick = { showEditor = false }) {
                                        Text("Cerrar")
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                            )
                        }
                    ) { pads ->
                        Box(
                            modifier = Modifier
                                .padding(pads)
                                .fillMaxSize()
                        ) {
                            // NAV "trampa": al hacer popBackStack() desde tu EditScreen,
                            // volvemos a "done" y ahí cerramos el diálogo.
                            val tmpNav = rememberNavController()

                            val backEntry by tmpNav.currentBackStackEntryAsState()
                            LaunchedEffect(backEntry?.destination?.route) {
                                if (backEntry?.destination?.route == "done") {
                                    showEditor = false
                                }
                            }
                            // Entramos a "edit" una sola vez
                            LaunchedEffect(Unit) {
                                if (tmpNav.currentDestination?.route != "edit") {
                                    tmpNav.navigate("edit")
                                }
                            }

                            NavHost(
                                navController = tmpNav,
                                startDestination = "done"
                            ) {
                                composable("done") { /* vacío; LaunchedEffect cierra */ }
                                composable("edit") {
                                    // Tu EditScreen ORIGINAL (no lo cambies)
                                    EditScreen(nav = tmpNav, noteId = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
