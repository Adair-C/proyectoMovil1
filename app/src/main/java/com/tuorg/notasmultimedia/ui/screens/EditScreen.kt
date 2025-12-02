@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.tuorg.notasmultimedia.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tuorg.notasmultimedia.model.db.ItemType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
fun EditScreen(
    nav: NavController,
    noteId: String? = null,
    vm: NoteEditViewModel = viewModel(factory = NoteEditViewModel.provideFactory(noteId))
) {
    val ui by vm.state.collectAsState()

    val ctx = LocalContext.current
    val dueDate = ui.dueAt?.toLocalDate() ?: LocalDate.now()
    val dueTime = ui.dueAt?.toLocalTime() ?: LocalTime.of(9, 0)

    fun pickDate() {
        val init = ui.dueAt?.toLocalDate() ?: LocalDate.now()
        DatePickerDialog(
            ctx,
            { _, y, m, d ->
                val date = LocalDate.of(y, m + 1, d)
                val time = ui.dueAt?.toLocalTime() ?: LocalTime.of(9, 0)
                vm.setDue(LocalDateTime.of(date, time))
            },
            init.year, init.monthValue - 1, init.dayOfMonth
        ).show()
    }

    fun pickTime() {
        val init = ui.dueAt?.toLocalTime() ?: LocalTime.of(9, 0)
        TimePickerDialog(
            ctx,
            { _, h, min ->
                val date = ui.dueAt?.toLocalDate() ?: LocalDate.now()
                vm.setDue(LocalDateTime.of(date, LocalTime.of(h, min)))
            },
            init.hour, init.minute, true
        ).show()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (noteId == null) "Nueva nota" else "Editar nota") },
                navigationIcon = {
                    TextButton(onClick = { nav.popBackStack() }) { Text("Cancelar") }
                },
                actions = {
                    TextButton(
                        enabled = ui.title.isNotBlank(),
                        onClick = { vm.save { nav.popBackStack() } }
                    ) { Text("Guardar") }
                }
            )
        }
    ) { pads ->
        Column(
            Modifier
                .padding(pads)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = ui.title,
                onValueChange = vm::setTitle,
                label = { Text("Título") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.description,
                onValueChange = vm::setDescription,
                label = { Text("Descripción") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Tipo", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilterChip(
                    selected = ui.type == ItemType.TASK,
                    onClick = { vm.setType(ItemType.TASK) },
                    label = { Text("Tarea") }
                )
                FilterChip(
                    selected = ui.type == ItemType.NOTE,
                    onClick = { vm.setType(ItemType.NOTE) },
                    label = { Text("Nota") }
                )
            }

            if (ui.type == ItemType.TASK) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ElevatedButton(onClick = ::pickDate) { Text("Fecha: $dueDate") }
                    ElevatedButton(onClick = ::pickTime) {
                        Text("Hora: ${"%02d:%02d".format(dueTime.hour, dueTime.minute)}")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(checked = ui.completed, onCheckedChange = vm::setCompleted)
                    Text("Marcar como completada")
                }
            }

            Text(
                "Adjuntos (próximo): Foto, Video, Archivo, Audio",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ElevatedButton(onClick = { /* TODO */ }) { Text("+ Foto") }
                ElevatedButton(onClick = { /* TODO */ }) { Text("+ Video") }
                ElevatedButton(onClick = { /* TODO */ }) { Text("+ Archivo") }
                ElevatedButton(onClick = { /* TODO */ }) { Text("+ Audio") }
            }
        }
    }
}
