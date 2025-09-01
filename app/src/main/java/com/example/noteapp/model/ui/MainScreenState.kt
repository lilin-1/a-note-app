package com.example.noteapp.model.ui

import com.example.noteapp.model.domain.Note
import com.example.noteapp.repository.SearchType
import com.example.noteapp.service.AccountingService
import com.example.noteapp.ui.DateFilterType
import com.example.noteapp.ui.DateRange

/**
 * 主界面UI状态
 */
data class MainScreenState(
    val notes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val searchType: SearchType = SearchType.TITLE,
    val isSearching: Boolean = false,
    val showSearchBar: Boolean = false,
    val showFunctionMenu: Boolean = false,
    val showAccountingStats: Boolean = false,
    val accountingStats: AccountingService.AccountingStatistics? = null,
    val dateFilterType: DateFilterType = DateFilterType.ALL,
    val customDateRange: DateRange? = null
)

/**
 * 主界面UI事件
 */
sealed class MainScreenEvent {
    object ToggleSearchBar : MainScreenEvent()
    object ToggleFunctionMenu : MainScreenEvent()
    object ToggleAccountingStats : MainScreenEvent()
    object ClearSearch : MainScreenEvent()
    data class UpdateSearchQuery(val query: String) : MainScreenEvent()
    data class UpdateSearchType(val type: SearchType) : MainScreenEvent()
    data class NavigateToNoteDetail(val noteId: String) : MainScreenEvent()
    data class DeleteNote(val note: Note) : MainScreenEvent()
    data class NavigateToScreen(val route: String) : MainScreenEvent()
}