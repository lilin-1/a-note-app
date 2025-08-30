package com.example.noteapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

enum class DateFilterType {
    ALL,        // 全部
    TODAY,      // 今天
    YESTERDAY,  // 昨天
    THIS_WEEK,  // 本周
    THIS_MONTH, // 本月
    THIS_YEAR,  // 今年
    CUSTOM      // 自定义范围
}

data class DateRange(
    val startDate: Date,
    val endDate: Date
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterComponent(
    selectedType: DateFilterType,
    customDateRange: DateRange?,
    onTypeSelected: (DateFilterType) -> Unit,
    onCustomDateRangeSelected: (DateRange) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    var tempStartDate by remember { mutableStateOf<Date?>(null) }
    var tempEndDate by remember { mutableStateOf<Date?>(null) }
    
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "日期筛选",
            fontSize = 16.sp,
            style = MaterialTheme.typography.titleMedium
        )
        
        // 预设日期选项
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DateFilterOption(
                text = "全部",
                selected = selectedType == DateFilterType.ALL,
                onClick = { onTypeSelected(DateFilterType.ALL) }
            )
            
            DateFilterOption(
                text = "今天",
                selected = selectedType == DateFilterType.TODAY,
                onClick = { onTypeSelected(DateFilterType.TODAY) }
            )
            
            DateFilterOption(
                text = "昨天",
                selected = selectedType == DateFilterType.YESTERDAY,
                onClick = { onTypeSelected(DateFilterType.YESTERDAY) }
            )
            
            DateFilterOption(
                text = "本周",
                selected = selectedType == DateFilterType.THIS_WEEK,
                onClick = { onTypeSelected(DateFilterType.THIS_WEEK) }
            )
            
            DateFilterOption(
                text = "本月",
                selected = selectedType == DateFilterType.THIS_MONTH,
                onClick = { onTypeSelected(DateFilterType.THIS_MONTH) }
            )
            
            DateFilterOption(
                text = "今年",
                selected = selectedType == DateFilterType.THIS_YEAR,
                onClick = { onTypeSelected(DateFilterType.THIS_YEAR) }
            )
            
            DateFilterOption(
                text = "自定义范围",
                selected = selectedType == DateFilterType.CUSTOM,
                onClick = { 
                    onTypeSelected(DateFilterType.CUSTOM)
                    showDatePicker = true
                    isSelectingStartDate = true
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
                        text = "自定义日期范围",
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "${dateFormatter.format(customDateRange.startDate)} 至 ${dateFormatter.format(customDateRange.endDate)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    TextButton(
                        onClick = {
                            showDatePicker = true
                            isSelectingStartDate = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "修改日期",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("修改日期")
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
fun DateFilterOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    title: String,
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
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = title,
                    modifier = Modifier.padding(16.dp)
                )
            }
        )
    }
}

// 日期范围计算工具函数
object DateFilterUtils {
    
    fun getDateRange(type: DateFilterType, customRange: DateRange? = null): DateRange? {
        val calendar = Calendar.getInstance()
        val now = Date()
        
        return when (type) {
            DateFilterType.ALL -> null
            
            DateFilterType.TODAY -> {
                val startOfDay = calendar.apply {
                    time = now
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                val endOfDay = calendar.apply {
                    time = now
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time
                
                DateRange(startOfDay, endOfDay)
            }
            
            DateFilterType.YESTERDAY -> {
                calendar.time = now
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                
                val startOfYesterday = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                val endOfYesterday = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time
                
                DateRange(startOfYesterday, endOfYesterday)
            }
            
            DateFilterType.THIS_WEEK -> {
                calendar.time = now
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                
                val startOfWeek = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                calendar.add(Calendar.DAY_OF_YEAR, 6)
                val endOfWeek = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time
                
                DateRange(startOfWeek, endOfWeek)
            }
            
            DateFilterType.THIS_MONTH -> {
                calendar.time = now
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                
                val startOfMonth = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val endOfMonth = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time
                
                DateRange(startOfMonth, endOfMonth)
            }
            
            DateFilterType.THIS_YEAR -> {
                calendar.time = now
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                
                val startOfYear = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                val endOfYear = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time
                
                DateRange(startOfYear, endOfYear)
            }
            
            DateFilterType.CUSTOM -> customRange
        }
    }
}