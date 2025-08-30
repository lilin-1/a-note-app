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
import com.example.noteapp.ui.SearchComponent
import com.example.noteapp.ui.AccountingStatsDialog
import com.example.noteapp.ui.DateFilterComponent
import com.example.noteapp.ui.CalendarScreen
import com.example.noteapp.ui.DateFilterType
import com.example.noteapp.repository.SearchType
import com.example.noteapp.viewmodel.NoteViewModel
import com.example.noteapp.viewmodel.NoteViewModelFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteapp.ui.theme.NoteappTheme
import java.text.SimpleDateFormat
import java.util.*

// 将Note数据类转换为NoteEntity的扩展函数
fun NoteEntity.toNote(): Note {
    return Note(
        id = this.id,
        title = this.title,
        content = this.content,
        creationTime = this.creationTime,
        lastEditTime = this.lastEditTime,
        tags = this.tags,
        hasImages = this.hasImages
    )
}

// 保留原有的Note数据类用于UI显示
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val creationTime: Date,
    val lastEditTime: Date,
    val tags: List<String>,
    val hasImages: Boolean = false
)

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
                        MainScreen(
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                    composable("add_note") {
                        NoteEditScreen(
                            onSave = { title, content, tags ->
                                viewModel.createNote(title, content, tags)
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("edit_note/{noteId}") { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
                        var note by remember { mutableStateOf<NoteEntity?>(null) }
                        
                        LaunchedEffect(noteId) {
                            note = viewModel.getNoteById(noteId)
                        }
                        
                        note?.let { noteEntity ->
                            NoteEditScreen(
                                note = noteEntity,
                                onSave = { title, content, tags ->
                                    viewModel.updateNote(noteId, title, content, tags)
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
                                // TODO: 可以跳转到笔记详情或其他操作
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: NoteViewModel
) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchType by viewModel.searchType.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val accountingStats by viewModel.getAccountingStatistics().collectAsState()
    val dateFilterType by viewModel.dateFilterType.collectAsState()
    val customDateRange by viewModel.customDateRange.collectAsState()
    var showSearchBar by remember { mutableStateOf(false) }
    var showAccountingStats by remember { mutableStateOf(false) }
    var showDateFilter by remember { mutableStateOf(false) }
    
    val displayNotes = notes.map { it.toNote() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "我的笔记",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("calendar") }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "日历视图"
                        )
                    }
                    IconButton(onClick = { showDateFilter = !showDateFilter }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "日期筛选"
                        )
                    }
                    IconButton(onClick = { showAccountingStats = true }) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "记账统计"
                        )
                    }
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索"
                        )
                    }
                }
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
        ) {
            // 搜索组件
            if (showSearchBar) {
                SearchComponent(
                    searchQuery = searchQuery,
                    searchType = searchType,
                    isSearching = isSearching,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onSearchTypeChange = viewModel::updateSearchType,
                    onClearSearch = {
                        viewModel.clearSearch()
                        showSearchBar = false
                    }
                )
            }
            
            // 日期筛选组件
            if (showDateFilter) {
                DateFilterComponent(
                    selectedType = dateFilterType,
                    customDateRange = customDateRange,
                    onTypeSelected = viewModel::updateDateFilterType,
                    onCustomDateRangeSelected = viewModel::updateCustomDateRange,
                    modifier = Modifier.padding(16.dp)
                )
            }
            

            // 笔记列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayNotes) { note ->
                    NoteItem(
                        note = note,
                        onClick = {
                            navController.navigate("edit_note/${note.id}")
                        },
                        onDelete = {
                            viewModel.deleteNote(notes.find { it.id == note.id }!!)
                        }
                    )
                }
            }
        }
        
        // 记账统计对话框
        if (showAccountingStats) {
            AccountingStatsDialog(
                statistics = accountingStats,
                onDismiss = { showAccountingStats = false }
            )
        }
        

    }
}

@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val dateFormatter = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    val creationFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = note.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = note.content,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 标签显示
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    note.tags.take(2).forEach { tag ->
                        Text(
                            text = tag,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (note.tags.size > 2) {
                        Text(
                            text = "+${note.tags.size - 2}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 时间信息
                Column {
                    Text(
                        text = "创建: ${creationFormatter.format(note.creationTime)}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    if (note.creationTime != note.lastEditTime) {
                        Text(
                            text = "修改: ${creationFormatter.format(note.lastEditTime)}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            // 右侧操作区域
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 管理按钮
                Box {
                    IconButton(
                        onClick = { showMenu = !showMenu },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "管理笔记",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // 下拉菜单
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("删除笔记") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "删除"
                                )
                            }
                        )
                    }
                }
                
                // 图片标识
                if (note.hasImages) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Photo,
                            contentDescription = "包含图片",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    // Preview不支持导航和数据库，这里只显示UI结构
    NoteappTheme {
        // 预览版本的简化UI
        Text("笔记应用预览 - 请运行应用查看完整功能")
    }
}