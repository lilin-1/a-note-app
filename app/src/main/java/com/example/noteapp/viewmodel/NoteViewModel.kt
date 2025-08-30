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

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    
    private val accountingService = AccountingService()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _searchType = MutableStateFlow(SearchType.ALL)
    val searchType: StateFlow<SearchType> = _searchType.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    // 日期筛选相关状态
    private val _dateFilterType = MutableStateFlow(DateFilterType.ALL)
    val dateFilterType: StateFlow<DateFilterType> = _dateFilterType.asStateFlow()
    
    private val _customDateRange = MutableStateFlow<DateRange?>(null)
    val customDateRange: StateFlow<DateRange?> = _customDateRange.asStateFlow()
    
    // 日历标签筛选
    private val _calendarSelectedTag = MutableStateFlow<String?>(null)
    val calendarSelectedTag: StateFlow<String?> = _calendarSelectedTag.asStateFlow()
    
    val notes: StateFlow<List<NoteEntity>> = combine(
        searchQuery.debounce(300).distinctUntilChanged(),
        searchType,
        dateFilterType,
        customDateRange
    ) { query, type, dateFilter, customRange ->
        Triple(query, type, Pair(dateFilter, customRange))
    }.flatMapLatest { (query, type, dateFilterData) ->
        val (dateFilter, customRange) = dateFilterData
        val baseFlow = if (query.isBlank()) {
            repository.getAllNotes()
        } else {
            repository.searchNotes(query, type)
        }
        
        baseFlow.map { noteList ->
            // 应用日期筛选
            val dateRange = DateFilterUtils.getDateRange(dateFilter, customRange)
            if (dateRange != null) {
                noteList.filter { note ->
                    note.creationTime >= dateRange.startDate && note.creationTime <= dateRange.endDate
                }
            } else {
                noteList
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _isSearching.value = query.isNotBlank()
    }
    
    fun updateSearchType(type: SearchType) {
        _searchType.value = type
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
        _isSearching.value = false
        _searchType.value = SearchType.ALL
    }
    
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
    
    // 日期筛选相关方法
    fun updateDateFilterType(type: DateFilterType) {
        _dateFilterType.value = type
    }
    
    fun updateCustomDateRange(range: DateRange) {
        _customDateRange.value = range
    }
    
    fun clearDateFilter() {
        _dateFilterType.value = DateFilterType.ALL
        _customDateRange.value = null
    }
    
    // 日历相关方法
    fun updateCalendarSelectedTag(tag: String?) {
        _calendarSelectedTag.value = tag
    }
    
    fun getAllTags(): StateFlow<List<String>> {
        return notes.map { noteList ->
            noteList.flatMap { it.tags }.distinct().sorted()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
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