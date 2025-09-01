package com.example.noteapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteapp.data.NoteEntity
import com.example.noteapp.ui.common.UIComponents
import com.example.noteapp.ui.common.DateFormatters
import com.example.noteapp.ui.common.Dimensions
import com.example.noteapp.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterScreen(
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    // 独立的筛选状态，不影响主界面
    var localDateFilterType by remember { mutableStateOf(DateFilterType.ALL) }
    var localCustomDateRange by remember { mutableStateOf<DateRange?>(null) }
    var expandedSections by remember { mutableStateOf(setOf<String>()) }
    
    // 获取所有笔记并在本地进行筛选
    val allNotes by viewModel.getAllNotes().collectAsState(initial = emptyList())
    val filteredNotes = remember(allNotes, localDateFilterType, localCustomDateRange) {
        applyLocalDateFilter(allNotes, localDateFilterType, localCustomDateRange)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "日期筛选",
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
                    IconButton(
                        onClick = {
                            localDateFilterType = DateFilterType.ALL
                            localCustomDateRange = null
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "清除筛选"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimensions.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingLarge)
        ) {
            // 当前筛选状态
            item {
                CurrentFilterStatus(
                    dateFilterType = localDateFilterType,
                    customDateRange = localCustomDateRange,
                    noteCount = filteredNotes.size
                )
            }
            
            // 筛选选项（合并所有选项）
            item {
                FilterOptionsSection(
                    expanded = expandedSections.contains("filter"),
                    onExpandToggle = {
                        expandedSections = if (expandedSections.contains("filter")) {
                            expandedSections - "filter"
                        } else {
                            expandedSections + "filter"
                        }
                    },
                    selectedType = localDateFilterType,
                    customDateRange = localCustomDateRange,
                    onTypeSelected = { localDateFilterType = it },
                    onCustomDateRangeSelected = { localCustomDateRange = it }
                )
            }
            
            // 筛选结果列表
            item {
                FilteredNotesList(
                    notes = filteredNotes,
                    onNoteClick = { noteId ->
                        // 这里可以添加跳转到笔记详情的逻辑
                    }
                )
            }
        }
    }
}

@Composable
private fun CurrentFilterStatus(
    dateFilterType: DateFilterType,
    customDateRange: DateRange?,
    noteCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall)
        ) {
            Text(
                text = "当前筛选状态",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            
            UIComponents.StatItem(
                label = "筛选类型",
                value = getFilterTypeDisplayName(dateFilterType, customDateRange)
            )
            UIComponents.StatItem(
                label = "结果数量",
                value = "${noteCount} 条笔记"
            )
            if (customDateRange != null) {
                UIComponents.StatItem(
                    label = "日期范围",
                    value = "${DateFormatters.dateOnlyFormatter.format(customDateRange.startDate)} 至 ${DateFormatters.dateOnlyFormatter.format(customDateRange.endDate)}"
                )
            }
        }
    }
}

@Composable
private fun FilterOptionsSection(
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    selectedType: DateFilterType,
    customDateRange: DateRange?,
    onTypeSelected: (DateFilterType) -> Unit,
    onCustomDateRangeSelected: (DateRange) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    var tempStartDate by remember { mutableStateOf<java.util.Date?>(null) }
    var tempEndDate by remember { mutableStateOf<java.util.Date?>(null) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.paddingLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "筛选选项",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onExpandToggle) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "收起" else "展开"
                    )
                }
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
                ) {
                    Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                    
                    val filterOptions = listOf(
                        DateFilterType.ALL to "全部",
                        DateFilterType.TODAY to "今天",
                        DateFilterType.YESTERDAY to "昨天",
                        DateFilterType.THIS_WEEK to "本周",
                        DateFilterType.THIS_MONTH to "本月",
                        DateFilterType.THIS_YEAR to "今年",
                        DateFilterType.CUSTOM to "自定义范围"
                    )
                    
                    filterOptions.forEach { (type, text) ->
                        DateFilterOption(
                            text = text,
                            selected = selectedType == type,
                            onClick = {
                                if (type == DateFilterType.CUSTOM) {
                                    onTypeSelected(type)
                                    showDatePicker = true
                                    isSelectingStartDate = true
                                } else {
                                    onTypeSelected(type)
                                }
                            }
                        )
                    }
                    
                    // 显示自定义日期范围
                    if (selectedType == DateFilterType.CUSTOM && customDateRange != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "已选择日期范围",
                                    fontSize = 14.sp,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = "${DateFormatters.dateOnlyFormatter.format(customDateRange.startDate)} 至 ${DateFormatters.dateOnlyFormatter.format(customDateRange.endDate)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 日期选择器对话框
    if (showDatePicker) {
        DatePickerDialog(
            title = if (isSelectingStartDate) "选择开始日期" else "选择结束日期",
            onDateSelected = { selectedDate ->
                if (isSelectingStartDate) {
                    tempStartDate = selectedDate
                    isSelectingStartDate = false
                } else {
                    tempEndDate = selectedDate
                    val startDate = tempStartDate ?: selectedDate
                    val endDate = selectedDate
                    
                    // 确保开始日期不晚于结束日期
                    val finalStartDate = if (startDate <= endDate) startDate else endDate
                    val finalEndDate = if (startDate <= endDate) endDate else startDate
                    
                    onCustomDateRangeSelected(DateRange(finalStartDate, finalEndDate))
                    showDatePicker = false
                    tempStartDate = null
                    tempEndDate = null
                }
            },
            onDismiss = {
                showDatePicker = false
                tempStartDate = null
                tempEndDate = null
                isSelectingStartDate = true
            }
        )
    }
}

@Composable
private fun FilteredNotesList(
    notes: List<NoteEntity>,
    onNoteClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall)
        ) {
            Text(
                text = "筛选结果 (${notes.size}条)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (notes.isEmpty()) {
                Text(
                    text = "没有找到符合条件的笔记",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(Dimensions.paddingLarge)
                )
            } else {
                // 显示笔记列表
                notes.forEach { note ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        onClick = { onNoteClick(note.id) }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 标签显示
                                if (note.tags.isNotEmpty()) {
                                    UIComponents.TagList(
                                        tags = note.tags,
                                        maxVisible = 2
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = DateFormatters.shortDateFormatter.format(note.creationTime),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 本地日期筛选函数
 */
private fun applyLocalDateFilter(
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

private fun getFilterTypeDisplayName(dateFilterType: DateFilterType, customDateRange: DateRange?): String {
    return when (dateFilterType) {
        DateFilterType.ALL -> "全部"
        DateFilterType.TODAY -> "今天"
        DateFilterType.YESTERDAY -> "昨天"
        DateFilterType.THIS_WEEK -> "本周"
        DateFilterType.THIS_MONTH -> "本月"
        DateFilterType.THIS_YEAR -> "今年"
        DateFilterType.CUSTOM -> if (customDateRange != null) "自定义范围" else "自定义范围（未设置）"
    }
}