@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.tuorg.notasmultimedia.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.tuorg.notasmultimedia.BuildConfig
import com.tuorg.notasmultimedia.model.db.ItemType
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Objects

@Composable
fun EditScreen(
    nav: NavController,
    noteId: String? = null,
    vm: NoteEditViewModel = viewModel(factory = NoteEditViewModel.provideFactory(noteId))
) {
    val ui by vm.state.collectAsState()
    val ctx = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    // --- Funciones y Launchers para Multimedia ---

    // Crea un Uri temporal para guardar la foto o video capturado
    fun getUriForFile(context: Context, extension: String): Uri {
        val file = File.createTempFile(
            "temp_media_${System.currentTimeMillis()}",
            extension,
            context.externalCacheDir
        )
        return FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
    }

    // Launcher para tomar una foto
    val takePhotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempUri?.let { vm.onMediaSelected(it, "image/jpeg") }
        }
    }

    // Launcher para grabar un video
    val recordVideoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) {
            tempUri?.let { vm.onMediaSelected(it, "video/mp4") }
        }
    }

    // Launcher para seleccionar imagen/video de la galería (usando el selector de fotos moderno)
    val pickVisualMediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val mimeType = ctx.contentResolver.getType(uri)
            vm.onMediaSelected(uri, mimeType)
        }
    }


    // --- Diálogos ---

    // Diálogo para elegir la fuente de la multimedia
    if (ui.showMediaPicker) {
        Dialog(onDismissRequest = { vm.showMediaPicker(false) }) {
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Añadir multimedia", style = MaterialTheme.typography.titleLarge)

                    TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
                        vm.showMediaPicker(false)
                        val uri = getUriForFile(ctx, ".jpg")
                        tempUri = uri
                        takePhotoLauncher.launch(uri)
                    }) { Text("Tomar foto") }

                    TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
                        vm.showMediaPicker(false)
                        val uri = getUriForFile(ctx, ".mp4")
                        tempUri = uri
                        recordVideoLauncher.launch(uri)
                    }) { Text("Grabar video") }

                    TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
                        vm.showMediaPicker(false)
                        pickVisualMediaLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    }) { Text("Elegir de la galería") }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { vm.showMediaPicker(false) }) {
                            Text("Cancelar")
                        }
                    }
                }
            }
        }
    }

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

    // --- UI Principal de la pantalla ---
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
                        Text("Hora: %02d:%02d".format(dueTime.hour, dueTime.minute))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(checked = ui.completed, onCheckedChange = vm::setCompleted)
                    Text("Marcar como completada")
                }
            }

            Text(
                "Adjuntos",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ElevatedButton(onClick = { vm.showMediaPicker(true) }) { Text("+ Foto/Video") }
                ElevatedButton(onClick = { /* TODO */ }) { Text("+ Archivo") }
                ElevatedButton(onClick = { /* TODO */ }) { Text("+ Audio") }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)){
                items(ui.attachments){
                    AsyncImage(model = it.uri, contentDescription = it.description, modifier = Modifier
                        .size(96.dp)
                        .clickable { /*TODO: open media*/ })
                }
            }
        }
    }
}
