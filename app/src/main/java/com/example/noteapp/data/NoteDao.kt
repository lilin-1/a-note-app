package com.example.noteapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY lastEditTime DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' ORDER BY lastEditTime DESC")
    fun searchNotesByTitle(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE content LIKE '%' || :query || '%' ORDER BY lastEditTime DESC")
    fun searchNotesByContent(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE tags LIKE '%' || :tag || '%' ORDER BY lastEditTime DESC")
    fun searchNotesByTag(tag: String): Flow<List<NoteEntity>>

    @Query("""SELECT * FROM notes WHERE 
        title LIKE '%' || :query || '%' OR 
        content LIKE '%' || :query || '%' OR 
        tags LIKE '%' || :query || '%' 
        ORDER BY lastEditTime DESC""")
    fun searchNotesAll(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)
}