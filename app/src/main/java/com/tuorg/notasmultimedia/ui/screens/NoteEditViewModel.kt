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

data class EditUiState(
    val id: String? = null,
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
    private val noteId: String?,
) : ViewModel() {

    private val repo = Graph.notes

    private val _state = MutableStateFlow(EditUiState())
    val state = _state.asStateFlow()

    init {
        if (!noteId.isNullOrBlank()) {
            viewModelScope.launch {
                val existing = repo.byId(noteId).firstOrNull()
                existing?.let { nwr ->
                    val n = nwr.note
                    _state.value = EditUiState(
                        id = n.id,
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
        val attachmentType = when {
            mimeType?.startsWith("image") == true -> AttachmentType.IMAGE
            mimeType?.startsWith("video") == true -> AttachmentType.VIDEO
            else -> return // No se admite este tipo de archivo por ahora
        }

        val newAttachment = AttachmentEntity(
            noteId = state.value.id ?: "", // Se corregirá al guardar
            type = attachmentType,
            uri = uri.toString(),
            description = null
        )
        _state.value = _state.value.copy(
            attachments = _state.value.attachments + newAttachment,
            showMediaPicker = false
        )
    }


    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            val s   = _state.value
            val now = LocalDateTime.now()
            val id  = s.id ?: UUID.randomUUID().toString()

            val entity = NoteEntity(
                id          = id,
                title       = s.title.trim(),
                content     = s.content.trim(),
                type        = s.type,
                isDeleted   = false,
                dirty       = false,
                description = s.description.trim(),
                createdAt   = s.createdAt ?: now,
                dueAt       = if (s.type == ItemType.TASK) s.dueAt else null,
                completed   = if (s.type == ItemType.TASK) s.completed else false
            )

            val attachmentsWithNoteId = s.attachments.map { it.copy(noteId = id) }

            repo.upsertGraph(
                note = entity,
                attachments = attachmentsWithNoteId,
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
