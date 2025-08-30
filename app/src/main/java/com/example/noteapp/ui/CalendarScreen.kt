package com.example.noteapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteapp.data.NoteEntity
import com.example.noteapp.viewmodel.NoteViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: NoteViewModel,
    onBack: () -> Unit,
    onDateSelected: (Date, List<NoteEntity>) -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val calendarSelectedTag by viewModel.calendarSelectedTag.collectAsState()
    val allTags by viewModel.getAllTags().collectAsState()
    
    var showCalendarTagFilter by remember { mutableStateOf(false) }
    var showDayNotesDialog by remember { mutableStateOf(false) }
    var selectedDayNotes by remember { mutableStateOf<List<NoteEntity>>(emptyList()) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "日历视图",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    OutlinedButton(
                        onClick = { showCalendarTagFilter = true },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = calendarSelectedTag ?: "选择标签",
                            fontSize = 14.sp
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 日历组件
            CalendarViewComponent(
                notes = notes,
                selectedTag = calendarSelectedTag,
                onDateSelected = { date ->
                    val dayNotes = viewModel.getNotesForDate(date)
                    if (dayNotes.isNotEmpty()) {
                        selectedDate = date
                        selectedDayNotes = dayNotes
                        showDayNotesDialog = true
                    }
                }
            )
            
            // 使用说明
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "使用说明",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "• 点击有笔记的日期可查看当日笔记详情",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "• 使用右上角按钮筛选特定标签的笔记",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "• 圆点表示该日有笔记，不同颜色表示是否含筛选标签",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // 标签筛选对话框
        if (showCalendarTagFilter) {
            CalendarTagFilterDialog(
                allTags = allTags,
                selectedTag = calendarSelectedTag,
                onTagSelected = { tag ->
                    viewModel.updateCalendarSelectedTag(tag)
                    showCalendarTagFilter = false
                },
                onDismiss = { showCalendarTagFilter = false }
            )
        }
        
        // 当日笔记详情对话框
        if (showDayNotesDialog && selectedDate != null) {
            DayNotesDialog(
                date = selectedDate!!,
                notes = selectedDayNotes,
                onDismiss = { showDayNotesDialog = false },
                onNoteClick = { note ->
                    showDayNotesDialog = false
                    onDateSelected(selectedDate!!, selectedDayNotes)
                }
            )
        }
    }
}

@Composable
fun DayNotesDialog(
    date: Date,
    notes: List<NoteEntity>,
    onDismiss: () -> Unit,
    onNoteClick: (NoteEntity) -> Unit
) {
    val dateFormatter = remember { java.text.SimpleDateFormat("yyyy年MM月dd日", java.util.Locale.getDefault()) }
    val timeFormatter = remember { java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("${dateFormatter.format(date)} 的笔记")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                notes.forEach { note ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = { onNoteClick(note) }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = note.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                            
                            if (note.content.isNotBlank()) {
                                Text(
                                    text = note.content,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                            
                            // 标签
                            if (note.tags.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    note.tags.take(3).forEach { tag ->
                                        Text(
                                            text = tag,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (note.tags.size > 3) {
                                        Text(
                                            text = "+${note.tags.size - 3}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            
                            Text(
                                text = timeFormatter.format(note.creationTime),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}