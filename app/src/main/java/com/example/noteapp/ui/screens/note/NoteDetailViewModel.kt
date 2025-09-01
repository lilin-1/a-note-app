package com.example.noteapp.ui.screens.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.model.domain.toNote
import com.example.noteapp.model.ui.NoteDetailEvent
import com.example.noteapp.model.ui.NoteDetailState
import com.example.noteapp.viewmodel.NoteViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 笔记详情ViewModel
 * 管理笔记详情界面的状态和业务逻辑
 */
class NoteDetailViewModel(
    private val noteViewModel: NoteViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteDetailState())
    val uiState: StateFlow<NoteDetailState> = _uiState.asStateFlow()

    /**
     * 处理UI事件
     */
    fun onEvent(event: NoteDetailEvent) {
        when (event) {
            is NoteDetailEvent.LoadNote -> {
                loadNote(event.noteId)
            }
            // 导航事件由UI层处理
            is NoteDetailEvent.NavigateBack,
            is NoteDetailEvent.NavigateToEdit -> {
                // 这些事件由UI层的导航控制器处理
            }
        }
    }

    /**
     * 加载笔记详情
     */
    private fun loadNote(noteId: String) {
        viewModelScope.launch {
            try {
                println("[DEBUG] Loading note with ID: '$noteId' (length: ${noteId.length})")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                // 先检查所有笔记的ID
                val allNotes = noteViewModel.notes.value
                println("[DEBUG] All available notes:")
                allNotes.forEach { note ->
                    println("[DEBUG] - Note ID: '${note.id}' (length: ${note.id.length}), Title: '${note.title}'")
                }
                
                val noteEntity = noteViewModel.getNoteById(noteId)
                println("[DEBUG] Retrieved noteEntity: $noteEntity")
                
                if (noteEntity != null) {
                    val note = noteEntity.toNote()
                    println("[DEBUG] Converted to Note: $note")
                    _uiState.value = _uiState.value.copy(
                        note = note,
                        isLoading = false,
                        error = null
                    )
                    println("[DEBUG] Updated UI state with note data")
                } else {
                    println("[DEBUG] Note not found for ID: $noteId")
                    _uiState.value = _uiState.value.copy(
                        note = null,
                        isLoading = false,
                        error = "笔记不存在 (ID: $noteId)"
                    )
                }
            } catch (e: Exception) {
                println("[DEBUG] Error loading note: ${e.message}")
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载笔记失败: ${e.message}"
                )
            }
        }
    }
}