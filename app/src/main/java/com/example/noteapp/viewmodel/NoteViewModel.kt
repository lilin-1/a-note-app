package com.example.noteapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.NoteEntity
import com.example.noteapp.repository.NoteRepository
import com.example.noteapp.repository.SearchType
import com.example.noteapp.service.AccountingService
import com.example.noteapp.utils.TagParser
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
    
    val notes: StateFlow<List<NoteEntity>> = combine(
        searchQuery.debounce(300).distinctUntilChanged(),
        searchType
    ) { query, type ->
        Pair(query, type)
    }.flatMapLatest { (query, type) ->
        if (query.isBlank()) {
            repository.getAllNotes()
        } else {
            repository.searchNotes(query, type)
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
        hasImages: Boolean = false
    ) {
        viewModelScope.launch {
            repository.createNote(title, content, tags, hasImages)
        }
    }
    
    fun updateNote(
        id: String,
        title: String,
        content: String,
        tags: List<String> = emptyList(),
        hasImages: Boolean = false
    ) {
        viewModelScope.launch {
            repository.updateNoteContent(id, title, content, tags, hasImages)
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