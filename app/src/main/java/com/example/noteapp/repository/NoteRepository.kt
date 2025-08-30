package com.example.noteapp.repository

import com.example.noteapp.data.NoteDao
import com.example.noteapp.data.NoteEntity
import com.example.noteapp.data.NoteImage
import kotlinx.coroutines.flow.Flow
import java.util.*

enum class SearchType {
    ALL,      // 搜索所有字段
    TITLE,    // 仅搜索标题
    CONTENT,  // 仅搜索内容
    TAG       // 仅搜索标签
}

class NoteRepository(private val noteDao: NoteDao) {
    
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()
    
    suspend fun getNoteById(id: String): NoteEntity? = noteDao.getNoteById(id)
    
    fun searchNotesByTitle(query: String): Flow<List<NoteEntity>> = 
        noteDao.searchNotesByTitle(query)
    
    fun searchNotesByContent(query: String): Flow<List<NoteEntity>> = 
        noteDao.searchNotesByContent(query)
    
    fun searchNotesByTag(tag: String): Flow<List<NoteEntity>> = 
        noteDao.searchNotesByTag(tag)
    
    fun searchNotesAll(query: String): Flow<List<NoteEntity>> = 
        noteDao.searchNotesAll(query)
    
    fun searchNotes(query: String, searchType: SearchType): Flow<List<NoteEntity>> {
        return when (searchType) {
            SearchType.ALL -> searchNotesAll(query)
            SearchType.TITLE -> searchNotesByTitle(query)
            SearchType.CONTENT -> searchNotesByContent(query)
            SearchType.TAG -> searchNotesByTag(query)
        }
    }
    
    suspend fun insertNote(note: NoteEntity) = noteDao.insertNote(note)
    
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
    
    suspend fun deleteNoteById(id: String) = noteDao.deleteNoteById(id)
    
    suspend fun createNote(
        title: String,
        content: String,
        tags: List<String> = emptyList(),
        images: List<NoteImage> = emptyList()
    ): NoteEntity {
        val currentTime = Date()
        val note = NoteEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            creationTime = currentTime,
            lastEditTime = currentTime,
            tags = tags,
            images = images,
            hasImages = images.isNotEmpty()
        )
        insertNote(note)
        return note
    }
    
    suspend fun updateNote(
        id: String,
        title: String,
        content: String,
        tags: List<String> = emptyList(),
        images: List<NoteImage> = emptyList()
    ) {
        val existingNote = getNoteById(id)
        existingNote?.let { note ->
            val updatedNote = note.copy(
                title = title,
                content = content,
                tags = tags,
                images = images,
                hasImages = images.isNotEmpty(),
                lastEditTime = Date()
            )
            updateNote(updatedNote)
        }
    }
}