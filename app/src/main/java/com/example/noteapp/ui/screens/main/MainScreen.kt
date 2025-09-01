package com.example.noteapp.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.noteapp.model.ui.MainScreenEvent
import com.example.noteapp.ui.AccountingStatsDialog
import com.example.noteapp.ui.SearchComponent
import com.example.noteapp.ui.components.common.MainTopBar
import com.example.noteapp.ui.components.note.NoteItem

/**
 * 主界面Screen
 * 遵循MVVM架构，UI与业务逻辑完全分离
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainScreenViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            MainTopBar(
                showFunctionMenu = uiState.showFunctionMenu,
                onSearchClick = { viewModel.onEvent(MainScreenEvent.ToggleSearchBar) },
                onMenuClick = { viewModel.onEvent(MainScreenEvent.ToggleFunctionMenu) },
                onMenuDismiss = { viewModel.closeFunctionMenu() },
                onNavigateToCalendar = { navController.navigate("calendar") },
                onNavigateToStats = { navController.navigate("accounting_stats") },
                onNavigateToFilter = { navController.navigate("date_filter") },
                onNavigateToBackup = { navController.navigate("backup") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_note") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "新建笔记"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            // 搜索组件
            if (uiState.showSearchBar) {
                SearchComponent(
                    searchQuery = uiState.searchQuery,
                    searchType = uiState.searchType,
                    isSearching = uiState.isSearching,
                    onSearchQueryChange = { query ->
                        viewModel.onEvent(MainScreenEvent.UpdateSearchQuery(query))
                    },
                    onSearchTypeChange = { type ->
                        viewModel.onEvent(MainScreenEvent.UpdateSearchType(type))
                    },
                    onClearSearch = {
                        viewModel.onEvent(MainScreenEvent.ClearSearch)
                    }
                )
            }

            // 笔记列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.notes) { note ->
                    NoteItem(
                        note = note,
                        onClick = {
                            navController.navigate("note_detail/${note.id}")
                        },
                        onDelete = {
                            viewModel.onEvent(MainScreenEvent.DeleteNote(note))
                        }
                    )
                }
            }
        }
        
        // 记账统计对话框
        uiState.accountingStats?.let { stats ->
            if (uiState.showAccountingStats) {
                AccountingStatsDialog(
                    statistics = stats,
                    onDismiss = { viewModel.onEvent(MainScreenEvent.ToggleAccountingStats) }
                )
            }
        }
    }
}