package com.example.noteapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteapp.data.NoteEntity
import com.example.noteapp.viewmodel.NoteViewModel
import com.example.noteapp.ui.DateRange
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: NoteViewModel,
    onBack: () -> Unit,
    onDateSelected: (Date, List<NoteEntity>) -> Unit,
    onNoteClick: (String) -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val calendarSelectedTag by viewModel.calendarSelectedTag.collectAsState()
    val allTags by viewModel.getAllTags().collectAsState()
    
    var showCalendarTagFilter by remember { mutableStateOf(false) }
    var showDayNotesDialog by remember { mutableStateOf(false) }
    var selectedDayNotes by remember { mutableStateOf<List<NoteEntity>>(emptyList()) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var showTagStatistics by remember { mutableStateOf(false) }
    
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 统计按钮
                    if (calendarSelectedTag != null) {
                        IconButton(
                            onClick = { showTagStatistics = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "标签统计"
                            )
                        }
                    }
                    
                    // 标签筛选按钮
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
                    onNoteClick(note.id)
                }
            )
        }
        
        // 标签统计对话框
        if (showTagStatistics && calendarSelectedTag != null) {
            TagStatisticsDialog(
                tag = calendarSelectedTag!!,
                notes = notes,
                onDismiss = { showTagStatistics = false }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagStatisticsDialog(
    tag: String,
    notes: List<NoteEntity>,
    onDismiss: () -> Unit
) {
    var selectedDateRange by remember { mutableStateOf<DateRange?>(null) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    
    // 筛选包含指定标签的笔记
    val taggedNotes = remember(notes, tag, selectedDateRange) {
        val filteredByTag = notes.filter { note -> note.tags.contains(tag) }
        
        if (selectedDateRange != null) {
            filteredByTag.filter { note ->
                note.creationTime >= selectedDateRange!!.startDate &&
                note.creationTime <= selectedDateRange!!.endDate
            }
        } else {
            filteredByTag
        }
    }
    
    // 按日期分组统计
    val dailyStats = remember(taggedNotes) {
        taggedNotes.groupBy { note ->
            val calendar = Calendar.getInstance().apply { time = note.creationTime }
            Triple(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }.mapKeys { (dateTriple, _) ->
            Calendar.getInstance().apply {
                set(dateTriple.first, dateTriple.second, dateTriple.third)
            }.time
        }.toSortedMap(compareByDescending { it })
    }
    
    val dateFormatter = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()) }
    val rangeFormatter = remember { SimpleDateFormat("MM-dd", Locale.getDefault()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("标签统计：$tag")
                if (selectedDateRange != null) {
                    Text(
                        text = "${rangeFormatter.format(selectedDateRange!!.startDate)} 至 ${rangeFormatter.format(selectedDateRange!!.endDate)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 日期范围选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "统计范围：${selectedDateRange?.let { "${rangeFormatter.format(it.startDate)} 至 ${rangeFormatter.format(it.endDate)}" } ?: "全部时间"}",
                        fontSize = 14.sp
                    )
                    
                    Row {
                        TextButton(
                            onClick = { showDateRangePicker = true }
                        ) {
                            Text("选择日期")
                        }
                        
                        if (selectedDateRange != null) {
                            TextButton(
                                onClick = { selectedDateRange = null }
                            ) {
                                Text("清除")
                            }
                        }
                    }
                }
                
                HorizontalDivider()
                
                // 统计概览
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "统计概览",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "总笔记数：${taggedNotes.size} 条",
                            fontSize = 12.sp
                        )
                        Text(
                            text = "记录天数：${dailyStats.size} 天",
                            fontSize = 12.sp
                        )
                        if (dailyStats.isNotEmpty()) {
                            val avgPerDay = taggedNotes.size.toFloat() / dailyStats.size
                            Text(
                                text = "平均每天：%.1f 条".format(avgPerDay),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                // 每日统计列表
                if (dailyStats.isNotEmpty()) {
                    Text(
                        text = "每日统计",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(dailyStats.toList()) { (date, dayNotes) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = dateFormatter.format(date),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${dayNotes.size} 条",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
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
    
    // 日期范围选择器
    if (showDateRangePicker) {
        DateRangePickerDialog(
            onDateRangeSelected = { startDate, endDate ->
                selectedDateRange = DateRange(startDate, endDate)
                showDateRangePicker = false
            },
            onDismiss = { showDateRangePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDateRangeSelected: (Date, Date) -> Unit,
    onDismiss: () -> Unit
) {
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    
    val datePickerState = rememberDatePickerState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isSelectingStartDate) "选择开始日期" else "选择结束日期")
        },
        text = {
            Column {
                if (startDate != null || endDate != null) {
                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    Text(
                        text = "开始：${startDate?.let { formatter.format(it) } ?: "未选择"}\n" +
                                "结束：${endDate?.let { formatter.format(it) } ?: "未选择"}",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                DatePicker(state = datePickerState)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Date(millis)
                        if (isSelectingStartDate) {
                            startDate = selectedDate
                            isSelectingStartDate = false
                        } else {
                            endDate = selectedDate
                            val finalStartDate = startDate ?: selectedDate
                            val finalEndDate = selectedDate
                            
                            // 确保开始日期不晚于结束日期
                            if (finalStartDate <= finalEndDate) {
                                onDateRangeSelected(finalStartDate, finalEndDate)
                            } else {
                                onDateRangeSelected(finalEndDate, finalStartDate)
                            }
                        }
                    }
                },
                enabled = datePickerState.selectedDateMillis != null
            ) {
                Text(if (isSelectingStartDate) "下一步" else "确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}