package com.example.noteapp.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.model.domain.Note
import com.example.noteapp.model.domain.toNotes
import com.example.noteapp.model.ui.MainScreenEvent
import com.example.noteapp.model.ui.MainScreenState
import com.example.noteapp.repository.SearchType
import com.example.noteapp.viewmodel.NoteViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 主界面ViewModel
 * 管理主界面的状态和业务逻辑
 */
class MainScreenViewModel(
    private val noteViewModel: NoteViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenState())
    val uiState: StateFlow<MainScreenState> = _uiState.asStateFlow()

    init {
        // 监听笔记数据变化
        viewModelScope.launch {
            noteViewModel.notes.collect { noteEntities ->
                _uiState.value = _uiState.value.copy(
                    notes = noteEntities.toNotes()
                )
            }
        }

        // 监听搜索状态变化
        viewModelScope.launch {
            combine(
                noteViewModel.searchQuery,
                noteViewModel.searchType,
                noteViewModel.isSearching
            ) { query, type, searching ->
                _uiState.value = _uiState.value.copy(
                    searchQuery = query,
                    searchType = type,
                    isSearching = searching
                )
            }.collect {}
        }

        // 记账统计数据按需加载，不在初始化时监听以提升性能

        // 监听日期筛选状态
        viewModelScope.launch {
            combine(
                noteViewModel.dateFilterType,
                noteViewModel.customDateRange
            ) { filterType, dateRange ->
                _uiState.value = _uiState.value.copy(
                    dateFilterType = filterType,
                    customDateRange = dateRange
                )
            }.collect {}
        }
    }

    /**
     * 处理UI事件
     */
    fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.ToggleSearchBar -> {
                _uiState.value = _uiState.value.copy(
                    showSearchBar = !_uiState.value.showSearchBar
                )
            }
            is MainScreenEvent.ToggleFunctionMenu -> {
                _uiState.value = _uiState.value.copy(
                    showFunctionMenu = !_uiState.value.showFunctionMenu
                )
            }
            is MainScreenEvent.ToggleAccountingStats -> {
                val shouldShow = !_uiState.value.showAccountingStats
                _uiState.value = _uiState.value.copy(
                    showAccountingStats = shouldShow
                )
                // 只在需要显示时才加载统计数据
                if (shouldShow && _uiState.value.accountingStats == null) {
                    loadAccountingStats()
                }
            }
            is MainScreenEvent.ClearSearch -> {
                noteViewModel.clearSearch()
                _uiState.value = _uiState.value.copy(
                    showSearchBar = false
                )
            }
            is MainScreenEvent.UpdateSearchQuery -> {
                noteViewModel.updateSearchQuery(event.query)
            }
            is MainScreenEvent.UpdateSearchType -> {
                noteViewModel.updateSearchType(event.type)
            }
            is MainScreenEvent.DeleteNote -> {
                // 需要找到对应的NoteEntity来删除
                viewModelScope.launch {
                    val noteEntities = noteViewModel.notes.first()
                    val noteEntity = noteEntities.find { it.id == event.note.id }
                    noteEntity?.let {
                        noteViewModel.deleteNote(it)
                    }
                }
            }
            // 导航事件由UI层处理
            is MainScreenEvent.NavigateToNoteDetail,
            is MainScreenEvent.NavigateToScreen -> {
                // 这些事件由UI层的导航控制器处理
            }
        }
    }

    /**
     * 关闭功能菜单
     */
    fun closeFunctionMenu() {
        _uiState.value = _uiState.value.copy(showFunctionMenu = false)
    }

    /**
     * 按需加载记账统计数据
     */
    fun loadAccountingStats() {
        viewModelScope.launch {
            noteViewModel.getAccountingStatistics().collect { stats ->
                _uiState.value = _uiState.value.copy(
                    accountingStats = stats
                )
            }
        }
    }
}