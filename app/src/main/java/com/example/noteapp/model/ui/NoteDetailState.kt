package com.example.noteapp.model.ui

import com.example.noteapp.model.domain.Note

/**
 * 笔记详情界面UI状态
 */
data class NoteDetailState(
    val note: Note? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 笔记详情界面UI事件
 */
sealed class NoteDetailEvent {
    object NavigateBack : NoteDetailEvent()
    object NavigateToEdit : NoteDetailEvent()
    data class LoadNote(val noteId: String) : NoteDetailEvent()
}