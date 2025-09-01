package com.example.noteapp.repository

import com.example.noteapp.data.NoteDao
import com.example.noteapp.data.NoteEntity
import com.example.noteapp.data.NoteImage
import com.example.noteapp.utils.orDefault
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
    
    fun searchNotes(query: String, searchType: SearchType): Flow<List<NoteEntity>> = 
        when (searchType) {
            SearchType.ALL -> searchNotesAll(query)
            SearchType.TITLE -> searchNotesByTitle(query)
            SearchType.CONTENT -> searchNotesByContent(query)
            SearchType.TAG -> searchNotesByTag(query)
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
        val note = buildNoteEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            tags = tags,
            images = images,
            isNewNote = true
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
        getNoteById(id)?.let { existingNote ->
            val updatedNote = buildNoteEntity(
                id = id,
                title = title,
                content = content,
                tags = tags,
                images = images,
                isNewNote = false,
                creationTime = existingNote.creationTime
            )
            updateNote(updatedNote)
        }
    }
    
    /**
     * 构建NoteEntity对象的通用方法
     */
    private fun buildNoteEntity(
        id: String,
        title: String,
        content: String,
        tags: List<String>,
        images: List<NoteImage>,
        isNewNote: Boolean,
        creationTime: Date = Date()
    ): NoteEntity {
        val currentTime = Date()
        return NoteEntity(
            id = id,
            title = title,
            content = content,
            creationTime = if (isNewNote) currentTime else creationTime,
            lastEditTime = currentTime,
            tags = tags,
            images = images,
            hasImages = images.isNotEmpty()
        )
    }
}