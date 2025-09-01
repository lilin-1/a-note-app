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
import com.example.noteapp.ui.NoteDetailScreen
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteapp.ui.theme.NoteappTheme
import com.example.noteapp.ui.common.UIComponents
import com.example.noteapp.ui.common.DateFormatters
import com.example.noteapp.ui.common.Dimensions
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
        images = this.images,
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
    val images: List<com.example.noteapp.data.NoteImage> = emptyList(),
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
                            onSave = { title, content, tags, images ->
                                viewModel.createNote(title, content, tags, images)
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
                        var note by remember { mutableStateOf<NoteEntity?>(null) }
                        
                        LaunchedEffect(noteId) {
                            note = viewModel.getNoteById(noteId)
                        }
                        
                        note?.let { noteEntity ->
                            NoteDetailScreen(
                                note = noteEntity,
                                onBack = { navController.popBackStack() },
                                onEdit = { navController.navigate("edit_note/${noteEntity.id}") }
                            )
                        }
                    }
                    composable("backup") {
                        BackupScreen(
                            onBack = { navController.popBackStack() }
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
                            onBack = { navController.popBackStack() }
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
    var showFunctionMenu by remember { mutableStateOf(false) }
    
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
                    // 搜索按钮（保持独立）
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索"
                        )
                    }
                    
                    // 功能菜单按钮
                    Box {
                        IconButton(onClick = { showFunctionMenu = !showFunctionMenu }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "功能菜单"
                            )
                        }
                        
                        // 功能下拉菜单
                        DropdownMenu(
                            expanded = showFunctionMenu,
                            onDismissRequest = { showFunctionMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("日历视图") },
                                onClick = {
                                    showFunctionMenu = false
                                    navController.navigate("calendar")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "日历视图"
                                    )
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("记账统计") },
                                onClick = {
                                    showFunctionMenu = false
                                    navController.navigate("accounting_stats")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = "记账统计"
                                    )
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("日期筛选") },
                                onClick = {
                                    showFunctionMenu = false
                                    navController.navigate("date_filter")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "日期筛选"
                                    )
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { Text("数据备份") },
                                onClick = {
                                    showFunctionMenu = false
                                    navController.navigate("backup")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Backup,
                                        contentDescription = "数据备份"
                                    )
                                }
                            )
                        }
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
            
            // 日期筛选已移至独立界面
            

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
                            navController.navigate("note_detail/${note.id}")
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
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.paddingSmall)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(Dimensions.cornerRadiusMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            NoteContent(
                note = note,
                modifier = Modifier.weight(1f)
            )
            
            NoteActions(
                note = note,
                showMenu = showMenu,
                onMenuToggle = { showMenu = !showMenu },
                onDelete = {
                    showMenu = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
private fun NoteContent(
    note: Note,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 标题
        Text(
            text = note.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
        
        // 内容
        Text(
            text = note.content,
            fontSize = 14.sp,
            color = Color.Gray,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
        
        // 标签
        UIComponents.TagList(
            tags = note.tags,
            maxVisible = 2
        )
        
        Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
        
        // 时间信息
        NoteTimeInfo(note = note)
    }
}

@Composable
private fun NoteTimeInfo(note: Note) {
    Column {
        Text(
            text = "创建: ${DateFormatters.fullDateFormatter.format(note.creationTime)}",
            fontSize = 11.sp,
            color = Color.Gray
        )
        if (note.creationTime != note.lastEditTime) {
            Text(
                text = "修改: ${DateFormatters.fullDateFormatter.format(note.lastEditTime)}",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun NoteActions(
    note: Note,
    showMenu: Boolean,
    onMenuToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
    ) {
        // 管理按钮
        Box {
            UIComponents.IconButtonWithDescription(
                icon = Icons.Default.MoreVert,
                contentDescription = "管理笔记",
                onClick = onMenuToggle,
                modifier = Modifier.size(Dimensions.iconSizeLarge),
                size = Dimensions.iconSizeMedium
            )
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = onMenuToggle
            ) {
                DropdownMenuItem(
                    text = { Text("删除笔记") },
                    onClick = onDelete,
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
            NoteImageIndicator()
        }
    }
}

@Composable
private fun NoteImageIndicator() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(Dimensions.cornerRadiusSmall)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Photo,
            contentDescription = "包含图片",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Dimensions.iconSizeMedium)
        )
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