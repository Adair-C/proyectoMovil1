package com.tuorg.notasmultimedia.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tuorg.notasmultimedia.di.Graph
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
    val completed: Boolean = false
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
                        content = n.content,            // ✅ traemos el content guardado
                        type = n.type,
                        createdAt = n.createdAt,
                        dueAt = n.dueAt,
                        completed = n.completed
                    )
                }
            }
        }
    }

    fun setTitle(v: String)       { _state.value = _state.value.copy(title = v) }
    fun setDescription(v: String) { _state.value = _state.value.copy(description = v) }
    fun setContent(v: String)     { _state.value = _state.value.copy(content = v) } // ✅
    fun setType(t: ItemType)      { _state.value = _state.value.copy(type = t) }
    fun setDue(dt: LocalDateTime?){ _state.value = _state.value.copy(dueAt = dt) }
    fun setCompleted(c: Boolean)  { _state.value = _state.value.copy(completed = c) }

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

            repo.upsertGraph(
                note = entity,
                attachments = emptyList(),
                reminders = emptyList()
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
