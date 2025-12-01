@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.tuorg.notasmultimedia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun SettingsScreen() {
    var dark by remember { mutableStateOf(false) }
    var spanish by remember { mutableStateOf(true) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Ajustes") }) }
    ) { pads ->
        Column(
            modifier = Modifier
                .padding(pads)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Preferencias", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tema oscuro (placeholder)")
                Switch(checked = dark, onCheckedChange = { dark = it })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Idioma español (placeholder)")
                Switch(checked = spanish, onCheckedChange = { spanish = it })
            }

            Text(
                "Aquí luego conectarás tema real (MaterialTheme) e idioma (resources).",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
