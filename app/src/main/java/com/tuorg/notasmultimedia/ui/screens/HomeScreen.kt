@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.tuorg.notasmultimedia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tuorg.notasmultimedia.model.db.ItemType
import com.tuorg.notasmultimedia.model.db.NoteWithRelations
import com.tuorg.notasmultimedia.nav.Routes
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.tuorg.notasmultimedia.R
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.ExperimentalMaterial3Api

//  Versión MÓVIL
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController, vm: HomeViewModel = viewModel()) {
    val items by vm.items.collectAsState()
    val tab by vm.tab.collectAsState()
    val query by vm.query.collectAsState()

    ModalNavigationDrawer(
        drawerContent = {
            AppDrawerContent(
                onHome = { /* ya estás en Home */ },
                onNew = { nav.navigate(Routes.EDIT) },
                onSettings = { nav.navigate(Routes.SETTINGS) }
            )
        }
    ) {
        HomeContent(
            query = query,
            tab = tab,
            items = items,
            onQueryChange = vm::setQuery,
            onTabChange = vm::setTab,
            onCreateNew = { nav.navigate(Routes.EDIT) },
            onOpenDetail = { id -> nav.navigate("detail/$id") },
            showFab = true,
            onOpenDrawer = null
        )
    }
}

//  Versión TABLET
@Composable
fun HomeListOnly(
    vm: HomeViewModel = viewModel(),
    onOpenDetail: (String) -> Unit,
    onCreateNew: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val items by vm.items.collectAsState()
    val tab by vm.tab.collectAsState()
    val query by vm.query.collectAsState()

    HomeContent(
        query = query,
        tab = tab,
        items = items,
        onQueryChange = vm::setQuery,
        onTabChange = vm::setTab,
        onCreateNew = onCreateNew,
        onOpenDetail = onOpenDetail,
        showFab = true,
        onOpenDrawer = onOpenDrawer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    query: String,
    tab: Int,
    items: List<NoteWithRelations>,
    onQueryChange: (String) -> Unit,
    onTabChange: (Int) -> Unit,
    onCreateNew: () -> Unit,
    onOpenDetail: (String) -> Unit,
    showFab: Boolean,
    onOpenDrawer: (() -> Unit)?
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_settings)) },
                navigationIcon = {
                    if (onOpenDrawer != null) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                },
                actions = {
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = { Text(stringResource(R.string.menu_settings)) },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true,
                        modifier = Modifier.width(220.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(onClick = onCreateNew) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
    ) { pads ->
        Column(Modifier.padding(pads)) {
            TabRow(selectedTabIndex = tab) {
                listOf(
                    stringResource(R.string.tab_all),
                    stringResource(R.string.tab_tasks),
                    stringResource(R.string.tab_notes)
                ).forEachIndexed { i, t ->
                    Tab(selected = tab == i, onClick = { onTabChange(i) }, text = { Text(t) })
                }
            }
            LazyColumn(Modifier.fillMaxSize().padding(8.dp)) {
                items(items) { it ->
                    ElevatedCard(
                        onClick = { onOpenDetail(it.note.id) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                if (it.note.type == ItemType.TASK) "🗓 ${it.note.title}" else it.note.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                it.note.description.take(80),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
