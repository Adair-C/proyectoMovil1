package com.tuorg.notasmultimedia.ui.tablet

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabletHome(
    drawerContent: @Composable () -> Unit,
    // ahora recibimos también funciones para abrir drawer y disparar acciones globales
    homePane: @Composable (
        onItemClick: (String) -> Unit,
        onOpenDrawer: () -> Unit,
        onCreateNew: () -> Unit,
    ) -> Unit,
    // el panel derecho lo decide el padre (detalle o settings)
    rightPane: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentId by remember { mutableStateOf<String?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { ModalDrawerSheet { drawerContent() } }
    ) {
        Row(Modifier.fillMaxSize()) {
            // Izquierda: lista
            Surface(Modifier.weight(if (currentId == null) 1f else 1f)) {
                homePane(
                    { id -> currentId = id },                           // onItemClick
                    { scope.launch { drawerState.open() } },            // onOpenDrawer
                    { /* se propaga al padre vía lambda de Main */ }    // onCreateNew (el padre inyectará uno real)
                )
            }
            // Sólo mostramos el panel derecho si el padre puso contenido (detalle/settings/overlay)
            // En esta versión, el padre decide cuándo mostrarlo; aquí lo mostramos siempre.
            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
            )
            Surface(Modifier.weight(1f)) {
                rightPane()
            }
        }
    }
}
