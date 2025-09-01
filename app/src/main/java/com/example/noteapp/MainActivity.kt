package com.example.noteapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.noteapp.data.NoteDatabase
import com.example.noteapp.data.NoteEntity
import com.example.noteapp.repository.NoteRepository
import com.example.noteapp.ui.NoteEditScreen
import com.example.noteapp.ui.BackupScreen
import com.example.noteapp.ui.AccountingStatsScreen
import com.example.noteapp.ui.SearchComponent
import com.example.noteapp.ui.AccountingStatsDialog
import com.example.noteapp.ui.DateFilterComponent
import com.example.noteapp.ui.CalendarScreen
import com.example.noteapp.ui.DateFilterScreen
import com.example.noteapp.ui.DateFilterType
import com.example.noteapp.repository.SearchType
import com.example.noteapp.viewmodel.NoteViewModel
import com.example.noteapp.viewmodel.NoteViewModelFactory
import com.example.noteapp.viewmodel.BackupViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteapp.ui.theme.NoteappTheme
import com.example.noteapp.ui.screens.main.MainScreen
import com.example.noteapp.ui.screens.main.MainScreenViewModel

// Note数据类和扩展函数已移至model.domain包

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoteappTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val database = NoteDatabase.getDatabase(context)
                val repository = NoteRepository(database.noteDao())
                val viewModel: NoteViewModel = viewModel(
                    factory = NoteViewModelFactory(repository)
                )
                
                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        val mainScreenViewModel = viewModel<MainScreenViewModel> {
                            MainScreenViewModel(viewModel)
                        }
                        MainScreen(
                            navController = navController,
                            viewModel = mainScreenViewModel
                        )
                    }
                    composable("add_note") {
                        NoteEditScreen(
                            onSave = { title, content, tags, images ->
                                viewModel.createNote(title, content, tags, images)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("edit_note/{noteId}") { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
                        // 从当前笔记列表中直接查找，避免异步加载延迟
                        val notes by viewModel.notes.collectAsState()
                        val note = remember(noteId, notes) {
                            notes.find { it.id == noteId }
                        }
                        
                        note?.let { noteEntity ->
                            NoteEditScreen(
                                note = noteEntity,
                                onSave = { title, content, tags, images ->
                                    viewModel.updateNote(noteId, title, content, tags, images)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    composable("calendar") {
                        CalendarScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onDateSelected = { date, notes ->
                                // 暂时不处理，由CalendarScreen内部处理
                            },
                            onNoteClick = { noteId ->
                                navController.navigate("note_detail/$noteId")
                            }
                        )
                    }
                    composable("note_detail/{noteId}") { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
                        val noteDetailViewModel = viewModel<com.example.noteapp.ui.screens.note.NoteDetailViewModel> {
                            com.example.noteapp.ui.screens.note.NoteDetailViewModel(viewModel)
                        }
                        com.example.noteapp.ui.screens.note.NoteDetailScreen(
                            noteId = noteId,
                            viewModel = noteDetailViewModel,
                            onBack = { navController.popBackStack() },
                            onEdit = { navController.navigate("edit_note/$noteId") }
                        )
                    }
                    composable("backup") {
                        val backupViewModel = viewModel<BackupViewModel>()
                        BackupScreen(
                            onBack = { navController.popBackStack() },
                            viewModel = backupViewModel
                        )
                    }
                    composable("accounting_stats") {
                        val notes by viewModel.notes.collectAsState()
                        AccountingStatsScreen(
                            notes = notes,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("date_filter") {
                        DateFilterScreen(
                            viewModel = viewModel,
                            navController = navController,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

// MainScreen已移至ui.screens.main包

// 所有UI组件已移至对应的组件包中