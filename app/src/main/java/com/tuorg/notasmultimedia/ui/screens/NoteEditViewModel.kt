package com.tuorg.notasmultimedia.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tuorg.notasmultimedia.di.Graph
import com.tuorg.notasmultimedia.model.db.AttachmentEntity
import com.tuorg.notasmultimedia.model.db.AttachmentType
import com.tuorg.notasmultimedia.model.db.ItemType
import com.tuorg.notasmultimedia.model.db.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import android.content.Intent


data class EditUiState(
    val id: String, // ID ya no es nullable, siempre tendrá un valor
    val isNewNote: Boolean = true,
    val title: String = "",
    val description: String = "",
    val content: String = "",
    val type: ItemType = ItemType.NOTE,
    val createdAt: LocalDateTime? = null,
    val dueAt: LocalDateTime? = null,
    val completed: Boolean = false,
    val attachments: List<AttachmentEntity> = emptyList(),
    val showMediaPicker: Boolean = false,
)

class NoteEditViewModel(
    noteId: String?, // ID que llega desde la navegación, puede ser null
) : ViewModel() {

    private val repo = Graph.notes
    private val contentResolver = Graph.contentResolver

    // ID estable para la sesión de edición. O el que llega, o uno nuevo.
    private val stableNoteId = noteId ?: UUID.randomUUID().toString()

    private val _state = MutableStateFlow(EditUiState(id = stableNoteId, isNewNote = noteId == null))
    val state = _state.asStateFlow()

    init {
        // Si el noteId NO es nulo, significa que estamos editando una nota existente.
        // Por lo tanto, cargamos sus datos.
        if (noteId != null) {
            viewModelScope.launch {
                repo.byId(noteId).firstOrNull()?.let { nwr ->
                    val n = nwr.note
                    _state.value = EditUiState(
                        id = n.id,
                        isNewNote = false,
                        title = n.title,
                        description = n.description,
                        content = n.content,
                        type = n.type,
                        createdAt = n.createdAt,
                        dueAt = n.dueAt,
                        completed = n.completed,
                        attachments = nwr.attachments,
                    )
                }
            }
        }
    }

    fun setTitle(v: String)       { _state.value = _state.value.copy(title = v) }
    fun setDescription(v: String) { _state.value = _state.value.copy(description = v) }
    fun setContent(v: String)     { _state.value = _state.value.copy(content = v) }
    fun setType(t: ItemType)      { _state.value = _state.value.copy(type = t) }
    fun setDue(dt: LocalDateTime?){ _state.value = _state.value.copy(dueAt = dt) }
    fun setCompleted(c: Boolean)  { _state.value = _state.value.copy(completed = c) }

    fun showMediaPicker(show: Boolean) { _state.value = _state.value.copy(showMediaPicker = show) }

    fun onMediaSelected(uri: Uri, mimeType: String?) {
        // ¡LA SOLUCIÓN! Tomamos el permiso persistente para la URI
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val attachmentType = when {
            mimeType?.startsWith("image") == true -> AttachmentType.IMAGE
            mimeType?.startsWith("video") == true -> AttachmentType.VIDEO
            else -> return // Tipo de archivo no soportado
        }

        // El adjunto se crea directamente con la ID correcta y estable.
        val newAttachment = AttachmentEntity(
            noteId = stableNoteId,
            type = attachmentType,
            uri = uri.toString(),
            description = null
        )
        _state.value = _state.value.copy(
            attachments = _state.value.attachments + newAttachment,
            showMediaPicker = false
        )
    }

    fun onAttachmentRemoved(attachment: AttachmentEntity) {
        _state.value = _state.value.copy(attachments = _state.value.attachments.filterNot { it.uri == attachment.uri })
    }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            val s   = _state.value
            val now = LocalDateTime.now()

            // LA VERDAD DE Entities.kt: updatedAt es Long, createdAt es LocalDateTime.
            // Creamos las variables con los tipos correctos que pide el constructor de NoteEntity.
            val updatedAtAsLong = System.currentTimeMillis()

            val entity = NoteEntity(
                id          = s.id,
                title       = s.title.trim(),
                content     = s.content.trim(),
                type        = s.type,
                isDeleted   = false,
                dirty       = true,
                description = s.description.trim(),
                createdAt   = s.createdAt ?: now, // Se pasa LocalDateTime (Correcto)
                updatedAt   = updatedAtAsLong,    // Se pasa Long (Correcto)
                dueAt       = if (s.type == ItemType.TASK) s.dueAt else null, // Se pasa LocalDateTime? (Correcto)
                completed   = if (s.type == ItemType.TASK) s.completed else false
            )

            repo.upsertGraph(
                note = entity,
                attachments = s.attachments,
                reminders = emptyList() //TODO: Guardar recordatorios
            )
            onSaved()
        }
    }

    companion object {
        fun provideFactory(noteId: String?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NoteEditViewModel(noteId) as T
                }
            }
    }
}
