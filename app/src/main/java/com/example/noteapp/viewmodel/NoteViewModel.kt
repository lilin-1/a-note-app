package com.example.noteapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.NoteEntity
import com.example.noteapp.repository.NoteRepository
import com.example.noteapp.repository.SearchType
import com.example.noteapp.service.AccountingService
import com.example.noteapp.utils.TagParser
import com.example.noteapp.ui.DateFilterType
import com.example.noteapp.ui.DateRange
import com.example.noteapp.ui.DateFilterUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    
    private val accountingService = AccountingService()
    
    // 搜索状态管理
    private val searchState = SearchState()
    val searchQuery: StateFlow<String> = searchState.query
    val searchType: StateFlow<SearchType> = searchState.type
    val isSearching: StateFlow<Boolean> = searchState.isSearching
    
    // 筛选状态管理
    private val filterState = FilterState()
    val dateFilterType: StateFlow<DateFilterType> = filterState.dateFilterType
    val customDateRange: StateFlow<DateRange?> = filterState.customDateRange
    val calendarSelectedTag: StateFlow<String?> = filterState.calendarSelectedTag
    
    /**
     * 搜索状态管理类
     */
    private class SearchState {
        private val _query = MutableStateFlow("")
        val query: StateFlow<String> = _query.asStateFlow()
        
        private val _type = MutableStateFlow(SearchType.ALL)
        val type: StateFlow<SearchType> = _type.asStateFlow()
        
        private val _isSearching = MutableStateFlow(false)
        val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
        
        fun updateQuery(newQuery: String) {
            _query.value = newQuery
            _isSearching.value = newQuery.isNotBlank()
        }
        
        fun updateType(newType: SearchType) {
            _type.value = newType
        }
        
        fun clear() {
            _query.value = ""
            _isSearching.value = false
            _type.value = SearchType.ALL
        }
    }
    
    /**
     * 筛选状态管理类
     */
    private class FilterState {
        private val _dateFilterType = MutableStateFlow(DateFilterType.ALL)
        val dateFilterType: StateFlow<DateFilterType> = _dateFilterType.asStateFlow()
        
        private val _customDateRange = MutableStateFlow<DateRange?>(null)
        val customDateRange: StateFlow<DateRange?> = _customDateRange.asStateFlow()
        
        private val _calendarSelectedTag = MutableStateFlow<String?>(null)
        val calendarSelectedTag: StateFlow<String?> = _calendarSelectedTag.asStateFlow()
        
        fun updateDateFilterType(type: DateFilterType) {
            _dateFilterType.value = type
        }
        
        fun updateCustomDateRange(range: DateRange) {
            _customDateRange.value = range
        }
        
        fun updateCalendarSelectedTag(tag: String?) {
            _calendarSelectedTag.value = tag
        }
        
        fun clearDateFilter() {
            _dateFilterType.value = DateFilterType.ALL
            _customDateRange.value = null
        }
    }
    
    val notes: StateFlow<List<NoteEntity>> = createNotesFlow()
    
    /**
     * 创建笔记流，整合搜索和筛选逻辑
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun createNotesFlow(): StateFlow<List<NoteEntity>> {
        return combine(
            searchQuery.debounce(300).distinctUntilChanged(),
            searchType,
            dateFilterType,
            customDateRange
        ) { query, type, dateFilter, customRange ->
            NotesQuery(query, type, dateFilter, customRange)
        }.flatMapLatest { query ->
            getFilteredNotes(query)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    
    /**
     * 笔记查询参数
     */
    private data class NotesQuery(
        val searchQuery: String,
        val searchType: SearchType,
        val dateFilterType: DateFilterType,
        val customDateRange: DateRange?
    )
    
    /**
     * 根据查询参数获取筛选后的笔记
     */
    private fun getFilteredNotes(query: NotesQuery): Flow<List<NoteEntity>> {
        val baseFlow = if (query.searchQuery.isBlank()) {
            repository.getAllNotes()
        } else {
            repository.searchNotes(query.searchQuery, query.searchType)
        }
        
        return baseFlow.map { noteList ->
            applyDateFilter(noteList, query.dateFilterType, query.customDateRange)
        }
    }
    
    /**
     * 应用日期筛选
     */
    private fun applyDateFilter(
        notes: List<NoteEntity>,
        dateFilterType: DateFilterType,
        customDateRange: DateRange?
    ): List<NoteEntity> {
        val dateRange = DateFilterUtils.getDateRange(dateFilterType, customDateRange)
        return if (dateRange != null) {
            notes.filter { note ->
                note.creationTime >= dateRange.startDate && note.creationTime <= dateRange.endDate
            }
        } else {
            notes
        }
    }
    
    // 搜索相关方法
    fun updateSearchQuery(query: String) = searchState.updateQuery(query)
    fun updateSearchType(type: SearchType) = searchState.updateType(type)
    fun clearSearch() = searchState.clear()
    
    fun createNote(
        title: String,
        content: String,
        tags: List<String> = emptyList(),
        images: List<com.example.noteapp.data.NoteImage> = emptyList()
    ) {
        viewModelScope.launch {
            repository.createNote(title, content, tags, images)
        }
    }
    
    fun updateNote(
        id: String,
        title: String,
        content: String,
        tags: List<String> = emptyList(),
        images: List<com.example.noteapp.data.NoteImage> = emptyList()
    ) {
        viewModelScope.launch {
            repository.updateNote(id, title, content, tags, images)
        }
    }
    
    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
    
    suspend fun getNoteById(id: String): NoteEntity? {
        return repository.getNoteById(id)
    }
    
    // 记账功能相关方法
    fun getAccountingStatistics(): StateFlow<AccountingService.AccountingStatistics> {
        return notes.map { noteList ->
            accountingService.calculateStatistics(noteList)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AccountingService.AccountingStatistics(
                totalIncome = java.math.BigDecimal.ZERO,
                totalExpense = java.math.BigDecimal.ZERO,
                balance = java.math.BigDecimal.ZERO,
                typeStatistics = emptyMap(),
                noteCount = 0
            )
        )
    }
    
    fun getAccountingNotes(): StateFlow<List<NoteEntity>> {
        return notes.map { noteList ->
            accountingService.getAccountingNotes(noteList)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    
    fun validateAccountingTag(tag: String): Pair<Boolean, String?> {
        return TagParser.validateAccountingTag(tag)
    }
    
    fun createAccountingTag(type: String, amount: java.math.BigDecimal): String {
        return TagParser.createAccountingTag(type, amount)
    }
    
    fun getCommonAccountingTypes(): List<String> {
        return TagParser.getCommonAccountingTypes()
    }
    
    // 筛选相关方法
    fun updateDateFilterType(type: DateFilterType) = filterState.updateDateFilterType(type)
    fun updateCustomDateRange(range: DateRange) = filterState.updateCustomDateRange(range)
    fun clearDateFilter() = filterState.clearDateFilter()
    fun updateCalendarSelectedTag(tag: String?) = filterState.updateCalendarSelectedTag(tag)
    
    fun getAllTags(): StateFlow<List<String>> {
        return notes.map { noteList ->
            noteList.flatMap { it.tags }.distinct().sorted()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    
    /**
     * 获取所有笔记（不受筛选影响）
     */
    fun getAllNotes(): Flow<List<NoteEntity>> {
        return repository.getAllNotes()
    }
    
    fun getNotesForDate(date: java.util.Date): List<NoteEntity> {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        val targetYear = calendar.get(java.util.Calendar.YEAR)
        val targetDayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        
        return notes.value.filter { note ->
            calendar.time = note.creationTime
            calendar.get(java.util.Calendar.YEAR) == targetYear &&
                    calendar.get(java.util.Calendar.DAY_OF_YEAR) == targetDayOfYear
        }
    }
}

class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}