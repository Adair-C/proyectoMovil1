@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.tuorg.notasmultimedia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
import com.tuorg.notasmultimedia.ui.screens.EditScreen
import com.tuorg.notasmultimedia.ui.screens.HomeListOnly
import com.tuorg.notasmultimedia.ui.screens.SettingsScreen
import com.tuorg.notasmultimedia.ui.screens.TabletDetailPlaceholder
import com.tuorg.notasmultimedia.ui.tablet.TabletHome
import com.tuorg.notasmultimedia.ui.theme.AppTheme

class MainActivity : AppCompatActivity() {
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
            // --------- Flujo MÓVIL ----------
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
            homePane = { onItemClick, onOpenDrawer, onCreateNew ->
                HomeListOnly(
                    onOpenDetail = { id ->
                        showSettings = false
                        selectedId = id          // para el rightPane
                        onItemClick(id)          // si luego quieres que TabletHome reaccione
                    },
                    onCreateNew = {
                        showSettings = false
                        showEditor = true        // abre el editor en diálogo
                        onCreateNew()
                    },
                    onOpenDrawer = onOpenDrawer
                )
            },
            rightPane = {
                when {
                    showSettings -> SettingsScreen { showSettings = false }
                    selectedId != null -> DetailScreenStandalone(id = selectedId!!)
                    else -> TabletDetailPlaceholder()
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
                            val tmpNav = rememberNavController()

                            val backEntry by tmpNav.currentBackStackEntryAsState()
                            var hasShownEdit by remember { mutableStateOf(false) }

                            // Vigila la ruta para saber cuándo cerrar el diálogo
                            LaunchedEffect(backEntry?.destination?.route) {
                                when (backEntry?.destination?.route) {
                                    "edit" -> {
                                        // Ya mostramos la pantalla de edición al menos una vez
                                        hasShownEdit = true
                                    }

                                    "done" -> {
                                        // Solo cerrar si venimos de "edit"
                                        if (hasShownEdit) {
                                            hasShownEdit = false
                                            showEditor = false
                                        }
                                    }
                                }
                            }

                            // Entrar a "edit" una sola vez al abrir el diálogo
                            LaunchedEffect(Unit) {
                                if (tmpNav.currentDestination?.route != "edit") {
                                    tmpNav.navigate("edit")
                                }
                            }

                            NavHost(
                                navController = tmpNav,
                                startDestination = "done"
                            ) {
                                composable("done") { /* vacío; solo se usa para "aterrizar" tras popBackStack */ }
                                composable("edit") {
                                    // Usa tu EditScreen normalmente.
                                    // Dentro de EditScreen, al guardar/cancelar, se llama nav.popBackStack()
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
