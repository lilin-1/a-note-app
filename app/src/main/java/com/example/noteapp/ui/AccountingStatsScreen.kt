package com.example.noteapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteapp.data.NoteEntity
import com.example.noteapp.service.AccountingService
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingStatsScreen(
    notes: List<NoteEntity>,
    onBack: () -> Unit
) {
    val accountingService = remember { AccountingService() }
    
    // 日期筛选状态
    var selectedDateFilter by remember { mutableStateOf(DateFilterType.ALL) }
    var customStartDate by remember { mutableStateOf<Date?>(null) }
    var customEndDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    
    // 筛选后的笔记和统计数据
    val filteredNotes = remember(notes, selectedDateFilter, customStartDate, customEndDate) {
        when (selectedDateFilter) {
            DateFilterType.ALL -> accountingService.getAccountingNotes(notes)
            DateFilterType.TODAY -> {
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                val tomorrow = Calendar.getInstance().apply {
                    time = today
                    add(Calendar.DAY_OF_MONTH, 1)
                }.time
                accountingService.filterByDateRange(notes, today, tomorrow)
            }
            DateFilterType.YESTERDAY -> {
                val yesterday = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                accountingService.filterByDateRange(notes, yesterday, today)
            }
            DateFilterType.THIS_WEEK -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val weekStart = calendar.time
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                val weekEnd = calendar.time
                accountingService.filterByDateRange(notes, weekStart, weekEnd)
            }
            DateFilterType.THIS_MONTH -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val monthStart = calendar.time
                calendar.add(Calendar.MONTH, 1)
                val monthEnd = calendar.time
                accountingService.filterByDateRange(notes, monthStart, monthEnd)
            }
            DateFilterType.THIS_YEAR -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val yearStart = calendar.time
                calendar.add(Calendar.YEAR, 1)
                val yearEnd = calendar.time
                accountingService.filterByDateRange(notes, yearStart, yearEnd)
            }
            DateFilterType.CUSTOM -> {
                if (customStartDate != null && customEndDate != null) {
                    accountingService.filterByDateRange(notes, customStartDate!!, customEndDate!!)
                } else {
                    accountingService.getAccountingNotes(notes)
                }
            }
        }
    }
    
    val statistics = remember(filteredNotes) {
        accountingService.calculateStatistics(filteredNotes)
    }
    
    val typeGroups = remember(filteredNotes) {
        accountingService.groupByType(filteredNotes)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "记账统计",
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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 日期筛选器
            DateFilterCard(
                selectedFilter = selectedDateFilter,
                onFilterChange = { selectedDateFilter = it },
                customStartDate = customStartDate,
                customEndDate = customEndDate,
                onShowDatePicker = { isStart ->
                    isSelectingStartDate = isStart
                    showDatePicker = true
                }
            )
            
            // 统计概览
            AccountingStatsComponent(
                statistics = statistics,
                modifier = Modifier.fillMaxWidth()
            )
            
            // 按类型详细统计
            if (typeGroups.isNotEmpty()) {
                TypeDetailCard(
                    typeGroups = typeGroups,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // 时间范围说明
            TimeRangeInfo(
                selectedFilter = selectedDateFilter,
                customStartDate = customStartDate,
                customEndDate = customEndDate,
                noteCount = filteredNotes.size
            )
        }
    }
    
    // 日期选择器
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                if (isSelectingStartDate) {
                    customStartDate = date
                    selectedDateFilter = DateFilterType.CUSTOM
                } else {
                    customEndDate = date
                    selectedDateFilter = DateFilterType.CUSTOM
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun DateFilterCard(
    selectedFilter: DateFilterType,
    onFilterChange: (DateFilterType) -> Unit,
    customStartDate: Date?,
    customEndDate: Date?,
    onShowDatePicker: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "筛选",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "时间筛选",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // 预设时间选项
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    DateFilterType.ALL to "全部",
                    DateFilterType.TODAY to "今天",
                    DateFilterType.THIS_WEEK to "本周",
                    DateFilterType.THIS_MONTH to "本月"
                ).forEach { (filter, label) ->
                    FilterChip(
                        onClick = { onFilterChange(filter) },
                        label = { Text(label, fontSize = 12.sp) },
                        selected = selectedFilter == filter,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { onFilterChange(DateFilterType.THIS_YEAR) },
                    label = { Text("今年", fontSize = 12.sp) },
                    selected = selectedFilter == DateFilterType.THIS_YEAR,
                    modifier = Modifier.weight(1f)
                )
                
                FilterChip(
                    onClick = { onFilterChange(DateFilterType.CUSTOM) },
                    label = { Text("自定义", fontSize = 12.sp) },
                    selected = selectedFilter == DateFilterType.CUSTOM,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 自定义日期选择
            if (selectedFilter == DateFilterType.CUSTOM) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onShowDatePicker(true) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "选择开始日期"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = customStartDate?.let {
                                    SimpleDateFormat("MM-dd", Locale.getDefault()).format(it)
                                } ?: "开始日期",
                                fontSize = 12.sp
                            )
                        }
                        
                        OutlinedButton(
                            onClick = { onShowDatePicker(false) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "选择结束日期"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = customEndDate?.let {
                                    SimpleDateFormat("MM-dd", Locale.getDefault()).format(it)
                                } ?: "结束日期",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypeDetailCard(
    typeGroups: List<AccountingService.AccountingByType>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "类型详情",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            typeGroups.forEach { typeGroup ->
                TypeDetailItem(
                    typeGroup = typeGroup
                )
            }
        }
    }
}

@Composable
fun TypeDetailItem(
    typeGroup: AccountingService.AccountingByType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = typeGroup.type,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "¥${typeGroup.amount.stripTrailingZeros().toPlainString()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = "${typeGroup.notes.size}条记录",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TimeRangeInfo(
    selectedFilter: DateFilterType,
    customStartDate: Date?,
    customEndDate: Date?,
    noteCount: Int,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    
    val timeRangeText = when (selectedFilter) {
        DateFilterType.ALL -> "全部时间"
        DateFilterType.TODAY -> "今天"
        DateFilterType.YESTERDAY -> "昨天"
        DateFilterType.THIS_WEEK -> "本周"
        DateFilterType.THIS_MONTH -> "本月"
        DateFilterType.THIS_YEAR -> "今年"
        DateFilterType.CUSTOM -> {
            if (customStartDate != null && customEndDate != null) {
                "${dateFormat.format(customStartDate)} 至 ${dateFormat.format(customEndDate)}"
            } else {
                "请选择日期范围"
            }
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "统计范围：$timeRangeText",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "包含 $noteCount 条记账记录",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(Date(millis))
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}