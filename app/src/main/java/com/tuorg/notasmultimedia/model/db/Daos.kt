package com.tuorg.notasmultimedia.model.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): NoteEntity?

    @Transaction
    @Query("""
        SELECT * FROM notes
        WHERE isDeleted = 0
        ORDER BY createdAt DESC
    """)
    fun observeAll(): Flow<List<NoteWithRelations>>

    @Transaction
    @Query("""
        SELECT * FROM notes
        WHERE isDeleted = 0 AND type = :type
        ORDER BY 
          CASE WHEN :type = 'TASK' THEN dueAt END ASC,
          createdAt DESC
    """)
    fun observeByType(type: ItemType): Flow<List<NoteWithRelations>>

    @Transaction
    @Query("""
        SELECT * FROM notes
        WHERE isDeleted = 0 AND id = :id
        LIMIT 1
    """)
    fun observeById(id: String): Flow<NoteWithRelations?>

    @Query("""
        SELECT * FROM notes
        WHERE isDeleted = 0 AND (
              title       LIKE '%' || :q || '%'
          OR  description LIKE '%' || :q || '%'
          OR  content     LIKE '%' || :q || '%'
        )
        ORDER BY createdAt DESC
    """)
    fun search(q: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)
}

@Dao
interface AttachmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<AttachmentEntity>)

    @Query("DELETE FROM attachments WHERE noteId = :noteId")
    suspend fun deleteByNote(noteId: String)
}

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<ReminderEntity>)

    @Query("DELETE FROM reminders WHERE noteId = :noteId")
    suspend fun deleteByNote(noteId: String)
}
