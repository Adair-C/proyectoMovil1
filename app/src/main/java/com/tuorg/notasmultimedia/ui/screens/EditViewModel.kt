package com.tuorg.notasmultimedia.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuorg.notasmultimedia.data.NoteRepository
import com.tuorg.notasmultimedia.model.db.ItemType
import com.tuorg.notasmultimedia.model.db.NoteEntity
import com.tuorg.notasmultimedia.model.db.ReminderEntity
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

/**
 * ViewModel para crear/editar notas/tareas.
 * Ajusta los defaults según tu UI.
 */
class EditViewModel : ViewModel() {

    var noteId: String = UUID.randomUUID().toString()
    var title: String = ""
    var description: String = ""

    // ✅ Campo de contenido requerido por NoteEntity
    var content: String = ""

    var type: ItemType = ItemType.NOTE
    var createdAt: LocalDateTime = LocalDateTime.now()
    var dueAt: LocalDateTime? = null
    var completed: Boolean = false

    /**
     * Guarda la nota y encola recordatorio si es TASK con dueAt.
     */
    fun save(repo: NoteRepository) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()

            val note = NoteEntity(
                id = noteId,
                title = title,
                content = content,             // ✅ ahora sí se pasa content
                type = type,
                updatedAt = now,
                isDeleted = false,
                dirty = false,
                description = description,
                createdAt = createdAt,
                dueAt = dueAt,
                completed = completed
            )

            val reminders =
                if (type == ItemType.TASK && dueAt != null)
                    listOf(ReminderEntity(noteId = noteId, triggerAt = dueAt!!))
                else emptyList()

            repo.upsertGraph(
                note = note,
                attachments = emptyList(),
                reminders = reminders
            )
        }
    }
}
