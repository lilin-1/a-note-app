package com.example.noteapp.repository

import com.example.noteapp.data.NoteDao
import com.example.noteapp.data.NoteEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

class NoteRepository(private val noteDao: NoteDao) {
    
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()
    
    suspend fun getNoteById(id: String): NoteEntity? = noteDao.getNoteById(id)
    
    fun searchNotesByTitle(query: String): Flow<List<NoteEntity>> = 
        noteDao.searchNotesByTitle(query)
    
    fun searchNotesByTag(tag: String): Flow<List<NoteEntity>> = 
        noteDao.searchNotesByTag(tag)
    
    suspend fun insertNote(note: NoteEntity) = noteDao.insertNote(note)
    
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
    
    suspend fun deleteNoteById(id: String) = noteDao.deleteNoteById(id)
    
    suspend fun createNote(
        title: String,
        content: String,
        tags: List<String> = emptyList(),
        hasImages: Boolean = false
    ): NoteEntity {
        val currentTime = Date()
        val note = NoteEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            creationTime = currentTime,
            lastEditTime = currentTime,
            tags = tags,
            hasImages = hasImages
        )
        insertNote(note)
        return note
    }
    
    suspend fun updateNoteContent(
        id: String,
        title: String,
        content: String,
        tags: List<String> = emptyList(),
        hasImages: Boolean = false
    ) {
        val existingNote = getNoteById(id)
        existingNote?.let { note ->
            val updatedNote = note.copy(
                title = title,
                content = content,
                tags = tags,
                hasImages = hasImages,
                lastEditTime = Date()
            )
            updateNote(updatedNote)
        }
    }
}